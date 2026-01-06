package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository สำหรับ track notifications ที่ส่งไปแล้ว
 */
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    
    /**
     * ตรวจสอบว่าเคยส่ง notification สำหรับ event นี้ไปแล้วหรือยัง
     * ใช้ป้องกันการส่งซ้ำ
     */
    boolean existsByEventIdAndUserIdAndNotificationType(Long eventId, Long userId, String notificationType);
}
