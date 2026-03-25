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
            SELECT e.event_id,
                   e.title,
                   e.description,
                   e.start_date,
                   e.end_date,
                   e.location,
                   e.latitude,
                   e.longitude,
                   e.notification_time,
                   e.notification_type,
                   e.remind_before_value,
                   e.remind_before_unit,
                   e.remind_before_minutes,
                   e.repeat_type,
                   e.repeat_until,
                   e.repeat_interval,
                   e.repeat_days,
                   e.color,
                   e.category,
                   e.priority,
                   e.pinned,
                   e.image_url,
                   e.group_id,
                   u.user_id,
                   u.username,
                   u.name,
                   u.picture_url AS userImageUrl,
                   e.create_by_id,
                   e.all_day
            FROM (SELECT e2.*
                  FROM events e2
                  WHERE e2.group_id = :groupId
                  ORDER BY
                    CASE WHEN :sortBy = 'eventId' AND :sortOrder = 'ASC' THEN e2.event_id END ASC,
                    CASE WHEN :sortBy = 'eventId' AND :sortOrder = 'DESC' THEN e2.event_id END DESC,
                    CASE WHEN :sortBy = 'startDate' AND :sortOrder = 'ASC' THEN e2.start_date END ASC,
                    CASE WHEN :sortBy = 'startDate' AND :sortOrder = 'DESC' THEN e2.start_date END DESC,
                    CASE WHEN :sortBy = 'endDate' AND :sortOrder = 'ASC' THEN e2.end_date END ASC,
                    CASE WHEN :sortBy = 'endDate' AND :sortOrder = 'DESC' THEN e2.end_date END DESC,
                    CASE WHEN :sortBy = 'title' AND :sortOrder = 'ASC' THEN e2.title END ASC,
                    CASE WHEN :sortBy = 'title' AND :sortOrder = 'DESC' THEN e2.title END DESC,
                    e2.event_id DESC
                  LIMIT :#{#pageable.pageSize}
                  OFFSET :#{#pageable.offset}) e
                     LEFT JOIN event_user eu ON e.event_id = eu.event_id
                     LEFT JOIN users u ON u.user_id = eu.user_id
            ORDER BY
                CASE WHEN :sortBy = 'eventId' AND :sortOrder = 'ASC' THEN e.event_id END ASC,
                CASE WHEN :sortBy = 'eventId' AND :sortOrder = 'DESC' THEN e.event_id END DESC,
                CASE WHEN :sortBy = 'startDate' AND :sortOrder = 'ASC' THEN e.start_date END ASC,
                CASE WHEN :sortBy = 'startDate' AND :sortOrder = 'DESC' THEN e.start_date END DESC,
                CASE WHEN :sortBy = 'endDate' AND :sortOrder = 'ASC' THEN e.end_date END ASC,
                CASE WHEN :sortBy = 'endDate' AND :sortOrder = 'DESC' THEN e.end_date END DESC,
                CASE WHEN :sortBy = 'title' AND :sortOrder = 'ASC' THEN e.title END ASC,
                CASE WHEN :sortBy = 'title' AND :sortOrder = 'DESC' THEN e.title END DESC,
                e.event_id DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM events e
            WHERE e.group_id = :groupId
              AND (:sortBy IS NULL OR :sortBy IS NOT NULL)
              AND (:sortOrder IS NULL OR :sortOrder IS NOT NULL)
            """,
            nativeQuery = true)
    Page<EventProjection> findAllByGroupId(@Param("groupId") Long groupId,
                                           Pageable pageable,
                                           @Param("sortBy") String sortBy,
                                           @Param("sortOrder") String sortOrder);

    @Query(value = """
            SELECT COUNT(*)
            FROM events e
            WHERE e.group_id = :groupId
            """, nativeQuery = true)
    long countEventsByGroupId(@Param("groupId") Long groupId);

    @Query(value = """
            SELECT e.event_id,
                   e.title,
                   e.description,
                   e.start_date,
                   e.end_date,
                   e.location,
                   e.latitude,
                   e.longitude,
                   e.notification_time,
                   e.notification_type,
                   e.remind_before_value,
                   e.remind_before_unit,
                   e.remind_before_minutes,
                   e.repeat_type,
                   e.repeat_until,
                   e.repeat_interval,
                   e.repeat_days,
                   e.color,
                   e.category,
                   e.priority,
                   e.pinned,
                   e.image_url,
                   e.group_id,
                   u.user_id,
                   u.username,
                   u.name,
                   u.picture_url AS userImageUrl,
                   e.create_by_id,
                   e.all_day
            FROM (SELECT e2.*
                  FROM events e2
                  ORDER BY
                    CASE WHEN :sortBy = 'eventId' AND :sortOrder = 'ASC' THEN e2.event_id END ASC,
                    CASE WHEN :sortBy = 'eventId' AND :sortOrder = 'DESC' THEN e2.event_id END DESC,
                    CASE WHEN :sortBy = 'startDate' AND :sortOrder = 'ASC' THEN e2.start_date END ASC,
                    CASE WHEN :sortBy = 'startDate' AND :sortOrder = 'DESC' THEN e2.start_date END DESC,
                    CASE WHEN :sortBy = 'endDate' AND :sortOrder = 'ASC' THEN e2.end_date END ASC,
                    CASE WHEN :sortBy = 'endDate' AND :sortOrder = 'DESC' THEN e2.end_date END DESC,
                    CASE WHEN :sortBy = 'title' AND :sortOrder = 'ASC' THEN e2.title END ASC,
                    CASE WHEN :sortBy = 'title' AND :sortOrder = 'DESC' THEN e2.title END DESC,
                    e2.event_id DESC
                  LIMIT :#{#pageable.pageSize}
                  OFFSET :#{#pageable.offset}) e
                     LEFT JOIN event_user eu ON e.event_id = eu.event_id
                     LEFT JOIN users u ON u.user_id = eu.user_id
            ORDER BY
                CASE WHEN :sortBy = 'eventId' AND :sortOrder = 'ASC' THEN e.event_id END ASC,
                CASE WHEN :sortBy = 'eventId' AND :sortOrder = 'DESC' THEN e.event_id END DESC,
                CASE WHEN :sortBy = 'startDate' AND :sortOrder = 'ASC' THEN e.start_date END ASC,
                CASE WHEN :sortBy = 'startDate' AND :sortOrder = 'DESC' THEN e.start_date END DESC,
                CASE WHEN :sortBy = 'endDate' AND :sortOrder = 'ASC' THEN e.end_date END ASC,
                CASE WHEN :sortBy = 'endDate' AND :sortOrder = 'DESC' THEN e.end_date END DESC,
                CASE WHEN :sortBy = 'title' AND :sortOrder = 'ASC' THEN e.title END ASC,
                CASE WHEN :sortBy = 'title' AND :sortOrder = 'DESC' THEN e.title END DESC,
                e.event_id DESC
            """, nativeQuery = true)
    List<EventProjection> findAllEvents(Pageable pageable,
                                        @Param("sortBy") String sortBy,
                                        @Param("sortOrder") String sortOrder);

    @Query(value = """
            SELECT COUNT(*)
            FROM events
            """, nativeQuery = true)
    long countAllEvents();

    @Query("""
            SELECT e.eventId AS eventId,
                   e.title AS title,
                   e.startDate AS startDate,
                   e.endDate AS endDate,
                   e.color AS color,
                   e.allDay AS allDay,
                   e.priority AS priority,
                   assignee.userId AS userId,
                   assignee.username AS username,
                   assignee.name AS name,
                   assignee.pictureUrl AS userImageUrl,
                   creator.userId AS createdByUserId,
                   creator.username AS createdByUsername,
                   creator.name AS createdByName,
                   creator.pictureUrl AS createdByImageUrl
            FROM Event e
            JOIN e.group g
            JOIN g.userGroups ug
            LEFT JOIN e.users assignee
            LEFT JOIN User creator ON creator.userId = e.createById
            WHERE e.startDate >= :startDateTime
              AND e.startDate <= :endDateTime
              AND ug.user.userId = :userId
              AND (:groupId IS NULL OR g.groupId = :groupId)
            ORDER BY e.eventId
            """)
    List<EventProjection> findAllEventsByMonthRange(@Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime,
                                                    @Param("userId") Long userId,
                                                    @Param("groupId") Long groupId);

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

    /**
     * Get events whose notification time falls within a day window.
     * - includeSent=false => only pending notifications
     * - includeSent=true  => include both pending and already sent
     * - groupId optional for group-scoped checks
     */
    @Query(value = """
            SELECT e.*
            FROM events e
            WHERE e.notification_time IS NOT NULL
              AND e.notification_time >= :fromDateTime
              AND e.notification_time < :toDateTime
              AND (:groupId IS NULL OR e.group_id = :groupId)
              AND (:includeSent = true OR e.notification_sent = false)
            ORDER BY e.notification_time ASC
            """, nativeQuery = true)
    List<Event> findNotificationScheduleByDate(
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            @Param("includeSent") boolean includeSent,
            @Param("groupId") Long groupId
    );
}

