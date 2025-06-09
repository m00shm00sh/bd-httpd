
-- NOTE: sqlite update triggers are a bit iffy - should set updated to NULL and let trigger fill in value
--       instead of keeping track of modified rowids

-- +goose Up
CREATE TABLE Users (
	id UUID PRIMARY KEY,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	email TEXT NOT NULL UNIQUE
);

-- +goose StatementBegin
CREATE TRIGGER users_set_mtime AFTER UPDATE ON Users
FOR EACH ROW WHEN NEW.updated_at IS NULL
BEGIN
    UPDATE Users SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
END;
-- +goose StatementEnd

-- +goose Down
DROP TRIGGER users_set_mtime;
DROP TABLE Users;
