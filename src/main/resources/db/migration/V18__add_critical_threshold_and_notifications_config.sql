-- Migration V18: Add critical threshold, notifications configuration, and marketing logs
ALTER TABLE supplies ADD COLUMN critical_threshold DECIMAL(15, 4) DEFAULT NULL;
ALTER TABLE customers ADD COLUMN email VARCHAR(255) DEFAULT NULL;

ALTER TABLE operational_configs ADD COLUMN annulment_notification_pref VARCHAR(50) DEFAULT 'INSTANT';
ALTER TABLE operational_configs ADD COLUMN daily_summary_time VARCHAR(5) DEFAULT '22:00';
ALTER TABLE operational_configs ADD COLUMN last_daily_summary_sent_at DATE DEFAULT NULL;

CREATE TABLE marketing_notification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    sent_at DATETIME NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    restaurant_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
