-- Migration to add unresolved attendance alert preferences and overtime rates

-- Alter operational_configs table to support unresolved attendance notification preferences
ALTER TABLE operational_configs ADD COLUMN unresolved_attendance_notification_pref VARCHAR(50) NOT NULL DEFAULT 'BOTH';

-- Alter attendance_records to support marking unresolved check-ins upon shift closure
ALTER TABLE attendance_records ADD COLUMN is_unresolved BOOLEAN NOT NULL DEFAULT FALSE;

-- Alter staff_profiles to support custom differentiated overtime hourly rates
ALTER TABLE staff_profiles ADD COLUMN overtime_hourly_rate DECIMAL(19, 4);
