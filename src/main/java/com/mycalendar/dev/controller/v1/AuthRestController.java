package com.mycalendar.dev.controller.v1;

import com.mycalendar.dev.payload.request.*;
import com.mycalendar.dev.payload.response.JwtResponse;
import com.mycalendar.dev.payload.response.UserResponse;
import com.mycalendar.dev.security.JwtTokenProvider;
import com.mycalendar.dev.service.implement.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthRestController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> login(@Valid @ModelAttribute SignInRequest signInRequest) {
        JwtResponse jwtResponse = authService.signIn(signInRequest.getUsernameOrEmail(), signInRequest.getPassword());

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping(value = "/refresh-token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> refreshAccessToken(@Valid @ModelAttribute RenewTokenRequest renewToken) {
        if (jwtTokenProvider.isRefreshTokenRevoked(renewToken.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token has been revoked");
        }

        JwtResponse jwtResponse = authService.refreshAccessToken(renewToken.getRefreshToken());

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping(value = "/sign-out", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> logout(@Valid @ModelAttribute SignOutRequest signOutRequest) {
        if (jwtTokenProvider.isTokenRevoked(signOutRequest.getAccessToken()) || jwtTokenProvider.isRefreshTokenRevoked(signOutRequest.getRefreshToken())) {
            return ResponseEntity.badRequest().body("Tokens are already revoked.");
        }

        jwtTokenProvider.revokeToken(signOutRequest.getAccessToken());
        jwtTokenProvider.revokeRefreshToken(signOutRequest.getRefreshToken());

        return ResponseEntity.ok("Sign out successfully.");
    }

    @PostMapping(value = "/revoke-access-token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> revokeAccessToken(@Valid @ModelAttribute RevokeAccessTokenRequest revokeAccessTokenRequest) {
        if (jwtTokenProvider.isTokenRevoked(revokeAccessTokenRequest.getAccessToken())) {
            return ResponseEntity.badRequest().body("Access token is already revoked.");
        }

        jwtTokenProvider.revokeToken(revokeAccessTokenRequest.getAccessToken());

        return ResponseEntity.ok("Access Token Revoked Successfully.");
    }

    @PostMapping(value = "/revoke-refresh-token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> revokeRefreshToken(@Valid @ModelAttribute RevokeRefreshTokenRequest revokeRefreshTokenRequest) {
        if (jwtTokenProvider.isRefreshTokenRevoked(revokeRefreshTokenRequest.getRefreshToken())) {
            return ResponseEntity.badRequest().body("Refresh token is already revoked.");
        }

        jwtTokenProvider.revokeRefreshToken(revokeRefreshTokenRequest.getRefreshToken());

        return ResponseEntity.ok("Refresh Token Revoked Successfully.");
    }

    @GetMapping("/activate-account/{activateCode}")
    public ResponseEntity<String> activateAccount(@PathVariable String activateCode) {
        authService.activateAccount(activateCode);
        return ResponseEntity.ok("Account activated successfully.");
    }

    @PostMapping(value = "/change-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> changePassword(@Valid @ModelAttribute ChangePasswordRequest changePasswordRequest) {
        if (changePasswordRequest.getOldPassword().equals(changePasswordRequest.getNewPassword())) {
            throw new IllegalArgumentException("New password must be different from the old password.");
        }

        if (changePasswordRequest.getNewPassword().length() < 5 || changePasswordRequest.getNewPassword().length() > 24) {
            throw new IllegalArgumentException("Password should contain at least 5 characters, but no more than 24 characters.");
        }

        authService.changePassword(changePasswordRequest.getUsernameOrEmail(), changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());

        return ResponseEntity.ok("Password changed successfully.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authService.forgotPassword(forgotPasswordRequest.getEmail());
        return ResponseEntity.ok("Password reset link has been sent to your email.");
    }

    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> resetPassword(@Valid @ModelAttribute ResetPasswordRequest resetPasswordRequest) {
        authService.resetPassword(resetPasswordRequest.getToken(), resetPasswordRequest.getPassword());
        return ResponseEntity.ok("Password reset successfully.");
    }

    @GetMapping("/current-user")
    public UserResponse getCurrentUser() {
        return authService.getCurrentUser();
    }

    @PostMapping(value = "/google-sign-in", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> googleSignIn(@Valid @RequestBody GoogleSignInRequest googleSignInRequest) {
        return authService.googleSignIn(googleSignInRequest.getIdToken());
    }
}