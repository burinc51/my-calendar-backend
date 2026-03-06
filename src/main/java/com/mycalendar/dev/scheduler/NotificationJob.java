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
            // 1) ส่ง notification สำหรับ event ที่ถึงเวลาแล้ว
            notificationService.processEventNotifications();
            // 2) เลื่อน recurring events ไปยัง occurrence ถัดไป
            notificationService.rescheduleRecurringNotifications();
        } catch (Exception e) {
            log.error("❌ Error in NotificationJob: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
