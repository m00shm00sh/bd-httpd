-- Postgres

CREATE TABLE users (
	id UUID PRIMARY KEY DEFAULT gen_random_UUID() NOT NULL,
	created_at TIMESTAMP DEFAULT now() NOT NULL,
	updated_at TIMESTAMP DEFAULT now() NOT NULL,
	email TEXT NOT NULL UNIQUE,
	pass TEXT DEFAULT '' NOT NULL,
	is_chirpy_red INTEGER DEFAULT 0 NOT NULL
);

CREATE TABLE chirps (
	id UUID PRIMARY KEY NOT NULL DEFAULT gen_random_UUID(),
	created_at TIMESTAMP NOT NULL DEFAULT now(),
	updated_at TIMESTAMP NOT NULL DEFAULT now(),
	body text NOT NULL,
	user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
	token CHAR(64) PRIMARY KEY NOT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT now(),
	updated_at TIMESTAMP NOT NULL DEFAULT now(),
	user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	expires_at TIMESTAMP NOT NULL,
	revoked_at TIMESTAMP
);

CREATE FUNCTION set_timestamp() RETURNS trigger
	LANGUAGE plpgsql AS
$$BEGIN
	NEW.updated_at := now();
	RETURN NEW;
END;$$;

CREATE TRIGGER users_set_timestamp BEFORE UPDATE ON users
	FOR EACH ROW EXECUTE PROCEDURE set_timestamp();
CREATE TRIGGER chirps_set_timestamp BEFORE UPDATE ON chirps
	FOR EACH ROW EXECUTE PROCEDURE set_timestamp();
CREATE TRIGGER refresh_tokens_set_timestamp BEFORE UPDATE ON refresh_tokens
	FOR EACH ROW EXECUTE PROCEDURE set_timestamp();

