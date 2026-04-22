package com.mycalendar.dev.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.mycalendar.dev.service.email.EmailSender;
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailSender emailSender;

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            emailSender.send(to, subject, text);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
    public void sendTestEmail(String to) {
        String subject = "Test Email - My Calendar App";
        String text = "If you see this email, your email delivery configuration is working correctly!\n\n" +
                "Email provider integration has been successfully tested.\n\n" +
                "Best regards,\nMy Calendar App";
        sendSimpleEmail(to, subject, text);
    }
}
