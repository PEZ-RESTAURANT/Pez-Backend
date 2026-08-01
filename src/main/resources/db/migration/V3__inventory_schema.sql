-- Alter product table to add columns for Phase 4
ALTER TABLE product ADD COLUMN estimated_prep_time_minutes INT NULL;
ALTER TABLE product ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

-- Create supplies table
CREATE TABLE supplies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    unit VARCHAR(50) NULL,
    current_stock DECIMAL(15, 4) NOT NULL DEFAULT 0.0000,
    min_threshold DECIMAL(15, 4) NULL
);

-- Create stock movements table
CREATE TABLE stock_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supply_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    quantity DECIMAL(15, 4) NOT NULL,
    registered_by VARCHAR(100) NOT NULL,
    date TIMESTAMP(6) NOT NULL,
    reason VARCHAR(255) NULL,
    FOREIGN KEY (supply_id) REFERENCES supplies(id) ON DELETE CASCADE
);

-- Create recipes table
CREATE TABLE recipes (
    product_id BIGINT NOT NULL,
    supply_id BIGINT NOT NULL,
    quantity_used DECIMAL(10, 4) NOT NULL,
    PRIMARY KEY (product_id, supply_id),
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    FOREIGN KEY (supply_id) REFERENCES supplies(id) ON DELETE CASCADE
);
