-- Drop tables in reverse order of foreign key constraints
DROP TABLE IF EXISTS request_status_history;
DROP TABLE IF EXISTS service_requests;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL,       -- 'USER' or 'ADMINISTRATOR'
    status VARCHAR(20) NOT NULL      -- 'ACTIVE' or 'INACTIVE'
);

-- 2. Resources / Maintenance Services Table
CREATE TABLE resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(50) NOT NULL UNIQUE,   -- e.g. 'S101', 'S102', 'S103'
    name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    available_capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL              -- 'ACTIVE' or 'INACTIVE'
);

-- 3. Service Requests Table
CREATE TABLE service_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    reference VARCHAR(100) NOT NULL,
    charge DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL,             -- 'SUBMITTED', 'APPROVED/ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
    submitted_date DATETIME NOT NULL,
    snapshot_data TEXT NOT NULL,             -- Historical snapshot of submitted details & resource parameters (JSON/Text)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_request_resource FOREIGN KEY (resource_id) REFERENCES resources(id)
);

-- 4. Request Status History / Audit Trail Table
CREATE TABLE request_status_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    old_status VARCHAR(30) NULL,
    new_status VARCHAR(30) NOT NULL,
    event_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_request FOREIGN KEY (request_id) REFERENCES service_requests(request_id) ON DELETE CASCADE,
    CONSTRAINT fk_history_actor FOREIGN KEY (actor_id) REFERENCES users(id)
);
