CREATE TABLE tb_country (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    sgl VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_state (
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    sgl VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_state_country FOREIGN KEY (country_id) REFERENCES tb_country(id)
);

CREATE TABLE tb_city (
    id BIGSERIAL PRIMARY KEY,
    state_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    ibge_code VARCHAR(7) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_city_state FOREIGN KEY (state_id) REFERENCES tb_state(id)
);

CREATE TABLE tb_client (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(180) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    cpf VARCHAR(20) UNIQUE,
    phone VARCHAR(30),
    active BOOLEAN,
    created_at TIMESTAMP,
    role VARCHAR(20)
);

CREATE TABLE tb_address (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    street VARCHAR(255) NOT NULL,
    number VARCHAR(20),
    default_address BOOLEAN NOT NULL DEFAULT FALSE,
    complement VARCHAR(100),
    district VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_address_client FOREIGN KEY (client_id) REFERENCES tb_client(id),
    CONSTRAINT fk_address_city FOREIGN KEY (city_id) REFERENCES tb_city(id)
);

CREATE TABLE tb_product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(180) NOT NULL,
    description VARCHAR(1000),
    price NUMERIC(15, 2) NOT NULL,
    stock INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tb_product_image (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES tb_product(id)
);

CREATE TABLE tb_cart (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    session_id VARCHAR(120),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_client FOREIGN KEY (client_id) REFERENCES tb_client(id)
);

CREATE TABLE tb_cart_item (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES tb_cart(id),
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES tb_product(id)
);

CREATE TABLE tb_order (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL UNIQUE,
    client_id BIGINT NOT NULL,
    delivery_adress_id BIGINT NOT NULL,
    order_number VARCHAR(50) UNIQUE,
    order_date TIMESTAMP,
    status_order VARCHAR(20) NOT NULL,
    subtotal NUMERIC(15, 2),
    discount NUMERIC(15, 2),
    total NUMERIC(15, 2),
    CONSTRAINT fk_order_cart FOREIGN KEY (cart_id) REFERENCES tb_cart(id),
    CONSTRAINT fk_order_client FOREIGN KEY (client_id) REFERENCES tb_client(id),
    CONSTRAINT fk_order_address FOREIGN KEY (delivery_adress_id) REFERENCES tb_address(id)
);

CREATE TABLE tb_order_item (
    id BIGSERIAL PRIMARY KEY,
    order_cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_cart_id) REFERENCES tb_order(id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES tb_product(id)
);

CREATE INDEX idx_client_email ON tb_client(email);
CREATE INDEX idx_client_cpf ON tb_client(cpf);
CREATE INDEX idx_product_name ON tb_product(name);
CREATE INDEX idx_product_active ON tb_product(active);
CREATE INDEX idx_order_number ON tb_order(order_number);
CREATE INDEX idx_order_client ON tb_order(client_id);
CREATE INDEX idx_order_status ON tb_order(status_order);
CREATE INDEX idx_cart_client ON tb_cart(client_id);
CREATE INDEX idx_address_client ON tb_address(client_id);
CREATE INDEX idx_city_state ON tb_city(state_id);
CREATE INDEX idx_city_ibge ON tb_city(ibge_code);
CREATE INDEX idx_state_country ON tb_state(country_id);
