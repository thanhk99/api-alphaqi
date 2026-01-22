-- Migration script to add CIO_REPORT type to reports table
-- Run this script on your PostgreSQL database

-- Drop the old check constraint
ALTER TABLE reports DROP CONSTRAINT IF EXISTS reports_type_check;

-- Create new check constraint with CIO_REPORT included
ALTER TABLE reports ADD CONSTRAINT reports_type_check 
    CHECK (type IN ('MACRO', 'INVESTMENT_STRATEGY', 'COMPANY_INDUSTRY', 'ASSET_MANAGEMENT', 'CIO_REPORT'));

-- Verify the constraint was created
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conrelid = 'reports'::regclass AND conname = 'reports_type_check';
