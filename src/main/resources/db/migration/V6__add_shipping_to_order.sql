ALTER TABLE tb_order
    ADD COLUMN shipping_cost NUMERIC(15, 2) NOT NULL DEFAULT 0,
    ADD COLUMN zip_code      VARCHAR(9);
