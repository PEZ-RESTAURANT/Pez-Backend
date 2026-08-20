-- Alter restaurants to add address column
ALTER TABLE restaurants ADD COLUMN address VARCHAR(255) DEFAULT NULL;

-- Alter sale to add ticket_number column
ALTER TABLE sale ADD COLUMN ticket_number VARCHAR(50) DEFAULT NULL;

-- Create billing_sequences table
CREATE TABLE billing_sequences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    current_value INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_restaurant_doc_type UNIQUE (restaurant_id, document_type)
);

-- Seed sequences for tenant 1
INSERT INTO billing_sequences (restaurant_id, document_type, current_value) VALUES (1, 'BOLETA', 0);
INSERT INTO billing_sequences (restaurant_id, document_type, current_value) VALUES (1, 'FACTURA_ELECTRONICA', 0);
