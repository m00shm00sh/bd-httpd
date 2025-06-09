-- name: CreateUser :one
INSERT INTO Users (id, pass, email) VALUES (?, ?, ?) RETURNING *;

-- name: GetUserByEmail :one
SELECT * FROM Users WHERE email = ?;

-- name: UpdateUser :one
UPDATE Users SET email = ?, pass = ?, updated_at = NULL WHERE id = ? RETURNING *;

-- name: UpgradeUser :one
UPDATE Users SET is_chirpy_red = TRUE WHERE id = ? RETURNING *;

-- name: DeleteUsers :exec
DELETE FROM Users;

