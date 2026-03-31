package com.mycalendar.dev.service.implement;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.mycalendar.dev.entity.User;
import com.mycalendar.dev.exception.APIException;
import com.mycalendar.dev.exception.NotFoundException;
import com.mycalendar.dev.payload.response.JwtResponse;
import com.mycalendar.dev.payload.response.UserResponse;
import com.mycalendar.dev.repository.UserRepository;
import com.mycalendar.dev.security.JwtTokenProvider;
import com.mycalendar.dev.service.EmailService;
import com.mycalendar.dev.service.IAuthService;
import com.mycalendar.dev.util.EntityMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final long OTP_EXPIRY_MILLIS = 5 * 60 * 1000;
    private static final long FORGOT_PASSWORD_VERIFY_WINDOW_MILLIS = 10 * 60 * 1000;
    private static final String OTP_PURPOSE_ACCOUNT_ACTIVATION = "ACCOUNT_ACTIVATION";
    private static final String OTP_PURPOSE_FORGOT_PASSWORD = "FORGOT_PASSWORD";

    @Value("${app.access.token.expiration.milliseconds}")
    private long expiresIn;

    @Value("${app.refresh.token.expiration.milliseconds}")
    private long refreshExpiresIn;

    @Value("${app.google.client-id}")
    private String googleClientId;

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtTokenProvider jwtTokenProvider, UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public JwtResponse signIn(String usernameOrEmail, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String principal = authentication.getName();
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal);
            String token = jwtTokenProvider.generateToken(userDetails);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
            User user = userRepository.findByUsernameOrEmail(principal, principal)
                    .orElseThrow(() -> new NotFoundException("User", "username/email", principal));
            return createAccessToken(token, refreshToken, user);
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
            User user = userRepository.findByUsernameOrEmail(username, username)
                    .orElseThrow(() -> new NotFoundException("User", "username/email", username));
            return createAccessToken(newAccessToken, newRefreshToken, user);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid refresh token", e);
        }
    }

    @Override
    public void verifyOtp(String email, String otpCode) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedOtp = otpCode == null ? "" : otpCode.trim();
        var user = userRepository.findByEmailAndOtpCodeIncludingInactive(normalizedEmail, normalizedOtp)
                .orElseThrow(() -> new NotFoundException("User", "email/otp", normalizedEmail));

        if (Boolean.TRUE.equals(user.isActive())) {
            throw new IllegalArgumentException("Account is already activated.");
        }

        if (user.getOtpExpiredAt() == null || user.getOtpExpiredAt().before(new Date())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }

        if (!OTP_PURPOSE_ACCOUNT_ACTIVATION.equals(user.getOtpPurpose())) {
            throw new IllegalArgumentException("OTP purpose is invalid for account activation.");
        }

        user.setActive(true);
        user.setOtpCode(null);
        user.setOtpExpiredAt(null);
        user.setOtpPurpose(null);
        user.setOtpVerifiedAt(null);
        user.setActivatedDate(new Date());

        userRepository.save(user);
    }

    @Override
    public void resendOtp(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIncludingInactive(normalizedEmail)
                .orElseThrow(() -> new NotFoundException("User", "email", normalizedEmail));

        if (user.isActive()) {
            throw new IllegalArgumentException("Account is already activated.");
        }

        String otpCode = generateOtpCode();
        user.setOtpCode(otpCode);
        user.setOtpExpiredAt(new Date(System.currentTimeMillis() + OTP_EXPIRY_MILLIS));
        user.setOtpPurpose(OTP_PURPOSE_ACCOUNT_ACTIVATION);
        user.setOtpVerifiedAt(null);
        userRepository.save(user);

        sendOtpEmail(user.getEmail(), user.getName(), otpCode);
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
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIncludingInactive(normalizedEmail)
                .orElseThrow(() -> new NotFoundException("User", "email", normalizedEmail));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is not activated.");
        }

        String otpCode = generateOtpCode();
        user.setOtpCode(otpCode);
        user.setOtpExpiredAt(new Date(System.currentTimeMillis() + OTP_EXPIRY_MILLIS));
        user.setOtpPurpose(OTP_PURPOSE_FORGOT_PASSWORD);
        user.setOtpVerifiedAt(null);
        userRepository.save(user);

        sendForgotPasswordOtpEmail(user.getEmail(), user.getName(), otpCode);
    }

    @Override
    public void verifyForgotPasswordOtp(String email, String otpCode) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedOtp = otpCode == null ? "" : otpCode.trim();

        User user = userRepository.findByEmailIncludingInactive(normalizedEmail)
                .orElseThrow(() -> new NotFoundException("User", "email", normalizedEmail));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is not activated.");
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(normalizedOtp)) {
            throw new IllegalArgumentException("Invalid OTP code.");
        }

        if (user.getOtpExpiredAt() == null || user.getOtpExpiredAt().before(new Date())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }

        if (!OTP_PURPOSE_FORGOT_PASSWORD.equals(user.getOtpPurpose())) {
            throw new IllegalArgumentException("OTP purpose is invalid for forgot-password flow.");
        }

        user.setOtpVerifiedAt(new Date());
        user.setOtpCode(null);
        user.setOtpExpiredAt(null);
        userRepository.save(user);
    }

    @Override
    public void resetPassword(String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmailIncludingInactive(normalizedEmail)
                .orElseThrow(() -> new NotFoundException("User", "email", normalizedEmail));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is not activated.");
        }

        if (!OTP_PURPOSE_FORGOT_PASSWORD.equals(user.getOtpPurpose())) {
            throw new IllegalArgumentException("Forgot-password OTP has not been verified.");
        }

        if (user.getOtpVerifiedAt() == null) {
            throw new IllegalArgumentException("OTP has not been verified.");
        }

        Date verifiedAt = user.getOtpVerifiedAt();
        if (verifiedAt.before(new Date(System.currentTimeMillis() - FORGOT_PASSWORD_VERIFY_WINDOW_MILLIS))) {
            throw new IllegalArgumentException("OTP verification has expired. Please verify OTP again.");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setOtpVerifiedAt(null);
        user.setOtpCode(null);
        user.setOtpExpiredAt(null);
        user.setOtpPurpose(null);

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

    private JwtResponse createAccessToken(String accessToken, String refreshToken, User user) {
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setAccessToken(accessToken);
        jwtResponse.setExpiresIn(expiresIn / 1000);
        jwtResponse.setRefreshToken(refreshToken);
        jwtResponse.setRefreshExpiresIn(refreshExpiresIn / 1000);
        jwtResponse.setUserId(user.getUserId());
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setName(user.getName());
        jwtResponse.setEmail(user.getEmail());
        jwtResponse.setPictureUrl(user.getPictureUrl());
        boolean isAdmin = user.getRoles() != null && user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
        jwtResponse.setIsAdmin(isAdmin);
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

    private String generateOtpCode() {
        int otp = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(otp);
    }

    private void sendOtpEmail(String email, String name, String otpCode) {
        String safeName = name == null || name.isBlank() ? "User" : name;
        String subject = "Your OTP Code - GRPlan App";
        String text = "Hello " + safeName + ",\n\n"
                + "Your OTP code is: " + otpCode + "\n"
                + "This OTP expires in 5 minutes.\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Best regards,\nGRPlan App";
        emailService.sendSimpleEmail(email, subject, text);
    }

    private void sendForgotPasswordOtpEmail(String email, String name, String otpCode) {
        String safeName = name == null || name.isBlank() ? "User" : name;
        String subject = "Forgot Password OTP - GRPlan App";
        String text = "Hello " + safeName + ",\n\n"
                + "Your forgot-password OTP code is: " + otpCode + "\n"
                + "This OTP expires in 5 minutes.\n\n"
                + "If you did not request this, you can safely ignore this email.\n\n"
                + "Best regards,\nGRPlan App";

        emailService.sendSimpleEmail(email, subject, text);
    }
}
