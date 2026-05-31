-- ============================================================
-- V2: Datos iniciales del sistema
-- ============================================================

-- Roles
INSERT INTO roles (name, description) VALUES
    ('ADMIN',    'Administrador del sistema con acceso total'),
    ('OPERATOR', 'Operador de sucursal: ingresos, egresos y consultas'),
    ('AUDITOR',  'Auditor: solo lectura de movimientos y reportes');

-- Categorías de productos
INSERT INTO product_categories (name, description) VALUES
    ('Electrónicos',   'Dispositivos y equipos electrónicos'),
    ('Oficina',        'Suministros y equipos de oficina'),
    ('Limpieza',       'Productos de limpieza e higiene'),
    ('Herramientas',   'Herramientas y equipos de trabajo'),
    ('Computación',    'Equipos y accesorios de computación');

-- Usuario administrador inicial
-- Contraseña: Admin@1234 (BCrypt hash)
INSERT INTO users (name, last_name, email, password, role_id, active)
SELECT 'Admin', 'Sistema',
       'admin@inventory.com',
       '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
       r.id, TRUE
FROM roles r WHERE r.name = 'ADMIN';

-- Sucursales de ejemplo
INSERT INTO branches (name, address, phone, email, active) VALUES
    ('Sucursal Central',   'Av. Principal 100, Centro',          '555-0100', 'central@empresa.com',   TRUE),
    ('Sucursal Norte',     'Calle Norte 250, Zona Norte',        '555-0101', 'norte@empresa.com',     TRUE),
    ('Sucursal Sur',       'Av. Sur 500, Zona Sur',              '555-0102', 'sur@empresa.com',       TRUE),
    ('Sucursal Este',      'Boulevard Este 300, Zona Este',      '555-0103', 'este@empresa.com',      TRUE),
    ('Sucursal Oeste',     'Calle Poniente 150, Zona Oeste',     '555-0104', 'oeste@empresa.com',     FALSE);

-- Productos de ejemplo
INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'LAP-001', 'Laptop Dell Inspiron 15', 'Laptop 15" Intel Core i5, 8GB RAM, 256GB SSD',
       c.id, 'UNIDAD', 8500.00, TRUE
FROM product_categories c WHERE c.name = 'Computación';

INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'MON-001', 'Monitor Samsung 24"', 'Monitor Full HD 24 pulgadas, panel IPS',
       c.id, 'UNIDAD', 3200.00, TRUE
FROM product_categories c WHERE c.name = 'Computación';

INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'TEC-001', 'Teclado Mecánico Logitech', 'Teclado mecánico inalámbrico con retroiluminación',
       c.id, 'UNIDAD', 1200.00, TRUE
FROM product_categories c WHERE c.name = 'Computación';

INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'PAP-001', 'Resma de Papel A4', 'Resma 500 hojas, 75 gramos, color blanco',
       c.id, 'RESMA', 85.00, TRUE
FROM product_categories c WHERE c.name = 'Oficina';

INSERT INTO products (sku, name, description, category_id, unit_of_measure, reference_price, active)
SELECT 'SIL-001', 'Silla Ergonómica', 'Silla de oficina con soporte lumbar ajustable',
       c.id, 'UNIDAD', 2800.00, TRUE
FROM product_categories c WHERE c.name = 'Oficina';

-- Inventario inicial (stock en sucursales activas)
INSERT INTO inventory (product_id, branch_id, current_stock, min_stock, max_stock)
SELECT p.id, b.id,
       CASE
           WHEN p.sku = 'LAP-001' AND b.name = 'Sucursal Central' THEN 15
           WHEN p.sku = 'LAP-001' AND b.name = 'Sucursal Norte'   THEN 8
           WHEN p.sku = 'LAP-001' AND b.name = 'Sucursal Sur'     THEN 3
           WHEN p.sku = 'MON-001' AND b.name = 'Sucursal Central' THEN 25
           WHEN p.sku = 'MON-001' AND b.name = 'Sucursal Norte'   THEN 12
           WHEN p.sku = 'TEC-001' AND b.name = 'Sucursal Central' THEN 30
           WHEN p.sku = 'PAP-001' AND b.name = 'Sucursal Central' THEN 200
           WHEN p.sku = 'PAP-001' AND b.name = 'Sucursal Norte'   THEN 100
           WHEN p.sku = 'SIL-001' AND b.name = 'Sucursal Central' THEN 10
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
    (p.sku = 'LAP-001' AND b.name IN ('Sucursal Central','Sucursal Norte','Sucursal Sur')) OR
    (p.sku = 'MON-001' AND b.name IN ('Sucursal Central','Sucursal Norte')) OR
    (p.sku = 'TEC-001' AND b.name = 'Sucursal Central') OR
    (p.sku = 'PAP-001' AND b.name IN ('Sucursal Central','Sucursal Norte')) OR
    (p.sku = 'SIL-001' AND b.name = 'Sucursal Central')
);
