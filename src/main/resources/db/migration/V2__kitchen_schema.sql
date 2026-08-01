-- Create Kitchen Zones table
CREATE TABLE kitchen_zones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Create Product Kitchen Zones relation table
CREATE TABLE product_kitchen_zones (
    product_id BIGINT PRIMARY KEY,
    zone_id BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    FOREIGN KEY (zone_id) REFERENCES kitchen_zones(id) ON DELETE CASCADE
);
