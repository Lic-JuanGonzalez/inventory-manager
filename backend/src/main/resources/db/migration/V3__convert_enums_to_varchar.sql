-- Convert PostgreSQL custom ENUM columns to VARCHAR for Hibernate compatibility
ALTER TABLE transfer_requests
    ALTER COLUMN status TYPE VARCHAR(50) USING status::text;

ALTER TABLE inventory_movements
    ALTER COLUMN type   TYPE VARCHAR(50) USING type::text;

ALTER TABLE inventory_movements
    ALTER COLUMN reason TYPE VARCHAR(50) USING reason::text;
