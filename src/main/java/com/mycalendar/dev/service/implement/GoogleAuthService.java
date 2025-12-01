package com.mycalendar.dev.service.implement;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.mycalendar.dev.entity.Role;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.entity.UserSocialProvider;
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

        String googleSub = payload.getSubject();           // provider_id (สำคัญที่สุด)
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        // 1. หาจาก Google sub ก่อน (เร็ว + แม่นยำสุด)
        Optional<User> userOpt = socialProviderRepo
                .findByProviderAndProviderId(ProviderType.GOOGLE, googleSub)
                .map(UserSocialProvider::getUser);

        User user;

        if (userOpt.isPresent()) {
            // เคยล็อกอินด้วย Google มาก่อน → ใช้ user เดิม
            user = userOpt.get();

            // อัปเดตข้อมูลล่าสุดจาก Google (optional แต่ดี)
            updateSocialProvider(user, googleSub, email, name, pictureUrl);
        } else {
            // ยังไม่เคยผูก Google นี้เลย

            // 2. ลองหาจาก email ว่ามีผู้ใช้ที่มี email นี้อยู่แล้วไหม (Account Linking)
            Optional<User> existingUserByEmail = userRepository.findByEmail(email);

            if (existingUserByEmail.isPresent()) {
                // มีผู้ใช้จาก email/password → ผูก Google เข้ากับบัญชีเดิม
                user = existingUserByEmail.get();
                createSocialProvider(user, googleSub, email, name, pictureUrl);
            } else {
                // 3. ไม่เคยมีเลย → สร้างผู้ใช้ใหม่
                user = createNewUser(email, name);
                createSocialProvider(user, googleSub, email, name, pictureUrl);
            }
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        // สร้าง JWT
        String accessToken = jwtTokenProvider.generateToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                // ดึง picture จาก social provider ล่าสุด (Google)
                .pictureUrl(pictureUrl)
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

    private void createSocialProvider(User user, String providerId,
                                      String email, String displayName, String pictureUrl) {
        UserSocialProvider provider = new UserSocialProvider();
        provider.setUser(user);
        provider.setProvider(ProviderType.GOOGLE);
        provider.setProviderId(providerId);
        provider.setEmail(email);
        provider.setDisplayName(displayName);
        provider.setPictureUrl(pictureUrl);
        socialProviderRepo.save(provider);
    }

    private void updateSocialProvider(User user, String providerId,
                                      String email, String displayName, String pictureUrl) {
        socialProviderRepo.findByProviderAndProviderId(ProviderType.GOOGLE, providerId)
                .ifPresent(sp -> {
                    sp.setDisplayName(displayName);
                    sp.setPictureUrl(pictureUrl);
                    sp.setEmail(email);
                    // ไม่ต้อง save() เพราะ @Transactional
                });
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