package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.PushTokenRequest;
import com.mycalendar.dev.payload.response.event.EventResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface INotificationService {

    void processEventNotifications();

    /**
     * Advances recurring events to the next occurrence
     * after a notification has been sent.
     */
    void rescheduleRecurringNotifications();

    void registerPushToken(PushTokenRequest request);

    void unregisterPushToken(String token);

    boolean sendTestPushNotification(String token, String title, String body, Map<String, Object> data);

    List<EventResponse> getNotificationScheduleByDate(LocalDate date, boolean includeSent, Long groupId);
}
