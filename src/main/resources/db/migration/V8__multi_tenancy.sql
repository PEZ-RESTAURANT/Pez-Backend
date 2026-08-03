-- Create restaurants table
CREATE TABLE restaurants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    business_document_number VARCHAR(50) NULL,
    contact_email VARCHAR(255) NULL,
    contact_phone VARCHAR(50) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- Insert default restaurant
INSERT INTO restaurants (id, name, active, created_at, updated_at)
VALUES (1, 'RESTAURANTE_PENDIENTE_DEFINIR', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Add restaurant_id to all tenant-scoped tables with DEFAULT 1
ALTER TABLE user ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE product ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE recipes ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE product_kitchen_zones ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE supplies ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE stock_movements ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE kitchen_zones ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE orders ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE order_items ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE restaurant_tables ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE price_adjustments ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE sale ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE cash_register ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE cash_movement ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE cash_register_mismatches ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE staff_profiles ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE attendance_records ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE sanctions ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE payroll_adjustments ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE overtime_records ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE customers ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE points_transactions ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE satisfaction_surveys ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE loyalty_configs ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE analytics_configs ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;
ALTER TABLE audit_events ADD COLUMN restaurant_id BIGINT NOT NULL DEFAULT 1;

-- Add foreign key constraints to all tables
ALTER TABLE user ADD CONSTRAINT fk_user_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE product ADD CONSTRAINT fk_product_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE recipes ADD CONSTRAINT fk_recipes_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE product_kitchen_zones ADD CONSTRAINT fk_pkz_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE supplies ADD CONSTRAINT fk_supplies_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE stock_movements ADD CONSTRAINT fk_sm_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE kitchen_zones ADD CONSTRAINT fk_kz_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE orders ADD CONSTRAINT fk_orders_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE order_items ADD CONSTRAINT fk_oi_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE restaurant_tables ADD CONSTRAINT fk_rt_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE price_adjustments ADD CONSTRAINT fk_pa_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE sale ADD CONSTRAINT fk_sale_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE cash_register ADD CONSTRAINT fk_cr_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE cash_movement ADD CONSTRAINT fk_cm_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE cash_register_mismatches ADD CONSTRAINT fk_crm_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE staff_profiles ADD CONSTRAINT fk_sp_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE attendance_records ADD CONSTRAINT fk_ar_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE sanctions ADD CONSTRAINT fk_sanctions_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE payroll_adjustments ADD CONSTRAINT fk_pya_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE overtime_records ADD CONSTRAINT fk_otr_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE customers ADD CONSTRAINT fk_customers_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE points_transactions ADD CONSTRAINT fk_pt_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE satisfaction_surveys ADD CONSTRAINT fk_ss_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE loyalty_configs ADD CONSTRAINT fk_lc_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE analytics_configs ADD CONSTRAINT fk_ac_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
ALTER TABLE audit_events ADD CONSTRAINT fk_ae_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id);
