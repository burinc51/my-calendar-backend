-- ============================================================
-- Test Data สำหรับทดสอบ Push Notification Flow
-- ============================================================
-- วิธีใช้: รัน SQL นี้ใน PostgreSQL database ของคุณ
-- แก้ไข: YOUR_PUSH_TOKEN เป็น token จริงที่ได้จาก app
-- ============================================================

-- 1. ดูว่า User ID = 1 มีอยู่หรือไม่
SELECT * FROM users WHERE user_id = 1;

-- 2. ลงทะเบียน Push Token (ถ้า register ผ่าน API แล้วไม่ต้องรัน)
INSERT INTO push_tokens (token, user_id, platform, device_name, created_at, active)
VALUES (
    'ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]',  -- แก้เป็น token จริง
    1,                                            -- user_id
    'android',
    'Test Device',
    NOW(),
    true
)
ON CONFLICT (token) DO NOTHING;

-- 3. ดู Group ที่มีอยู่ (ต้องมี group ก่อนสร้าง event)
SELECT * FROM groups LIMIT 5;

-- 4. สร้าง Event ที่จะ trigger notification
-- NOTE: ต้องแก้ group_id เป็น id ที่มีอยู่จริง
INSERT INTO events (
    title,
    description,
    start_date,
    end_date,
    notification_time,
    notification_type,
    remind_before_minutes,
    group_id,
    create_by_id,
    color,
    priority,
    pinned,
    all_day
) VALUES (
    '🔔 Test Push Notification',
    'This event is for testing push notification',
    NOW() + INTERVAL '30 minutes',     -- Event จะเริ่มใน 30 นาที
    NOW() + INTERVAL '1 hour',         -- Event จบใน 1 ชม.
    NOW() - INTERVAL '1 minute',       -- ⚡ เวลาแจ้งเตือน = ผ่านมาแล้ว (จะ trigger ทันที)
    'PUSH',                            -- ประเภท = PUSH
    15,                                -- เตือนก่อน 15 นาที
    1,                                 -- ⚠️ แก้เป็น group_id ที่มีอยู่จริง
    1,                                 -- create_by_id
    '#FF5733',
    'HIGH',
    false,
    false
) RETURNING event_id;

-- 5. Assign User เข้า Event (สำคัญมาก!)
-- แก้ event_id เป็น id ที่ได้จากข้อ 4
INSERT INTO event_user (event_id, user_id)
VALUES (
    1,   -- ⚠️ แก้เป็น event_id ที่เพิ่งสร้าง
    1    -- user_id
)
ON CONFLICT DO NOTHING;

-- ============================================================
-- ตรวจสอบข้อมูล
-- ============================================================

-- ดู Push Tokens ที่ลงทะเบียน
SELECT * FROM push_tokens WHERE active = true;

-- ดู Events ที่ถึงเวลาแจ้งเตือน
SELECT 
    e.event_id,
    e.title,
    e.notification_type,
    e.notification_time,
    e.start_date,
    CASE 
        WHEN e.notification_time <= NOW() AND e.start_date > NOW() 
        THEN '✅ Ready to notify'
        ELSE '⏳ Not yet'
    END as status
FROM events e
WHERE e.notification_type IN ('PUSH', 'EMAIL')
  AND e.notification_time IS NOT NULL
ORDER BY e.notification_time DESC
LIMIT 10;

-- ดู Users ที่ assign ใน Event
SELECT 
    e.event_id,
    e.title,
    u.user_id,
    u.name,
    u.email
FROM events e
JOIN event_user eu ON e.event_id = eu.event_id
JOIN users u ON eu.user_id = u.user_id
WHERE e.notification_type = 'PUSH';

-- ดู Notification Logs (notifications ที่ส่งไปแล้ว)
SELECT * FROM notification_logs ORDER BY sent_at DESC LIMIT 10;

-- ============================================================
-- หลังจากรัน SQL แล้ว:
-- 1. เปิด Swagger หรือ App
-- 2. เรียก POST /api/v1/push-tokens/test-job
-- 3. ตรวจสอบ logs ที่ backend
-- 4. ถ้าสำเร็จจะได้รับ Push Notification บนเครื่อง!
-- ============================================================
