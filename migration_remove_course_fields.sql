-- Migration script to remove obsolete columns from courses table
-- Run this script on your PostgreSQL database

-- Remove course_type column and related constraints
ALTER TABLE courses DROP COLUMN IF EXISTS course_type;

-- Remove level column
ALTER TABLE courses DROP COLUMN IF EXISTS level;

-- Remove duration column
ALTER TABLE courses DROP COLUMN IF EXISTS duration;

-- Remove offline course-specific columns
ALTER TABLE courses DROP COLUMN IF EXISTS registration_start_date;
ALTER TABLE courses DROP COLUMN IF EXISTS registration_end_date;
ALTER TABLE courses DROP COLUMN IF EXISTS course_start_date;
ALTER TABLE courses DROP COLUMN IF EXISTS course_end_date;
ALTER TABLE courses DROP COLUMN IF EXISTS max_students;
ALTER TABLE courses DROP COLUMN IF EXISTS location;
ALTER TABLE courses DROP COLUMN IF EXISTS address;

-- Verify the remaining columns
-- Expected columns: id, title, description, price, thumbnail, category, 
--                   is_published, intro_video_url, average_rating, review_count,
--                   created_at, updated_at, is_show_home
