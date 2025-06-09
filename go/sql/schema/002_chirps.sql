-- +goose Up
CREATE TABLE Chirps (
	id UUID PRIMARY KEY,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	body TEXT NOT NULL,
	user_id UUID REFERENCES Users(id) ON DELETE CASCADE
);

-- +goose StatementBegin
CREATE TRIGGER chirps_set_mtime AFTER UPDATE ON Chirps
FOR EACH ROW WHEN NEW.updated_at IS NULL
BEGIN
    UPDATE Chirps SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
END;
-- +goose StatementEnd

-- +goose Down
DROP TRIGGER chirps_set_mtime;
DROP TABLE Chirps;
