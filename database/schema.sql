-- ===================================================================
-- Web Shop - database schema (MySQL 8+)
-- Usage:  mysql -u root -p < database/schema.sql
-- The schema matches the JPA entities, so the application can run with
-- spring.jpa.hibernate.ddl-auto=validate after executing this script.
-- ===================================================================

CREATE DATABASE IF NOT EXISTS webshop
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE webshop;

SET NAMES utf8mb4;

-- Optional: dedicated application user (adjust the password before use).
-- CREATE USER IF NOT EXISTS 'webshop'@'%' IDENTIFIED BY 'webshop';
-- GRANT ALL PRIVILEGES ON webshop.* TO 'webshop'@'%';
-- FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    code        VARCHAR(50)   NOT NULL,
    name        VARCHAR(150)  NOT NULL,
    description VARCHAR(1000) NULL,
    active      BIT(1)        NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_code UNIQUE (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    code           VARCHAR(50)    NOT NULL,
    name           VARCHAR(200)   NOT NULL,
    description    VARCHAR(2000)  NULL,
    image          VARCHAR(500)   NULL,
    price          DECIMAL(12, 2) NOT NULL,
    stock_quantity INT            NOT NULL DEFAULT 0,
    category_id    BIGINT         NOT NULL,
    active         BIT(1)         NOT NULL DEFAULT b'1',
    featured       BIT(1)         NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    CONSTRAINT uk_products_code UNIQUE (code),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT chk_products_stock_price CHECK (stock_quantity >= 0 AND price >= 0),
    INDEX idx_products_name (name),
    INDEX idx_products_category (category_id),
    INDEX idx_products_active (active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    username   VARCHAR(50)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    enabled    BIT(1)       NOT NULL DEFAULT b'1',
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'CUSTOMER'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS carts (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_carts_user UNIQUE (user_id),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    cart_id    BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   INT            NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT chk_cart_items_quantity CHECK (quantity >= 1)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orders (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(40)    NOT NULL,
    user_id      BIGINT         NOT NULL,
    total_amount DECIMAL(14, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL,
    created_at   DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_orders_order_number UNIQUE (order_number),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_orders_status CHECK (status IN
        ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'COMPLETED', 'CANCELLED')),
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_items (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NULL,
    product_code VARCHAR(50)    NOT NULL,
    product_name VARCHAR(200)   NOT NULL,
    quantity     INT            NOT NULL,
    unit_price   DECIMAL(12, 2) NOT NULL,
    total_price  DECIMAL(14, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    -- Historical data is kept in product_code / product_name / unit_price,
    -- so the reference is simply cleared when a product is deleted.
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL,
    CONSTRAINT chk_order_items_quantity CHECK (quantity >= 1)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
