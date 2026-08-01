-- Alter sale table to drop deprecated columns and add fields for Phase 5
ALTER TABLE sale DROP COLUMN customer_dni;
ALTER TABLE sale DROP COLUMN customer_ruc;
ALTER TABLE sale ADD COLUMN order_id BIGINT;
ALTER TABLE sale ADD COLUMN customer_document_number VARCHAR(50);
ALTER TABLE sale ADD COLUMN sale_status VARCHAR(50);
ALTER TABLE sale ADD CONSTRAINT fk_sale_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

-- Alter cash_movement table to store reasons
ALTER TABLE cash_movement ADD COLUMN reason VARCHAR(50);

-- Create cash_register_mismatches table
CREATE TABLE cash_register_mismatches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cash_register_id BIGINT NOT NULL,
    expected_amount DECIMAL(15, 4) NOT NULL,
    declared_amount DECIMAL(15, 4) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notified_admin_at TIMESTAMP(6) NULL,
    FOREIGN KEY (cash_register_id) REFERENCES cash_register(id) ON DELETE CASCADE
);
