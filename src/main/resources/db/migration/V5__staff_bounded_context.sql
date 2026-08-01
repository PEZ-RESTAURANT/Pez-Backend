-- Create Staff Profile table
CREATE TABLE staff_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT UNIQUE NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    agreed_amount DECIMAL(19, 4) NOT NULL,
    fingerprint_consent BOOLEAN NOT NULL DEFAULT FALSE,
    fingerprint_consent_date TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (account_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Create Attendance Record table
CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_profile_id BIGINT NOT NULL,
    check_in_at TIMESTAMP(6) NOT NULL,
    check_out_at TIMESTAMP(6),
    method VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (staff_profile_id) REFERENCES staff_profiles(id) ON DELETE CASCADE
);

-- Create Payroll Adjustment table
CREATE TABLE payroll_adjustments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_profile_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    sale_id BIGINT,
    registered_by VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (staff_profile_id) REFERENCES staff_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE SET NULL
);

-- Create Sanction table
CREATE TABLE sanctions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_profile_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    reason VARCHAR(2000) NOT NULL,
    registered_by VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (staff_profile_id) REFERENCES staff_profiles(id) ON DELETE CASCADE
);

-- Create Overtime Record table
CREATE TABLE overtime_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    staff_profile_id BIGINT NOT NULL,
    hours DECIMAL(10, 2) NOT NULL,
    date DATE NOT NULL,
    registered_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (staff_profile_id) REFERENCES staff_profiles(id) ON DELETE CASCADE
);
