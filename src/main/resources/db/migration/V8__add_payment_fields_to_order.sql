ALTER TABLE tb_order
    ADD COLUMN IF NOT EXISTS payment_id     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_order_payment_id ON tb_order (payment_id);
