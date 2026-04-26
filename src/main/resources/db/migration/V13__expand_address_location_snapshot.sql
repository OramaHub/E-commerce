ALTER TABLE tb_address
    ADD COLUMN recipient_name VARCHAR(120),
    ADD COLUMN recipient_phone VARCHAR(20),
    ADD COLUMN reference VARCHAR(255),
    ADD COLUMN city_name VARCHAR(150),
    ADD COLUMN state_uf VARCHAR(10),
    ADD COLUMN country_code VARCHAR(10) DEFAULT 'BR',
    ADD COLUMN ibge_code VARCHAR(7),
    ADD COLUMN latitude DECIMAL(10, 7),
    ADD COLUMN longitude DECIMAL(10, 7);

CREATE TABLE tb_order_shipping_address (
    order_id BIGINT PRIMARY KEY REFERENCES tb_order(id) ON DELETE CASCADE,
    recipient_name VARCHAR(120),
    recipient_phone VARCHAR(20),
    zip_code VARCHAR(20) NOT NULL,
    street VARCHAR(255) NOT NULL,
    number VARCHAR(20) NOT NULL,
    complement VARCHAR(100),
    district VARCHAR(100) NOT NULL,
    reference VARCHAR(255),
    city_name VARCHAR(150) NOT NULL,
    state_uf VARCHAR(10) NOT NULL,
    country_code VARCHAR(10) NOT NULL DEFAULT 'BR',
    ibge_code VARCHAR(7),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    original_address_id BIGINT,
    snapshot_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_shipping_address_zip_code
    ON tb_order_shipping_address(zip_code);

CREATE INDEX idx_order_shipping_address_city_state
    ON tb_order_shipping_address(city_name, state_uf);
