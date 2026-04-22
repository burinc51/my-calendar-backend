package com.mycalendar.dev.service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.email.provider", havingValue = "resend", matchIfMissing = true)
public class ResendEmailSender implements EmailSender {

    private final ObjectMapper objectMapper;
    private final HttpClient emailHttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.email.api-url:https://api.resend.com/emails}")
    private String apiUrl;

    @Value("${app.email.api-key:}")
    private String apiKey;

    @Value("${app.email.from-email:}")
    private String fromEmail;

    @Value("${app.email.from-name:My Calendar App}")
    private String fromName;

    @Value("${app.email.timeout-seconds:10}")
    private int timeoutSeconds;

    @Override
    public void send(String to, String subject, String text) {
        validateRequiredConfig();

        String from = (fromName == null || fromName.isBlank())
                ? fromEmail
                : fromName.trim() + " <" + fromEmail.trim() + ">";

        Map<String, Object> payload = Map.of(
                "from", from,
                "to", List.of(to),
                "subject", subject,
                "text", text
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = emailHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Email API request failed. status=" + response.statusCode() + ", body=" + response.body());
            }
            log.info("Email sent successfully via HTTP API to: {}", to);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Failed to send email via HTTP API: " + e.getMessage(), e);
        }
    }

    private void validateRequiredConfig() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing app.email.api-key (or EMAIL_API_KEY) for HTTP email provider.");
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("Missing app.email.from-email (or EMAIL_FROM_EMAIL) for HTTP email provider.");
        }
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize email payload", e);
        }
    }
}


