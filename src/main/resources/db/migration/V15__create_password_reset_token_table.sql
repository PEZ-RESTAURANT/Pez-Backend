-- Alter User table to add password_changed_at column
ALTER TABLE user ADD COLUMN password_changed_at TIMESTAMP(6) NULL;

-- Create Password Reset Token table
CREATE TABLE password_reset_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
