CREATE UNIQUE INDEX IF NOT EXISTS idx_payment_attempt_active_order
    ON tb_payment_attempt (order_id)
    WHERE status IN ('CREATED', 'PENDING', 'AUTHORIZED', 'AWAITING_CHALLENGE', 'IN_REVIEW');
