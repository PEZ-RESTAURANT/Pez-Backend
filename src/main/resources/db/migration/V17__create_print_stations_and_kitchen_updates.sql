-- Create print_stations table
CREATE TABLE print_stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    restaurant_id BIGINT NOT NULL,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

-- Add printing_enabled column to kitchen_zones
ALTER TABLE kitchen_zones ADD COLUMN printing_enabled BOOLEAN NOT NULL DEFAULT FALSE;
