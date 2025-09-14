CREATE TABLE Chirps (
	id UUID PRIMARY KEY,
	created_at TIMESTAMP NOT NULL DEFAULT (datetime('subsec')),
	updated_at TIMESTAMP DEFAULT (datetime('subsec')),
	body TEXT NOT NULL,
	user_id UUID REFERENCES Users(id) ON DELETE CASCADE
);

CREATE TRIGGER chirps_set_mtime AFTER UPDATE ON chirps
FOR EACH ROW WHEN NEW.updated_at IS NULL
BEGIN
    UPDATE Chirps SET updated_at = datetime('subsec') WHERE updated_at IS NULL;
END;

