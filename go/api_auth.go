package main

import (
	"database/sql"
	"dev.null/m00shm00sh/httpd/internal/database"
	"fmt"
	"net/http"
	"time"
)

func (ac *apiConf) loginUser() http.Handler {
	return http.HandlerFunc(ac.doLoginUser)
}
func (ac *apiConf) refresh() http.Handler {
	return chain(
		http.HandlerFunc(ac.doRefresh),
		authBearer,
	)
}
func (ac *apiConf) revoke() http.Handler {
	return chain(
		http.HandlerFunc(ac.doRevoke),
		authBearer,
	)
}

type userRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}
type userResponse struct {
	ID        string    `json:"id"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
	Email     string    `json:"email"`
	Red       bool      `json:"is_chirpy_red"`
}

func dbUserToUserResponse(row *database.User) userResponse {
	updated := time.Time{}
	if row.UpdatedAt.Valid {
		updated = row.UpdatedAt.Time
	}
	return userResponse{
		row.ID.(string),
		row.CreatedAt,
		updated,
		row.Email,
		row.IsChirpyRed,
	}
}

type refreshResponse struct {
	Token string `json:"token"`
}

func (ac *apiConf) doLoginUser(w http.ResponseWriter, r *http.Request) {
	var req userRequest
	if err := readJson(r, &req); err != nil {
		dataError(fmt.Sprintf("Decoding json: %s", err.Error()), w, 400)
		return
	}
	uRow, err := ac.db.GetUserByEmail(r.Context(), req.Email)
	if err != nil {
		w.WriteHeader(401)
		return
	}
	if !ac.passwordEncoder.Check(uRow.Pass, req.Password) {
		w.WriteHeader(401)
		return
	}
	jsonp, err := ac.addNewTokensToResponse(r.Context(), dbUserToUserResponse(&uRow))
	if err != nil {
		dataError(err.Error(), w, 500)
		return
	}
	writeJson(*jsonp, w, 200)
}

func (ac *apiConf) doRefresh(w http.ResponseWriter, r *http.Request) {
	_token := r.Context().Value("Auth-Bearer")
	if _token == nil {
		panic("expected auth middleware to be called in chain")
	}
	token := _token.(string)

	now := time.Now().UTC()

	_uid, err := ac.db.GetUserByRefresh(r.Context(), database.GetUserByRefreshParams{
		token,
		now.Add(time.Hour),
	})
	if err != nil {
		w.WriteHeader(401)
		w.Write([]byte("expired or revoked refresh"))
		return
	}
	uid := _uid.(string)

	jwt, err := ac.makeJwt(uid, time.Hour)
	writeJson(refreshResponse{jwt}, w, 200)
}

func (ac *apiConf) doRevoke(w http.ResponseWriter, r *http.Request) {
	_token := r.Context().Value("Auth-Bearer")
	if _token == nil {
		panic("expected auth middleware to be called in chain")
	}
	token := _token.(string)

	now := time.Now().UTC()

	err := ac.db.RevokeToken(r.Context(), database.RevokeTokenParams{
		sql.NullTime{now, true},
		token,
	})
	if err != nil {
		w.WriteHeader(400)
		return
	}
	w.WriteHeader(204)
}
