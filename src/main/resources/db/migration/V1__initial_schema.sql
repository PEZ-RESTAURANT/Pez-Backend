-- Clean up existing tables if they exist
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS account_permission_overrides CASCADE;
DROP TABLE IF EXISTS role_permission_defaults CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS user CASCADE;
DROP TABLE IF EXISTS audit_events CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS cash_movement CASCADE;
DROP TABLE IF EXISTS cash_register CASCADE;
DROP TABLE IF EXISTS sale_payment CASCADE;
DROP TABLE IF EXISTS sale_detail CASCADE;
DROP TABLE IF EXISTS sale CASCADE;
DROP TABLE IF EXISTS account_item CASCADE;
DROP TABLE IF EXISTS account CASCADE;

DROP TABLE IF EXISTS price_adjustments CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS restaurant_tables CASCADE;

-- Create Role table
CREATE TABLE role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- Create User table
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- Create User-Roles join table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- Create Permissions table
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    module VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL
);

-- Create Role Permission Defaults table
CREATE TABLE role_permission_defaults (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    permission_id BIGINT NOT NULL,
    granted BOOLEAN NOT NULL,
    UNIQUE (role_name, permission_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Create Account Permission Overrides table
CREATE TABLE account_permission_overrides (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    override_value VARCHAR(20) NOT NULL,
    granted_by VARCHAR(100) NOT NULL,
    override_date TIMESTAMP(6) NOT NULL,
    reason VARCHAR(255),
    UNIQUE (user_id, permission_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Create Audit Events table
CREATE TABLE audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    module VARCHAR(100) NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    device_id VARCHAR(100),
    payload JSON NOT NULL,
    reason VARCHAR(255),
    timestamp TIMESTAMP(6) NOT NULL
);

-- Create Product table
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(38,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- Create Cash Register table
CREATE TABLE cash_register (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    opening_balance DECIMAL(38,2) NOT NULL,
    current_balance DECIMAL(38,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    closed_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- Create Cash Movement table
CREATE TABLE cash_movement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cash_register_id BIGINT,
    amount DECIMAL(38,2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (cash_register_id) REFERENCES cash_register(id) ON DELETE CASCADE
);

-- Create Sale table
CREATE TABLE sale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    staff_id BIGINT,
    customer_name VARCHAR(255),
    customer_dni VARCHAR(255),
    customer_ruc VARCHAR(255),
    total DECIMAL(38,2),
    document_type VARCHAR(50),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

-- Create Sale Detail table
CREATE TABLE sale_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT,
    product_name VARCHAR(255),
    unit_price DECIMAL(38,2),
    quantity INTEGER,
    note VARCHAR(255),
    total_price DECIMAL(38,2),
    FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE CASCADE
);

-- Create Sale Payment table
CREATE TABLE sale_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT,
    method VARCHAR(50),
    amount DECIMAL(38,2),
    FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE CASCADE
);

-- Create Restaurant Tables table
CREATE TABLE restaurant_tables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    number INTEGER NOT NULL UNIQUE,
    floor INTEGER NOT NULL,
    zone_tag VARCHAR(100),
    position_x INTEGER NOT NULL,
    position_y INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL
);

-- Create Orders table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT,
    type VARCHAR(50) NOT NULL,
    customer_id BIGINT,
    status VARCHAR(50) NOT NULL,
    attended_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(id) ON DELETE SET NULL
);

-- Create Order Items table
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    note VARCHAR(255),
    waiter_id BIGINT NOT NULL,
    unit_price_snapshot DECIMAL(38,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    ready_at TIMESTAMP(6),
    delivered_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Create Price Adjustments table
CREATE TABLE price_adjustments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    scope VARCHAR(50) NOT NULL,
    validity VARCHAR(50) NOT NULL,
    start_at TIMESTAMP(6),
    end_at TIMESTAMP(6),
    new_value DECIMAL(38,2) NOT NULL,
    applied_by VARCHAR(100) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
