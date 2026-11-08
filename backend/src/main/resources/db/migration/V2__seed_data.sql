-- ChitCircle V2 Seed Data
-- Roles
INSERT INTO roles (id, role_name) VALUES 
('11111111-1111-1111-1111-111111111101', 'SUPER_ADMIN'),
('11111111-1111-1111-1111-111111111102', 'GROUP_ADMIN'),
('11111111-1111-1111-1111-111111111103', 'GROUP_MEMBER')
ON CONFLICT (id) DO NOTHING;

-- Admin User: admin@chitcircle.com / password123 (BCrypt: $2a$10$7R4Q7C9dklb53X30aUjQ6eq0B1R7aP5qLqA.w56eN2HlyXn46.kIe)
INSERT INTO users (id, full_name, phone_number, email, password_hash, status, created_at) VALUES
('22222222-2222-2222-2222-222222222201', 'Rajesh Sharma', '+919876543210', 'admin@chitcircle.com', '$2a$10$7R4Q7C9dklb53X30aUjQ6eq0B1R7aP5qLqA.w56eN2HlyXn46.kIe', 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Assign GROUP_ADMIN role
INSERT INTO user_roles (user_id, role_id) VALUES
('22222222-2222-2222-2222-222222222201', '11111111-1111-1111-1111-111111111102')
ON CONFLICT DO NOTHING;

-- Sample Member User: member@chitcircle.com / password123
INSERT INTO users (id, full_name, phone_number, email, password_hash, status, created_at) VALUES
('22222222-2222-2222-2222-222222222202', 'Amit Patel', '+919876543211', 'member@chitcircle.com', '$2a$10$7R4Q7C9dklb53X30aUjQ6eq0B1R7aP5qLqA.w56eN2HlyXn46.kIe', 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
('22222222-2222-2222-2222-222222222202', '11111111-1111-1111-1111-111111111103')
ON CONFLICT DO NOTHING;

