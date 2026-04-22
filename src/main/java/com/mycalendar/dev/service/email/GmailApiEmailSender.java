package com.mycalendar.dev.service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.email.provider", havingValue = "gmail-api")
public class GmailApiEmailSender implements EmailSender {

    private static final String GMAIL_SEND_ENDPOINT = "https://gmail.googleapis.com/gmail/v1/users/%s/messages/send";
    private static final String OAUTH_TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.email.from-email:}")
    private String fromEmail;

    @Value("${app.email.from-name:My Calendar App}")
    private String fromName;

    @Value("${app.email.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${app.email.gmail.client-id:}")
    private String gmailClientId;

    @Value("${app.email.gmail.client-secret:}")
    private String gmailClientSecret;

    @Value("${app.email.gmail.refresh-token:}")
    private String gmailRefreshToken;

    @Value("${app.email.gmail.user-id:me}")
    private String gmailUserId;

    @Override
    public void send(String to, String subject, String text) {
        validateRequiredConfig();

        String accessToken = exchangeAccessToken();
        String mime = buildMimeMessage(to, subject, text);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(mime.getBytes(StandardCharsets.UTF_8));

        Map<String, String> payload = Map.of("raw", raw);
        String endpoint = String.format(GMAIL_SEND_ENDPOINT, urlEncode(gmailUserId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Gmail API send failed. status=" + response.statusCode() + ", body=" + response.body());
            }
            log.info("Email sent successfully via Gmail API to: {}", to);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to send email via Gmail API: " + e.getMessage(), e);
        }
    }

    private String exchangeAccessToken() {
        String form = "client_id=" + urlEncode(gmailClientId)
                + "&client_secret=" + urlEncode(gmailClientSecret)
                + "&refresh_token=" + urlEncode(gmailRefreshToken)
                + "&grant_type=refresh_token";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OAUTH_TOKEN_ENDPOINT))
                .timeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Gmail token exchange failed. status=" + response.statusCode() + ", body=" + response.body());
            }

            JsonNode json = objectMapper.readTree(response.body());
            String accessToken = json.path("access_token").asText("");
            if (accessToken.isBlank()) {
                throw new IllegalStateException("Gmail token exchange succeeded but access_token is missing.");
            }
            return accessToken;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to exchange Gmail access token: " + e.getMessage(), e);
        }
    }

    private String buildMimeMessage(String to, String subject, String text) {
        String from = (fromName == null || fromName.isBlank())
                ? fromEmail
                : fromName.trim() + " <" + fromEmail.trim() + ">";

        return "From: " + from + "\r\n"
                + "To: " + to + "\r\n"
                + "Subject: " + subject + "\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "\r\n"
                + text;
    }

    private void validateRequiredConfig() {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("Missing app.email.from-email (or EMAIL_FROM_EMAIL) for Gmail API provider.");
        }
        if (gmailClientId == null || gmailClientId.isBlank()) {
            throw new IllegalStateException("Missing app.email.gmail.client-id (or GMAIL_CLIENT_ID).");
        }
        if (gmailClientSecret == null || gmailClientSecret.isBlank()) {
            throw new IllegalStateException("Missing app.email.gmail.client-secret (or GMAIL_CLIENT_SECRET).");
        }
        if (gmailRefreshToken == null || gmailRefreshToken.isBlank()) {
            throw new IllegalStateException("Missing app.email.gmail.refresh-token (or GMAIL_REFRESH_TOKEN).");
        }
    }

    private String toJson(Map<String, String> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize Gmail payload", e);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}

