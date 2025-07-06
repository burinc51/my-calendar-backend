package com.mycalendar.dev.service.implement;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.mycalendar.dev.exception.APIException;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.response.JwtResponse;
import com.mycalendar.dev.payload.response.UserResponse;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.security.JwtTokenProvider;
import com.mycalendar.dev.service.IAuthService;
import com.mycalendar.dev.util.EntityMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import java.util.Map;

import java.util.Date;

@Service
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.access.token.expiration.milliseconds}")
    private long expiresIn;

    @Value("${app.refresh.token.expiration.milliseconds}")
    private long refreshExpiresIn;

    @Value("${app.google.client-id}")
    private String googleClientId;

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public JwtResponse signIn(String usernameOrEmail, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);
            String token = jwtTokenProvider.generateToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
            return createAccessToken(token, refreshToken);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid username or password", e);
        }
    }

    @Override
    public JwtResponse refreshAccessToken(String refreshToken) {
        if (jwtTokenProvider.isRefreshTokenRevoked(refreshToken)) {
            throw new IllegalArgumentException("Refresh token is revoked");
        }
        try {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtTokenProvider.generateToken(userDetails);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
            return createAccessToken(newAccessToken, newRefreshToken);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid refresh token", e);
        }
    }

    @Override
    public void activateAccount(String activateCode) {
        var user = userRepository.findByActivateCode(activateCode)
                .orElseThrow(() -> new NotFoundException("User", "activate code", activateCode));
        user.setActive(true);
        user.setActivateCode(null);
        user.setActivatedDate(new Date());

        userRepository.save(user);
    }

    @Override
    public void changePassword(String usernameOrEmail, String oldPassword, String password) {
        userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).ifPresent(user -> {
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
            } else {
                throw new IllegalArgumentException("Old password is incorrect");
            }
        });
    }

    @Override
    public void forgotPassword(String email) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User", "email", email));
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        user.setResetPasswordToken(jwtTokenProvider.generateResetPasswordToken(userDetails));

        userRepository.save(user);
    }

    @Override
    public void resetPassword(String token, String password) {
        var user = userRepository.findByResetPasswordToken(token).orElseThrow(() -> new NotFoundException("User", "token", token));

        if (jwtTokenProvider.isResetPasswordExpired(token)) {
            throw new IllegalArgumentException("Token has expired");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setResetPasswordToken(null);

        userRepository.save(user);
    }

    @Override
    public UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof String username) {
            return userRepository.findByUsername(username)
                    .map(user -> EntityMapper.mapToEntity(user, UserResponse.class))
                    .orElseThrow(() -> new APIException(HttpStatus.UNAUTHORIZED, "User not found"));
        } else if (principal instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername())
                    .map(user -> EntityMapper.mapToEntity(user, UserResponse.class))
                    .orElseThrow(() -> new NotFoundException("User", "username", userDetails.getUsername()));
        } else {
            throw new NotFoundException("User", "principal", "unknown");
        }
    }

    private JwtResponse createAccessToken(String accessToken, String refreshToken) {
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setAccessToken(accessToken);
        jwtResponse.setExpiresIn(expiresIn / 1000);
        jwtResponse.setRefreshToken(refreshToken);
        jwtResponse.setRefreshExpiresIn(refreshExpiresIn / 1000);
        return jwtResponse;
    }


    public ResponseEntity<?> googleSignIn(String idTokenString) {
        GoogleIdToken.Payload payload = verifyGoogleIdToken(idTokenString);
        if (payload == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "Invalid ID token."));
        }

        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(payload.getEmail());
        userResponse.setName((String) payload.get("name"));
        userResponse.setImageUrl((String) payload.get("picture"));

        return ResponseEntity.ok(userResponse);
    }

    private GoogleIdToken.Payload verifyGoogleIdToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new com.google.api.client.http.javanet.NetHttpTransport(),
                    com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance())
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            return (idToken != null) ? idToken.getPayload() : null;
        } catch (Exception e) {
            // Use logger or handle exception properly
            return null;
        }
    }
}
