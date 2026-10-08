-- Seed Users
-- Two sample User accounts + One sample Administrator account
INSERT INTO users (id, name, phone, email, role, status) VALUES
(1, 'USER1', '0771234567', 'user1@property.lk', 'USER', 'ACTIVE'),
(2, 'USER2', '0777654321', 'user2@property.lk', 'USER', 'ACTIVE'),
(3, 'ADMIN1', '0770000000', 'admin@property.lk', 'ADMINISTRATOR', 'ACTIVE');

-- Seed Resources / Maintenance Tickets
-- S101: Capacity 10, Active
-- S102: Capacity 5, Active
-- S103: Capacity 0, Inactive
INSERT INTO resources (id, reference, name, capacity, available_capacity, status) VALUES
(1, 'S101', 'Maintenance Ticket A', 10, 10, 'ACTIVE'),
(2, 'S102', 'Maintenance Ticket B', 5, 5, 'ACTIVE'),
(3, 'S103', 'Maintenance Ticket C', 0, 0, 'INACTIVE');
