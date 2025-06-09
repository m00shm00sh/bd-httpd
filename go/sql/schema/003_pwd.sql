-- +goose Up
ALTER TABLE Users ADD COLUMN pass TEXT NOT NULL DEFAULT '';

UPDATE Users SET pass='';

-- +goose Down
ALTER TABLE Users DROP COLUMN pass;
