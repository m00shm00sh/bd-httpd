CREATE TABLE Refresh_tokens (
	token VARCHAR PRIMARY KEY,
	created_at TIMESTAMP NOT NULL DEFAULT (datetime('subsec')),
	updated_at TIMESTAMP DEFAULT (datetime('subsec')),
	user_id UUID REFERENCES Users(id) ON DELETE CASCADE,
	expires_at TIMESTAMP NOT NULL,
	revoked_at TIMESTAMP
);

CREATE TRIGGER tokens_set_mtime AFTER UPDATE ON Refresh_tokens
FOR EACH ROW WHEN NEW.updated_at IS NULL
BEGIN
    UPDATE Refresh_tokens SET updated_at = datetime('subsec') WHERE updated_at IS NULL;
END;
