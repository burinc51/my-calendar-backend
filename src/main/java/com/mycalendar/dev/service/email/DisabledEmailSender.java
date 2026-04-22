package com.mycalendar.dev.service.email;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(EmailSender.class)
public class DisabledEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String text) {
        throw new IllegalStateException("No email provider is configured. Set app.email.provider=resend, gmail-api, or smtp and define required credentials.");
    }
}


