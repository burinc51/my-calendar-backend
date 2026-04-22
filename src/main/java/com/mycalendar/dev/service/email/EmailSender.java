package com.mycalendar.dev.service.email;

public interface EmailSender {
    void send(String to, String subject, String text);
}

