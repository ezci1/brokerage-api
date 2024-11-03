-- Insert an admin user
INSERT INTO "users" (id, name, username, password, is_enabled) 
VALUES (1, 'Admin User', 'admin', '{bcrypt}$2a$12$jb51wny082o22Hqq1uB78.8llHrSMLB6tcPCrteSjEdhiunowRWG2', true);

-- Insert a customer user
INSERT INTO "users" (id, name, username, password, is_enabled) 
VALUES (2, 'Customer User', 'customer', '{bcrypt}$2a$12$jb51wny082o22Hqq1uB78.8llHrSMLB6tcPCrteSjEdhiunowRWG2', true);

-- Assign roles to admin and customer users
INSERT INTO "authorities" (user_id, role) VALUES (1, 'ROLE_ADMIN');
INSERT INTO "authorities" (user_id, role) VALUES (2, 'ROLE_CUSTOMER');