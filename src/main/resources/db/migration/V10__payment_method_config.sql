-- Crear tabla payment_method_configs
CREATE TABLE payment_method_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    restaurant_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_pmc_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);
