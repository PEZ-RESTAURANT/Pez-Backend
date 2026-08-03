-- Agregar columna anchor_table_id a restaurant_tables
ALTER TABLE restaurant_tables ADD COLUMN anchor_table_id BIGINT NULL;
ALTER TABLE restaurant_tables ADD CONSTRAINT fk_rt_anchor FOREIGN KEY (anchor_table_id) REFERENCES restaurant_tables(id) ON DELETE SET NULL;

-- Crear tabla reservations
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    customer_id BIGINT NULL,
    reservation_date_time TIMESTAMP(6) NOT NULL,
    party_size INT NOT NULL,
    notes VARCHAR(1000) NULL,
    table_id BIGINT NULL,
    status VARCHAR(50) NOT NULL,
    restaurant_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_reservation_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id),
    CONSTRAINT fk_reservation_table FOREIGN KEY (table_id) REFERENCES restaurant_tables(id) ON DELETE SET NULL
);
