package com.mycalendar.dev.controller.v1;
import com.mycalendar.dev.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/mail-test")
@RequiredArgsConstructor
@Slf4j
public class MailTestController {
    private final EmailService emailService;
    @PostMapping("/send-test")
    public ResponseEntity<?> sendTestEmail(@RequestParam String email) {
        try {
            emailService.sendTestEmail(email);
            return ResponseEntity.ok("""
                {
                  "status": "success",
                  "message": "Test email sent to: %s",
                  "data": null
                }
                """.formatted(email));
        } catch (Exception e) {
            log.error("Error sending test email", e);
            return ResponseEntity.status(500).body("""
                {
                  "status": "error",
                  "message": "Failed to send email: %s",
                  "data": null
                }
                """.formatted(e.getMessage()));
        }
    }
}
