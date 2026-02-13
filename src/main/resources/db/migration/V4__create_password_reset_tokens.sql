CREATE TABLE tb_password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    client_id BIGINT NOT NULL REFERENCES tb_client(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_tokens_token ON tb_password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_client_id ON tb_password_reset_tokens(client_id);
