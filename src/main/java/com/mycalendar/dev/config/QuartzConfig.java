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
      * Runs every 1 minute
      */
    @Bean
    public Trigger notificationJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(1)
                .repeatForever();
        
        return TriggerBuilder.newTrigger()
                .forJob(notificationJobDetail())
                .withIdentity("notificationTrigger", "notificationGroup")
                .withDescription("Trigger for notification job - runs every 1 minute")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
