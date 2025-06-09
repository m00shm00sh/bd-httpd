-- name: CreateChirp :one
INSERT INTO Chirps (id, body, user_id)  VALUES (?, ?, ?) RETURNING *;

-- name: GetAllChirps :many
SELECT * FROM Chirps;

-- name: GetChirp :one
SELECT * FROM Chirps WHERE id = ?;

-- name: GetChirpsForUser :many
SELECT * FROM Chirps WHERE user_id = ? ORDER BY created_at;

-- name: DeleteChirp :exec
DELETE FROM Chirps WHERE id = ?;

-- name: DeleteChirps :exec
DELETE FROM Chirps;
