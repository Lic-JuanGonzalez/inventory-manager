-- ============================================================
-- V2: Initial seed data
-- ============================================================

-- Roles
INSERT INTO roles (name, description) VALUES
    ('ADMIN',    'System administrator with full access'),
    ('OPERATOR', 'Branch operator: inbound, outbound, and queries'),
    ('AUDITOR',  'Auditor: read-only access to movements and reports');

-- Product categories
INSERT INTO product_categories (name, description) VALUES
    ('Electronics', 'Electronic devices and equipment'),
    ('Office',      'Office supplies and equipment'),
    ('Cleaning',    'Cleaning and hygiene products'),
    ('Tools',       'Work tools and equipment'),
    ('Computing',   'Computing equipment and accessories');

-- Initial admin user
-- Password: Admin@1234 (BCrypt cost 12)
INSERT INTO users (name, last_name, email, password, role_id, active)
SELECT 'Admin', 'System',
       'admin@inventory.com',
       '$2a$12$dMIg/wlW4HeBGVKsz2z1mu8FIHaEUHqtOTeFX5Wvw/2w//9FiQ6wu',
       r.id, TRUE
FROM roles r WHERE r.name = 'ADMIN';

-- Sample branches
INSERT INTO branches (name, address, phone, email, active) VALUES
    ('Central Branch', 'Main Ave. 100, Center',       '555-0100', 'central@company.com', TRUE),
    ('North Branch',   'North St. 250, North Zone',   '555-0101', 'north@company.com',   TRUE),
    ('South Branch',   'South Ave. 500, South Zone',  '555-0102', 'south@company.com',   TRUE),
    ('East Branch',    'East Blvd. 300, East Zone',   '555-0103', 'east@company.com',    TRUE),
    ('West Branch',    'West St. 150, West Zone',     '555-0104', 'west@company.com',    FALSE);

-- Sample products
INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'LAP-001', 'Dell Inspiron 15 Laptop', 'Laptop 15" Intel Core i5, 8GB RAM, 256GB SSD',
       c.id, 'UNIT', 8500.00, TRUE
FROM product_categories c WHERE c.name = 'Computing';

INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'MON-001', 'Samsung 24" Monitor', '24-inch Full HD monitor, IPS panel',
       c.id, 'UNIT', 3200.00, TRUE
FROM product_categories c WHERE c.name = 'Computing';

INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'TEC-001', 'Logitech Mechanical Keyboard', 'Wireless mechanical keyboard with backlight',
       c.id, 'UNIT', 1200.00, TRUE
FROM product_categories c WHERE c.name = 'Computing';

INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'PAP-001', 'A4 Paper Ream', '500 sheets, 75 grams, white',
       c.id, 'REAM', 85.00, TRUE
FROM product_categories c WHERE c.name = 'Office';

INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'SIL-001', 'Ergonomic Chair', 'Office chair with adjustable lumbar support',
       c.id, 'UNIT', 2800.00, TRUE
FROM product_categories c WHERE c.name = 'Office';

-- Initial inventory (stock in active branches)
INSERT INTO inventory (product_id, branch_id, current_stock, min_stock, max_stock)
SELECT p.id, b.id,
       CASE
           WHEN p.sku = 'LAP-001' AND b.name = 'Central Branch' THEN 15
           WHEN p.sku = 'LAP-001' AND b.name = 'North Branch'   THEN 8
           WHEN p.sku = 'LAP-001' AND b.name = 'South Branch'   THEN 3
           WHEN p.sku = 'MON-001' AND b.name = 'Central Branch' THEN 25
           WHEN p.sku = 'MON-001' AND b.name = 'North Branch'   THEN 12
           WHEN p.sku = 'TEC-001' AND b.name = 'Central Branch' THEN 30
           WHEN p.sku = 'PAP-001' AND b.name = 'Central Branch' THEN 200
           WHEN p.sku = 'PAP-001' AND b.name = 'North Branch'   THEN 100
           WHEN p.sku = 'SIL-001' AND b.name = 'Central Branch' THEN 10
           ELSE 0
       END,
       CASE
           WHEN p.sku = 'LAP-001' THEN 5
           WHEN p.sku = 'MON-001' THEN 5
           WHEN p.sku = 'TEC-001' THEN 10
           WHEN p.sku = 'PAP-001' THEN 50
           WHEN p.sku = 'SIL-001' THEN 3
           ELSE 5
       END,
       CASE
           WHEN p.sku = 'LAP-001' THEN 50
           WHEN p.sku = 'MON-001' THEN 50
           WHEN p.sku = 'TEC-001' THEN 100
           WHEN p.sku = 'PAP-001' THEN 500
           WHEN p.sku = 'SIL-001' THEN 30
           ELSE 100
       END
FROM products p
CROSS JOIN branches b
WHERE b.active = TRUE
AND (
    (p.sku = 'LAP-001' AND b.name IN ('Central Branch', 'North Branch', 'South Branch')) OR
    (p.sku = 'MON-001' AND b.name IN ('Central Branch', 'North Branch')) OR
    (p.sku = 'TEC-001' AND b.name = 'Central Branch') OR
    (p.sku = 'PAP-001' AND b.name IN ('Central Branch', 'North Branch')) OR
    (p.sku = 'SIL-001' AND b.name = 'Central Branch')
);
