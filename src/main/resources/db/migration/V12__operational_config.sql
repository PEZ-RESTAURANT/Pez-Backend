-- Create Operational Config table
CREATE TABLE operational_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cutoff_hour INT NOT NULL DEFAULT 3,
    cutoff_minute INT NOT NULL DEFAULT 0,
    unattended_threshold_minutes INT NOT NULL DEFAULT 15,
    waiting_dishes_threshold_minutes INT NOT NULL DEFAULT 30,
    restaurant_id BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_operational_config_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

-- Seed Initial Operational Config row for default tenant
INSERT INTO operational_configs (cutoff_hour, cutoff_minute, unattended_threshold_minutes, waiting_dishes_threshold_minutes, restaurant_id, created_at, updated_at, id)
VALUES (3, 0, 15, 30, 1, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), default);
