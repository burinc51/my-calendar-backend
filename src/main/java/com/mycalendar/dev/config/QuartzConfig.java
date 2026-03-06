package com.mycalendar.dev.config;

import com.mycalendar.dev.scheduler.NotificationJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    /**
     * Defines the JobDetail for NotificationJob
     */
    @Bean
    public JobDetail notificationJobDetail() {
        return JobBuilder.newJob(NotificationJob.class)
                .withIdentity("notificationJob", "notificationGroup")
                .withDescription("Job to process event notifications")
                .storeDurably()
                .build();
    }
    
    /**
      * Defines the Trigger for NotificationJob
      * Runs every 1 minute.
      * If the server restarts and a tick was missed, fires immediately on recovery.
      */
    @Bean
    public Trigger notificationJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(1)
                .repeatForever()
                .withMisfireHandlingInstructionFireNow(); // fire immediately after restart

        return TriggerBuilder.newTrigger()
                .forJob(notificationJobDetail())
                .withIdentity("notificationTrigger", "notificationGroup")
                .withDescription("Trigger for notification job - runs every 1 minute")
                .withSchedule(scheduleBuilder)
                .startNow() // start immediately when Spring boots
                .build();
    }
}
