-- Alter staff_invites to add email column
ALTER TABLE staff_invites ADD COLUMN email VARCHAR(255) NOT NULL;
