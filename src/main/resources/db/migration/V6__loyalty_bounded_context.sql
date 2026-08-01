-- Create Customers table
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    birthday DATE,
    address VARCHAR(255),
    data_consent_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    data_consent_date TIMESTAMP(6),
    points_balance INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- Create Satisfaction Survey table
CREATE TABLE satisfaction_surveys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    favorite_dish VARCHAR(255),
    favorite_drink VARCHAR(255),
    service_satisfaction INT NOT NULL,
    food_satisfaction INT NOT NULL,
    suggestion VARCHAR(1000),
    date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Create Points Transaction table
CREATE TABLE points_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount INT NOT NULL,
    sale_id BIGINT,
    date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE SET NULL
);

-- Create Loyalty Config table
CREATE TABLE loyalty_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    min_purchase_amount_for_points DECIMAL(19, 4) NOT NULL,
    points_per_currency_unit DECIMAL(19, 4) NOT NULL,
    review_satisfaction_threshold INT NOT NULL DEFAULT 4,
    google_review_url VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- Seed Initial Loyalty Config row
INSERT INTO loyalty_configs (min_purchase_amount_for_points, points_per_currency_unit, review_satisfaction_threshold, google_review_url, created_at, updated_at, id)
VALUES (10.0000, 1.0000, 4, 'https://google.com/review', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), default);
