CREATE TABLE tb_custom_order (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    client_id BIGINT NOT NULL REFERENCES tb_client(id),
    cap_line VARCHAR(20) NOT NULL,
    cap_model VARCHAR(30) NOT NULL,
    cap_material VARCHAR(20) NOT NULL,
    laser_cut BOOLEAN NOT NULL DEFAULT FALSE,
    full_laser_cut BOOLEAN NOT NULL DEFAULT FALSE,
    strap_type VARCHAR(20) NOT NULL,
    color_front VARCHAR(7) NOT NULL,
    color_mesh VARCHAR(7),
    color_brim VARCHAR(7) NOT NULL,
    color_brim_lining VARCHAR(7),
    quantity INTEGER NOT NULL,
    logo_url VARCHAR(1000),
    preview_image_url VARCHAR(1000),
    layout_image_url VARCHAR(1000),
    observations VARCHAR(2000),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE tb_custom_order_logo_detail (
    id BIGSERIAL PRIMARY KEY,
    custom_order_id BIGINT NOT NULL REFERENCES tb_custom_order(id) ON DELETE CASCADE,
    position VARCHAR(25) NOT NULL,
    technique VARCHAR(25) NOT NULL
);

CREATE INDEX idx_custom_order_client ON tb_custom_order(client_id);
CREATE INDEX idx_custom_order_status ON tb_custom_order(status);
CREATE INDEX idx_custom_order_logo_detail_order ON tb_custom_order_logo_detail(custom_order_id);
