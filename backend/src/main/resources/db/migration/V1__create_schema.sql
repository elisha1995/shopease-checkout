-- V1__create_schema.sql

-- ─── Enum Types ────────────────────────────────────────
CREATE TYPE membership_tier AS ENUM ('STANDARD', 'GOLD');
CREATE TYPE notification_channel AS ENUM ('EMAIL', 'SMS');

-- ─── Users ─────────────────────────────────────────────
CREATE TABLE users (
    id                       UUID PRIMARY KEY DEFAULT uuidv7(),
    email                    VARCHAR(255) NOT NULL UNIQUE,
    password_hash            VARCHAR(255) NOT NULL,
    full_name                VARCHAR(150) NOT NULL,
    phone                    VARCHAR(20),
    tier                     membership_tier NOT NULL DEFAULT 'STANDARD',
    notification_preferences notification_channel[] NOT NULL DEFAULT '{EMAIL}',
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),

);

CREATE INDEX idx_users_email ON users(email);

-- ─── Products ──────────────────────────────────────────
CREATE TABLE products (
    id          UUID PRIMARY KEY DEFAULT uuidv7(),
    sku         VARCHAR(50)    NOT NULL UNIQUE,
    name        VARCHAR(150)   NOT NULL,
    description TEXT,
    price       DECIMAL(10,2)  NOT NULL,
    image_url   VARCHAR(500),
    category    VARCHAR(50),
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_products_category ON products(category);

-- ─── Orders ────────────────────────────────────────────
CREATE TABLE orders (
    id              UUID          PRIMARY KEY DEFAULT uuidv7(),
    order_number    VARCHAR(20)   NOT NULL UNIQUE,
    user_id         UUID          NOT NULL REFERENCES users(id),
    subtotal        DECIMAL(10,2) NOT NULL,
    shipping_cost   DECIMAL(10,2) NOT NULL DEFAULT 0,
    total           DECIMAL(10,2) NOT NULL,
    currency        VARCHAR(3)    NOT NULL DEFAULT 'USD',
    payment_method  VARCHAR(20)   NOT NULL,
    transaction_id  VARCHAR(100),
    shipping_method VARCHAR(20)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'CONFIRMED',
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user ON orders(user_id);

-- ─── Order Items ───────────────────────────────────────
CREATE TABLE order_items (
    id           UUID PRIMARY KEY DEFAULT uuidv7(),
    order_id     UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id   UUID          REFERENCES products(id),
    product_name VARCHAR(150)  NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    quantity     INT           NOT NULL
);

CREATE INDEX idx_order_items_order ON order_items(order_id);

-- ─── Notification Logs ─────────────────────────────────
CREATE TABLE notification_logs (
    id         UUID PRIMARY KEY DEFAULT uuidv7(),
    order_id   UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    channel    notification_channel NOT NULL,
    success    BOOLEAN     NOT NULL,
    message    TEXT,
    attempts   INT         NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_logs_order ON notification_logs(order_id);
