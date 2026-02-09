CREATE TABLE tb_revoked_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    revoked_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_revoked_tokens_token_hash ON tb_revoked_tokens(token_hash);
CREATE INDEX idx_revoked_tokens_expires_at ON tb_revoked_tokens(expires_at);
