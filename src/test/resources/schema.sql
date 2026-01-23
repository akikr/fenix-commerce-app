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
DROP TABLE IF EXISTS tenant;

-- ============================================================
-- 2) CREATE TABLES (DDL)
-- ============================================================

-- tenant table
CREATE TABLE tenant (
                        tenant_id BINARY(16) NOT NULL,
                        tenant_name VARCHAR(255) NOT NULL,
                        status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
                        created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                        updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                        PRIMARY KEY (tenant_id),
                        UNIQUE KEY uk_tenant_name(tenant_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- End of Fenix Commerce Database Tables Initialization Script
-- ============================================================