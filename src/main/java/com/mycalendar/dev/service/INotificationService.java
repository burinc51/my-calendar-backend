package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.PushTokenRequest;

public interface INotificationService {

    void processEventNotifications();

    /**
     * Advances recurring events to the next occurrence
     * after a notification has been sent.
     */
    void rescheduleRecurringNotifications();

    void registerPushToken(PushTokenRequest request);

    void unregisterPushToken(String token);
}
