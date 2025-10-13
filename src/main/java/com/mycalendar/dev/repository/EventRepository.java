package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.projection.EventProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query(value = """
            SELECT
                e.event_id          AS eventId,
                e.title             AS title,
                e.description       AS description,
                e.start_date        AS startDate,
                e.end_date          AS endDate,
                e.location          AS location,
                e.latitude          AS latitude,
                e.longitude         AS longitude,
                e.notification_time         AS notificationTime,
                e.notification_type         AS notificationType,
                e.remind_before_minutes         AS remindBeforeMinutes,
                e.repeat_type         AS repeatType,
                e.repeat_until         AS repeatUntil,
                e.color         AS color,
                e.category         AS category,
                e.priority         AS priority,
                e.pinned         AS pinned,
                e.image_url         AS imageUrl,
                e.group_id         AS groupId,
            
                u.user_id         AS userId,
                u.username        AS username,
                u.name            AS name
            FROM events e
            LEFT JOIN event_user eu ON e.event_id = eu.event_id
            LEFT JOIN users u ON u.user_id = eu.user_id
            WHERE e.group_id = :groupId
            ORDER BY e.start_date DESC
            """,
            nativeQuery = true)
    Page<EventProjection> findAllEventSummaryByGroup(@Param("groupId") Long groupId, Pageable pageable);

}
