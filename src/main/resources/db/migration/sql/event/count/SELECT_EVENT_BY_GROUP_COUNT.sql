SELECT COUNT(DISTINCT e.event_id)
FROM events e
WHERE e.group_id = :groupId
