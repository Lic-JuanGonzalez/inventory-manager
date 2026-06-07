-- ============================================================
-- V1: Initial schema - Multi-Branch Inventory Management System
-- ============================================================

-- Roles
CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Users
CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    last_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    role_id      BIGINT       NOT NULL REFERENCES roles(id),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email  ON users(email);
CREATE INDEX idx_users_role   ON users(role_id);
CREATE INDEX idx_users_active ON users(active);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user  ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Branches
CREATE TABLE branches (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(150) NOT NULL UNIQUE,
    address    VARCHAR(255) NOT NULL,
    phone      VARCHAR(30),
    email      VARCHAR(150),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_branches_active ON branches(active);

-- Product categories
CREATE TABLE product_categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Products
CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(50)    NOT NULL UNIQUE,
    name            VARCHAR(200)   NOT NULL,
    description     TEXT,
    category_id     BIGINT         REFERENCES product_categories(id),
    unit_of_measure VARCHAR(30)    NOT NULL DEFAULT 'UNIT',
    reference_price NUMERIC(15,2)  NOT NULL DEFAULT 0,
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_sku      ON products(sku);
CREATE INDEX idx_products_name     ON products(name);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active   ON products(active);

-- Inventory per branch
CREATE TABLE inventory (
    id            BIGSERIAL PRIMARY KEY,
    product_id    BIGINT        NOT NULL REFERENCES products(id),
    branch_id     BIGINT        NOT NULL REFERENCES branches(id),
    current_stock NUMERIC(15,3) NOT NULL DEFAULT 0,
    min_stock     NUMERIC(15,3) NOT NULL DEFAULT 0,
    max_stock     NUMERIC(15,3) NOT NULL DEFAULT 0,
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_inventory_product_branch UNIQUE (product_id, branch_id),
    CONSTRAINT chk_stock_non_negative     CHECK (current_stock >= 0),
    CONSTRAINT chk_min_max_stock          CHECK (min_stock <= max_stock)
);

CREATE INDEX idx_inventory_product ON inventory(product_id);
CREATE INDEX idx_inventory_branch  ON inventory(branch_id);
CREATE INDEX idx_inventory_low     ON inventory(branch_id) WHERE current_stock <= min_stock;

-- Transfer requests
-- status values: PENDING, APPROVED, IN_TRANSIT, RECEIVED, CANCELLED
CREATE TABLE transfer_requests (
    id                    BIGSERIAL PRIMARY KEY,
    origin_branch_id      BIGINT        NOT NULL REFERENCES branches(id),
    destination_branch_id BIGINT        NOT NULL REFERENCES branches(id),
    product_id            BIGINT        NOT NULL REFERENCES products(id),
    quantity              NUMERIC(15,3) NOT NULL,
    status                VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    requested_by_id       BIGINT        NOT NULL REFERENCES users(id),
    approved_by_id        BIGINT        REFERENCES users(id),
    notes                 TEXT,
    request_date          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    approval_date         TIMESTAMPTZ,
    ship_date             TIMESTAMPTZ,
    reception_date        TIMESTAMPTZ,
    CONSTRAINT chk_transfer_different_branches CHECK (origin_branch_id <> destination_branch_id),
    CONSTRAINT chk_transfer_quantity_positive  CHECK (quantity > 0)
);

CREATE INDEX idx_transfers_status       ON transfer_requests(status);
CREATE INDEX idx_transfers_origin       ON transfer_requests(origin_branch_id);
CREATE INDEX idx_transfers_destination  ON transfer_requests(destination_branch_id);
CREATE INDEX idx_transfers_product      ON transfer_requests(product_id);
CREATE INDEX idx_transfers_requested_by ON transfer_requests(requested_by_id);

-- Inventory movements
-- type values:   INBOUND, OUTBOUND
-- reason values: PURCHASE, RETURN_INBOUND, POSITIVE_ADJUSTMENT, TRANSFER_INBOUND,
--                SALE, LOSS, NEGATIVE_ADJUSTMENT, TRANSFER_OUTBOUND
CREATE TABLE inventory_movements (
    id                  BIGSERIAL PRIMARY KEY,
    type                VARCHAR(50)   NOT NULL,
    reason              VARCHAR(50)   NOT NULL,
    quantity            NUMERIC(15,3) NOT NULL,
    stock_before        NUMERIC(15,3) NOT NULL,
    stock_after         NUMERIC(15,3) NOT NULL,
    product_id          BIGINT        NOT NULL REFERENCES products(id),
    branch_id           BIGINT        NOT NULL REFERENCES branches(id),
    user_id             BIGINT        NOT NULL REFERENCES users(id),
    transfer_request_id BIGINT        REFERENCES transfer_requests(id),
    observations        TEXT,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_movement_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_movements_product     ON inventory_movements(product_id);
CREATE INDEX idx_movements_branch      ON inventory_movements(branch_id);
CREATE INDEX idx_movements_user        ON inventory_movements(user_id);
CREATE INDEX idx_movements_type        ON inventory_movements(type);
CREATE INDEX idx_movements_reason      ON inventory_movements(reason);
CREATE INDEX idx_movements_date        ON inventory_movements(created_at DESC);
CREATE INDEX idx_movements_transfer    ON inventory_movements(transfer_request_id);

-- Audit log
CREATE TABLE audit_logs (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT       REFERENCES users(id),
    user_email     VARCHAR(150),
    action         VARCHAR(100) NOT NULL,
    entity_name    VARCHAR(100) NOT NULL,
    entity_id      VARCHAR(100),
    old_values     TEXT,
    new_values     TEXT,
    ip_address     VARCHAR(50),
    user_agent     VARCHAR(500),
    status         VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
    error_message  TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_user   ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_date   ON audit_logs(created_at DESC);
