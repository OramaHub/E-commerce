CREATE TABLE tb_media_library (
    id         BIGSERIAL     PRIMARY KEY,
    url        VARCHAR(1000) NOT NULL,
    filename   VARCHAR(255)  NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
