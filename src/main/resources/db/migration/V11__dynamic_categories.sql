-- Crear tabla categories
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    restaurant_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_categories_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

-- Modificar tabla product
ALTER TABLE product ADD COLUMN category_id BIGINT;
ALTER TABLE product ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id);

-- Para evitar fallar si H2 realiza validaciones estrictas y migrar, primero la hacemos nullable,
-- luego eliminamos la columna anterior de texto.
ALTER TABLE product DROP COLUMN category;

-- Hacemos la columna category_id NOT NULL para asegurar la integridad de la base de datos
ALTER TABLE product MODIFY COLUMN category_id BIGINT NOT NULL;
