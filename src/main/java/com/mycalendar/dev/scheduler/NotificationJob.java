package com.mycalendar.dev.scheduler;

import com.mycalendar.dev.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationJob extends QuartzJobBean {
    
    private final INotificationService notificationService;
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.debug("🔔 NotificationJob triggered");
        try {
            notificationService.processEventNotifications();
        } catch (Exception e) {
            log.error("❌ Error in NotificationJob: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
