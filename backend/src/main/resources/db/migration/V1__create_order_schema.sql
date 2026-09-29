CREATE TABLE pizza_order (
    id UUID NOT NULL PRIMARY KEY,
    pizza_type VARCHAR(255) NOT NULL,
    status VARCHAR(16) NOT NULL,
    order_code VARCHAR(40) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_pizza_order_code UNIQUE (order_code),
    CONSTRAINT ck_pizza_type_not_blank CHECK (LENGTH(TRIM(pizza_type)) > 0),
    CONSTRAINT ck_order_status CHECK (
        status IN ('PENDING', 'IN_PROGRESS', 'READY', 'COMPLETED')
    )
);

CREATE INDEX ix_pizza_order_status_created_at ON pizza_order (status, created_at);
CREATE INDEX ix_pizza_order_created_at ON pizza_order (created_at);

CREATE TABLE chef_station (
    id BIGINT NOT NULL PRIMARY KEY,
    CONSTRAINT ck_single_chef_station CHECK (id = 1)
);
