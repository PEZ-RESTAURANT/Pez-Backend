-- Create Blacklisted Tokens table for JWT revocation
CREATE TABLE blacklisted_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(700) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT uc_blacklisted_tokens_token UNIQUE (token)
);
