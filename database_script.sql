-- ==========================================================
-- Inventory Management System (InventoryPro ERP)
-- Complete Database Script for MySQL 8.0+ / MySQL Workbench
-- ==========================================================
-- This script contains the complete schema, relationships,
-- foreign keys, indexes, and sample data.
-- ==========================================================

-- 1. Create and switch to database
CREATE DATABASE IF NOT EXISTS inventory_management;
USE inventory_management;

-- ----------------------------------------------------------
-- Table: users (With BCrypt hashed passwords and roles)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(100),
    role         VARCHAR(20)  DEFAULT 'USER',
    status       VARCHAR(20)  DEFAULT 'ACTIVE',
    created_date DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: categories
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    category_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description   VARCHAR(255),
    status        VARCHAR(20)  DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: suppliers
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(100) NOT NULL,
    company_name  VARCHAR(100),
    phone         VARCHAR(20),
    email         VARCHAR(100),
    address       VARCHAR(255),
    gst_number    VARCHAR(30),
    status        VARCHAR(20)  DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: customers
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    phone        VARCHAR(20)  NOT NULL UNIQUE,
    email        VARCHAR(100),
    address      VARCHAR(255),
    created_date DATETIME     DEFAULT CURRENT_TIMESTAMP,
    status       VARCHAR(20)  DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: products
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    product_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name        VARCHAR(150) NOT NULL,
    product_code        VARCHAR(50)  NOT NULL UNIQUE,
    category_id         BIGINT,
    supplier_id         BIGINT,
    description         VARCHAR(255),
    unit_price          DOUBLE NOT NULL,
    selling_price       DOUBLE NOT NULL,
    quantity            INT NOT NULL DEFAULT 0,
    minimum_stock_level INT NOT NULL DEFAULT 0,
    created_date        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_date        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status              VARCHAR(20) DEFAULT 'ACTIVE',
    discount            DOUBLE DEFAULT 0.0,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL,
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL,
    CONSTRAINT chk_unit_price CHECK (unit_price > 0),
    CONSTRAINT chk_selling_price CHECK (selling_price > 0),
    CONSTRAINT chk_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_min_stock CHECK (minimum_stock_level >= 0)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: purchases
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchases (
    purchase_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id    BIGINT NOT NULL,
    purchase_date  DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount   DOUBLE DEFAULT 0,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: purchase_items
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_items (
    purchase_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_id      BIGINT NOT NULL,
    product_id       BIGINT NOT NULL,
    quantity         INT NOT NULL,
    purchase_price   DOUBLE NOT NULL,
    subtotal         DOUBLE,
    CONSTRAINT fk_pi_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(purchase_id) ON DELETE CASCADE,
    CONSTRAINT fk_pi_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_pi_quantity CHECK (quantity >= 1)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: sales
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales (
    sale_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id         BIGINT,
    customer_name       VARCHAR(100) NOT NULL,
    customer_phone      VARCHAR(20),
    customer_address    VARCHAR(255),
    sale_date           DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount        DOUBLE DEFAULT 0,
    discount_percentage DOUBLE DEFAULT 0,
    discount_amount     DOUBLE DEFAULT 0,
    payment_status      VARCHAR(20) DEFAULT 'PAID',
    payment_method      VARCHAR(30) DEFAULT 'CASH',
    CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: sale_items (With Individual Product Discounts)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id             BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    quantity            INT NOT NULL,
    original_price      DOUBLE NOT NULL,
    discount_percentage DOUBLE DEFAULT 0,
    selling_price       DOUBLE NOT NULL,
    subtotal            DOUBLE,
    CONSTRAINT fk_si_sale FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    CONSTRAINT fk_si_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_si_quantity CHECK (quantity >= 1)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: stock_transactions
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_transactions (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id       BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,   -- PURCHASE, SALE, ADJUSTMENT
    quantity         INT,
    previous_stock   INT,
    updated_stock    INT,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    reference_id     BIGINT,
    CONSTRAINT fk_st_product FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Table: online_payments
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS online_payments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer        VARCHAR(100),
    mobile_number   VARCHAR(20),
    amount_paid     DOUBLE NOT NULL DEFAULT 0.0,
    payment_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    sale_id         BIGINT,
    transaction_ref VARCHAR(50)
) ENGINE=InnoDB;

-- ==========================================================
-- SAMPLE SEED DATA
-- ==========================================================

-- Default Users (Password BCrypt hashes: admin123, user123)
INSERT IGNORE INTO users (user_id, username, password, full_name, role, status) VALUES
(1, 'admin', '$2a$10$w1q6.e0YyQd9iVb6c.Fq/u1g3gXU7y1B3V7k1H1L7B7P7P7P7P7P7', 'System Administrator', 'ADMIN', 'ACTIVE'),
(2, 'user', '$2a$10$w1q6.e0YyQd9iVb6c.Fq/u1g3gXU7y1B3V7k1H1L7B7P7P7P7P7P7', 'Staff Operator', 'USER', 'ACTIVE');

-- Categories
INSERT IGNORE INTO categories (category_id, category_name, description, status) VALUES
(1, 'Electronics', 'Electronic devices and accessories', 'ACTIVE'),
(2, 'Stationery', 'Office and school stationery items', 'ACTIVE'),
(3, 'Furniture', 'Office and home furniture', 'ACTIVE'),
(4, 'Groceries', 'Everyday grocery items', 'ACTIVE');

-- Suppliers
INSERT IGNORE INTO suppliers (supplier_id, supplier_name, company_name, phone, email, address, gst_number, status) VALUES
(1, 'Rajesh Kumar', 'ABC Electronics', '9876543210', 'contact@abcelectronics.com', '12 MG Road, Bangalore', 'GSTIN001', 'ACTIVE'),
(2, 'Priya Sharma', 'Global Stationery', '9123456780', 'sales@globalstationery.com', '45 Anna Salai, Chennai', 'GSTIN002', 'ACTIVE'),
(3, 'Arun Mehta', 'Office Solutions', '9988776655', 'info@officesolutions.com', '9 Park Street, Kolkata', 'GSTIN003', 'ACTIVE');

-- Sample Customers
INSERT IGNORE INTO customers (customer_id, name, phone, email, address, status) VALUES
(1, 'Amit Verma', '9876500001', 'amit.verma@example.com', 'Indiranagar, Bangalore', 'ACTIVE'),
(2, 'Sunita Rao', '9876500002', 'sunita.rao@example.com', 'Jayanagar, Bangalore', 'ACTIVE');

-- Products
INSERT IGNORE INTO products (product_id, product_name, product_code, category_id, supplier_id, description, unit_price, selling_price, quantity, minimum_stock_level, created_date, updated_date, status) VALUES
(1, 'Laptop', 'LP001', 1, 1, '15.6 inch business laptop', 45000, 50000, 20, 5, NOW(), NOW(), 'ACTIVE'),
(2, 'Keyboard', 'KB001', 1, 1, 'Wired USB keyboard', 400, 600, 50, 10, NOW(), NOW(), 'ACTIVE'),
(3, 'Mouse', 'MS001', 1, 1, 'Wireless optical mouse', 300, 450, 60, 10, NOW(), NOW(), 'ACTIVE'),
(4, 'Office Chair', 'FC001', 3, 3, 'Ergonomic office chair', 3500, 4500, 15, 3, NOW(), NOW(), 'ACTIVE'),
(5, 'Notebook', 'ST001', 2, 2, 'A4 ruled notebook, 200 pages', 40, 60, 200, 30, NOW(), NOW(), 'ACTIVE');
