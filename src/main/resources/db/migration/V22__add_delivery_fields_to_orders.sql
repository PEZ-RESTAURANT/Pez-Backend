ALTER TABLE orders ADD COLUMN delivery_customer_name VARCHAR(255) NULL;
ALTER TABLE orders ADD COLUMN delivery_customer_phone VARCHAR(50) NULL;
ALTER TABLE orders ADD COLUMN delivery_address VARCHAR(500) NULL;
ALTER TABLE orders ADD COLUMN delivery_maps_link VARCHAR(1000) NULL;
ALTER TABLE orders ADD COLUMN declared_payment_method VARCHAR(50) NULL;
