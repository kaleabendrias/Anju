-- Create schema for anju_db
CREATE DATABASE IF NOT EXISTS anju_db;
USE anju_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    email_encrypted TEXT,
    phone_encrypted TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS properties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unique_code VARCHAR(50) NOT NULL UNIQUE,
    owner_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    rent DECIMAL(12, 2) NOT NULL,
    deposit DECIMAL(12, 2) NOT NULL,
    rental_start_date DATE,
    rental_end_date DATE,
    materials_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_unique_code (unique_code),
    INDEX idx_property_owner (owner_id)
);

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unique_appointment_number VARCHAR(20) UNIQUE,
    idempotency_key VARCHAR(64) UNIQUE,
    order_amount DECIMAL(12, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    accompanying_staff_id BIGINT,
    resource_id BIGINT,
    patient_name VARCHAR(100),
    notes TEXT,
    reschedule_count INT DEFAULT 0,
    penalty_amount DECIMAL(12, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    operator_id BIGINT,
    cancel_reason VARCHAR(255),
    penalty_reason VARCHAR(255),
    service_type VARCHAR(30) NOT NULL DEFAULT 'STANDARD_CONSULTATION',
    INDEX idx_appointment_time (start_time, end_time),
    INDEX idx_medical_staff (accompanying_staff_id),
    INDEX idx_resource (resource_id),
    INDEX idx_unique_appointment_number (unique_appointment_number),
    INDEX idx_idempotency_key (idempotency_key),
    INDEX idx_service_type (service_type)
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trx_id VARCHAR(64) UNIQUE,
    idempotency_key VARCHAR(128) UNIQUE,
    appointment_id BIGINT,
    amount DECIMAL(14, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    refundable_flag BOOLEAN DEFAULT TRUE,
    original_transaction_id BIGINT,
    operator_id BIGINT,
    remark VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_idempotency_key (idempotency_key),
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_timestamp (timestamp)
);

CREATE TABLE IF NOT EXISTS settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_date DATE NOT NULL UNIQUE,
    total_income DECIMAL(14, 2) NOT NULL DEFAULT 0,
    total_refunds DECIMAL(14, 2) NOT NULL DEFAULT 0,
    total_penalties DECIMAL(14, 2) NOT NULL DEFAULT 0,
    net_amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    transaction_count INT DEFAULT 0,
    exception_flag BOOLEAN DEFAULT FALSE,
    exception_message TEXT,
    invoice_status VARCHAR(20) DEFAULT 'NOT_REQUESTED',
    invoice_requested_at TIMESTAMP NULL,
    invoice_issued_at TIMESTAMP NULL,
    invoice_rejected_at TIMESTAMP NULL,
    invoice_reject_reason VARCHAR(255),
    generated_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_settlement_date (settlement_date)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    operation VARCHAR(20) NOT NULL,
    operator_id BIGINT,
    operator_username VARCHAR(100),
    field_changes TEXT,
    summary TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_operator (operator_id),
    INDEX idx_timestamp (timestamp)
);

CREATE TABLE IF NOT EXISTS file_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    logical_id VARCHAR(64),
    file_hash VARCHAR(64) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    size BIGINT NOT NULL,
    version_number INT NOT NULL DEFAULT 1,
    is_deleted BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    expiration_time TIMESTAMP NULL,
    storage_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100),
    uploaded_by BIGINT,
    content_disposition VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    INDEX idx_file_hash (file_hash),
    INDEX idx_logical_id (logical_id),
    INDEX idx_is_deleted_expired (is_deleted, expiration_time)
);

-- Seed data for users (password: Anju@1234)
-- BCrypt hash generated with strength 10
INSERT INTO users (username, password_hash, role)
SELECT 'admin', '$2a$10$5S.ULcxVQtees4reLvbYnOKRkLhDAIJgKyfFqqL1a9lrVqFKjJhpS', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password_hash, role)
SELECT 'reviewer', '$2a$10$5S.ULcxVQtees4reLvbYnOKRkLhDAIJgKyfFqqL1a9lrVqFKjJhpS', 'REVIEWER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'reviewer');

INSERT INTO users (username, password_hash, role)
SELECT 'dispatcher', '$2a$10$5S.ULcxVQtees4reLvbYnOKRkLhDAIJgKyfFqqL1a9lrVqFKjJhpS', 'DISPATCHER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'dispatcher');

INSERT INTO users (username, password_hash, role)
SELECT 'finance', '$2a$10$5S.ULcxVQtees4reLvbYnOKRkLhDAIJgKyfFqqL1a9lrVqFKjJhpS', 'FINANCE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'finance');

INSERT INTO users (username, password_hash, role)
SELECT 'frontline', '$2a$10$5S.ULcxVQtees4reLvbYnOKRkLhDAIJgKyfFqqL1a9lrVqFKjJhpS', 'FRONTLINE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'frontline');
