-- Gym Management System Database Schema
-- PostgreSQL 15 Compatible

-- Drop tables in order of dependencies
DROP TABLE IF EXISTS equipment_usage CASCADE;
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS subscriptions CASCADE;
DROP TABLE IF EXISTS classes CASCADE;
DROP TABLE IF EXISTS trainers CASCADE;
DROP TABLE IF EXISTS members CASCADE;
DROP TABLE IF EXISTS branches CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Branches Table
CREATE TABLE branches (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(150) NOT NULL,
    capacity INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Members Table
CREATE TABLE members (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    join_date DATE NOT NULL,
    branch_id INT REFERENCES branches(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Trainers Table
CREATE TABLE trainers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    availability_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    branch_id INT REFERENCES branches(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Classes Table
CREATE TABLE classes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    trainer_id INT REFERENCES trainers(id) ON DELETE SET NULL,
    branch_id INT REFERENCES branches(id) ON DELETE CASCADE,
    schedule_time TIMESTAMP NOT NULL,
    max_occupancy INT NOT NULL DEFAULT 20,
    current_occupancy INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Subscriptions Table
CREATE TABLE subscriptions (
    id SERIAL PRIMARY KEY,
    member_id INT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    plan_name VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Users Table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Payments Table
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    member_id INT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_month INT NOT NULL CHECK (payment_month BETWEEN 1 AND 12),
    payment_year INT NOT NULL,
    payment_date DATE NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by INT REFERENCES users(id) ON DELETE SET NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_payments_member_month_year UNIQUE (member_id, payment_month, payment_year)
);

CREATE INDEX idx_payment_month_year ON payments(payment_month, payment_year);
CREATE INDEX idx_payments_verified ON payments(verified);

-- Attendance Table
CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    member_id INT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    branch_id INT NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL DEFAULT CURRENT_DATE,
    check_in_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    check_out_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Equipment Usage Table
CREATE TABLE equipment_usage (
    id SERIAL PRIMARY KEY,
    member_id INT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    branch_id INT NOT NULL REFERENCES branches(id) ON DELETE CASCADE,
    equipment_name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP
);

-- Insert Default Seed Data
INSERT INTO branches (name, location, capacity) VALUES
('Downtown Elite', '123 Main St, Metro City', 80),
('Westside Powerhouse', '456 West Blvd, Metro City', 120),
('Northside Fitness', '789 North Ave, Metro City', 60);

INSERT INTO members (name, email, phone, status, join_date, branch_id) VALUES
('John Doe', 'john@example.com', '1234567890', 'ACTIVE', '2024-01-01', 1),
('Jane Smith', 'jane@example.com', '0987654321', 'ACTIVE', '2024-01-15', 1),
('Bob Johnson', 'bob@example.com', '5551234567', 'ACTIVE', '2024-02-10', 2),
('Alice Brown', 'alice@example.com', '5559876543', 'ACTIVE', '2024-03-01', 3);

INSERT INTO trainers (name, specialty, availability_status, branch_id) VALUES
('Mike Tyson', 'Boxing & Conditioning', 'AVAILABLE', 1),
('Serena Williams', 'Agility & Endurance', 'AVAILABLE', 1),
('Arnold Schwarzenegger', 'Bodybuilding & Powerlifting', 'AVAILABLE', 2),
('Yogi Raman', 'Yoga & Meditation', 'AVAILABLE', 3);

INSERT INTO classes (name, trainer_id, branch_id, schedule_time, max_occupancy, current_occupancy) VALUES
('HIIT Blast', 2, 1, CURRENT_TIMESTAMP + INTERVAL '2 hours', 15, 0),
('Heavyweight Power', 3, 2, CURRENT_TIMESTAMP + INTERVAL '4 hours', 20, 0),
('Zen Yoga', 4, 3, CURRENT_TIMESTAMP + INTERVAL '1 day', 25, 0);

INSERT INTO subscriptions (member_id, plan_name, amount, start_date, end_date, status) VALUES
(1, 'Premium Monthly', 50.00, '2026-05-01', '2026-06-01', 'ACTIVE'),
(2, 'Annual Pass', 500.00, '2026-01-01', '2026-12-31', 'ACTIVE'),
(3, 'Basic Monthly', 30.00, '2026-05-15', '2026-06-15', 'ACTIVE'),
(4, 'Premium Monthly', 50.00, '2026-05-01', '2026-06-01', 'ACTIVE');

INSERT INTO payments (member_id, amount, payment_month, payment_year, payment_date, verified) VALUES
(1, 50.00, 5, 2026, '2026-05-01', TRUE),
(2, 500.00, 1, 2026, '2026-01-01', TRUE),
(3, 30.00, 5, 2026, '2026-05-15', FALSE),
(4, 50.00, 5, 2026, '2026-05-01', TRUE);
