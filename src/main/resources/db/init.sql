-- ============================================================
-- Fenix Commerce - Database Initialization Script
-- Multi-tenant Orders / Fulfillment / Tracking (MySQL 8.x)
-- UUID PKs stored as BINARY(16) using UUID_TO_BIN(..., 1)
-- ============================================================
-- Requirements:
--  - MySQL 8.0.16+ for CHECK constraint enforcement
--  - InnoDB + utf8mb4
-- ============================================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS fenix_commerce
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE fenix_commerce;

-- Set session configuration
SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
SET time_zone = '+00:00';

-- ============================================================
-- 1) DROP TABLES (safe reset)
-- ============================================================
DROP TABLE IF EXISTS tracking_events;
DROP TABLE IF EXISTS tracking;
DROP TABLE IF EXISTS fulfillments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS tenant;

-- ============================================================
-- 2) CREATE TABLES (DDL)
-- ============================================================

-- tenant table
CREATE TABLE tenant (
                        tenant_id BINARY(16) NOT NULL,
                        tenant_name VARCHAR(255) NOT NULL,
                        status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (tenant_id),
                        UNIQUE KEY uk_tenant_name (tenant_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- store table
CREATE TABLE store (
                       store_id BINARY(16) NOT NULL,
                       tenant_id BINARY(16) NOT NULL,
                       store_code VARCHAR(100) NOT NULL,
                       store_name VARCHAR(255) NOT NULL,
                       platform ENUM('SHOPIFY','NETSUITE','CUSTOM','MAGENTO','OTHER') NOT NULL DEFAULT 'OTHER',
                       timezone VARCHAR(64) NULL,
                       currency CHAR(3) NULL,
                       status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       PRIMARY KEY (store_id),
                       UNIQUE KEY uk_store_code_per_tenant (tenant_id, store_code),
                       KEY idx_store_tenant (tenant_id),

                       CONSTRAINT fk_store_tenant
                           FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id)
                               ON DELETE RESTRICT ON UPDATE CASCADE,

                       CONSTRAINT chk_store_currency
                           CHECK (currency IS NULL OR currency REGEXP '^[A-Z]{3}$')
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- orders table
CREATE TABLE orders (
                        order_id BINARY(16) NOT NULL,
                        tenant_id BINARY(16) NOT NULL,
                        store_id BINARY(16) NOT NULL,

                        external_order_id VARCHAR(128) NOT NULL,
                        external_order_number VARCHAR(128) NULL,

                        order_status ENUM('CREATED','CANCELLED','CLOSED') NOT NULL DEFAULT 'CREATED',
                        financial_status ENUM('UNKNOWN','PENDING','PAID','PARTIALLY_PAID','REFUNDED','PARTIALLY_REFUNDED','VOIDED')
    NOT NULL DEFAULT 'UNKNOWN',
                        fulfillment_status ENUM('UNFULFILLED','PARTIAL','FULFILLED','CANCELLED','UNKNOWN')
    NOT NULL DEFAULT 'UNKNOWN',

                        customer_email VARCHAR(320) NULL,

                        order_total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                        currency CHAR(3) NULL,

                        order_created_at DATETIME NULL,
                        order_updated_at DATETIME NULL,
                        ingested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        raw_payload_json JSON NULL,

                        PRIMARY KEY (order_id),

                        UNIQUE KEY uk_order_external (tenant_id, store_id, external_order_id),
                        KEY idx_orders_tenant_updated (tenant_id, order_updated_at),
                        KEY idx_orders_store_updated (store_id, order_updated_at),
                        KEY idx_orders_tenant_number (tenant_id, external_order_number),

                        CONSTRAINT fk_orders_tenant
                            FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id)
                                ON DELETE RESTRICT ON UPDATE CASCADE,

                        CONSTRAINT fk_orders_store
                            FOREIGN KEY (store_id) REFERENCES store(store_id)
                                ON DELETE RESTRICT ON UPDATE CASCADE,

                        CONSTRAINT chk_orders_currency
                            CHECK (currency IS NULL OR currency REGEXP '^[A-Z]{3}$'),

  CONSTRAINT chk_orders_amount
    CHECK (order_total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- order_items table
CREATE TABLE order_items (
                             order_item_id BINARY(16) NOT NULL,
                             tenant_id BINARY(16) NOT NULL,
                             order_id BINARY(16) NOT NULL,

                             external_line_item_id VARCHAR(128) NULL,
                             sku VARCHAR(128) NULL,
                             title VARCHAR(512) NULL,

                             quantity_ordered INT UNSIGNED NOT NULL DEFAULT 0,
                             unit_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                             PRIMARY KEY (order_item_id),
                             UNIQUE KEY uk_order_line (tenant_id, order_id, external_line_item_id),
                             KEY idx_items_tenant_order (tenant_id, order_id),
                             KEY idx_items_sku (tenant_id, sku),

                             CONSTRAINT fk_order_items_tenant
                                 FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id)
                                     ON DELETE RESTRICT ON UPDATE CASCADE,

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id) REFERENCES orders(order_id)
                                     ON DELETE CASCADE ON UPDATE CASCADE,

                             CONSTRAINT chk_items_qty
                                 CHECK (quantity_ordered >= 0),

                             CONSTRAINT chk_items_price
                                 CHECK (unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- fulfillments table
CREATE TABLE fulfillments (
                              fulfillment_id BINARY(16) NOT NULL,
                              tenant_id BINARY(16) NOT NULL,
                              order_id BINARY(16) NOT NULL,

                              external_fulfillment_id VARCHAR(128) NOT NULL,

                              fulfillment_status ENUM('CREATED','SHIPPED','DELIVERED','CANCELLED','FAILED','UNKNOWN')
    NOT NULL DEFAULT 'UNKNOWN',

                              carrier VARCHAR(64) NULL,
                              service_level VARCHAR(64) NULL,
                              ship_from_location VARCHAR(255) NULL,

                              shipped_at DATETIME NULL,
                              delivered_at DATETIME NULL,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              raw_payload_json JSON NULL,

                              PRIMARY KEY (fulfillment_id),

                              UNIQUE KEY uk_fulfillment_external (tenant_id, order_id, external_fulfillment_id),
                              KEY idx_fulfillments_tenant_order (tenant_id, order_id),
                              KEY idx_fulfillments_tenant_updated (tenant_id, updated_at),

                              CONSTRAINT fk_fulfillments_tenant
                                  FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id)
                                      ON DELETE RESTRICT ON UPDATE CASCADE,

                              CONSTRAINT fk_fulfillments_order
                                  FOREIGN KEY (order_id) REFERENCES orders(order_id)
                                      ON DELETE CASCADE ON UPDATE CASCADE,

                              CONSTRAINT chk_ship_delivery_order
                                  CHECK (delivered_at IS NULL OR shipped_at IS NULL OR delivered_at >= shipped_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- tracking table
CREATE TABLE tracking (
                          tracking_id BINARY(16) NOT NULL,
                          tenant_id BINARY(16) NOT NULL,
                          fulfillment_id BINARY(16) NOT NULL,

                          tracking_number VARCHAR(128) NOT NULL,
                          tracking_url VARCHAR(1024) NULL,
                          carrier VARCHAR(64) NULL,

                          tracking_status ENUM('LABEL_CREATED','IN_TRANSIT','OUT_FOR_DELIVERY','DELIVERED','EXCEPTION','UNKNOWN')
    NOT NULL DEFAULT 'UNKNOWN',

                          is_primary BOOLEAN NOT NULL DEFAULT FALSE,
                          last_event_at DATETIME NULL,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          PRIMARY KEY (tracking_id),

                          UNIQUE KEY uk_tracking_number (tenant_id, tracking_number),
                          KEY idx_tracking_tenant_fulfillment (tenant_id, fulfillment_id),
                          KEY idx_tracking_tenant_status (tenant_id, tracking_status),

                          CONSTRAINT fk_tracking_tenant
                              FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id)
                                  ON DELETE RESTRICT ON UPDATE CASCADE,

                          CONSTRAINT fk_tracking_fulfillment
                              FOREIGN KEY (fulfillment_id) REFERENCES fulfillments(fulfillment_id)
                                  ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- tracking_events table
CREATE TABLE tracking_events (
                                 tracking_event_id BINARY(16) NOT NULL,
                                 tenant_id BINARY(16) NOT NULL,
                                 tracking_id BINARY(16) NOT NULL,

                                 event_time DATETIME NOT NULL,
                                 event_code VARCHAR(64) NOT NULL,
                                 event_description VARCHAR(512) NULL,

                                 event_city VARCHAR(128) NULL,
                                 event_state VARCHAR(128) NULL,
                                 event_country VARCHAR(128) NULL,
                                 event_zip VARCHAR(32) NULL,

                                 source ENUM('CARRIER','SHOPIFY','FENIX','OTHER') NOT NULL DEFAULT 'OTHER',

                                 event_hash CHAR(64) NOT NULL,

                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 PRIMARY KEY (tracking_event_id),

                                 UNIQUE KEY uk_event_hash (tenant_id, event_hash),
                                 KEY idx_events_tenant_tracking_time (tenant_id, tracking_id, event_time),

                                 CONSTRAINT fk_events_tenant
                                     FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id)
                                         ON DELETE RESTRICT ON UPDATE CASCADE,

                                 CONSTRAINT fk_events_tracking
                                     FOREIGN KEY (tracking_id) REFERENCES tracking(tracking_id)
                                         ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- 3) INSERT DUMMY DATA
-- ============================================================

-- Insert tenants
INSERT INTO tenant (tenant_id, tenant_name, status)
VALUES
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), 'ACME Corporation', 'ACTIVE'),
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002', 1), 'TechStore Inc', 'ACTIVE'),
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003', 1), 'Fashion Hub', 'ACTIVE');

-- Insert stores
INSERT INTO store (store_id, tenant_id, store_code, store_name, platform, timezone, currency, status)
VALUES
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440101', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), 'ACME-US', 'ACME USA Store', 'SHOPIFY', 'America/New_York', 'USD', 'ACTIVE'),
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440102', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), 'ACME-EU', 'ACME Europe Store', 'NETSUITE', 'Europe/London', 'GBP', 'ACTIVE'),
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440201', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002', 1), 'TECH-MAIN', 'TechStore Main', 'SHOPIFY', 'America/Los_Angeles', 'USD', 'ACTIVE'),
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440301', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003', 1), 'FASHION-WEB', 'Fashion Hub Web', 'MAGENTO', 'America/Chicago', 'USD', 'ACTIVE');

-- Insert orders
INSERT INTO orders (
    order_id, tenant_id, store_id, external_order_id, external_order_number,
    order_status, financial_status, fulfillment_status,
    customer_email, order_total_amount, currency,
    order_created_at, order_updated_at,
    raw_payload_json
)
VALUES
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440501', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440101', 1),
     'EXT-ORD-001', '#1001', 'CREATED', 'PAID', 'UNFULFILLED', 'john.doe@example.com', 299.99, 'USD', '2025-01-20 10:00:00', '2025-01-20 10:30:00',
     JSON_OBJECT('shipping_address', 'New York, NY', 'notes', 'Gift wrapping requested')),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440502', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440101', 1),
     'EXT-ORD-002', '#1002', 'CREATED', 'PAID', 'PARTIAL', 'jane.smith@example.com', 599.50, 'USD', '2025-01-21 08:15:00', '2025-01-21 14:30:00',
     JSON_OBJECT('shipping_address', 'Los Angeles, CA', 'priority', 'high')),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440503', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440201', 1),
     'TECH-EXT-001', 'TEC-5001', 'CLOSED', 'REFUNDED', 'FULFILLED', 'bob.tech@example.com', 1249.99, 'USD', '2025-01-10 09:00:00', '2025-01-15 16:45:00',
     JSON_OBJECT('shipping_address', 'Seattle, WA', 'refund_reason', 'Customer return')),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440504', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440301', 1),
     'FASH-2025-001', 'FSH-9001', 'CREATED', 'PENDING', 'UNFULFILLED', 'alice.fashion@example.com', 125.00, 'USD', '2025-01-22 11:20:00', '2025-01-22 11:20:00',
     JSON_OBJECT('shipping_address', 'Chicago, IL', 'expedited_shipping', true));

-- Insert order items
INSERT INTO order_items (
    order_item_id, tenant_id, order_id, external_line_item_id, sku, title, quantity_ordered, unit_price
)
VALUES
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440601', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440501', 1),
     'LINE-001', 'SKU-LAPTOP-001', 'Dell XPS 13 Laptop', 1, 299.99),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440602', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440502', 1),
     'LINE-001', 'SKU-MOUSE-001', 'Logitech Wireless Mouse', 2, 29.99),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440603', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440502', 1),
     'LINE-002', 'SKU-DESK-001', 'Ergonomic Standing Desk', 1, 539.52),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440604', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440503', 1),
     'LINE-001', 'SKU-MONITOR-001', '4K Ultra Monitor', 1, 1249.99),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440605', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440504', 1),
     'LINE-001', 'SKU-SHIRT-001', 'Summer Casual Shirt', 1, 125.00);

-- Insert fulfillments
INSERT INTO fulfillments (
    fulfillment_id, tenant_id, order_id, external_fulfillment_id, fulfillment_status,
    carrier, service_level, ship_from_location, shipped_at, delivered_at,
    raw_payload_json
)
VALUES
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440701', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440501', 1),
     'FULFILL-001', 'SHIPPED', 'UPS', 'Ground', 'Warehouse A, NY', '2025-01-21 08:00:00', NULL,
     JSON_OBJECT('warehouse_id', 'WH-001', 'batch_number', 'BATCH-2025-01-21')),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440702', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440502', 1),
     'FULFILL-002', 'SHIPPED', 'FedEx', 'Overnight', 'Warehouse A, NY', '2025-01-22 07:30:00', NULL,
     JSON_OBJECT('warehouse_id', 'WH-001', 'batch_number', 'BATCH-2025-01-22')),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440703', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440503', 1),
     'FULFILL-003', 'DELIVERED', 'DHL', 'Standard', 'Warehouse B, CA', '2025-01-12 06:00:00', '2025-01-16 14:00:00',
     JSON_OBJECT('warehouse_id', 'WH-002', 'batch_number', 'BATCH-2025-01-12')),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440704', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440504', 1),
     'FULFILL-004', 'CREATED', 'UPS', 'Ground', 'Warehouse C, IL', NULL, NULL,
     JSON_OBJECT('warehouse_id', 'WH-003', 'batch_number', 'PENDING'));

-- Insert tracking numbers
INSERT INTO tracking (
    tracking_id, tenant_id, fulfillment_id, tracking_number, tracking_url, carrier,
    tracking_status, is_primary, last_event_at
)
VALUES
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440801', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440701', 1),
     '1Z999AA10123456784', 'https://tracking.ups.com/track?tracknum=1Z999AA10123456784', 'UPS', 'IN_TRANSIT', TRUE, '2025-01-22 10:15:00'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440802', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440702', 1),
     '794643957531', 'https://tracking.fedex.com/tracking?tracknumbers=794643957531', 'FedEx', 'OUT_FOR_DELIVERY', TRUE, '2025-01-22 14:30:00'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440803', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440703', 1),
     '1088363051', 'https://www.dhl.com/en/en/shipped.html?tracking=1088363051', 'DHL', 'DELIVERED', TRUE, '2025-01-16 14:00:00'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440804', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440003', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440704', 1),
     '1Z888AA2B5678901234', 'https://tracking.ups.com/track?tracknum=1Z888AA2B5678901234', 'UPS', 'LABEL_CREATED', TRUE, NULL);

-- Insert tracking events
INSERT INTO tracking_events (
    tracking_event_id, tenant_id, tracking_id, event_time, event_code, event_description,
    event_city, event_state, event_country, event_zip, source,
    event_hash
)
VALUES
    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440901', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440801', 1),
     '2025-01-21 08:00:00', 'LABEL_CREATED', 'Shipping label created', 'New York', 'NY', 'USA', '10001', 'CARRIER',
     'a0a1a2a3a4a5a6a7a8a9a0a1a2a3a4a5a6a7a8a9a0a1a2a3a4a5a6a7a8a901'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440902', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440801', 1),
     '2025-01-21 15:30:00', 'IN_TRANSIT', 'Package picked up', 'New York', 'NY', 'USA', '10001', 'CARRIER',
     'b1b2b3b4b5b6b7b8b9b0b1b2b3b4b5b6b7b8b9b0b1b2b3b4b5b6b7b8b9b002'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440903', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440801', 1),
     '2025-01-22 05:45:00', 'IN_TRANSIT', 'In transit to destination', 'New Jersey', 'NJ', 'USA', '07000', 'CARRIER',
     'c2c3c4c5c6c7c8c9c0c1c2c3c4c5c6c7c8c9c0c1c2c3c4c5c6c7c8c9c0c003'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440904', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440801', 1),
     '2025-01-22 10:15:00', 'OUT_FOR_DELIVERY', 'Out for delivery', 'Boston', 'MA', 'USA', '02101', 'CARRIER',
     'd3d4d5d6d7d8d9d0d1d2d3d4d5d6d7d8d9d0d1d2d3d4d5d6d7d8d9d0d1d004'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440905', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440802', 1),
     '2025-01-22 08:00:00', 'IN_TRANSIT', 'Picked up', 'New York', 'NY', 'USA', '10001', 'CARRIER',
     'e4e5e6e7e8e9e0e1e2e3e4e5e6e7e8e9e0e1e2e3e4e5e6e7e8e9e0e1e2e005'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440906', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440001', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440802', 1),
     '2025-01-22 14:30:00', 'OUT_FOR_DELIVERY', 'Out for delivery today', 'Los Angeles', 'CA', 'USA', '90001', 'CARRIER',
     'f5f6f7f8f9f0f1f2f3f4f5f6f7f8f9f0f1f2f3f4f5f6f7f8f9f0f1f2f3f4f006'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440907', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440803', 1),
     '2025-01-12 06:00:00', 'IN_TRANSIT', 'Shipment in transit', 'San Francisco', 'CA', 'USA', '94102', 'CARRIER',
     'a6a7a8a9a0a1a2a3a4a5a6a7a8a9a0a1a2a3a4a5a6a7a8a9a0a1a2a3a4a507'),

    (UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440908', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440002', 1), UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440803', 1),
     '2025-01-16 14:00:00', 'DELIVERED', 'Package delivered', 'Seattle', 'WA', 'USA', '98101', 'CARRIER',
     'b7b8b9b0b1b2b3b4b5b6b7b8b9b0b1b2b3b4b5b6b7b8b9b0b1b2b3b4b5b6b508');

-- ============================================================
-- 4) VERIFICATION QUERIES
-- ============================================================

-- Verify tenant count
SELECT COUNT(*) AS total_tenants FROM tenant WHERE status = 'ACTIVE';

-- Verify store count
SELECT COUNT(*) AS total_stores FROM store WHERE status = 'ACTIVE';

-- Verify order count
SELECT COUNT(*) AS total_orders FROM orders;

-- Verify order items count
SELECT COUNT(*) AS total_order_items FROM order_items;

-- Verify fulfillments count
SELECT COUNT(*) AS total_fulfillments FROM fulfillments;

-- Verify tracking count
SELECT COUNT(*) AS total_tracking FROM tracking;

-- Verify tracking events count
SELECT COUNT(*) AS total_tracking_events FROM tracking_events;

-- Sample: Show orders with customer info
SELECT
    BIN_TO_UUID(o.order_id, 1) AS order_id,
    BIN_TO_UUID(o.tenant_id, 1) AS tenant_id,
    o.external_order_id,
    o.external_order_number,
    o.customer_email,
    o.order_total_amount,
    o.currency,
    o.order_status,
    o.financial_status,
    o.fulfillment_status
FROM orders o
    LIMIT 10;

-- Sample: Show tracking with events count
SELECT
    BIN_TO_UUID(t.tracking_id, 1) AS tracking_id,
    t.tracking_number,
    t.carrier,
    t.tracking_status,
    COUNT(te.tracking_event_id) AS event_count,
    MAX(te.event_time) AS last_event_time
FROM tracking t
         LEFT JOIN tracking_events te ON t.tracking_id = te.tracking_id AND t.tenant_id = te.tenant_id
GROUP BY t.tracking_id, t.tracking_number, t.carrier, t.tracking_status;

-- ============================================================
-- END OF INITIALIZATION SCRIPT
-- ============================================================
