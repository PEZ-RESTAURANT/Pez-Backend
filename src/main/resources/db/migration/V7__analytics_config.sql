-- Create Analytics Config table
CREATE TABLE analytics_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    low_sales_threshold_units INT NOT NULL DEFAULT 5,
    low_sales_evaluation_period_days INT NOT NULL DEFAULT 30,
    date_presets VARCHAR(1000) NOT NULL DEFAULT '{}',
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- Seed Initial Analytics Config row
INSERT INTO analytics_configs (low_sales_threshold_units, low_sales_evaluation_period_days, date_presets, created_at, updated_at, id)
VALUES (5, 30, '{"Fiestas Patrias":{"startMonth":7,"startDay":28,"endMonth":7,"endDay":29},"Navidad":{"startMonth":12,"startDay":24,"endMonth":12,"endDay":25}}', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), default);
