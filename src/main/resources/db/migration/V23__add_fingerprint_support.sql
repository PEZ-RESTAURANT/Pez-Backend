ALTER TABLE staff_profiles ADD COLUMN fingerprint_id INT NULL;

CREATE TABLE unmapped_fingerprint_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_serial_number VARCHAR(255) NOT NULL,
    device_user_id INT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    restaurant_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);
