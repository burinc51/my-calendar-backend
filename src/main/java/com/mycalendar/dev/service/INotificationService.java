package com.mycalendar.dev.service;

import com.mycalendar.dev.payload.request.PushTokenRequest;

public interface INotificationService {
    
    void processEventNotifications();
    
    void registerPushToken(PushTokenRequest request);
    
    void unregisterPushToken(String token);
}
