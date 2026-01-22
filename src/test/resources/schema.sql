-- ============================================================
-- Fenix Commerce - Database Tables Initialization Script
-- Multi-tenant Orders / Fulfillment / Tracking (MySQL 8.x)
-- UUID PKs stored as BINARY(16) using UUID_TO_BIN(..., 1)
-- ============================================================
-- Requirements:
--  - MySQL 8.0.16+ for CHECK constraint enforcement
--  - InnoDB + utf8mb4
-- ============================================================

-- Create Database Tables if not exists for TestContainer setup

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
-- End of Fenix Commerce Database Tables Initialization Script
-- ============================================================