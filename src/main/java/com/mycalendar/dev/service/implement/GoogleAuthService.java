package com.mycalendar.dev.service.implement;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.mycalendar.dev.entity.Role;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.entity.UserSocialProvider;
import com.mycalendar.dev.enums.PictureSource;
import com.mycalendar.dev.enums.ProviderType;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.response.AuthResponse;
import com.mycalendar.dev.repository.RoleRepository;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.repository.UserSocialProviderRepository;
import com.mycalendar.dev.security.JwtTokenProvider;
import com.mycalendar.dev.service.IGoogleAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService implements IGoogleAuth {

    private final UserRepository userRepository;
    private final UserSocialProviderRepository socialProviderRepo;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final RoleRepository roleRepository;

    @Value("${app.google.client-id}")
    private String googleClientId;

    private static final NetHttpTransport TRANSPORT = new NetHttpTransport();
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public AuthResponse googleSignIn(String idTokenString) {
        GoogleIdToken.Payload payload = verifyGoogleIdToken(idTokenString);
        if (payload == null) {
            throw new BadCredentialsException("Invalid Google ID token");
        }

        System.out.println("payload = " + payload);

        String googleSub = payload.getSubject();           // provider_id (most important)
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        // 1. Look up by Google sub first (fastest and most accurate)
        Optional<User> userOpt = socialProviderRepo
                .findByProviderAndProviderId(ProviderType.GOOGLE, googleSub)
                .map(UserSocialProvider::getUser);

        User user;

        if (userOpt.isPresent()) {
            // Previously logged in with Google → reuse existing user
            user = userOpt.get();

            updateSocialProviderEmail(googleSub, email);
        } else {
            // Google account not linked yet

            // 2. Try to find an existing user by email (Account Linking)
            Optional<User> existingUserByEmail = userRepository.findByEmail(email);

            if (existingUserByEmail.isPresent()) {
                // User exists via email/password → link Google to the existing account
                user = existingUserByEmail.get();
                createSocialProvider(user, googleSub, email);
            } else {
                // 3. No existing account → create a new user
                user = createNewUser(email, name);
                createSocialProvider(user, googleSub, email);
            }
        }

        applyGooglePictureFirstTimeOnly(user, pictureUrl);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .pictureUrl(user.getPictureUrl())
                .isAdmin(user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN")))
                .build();
    }

    private User createNewUser(String email, String name) {
        User user = new User();

        String safeEmail = email != null ? email.trim().toLowerCase(Locale.ROOT) : null;
        user.setEmail(safeEmail);

        String safeName = (name != null && !name.trim().isEmpty())
                ? name.trim()
                : (safeEmail != null && safeEmail.contains("@") ? safeEmail.split("@")[0] : "user");
        user.setName(safeName);

        String baseUsername;
        if (safeEmail != null) {
            int at = safeEmail.indexOf('@');
            String local = (at > 0) ? safeEmail.substring(0, at) : safeEmail;
            baseUsername = sanitizeUsername(local);
            if (baseUsername.isEmpty()) {
                baseUsername = "user";
            }
        } else {
            baseUsername = "user";
        }

        String username = generateUniqueUsername(baseUsername);
        user.setUsername(username);

        // Random password since Google users don't need a usable password
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new NotFoundException("Role", "name", "USER"));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private String sanitizeUsername(String input) {
        String s = input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
        // allow letters, numbers, dot, underscore and hyphen
        s = s.replaceAll("[^a-z0-9._-]", "");
        if (s.length() > 50) {
            s = s.substring(0, 50);
        }
        return s;
    }

    private String generateUniqueUsername(String base) {
        String candidate = base;
        if (candidate.length() > 50) {
            candidate = candidate.substring(0, 50);
        }
        int suffix = 0;
        while (userRepository.existsByUsername(candidate)) {
            suffix++;
            String tail = "-" + suffix;
            int allowed = Math.max(1, 50 - tail.length());
            String prefix = base.length() > allowed ? base.substring(0, allowed) : base;
            candidate = prefix + tail;
            if (suffix > 1000) {
                candidate = prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
                break;
            }
        }
        return candidate;
    }

    private void createSocialProvider(User user, String providerId, String email) {
        UserSocialProvider provider = new UserSocialProvider();
        provider.setUser(user);
        provider.setProvider(ProviderType.GOOGLE);
        provider.setProviderId(providerId);
        provider.setEmail(email);
        socialProviderRepo.save(provider);
    }

    private void updateSocialProviderEmail(String providerId, String email) {
        socialProviderRepo.findByProviderAndProviderId(ProviderType.GOOGLE, providerId)
                .ifPresent(sp -> {
                    sp.setEmail(email);
                    socialProviderRepo.save(sp);
                });
    }

    private void applyGooglePictureFirstTimeOnly(User user, String googlePictureUrl) {
        if (user.getPictureUrl() == null && googlePictureUrl != null && !googlePictureUrl.isBlank()) {
            user.setPictureUrl(googlePictureUrl);
            user.setPictureSource(PictureSource.GOOGLE);
            userRepository.save(user);
        }
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(TRANSPORT, JSON_FACTORY)
                    .setAudience(Collections.singletonList(googleClientId))
                    .setIssuer("https://accounts.google.com")
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            return idToken != null ? idToken.getPayload() : null;

        } catch (Exception e) {
            log.error("Google ID token verification failed", e);
            return null;
        }
    }
}