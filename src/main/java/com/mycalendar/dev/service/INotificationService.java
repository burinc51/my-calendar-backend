package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.PushTokenRequest;

public interface INotificationService {

    void processEventNotifications();

    /**
     * เลื่อนเวลาของ recurring events ไปยัง occurrence ถัดไป
     * หลังจาก notification ถูกส่งแล้ว
     */
    void rescheduleRecurringNotifications();

    void registerPushToken(PushTokenRequest request);

    void unregisterPushToken(String token);
}
