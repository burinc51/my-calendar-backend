-- Profile picture ownership migration
-- 1) users becomes the profile source (picture_url + picture_source)
-- 2) backfill first picture from the latest Google social record
-- 3) remove deprecated profile fields from user_social_provider

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS picture_url VARCHAR(1000);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS picture_source VARCHAR(20);

WITH latest_google_social AS (
    SELECT usp.user_id,
           usp.picture_url,
           ROW_NUMBER() OVER (PARTITION BY usp.user_id ORDER BY usp.id DESC) AS rn
    FROM user_social_provider usp
    WHERE usp.provider = 'GOOGLE'
      AND usp.picture_url IS NOT NULL
)
UPDATE users u
SET picture_url = lgs.picture_url,
    picture_source = 'GOOGLE'
FROM latest_google_social lgs
WHERE u.user_id = lgs.user_id
  AND lgs.rn = 1
  AND u.picture_url IS NULL;

ALTER TABLE user_social_provider
    DROP COLUMN IF EXISTS display_name;

ALTER TABLE user_social_provider
    DROP COLUMN IF EXISTS picture_url;

