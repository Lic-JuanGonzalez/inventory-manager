-- Rename TransferStatus values to English
UPDATE transfer_requests SET status = 'PENDING'    WHERE status = 'PENDIENTE';
UPDATE transfer_requests SET status = 'APPROVED'   WHERE status = 'APROBADA';
UPDATE transfer_requests SET status = 'IN_TRANSIT' WHERE status = 'EN_TRANSITO';
UPDATE transfer_requests SET status = 'RECEIVED'   WHERE status = 'RECIBIDA';
UPDATE transfer_requests SET status = 'CANCELLED'  WHERE status = 'CANCELADA';

-- Rename MovementType values to English
UPDATE inventory_movements SET type = 'INBOUND'  WHERE type = 'ENTRADA';
UPDATE inventory_movements SET type = 'OUTBOUND' WHERE type = 'SALIDA';

-- Rename MovementReason values to English
UPDATE inventory_movements SET reason = 'PURCHASE'             WHERE reason = 'COMPRA';
UPDATE inventory_movements SET reason = 'RETURN_INBOUND'       WHERE reason = 'DEVOLUCION_ENTRADA';
UPDATE inventory_movements SET reason = 'POSITIVE_ADJUSTMENT'  WHERE reason = 'AJUSTE_POSITIVO';
UPDATE inventory_movements SET reason = 'TRANSFER_INBOUND'     WHERE reason = 'TRANSFERENCIA_ENTRADA';
UPDATE inventory_movements SET reason = 'SALE'                 WHERE reason = 'VENTA';
UPDATE inventory_movements SET reason = 'LOSS'                 WHERE reason = 'PERDIDA';
UPDATE inventory_movements SET reason = 'NEGATIVE_ADJUSTMENT'  WHERE reason = 'AJUSTE_NEGATIVO';
UPDATE inventory_movements SET reason = 'TRANSFER_OUTBOUND'    WHERE reason = 'TRANSFERENCIA_SALIDA';

-- Translate role descriptions
UPDATE roles SET description = 'System administrator with full access'              WHERE name = 'ADMIN';
UPDATE roles SET description = 'Branch operator: inbound, outbound, and queries'   WHERE name = 'OPERATOR';
UPDATE roles SET description = 'Auditor: read-only access to movements and reports' WHERE name = 'AUDITOR';

-- Translate admin user last name
UPDATE users SET last_name = 'System' WHERE email = 'admin@inventory.com' AND last_name = 'Sistema';

-- Translate product category names and descriptions
UPDATE product_categories SET name = 'Electronics', description = 'Electronic devices and equipment'        WHERE name = 'Electrónicos';
UPDATE product_categories SET name = 'Office',      description = 'Office supplies and equipment'           WHERE name = 'Oficina';
UPDATE product_categories SET name = 'Cleaning',    description = 'Cleaning and hygiene products'           WHERE name = 'Limpieza';
UPDATE product_categories SET name = 'Tools',       description = 'Work tools and equipment'                WHERE name = 'Herramientas';
UPDATE product_categories SET name = 'Computing',   description = 'Computing equipment and accessories'     WHERE name = 'Computación';

-- Translate product names and descriptions
UPDATE products SET name = 'Logitech Mechanical Keyboard',
                    description = 'Wireless mechanical keyboard with backlight'
WHERE sku = 'TEC-001';

UPDATE products SET description = '24-inch Full HD monitor, IPS panel'
WHERE sku = 'MON-001';

UPDATE products SET name = 'A4 Paper Ream',
                    description = '500 sheets, 75 grams, white'
WHERE sku = 'PAP-001';

UPDATE products SET name = 'Ergonomic Chair',
                    description = 'Office chair with adjustable lumbar support'
WHERE sku = 'SIL-001';

-- Translate branch names and addresses
UPDATE branches SET name = 'Central Branch', address = 'Main Ave. 100, Center'      WHERE name = 'Sucursal Central';
UPDATE branches SET name = 'North Branch',   address = 'North St. 250, North Zone'  WHERE name = 'Sucursal Norte';
UPDATE branches SET name = 'South Branch',   address = 'South Ave. 500, South Zone' WHERE name = 'Sucursal Sur';
UPDATE branches SET name = 'East Branch',    address = 'East Blvd. 300, East Zone'  WHERE name = 'Sucursal Este';
UPDATE branches SET name = 'West Branch',    address = 'West St. 150, West Zone'    WHERE name = 'Sucursal Oeste';
