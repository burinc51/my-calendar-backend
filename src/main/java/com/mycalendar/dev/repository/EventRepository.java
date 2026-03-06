package com.mycalendar.dev.repository;

import com.mycalendar.dev.entity.Event;
import com.mycalendar.dev.projection.EventProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query(value = """
            SELECT e.event_id              AS eventId,
                   e.title                 AS title,
                   e.description           AS description,
                   e.start_date            AS startDate,
                   e.end_date              AS endDate,
                   e.location              AS location,
                   e.latitude              AS latitude,
                   e.longitude             AS longitude,
                   e.notification_time     AS notificationTime,
                   e.notification_type     AS notificationType,
                   e.remind_before_minutes AS remindBeforeMinutes,
                   e.repeat_type           AS repeatType,
                   e.repeat_until          AS repeatUntil,
                   e.repeat_interval       AS repeatInterval,
                   e.repeat_days           AS repeatDays,
                   e.color                 AS color,
                   e.category              AS category,
                   e.priority              AS priority,
                   e.pinned                AS pinned,
                   e.image_url             AS imageUrl,
                   e.group_id              AS groupId,
                   u.user_id               AS userId,
                   u.username              AS username,
                   u.name                  AS name,
                   e.create_by_id          AS createById,
                   e.all_day               AS allDay
            FROM (SELECT e2.*
                  FROM events e2
                  WHERE e2.group_id = :groupId
                  ORDER BY e2.event_id LIMIT :#{#pageable.pageSize}
                  OFFSET :#{#pageable.offset}) e
                     LEFT JOIN event_user eu ON e.event_id = eu.event_id
                     LEFT JOIN users u ON u.user_id = eu.user_id
            """, nativeQuery = true)
    Page<EventProjection> findAllByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    @Query(value = """
            SELECT COUNT(*)
            FROM events e
            WHERE e.group_id = :groupId
            """, nativeQuery = true)
    long countEventsByGroupId(@Param("groupId") Long groupId);


    @Query(value = """
            SELECT e.event_id              AS eventId,
                   e.title                 AS title,
                   e.description           AS description,
                   e.start_date            AS startDate,
                   e.end_date              AS endDate,
                   e.location              AS location,
                   e.latitude              AS latitude,
                   e.longitude             AS longitude,
                   e.notification_time     AS notificationTime,
                   e.notification_type     AS notificationType,
                   e.remind_before_minutes AS remindBeforeMinutes,
                   e.repeat_type           AS repeatType,
                   e.repeat_until          AS repeatUntil,
                   e.repeat_interval       AS repeatInterval,
                   e.repeat_days           AS repeatDays,
                   e.color                 AS color,
                   e.category              AS category,
                   e.priority              AS priority,
                   e.pinned                AS pinned,
                   e.image_url             AS imageUrl,
                   e.group_id              AS groupId,
                   u.user_id               AS userId,
                   u.username              AS username,
                   u.name                  AS name,
                   e.create_by_id          AS createById,
                   e.all_day               AS allDay
            FROM (SELECT e2.*
                  FROM events e2
                  ORDER BY e2.event_id LIMIT :#{#pageable.pageSize}
                  OFFSET :#{#pageable.offset}) e
                     LEFT JOIN event_user eu ON e.event_id = eu.event_id
                     LEFT JOIN users u ON u.user_id = eu.user_id
            ORDER BY e.event_id
            """, nativeQuery = true)
    List<EventProjection> findAllEvents(Pageable pageable);

    @Query(value = """
            SELECT COUNT(*)
            FROM events
            """, nativeQuery = true)
    long countAllEvents();


    @Query(value = """
            SELECT e.event_id   AS eventId,
                   e.title      AS title,
                   e.start_date AS startDate,
                   e.end_date   AS endDate,
                   e.color      AS color,
                   e.all_day    AS allDay
            FROM events e
            WHERE e.start_date >= to_date(:startMonth || '-01', 'YYYY-MM-DD')
              AND e.start_date <= (to_date(:endMonth || '-01', 'YYYY-MM-DD') + INTERVAL '1 month' - INTERVAL '1 day')
            ORDER BY e.event_id
            """, nativeQuery = true)
    List<EventProjection> findAllEventsByMonthRange(@Param("startMonth") String startMonth,
                                                    @Param("endMonth") String endMonth);

    /**
     * Find events that are due for notification (optimized)
     * - notificationTime is within windowStart to now
     * - notificationType is PUSH or EMAIL
     * - notificationSent = false (not sent yet)
     */
    @Query(value = """
            SELECT e.* FROM events e
            WHERE e.notification_time IS NOT NULL
              AND e.notification_time <= :now
              AND e.notification_time >= :windowStart
              AND e.start_date > :now
              AND e.notification_type IN ('PUSH', 'EMAIL')
              AND e.notification_sent = false
            """, nativeQuery = true)
    List<Event> findEventsToNotify(
            @Param("now") LocalDateTime now,
            @Param("windowStart") LocalDateTime windowStart
    );

    /**
     * Find recurring events whose startDate has passed and need to be rescheduled to the next occurrence.
     * - repeatType is not NONE
     * - notificationSent = true (notification already sent for current occurrence)
     * - startDate <= now (current occurrence has passed or is happening now)
     * - repeatUntil is null OR repeatUntil > now (repeat period has not ended)
     */
    @Query(value = """
            SELECT e.* FROM events e
            WHERE e.repeat_type IS NOT NULL
              AND e.repeat_type <> 'NONE'
              AND e.notification_sent = true
              AND e.start_date <= :now
              AND (e.repeat_until IS NULL OR e.repeat_until > :now)
            """, nativeQuery = true)
    List<Event> findRecurringEventsToReschedule(@Param("now") LocalDateTime now);
}
