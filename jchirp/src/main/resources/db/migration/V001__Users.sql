CREATE TABLE Users (
	id UUID PRIMARY KEY,
	created_at TIMESTAMP NOT NULL DEFAULT (datetime('subsec')),
	updated_at TIMESTAMP DEFAULT (datetime('subsec')),
	email TEXT NOT NULL UNIQUE
);

CREATE TRIGGER users_set_mtime AFTER UPDATE ON users
FOR EACH ROW WHEN NEW.updated_at IS NULL
BEGIN
    UPDATE Users SET updated_at = datetime('subsec') WHERE updated_at IS NULL;
END;

ALTER TABLE Users ADD COLUMN pass TEXT NOT NULL DEFAULT '';

ALTER TABLE Users ADD COLUMN is_chirpy_red BOOLEAN NOT NULL DEFAULT FALSE;

