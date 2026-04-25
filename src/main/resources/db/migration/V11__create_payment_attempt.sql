CREATE TABLE IF NOT EXISTS tb_payment_attempt (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT        NOT NULL REFERENCES tb_order(id),
    provider            VARCHAR(50)   NOT NULL,
    provider_order_id   VARCHAR(100),
    provider_payment_id VARCHAR(100),
    status              VARCHAR(30)   NOT NULL,
    status_detail       VARCHAR(100),
    method              VARCHAR(30),
    amount              NUMERIC(15,2),
    currency            VARCHAR(3)    NOT NULL DEFAULT 'BRL',
    idempotency_key     VARCHAR(100),
    attempt_number      INTEGER       NOT NULL DEFAULT 1,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_attempt_idempotency_key
    ON tb_payment_attempt (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_payment_attempt_provider_order_id
    ON tb_payment_attempt (provider_order_id);

CREATE INDEX IF NOT EXISTS idx_payment_attempt_order_id
    ON tb_payment_attempt (order_id);

ALTER TABLE tb_order
    DROP COLUMN IF EXISTS payment_idempotency_key,
    DROP COLUMN IF EXISTS payment_attempt_counter;
