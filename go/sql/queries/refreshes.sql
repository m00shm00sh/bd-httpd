-- name: CreateRefresh :exec
INSERT INTO Refresh_tokens (token, user_id, expires_at) VALUES (?, ?, ?);

-- name: GetUserByRefresh :one
SELECT user_id FROM Refresh_tokens WHERE token = ? AND expires_at > ? AND revoked_at IS NULL;

-- name: RevokeToken :exec
UPDATE Refresh_tokens SET revoked_at = ?, updated_at = NULL WHERE token = ?;


