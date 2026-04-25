ALTER TABLE tb_order
    ADD COLUMN IF NOT EXISTS payment_idempotency_key VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_attempt_counter INTEGER NOT NULL DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS idx_order_payment_idempotency_key
    ON tb_order (payment_idempotency_key)
    WHERE payment_idempotency_key IS NOT NULL;
