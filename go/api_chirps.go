package main

import (
	"dev.null/m00shm00sh/httpd/internal/database"
	"fmt"
	"github.com/google/uuid"
	"net/http"
	"sort"
	"time"
)

func (ac *apiConf) postChirp() http.Handler {
	return chain(
		http.HandlerFunc(ac.doPostChirp),
		authBearer,
		ac.authJwt,
	)
}
func (ac *apiConf) getAllChirps() http.Handler {
	return http.HandlerFunc(ac.doGetAllChirps)
}
func (ac *apiConf) getOneChirp() http.Handler {
	return http.HandlerFunc(ac.doGetOneChirp)
}
func (ac *apiConf) deleteChirp() http.Handler {
	return chain(
		http.HandlerFunc(ac.doDeleteOneChirp),
		authBearer,
		ac.authJwt,
	)
}

type chirpRequest struct {
	Body string `json:"body"`
}

type chirpResponse struct {
	ID        string    `json:"id"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
	Body      string    `json:"body"`
	UserId    string    `json:"user_id"`
}

func dbChirpToChirpResponse(row *database.Chirp) chirpResponse {
	updated := time.Time{}
	if row.UpdatedAt.Valid {
		updated = row.UpdatedAt.Time
	}
	return chirpResponse{
		row.ID.(string),
		row.CreatedAt,
		updated,
		row.Body,
		row.UserID.(string),
	}
}

func (ac *apiConf) doPostChirp(w http.ResponseWriter, r *http.Request) {
	_uid := r.Context().Value("Auth-User")
	if _uid == nil {
		panic("expected auth middleware to be called in chain")
	}
	uid := _uid.(string)

	var req chirpRequest
	if err := readJson(r, &req); err != nil {
		dataError(fmt.Sprintf("Decoding json: %s", err.Error()), w, 400)
		return
	}

	if len(req.Body) > 140 {
		dataError(fmt.Sprintf("Chirp is too long"), w, 400)
		return
	}

	cRow, err := ac.db.CreateChirp(r.Context(), database.CreateChirpParams{
		uuid.New(),
		req.Body,
		uid,
	})
	if err != nil {
		dataError(fmt.Sprintf("DB: %s", err.Error()), w, 500)
		return
	}

	writeJson(dbChirpToChirpResponse(&cRow), w, 201)
}

func (ac *apiConf) doGetAllChirps(w http.ResponseWriter, r *http.Request) {
	aid := r.URL.Query()["author_id"]
	sortQ := r.URL.Query()["sort"]
	if sortQ == nil {
		sortQ = []string{"asc"}
	}
	var cRows []database.Chirp
	var err error
	if aid != nil {
		cRows, err = ac.db.GetChirpsForUser(r.Context(), aid[0])
	} else {
		cRows, err = ac.db.GetAllChirps(r.Context())
	}
	if err != nil {
		dataError(fmt.Sprintf("DB: %s", err.Error()), w, 404)
		return
	}
	if len(sortQ) == 1 && sortQ[0] == "desc" {
		sort.Slice(cRows, func(i, j int) bool { return cRows[i].CreatedAt.After(cRows[j].CreatedAt) })
	} else {
		sort.Slice(cRows, func(i, j int) bool { return cRows[i].CreatedAt.Before(cRows[j].CreatedAt) })
	}
	toRet := make([]chirpResponse, len(cRows))
	for i, cRow := range cRows {
		toRet[i] = dbChirpToChirpResponse(&cRow)
	}
	writeJson(toRet, w, 200)
}

func (ac *apiConf) doGetOneChirp(w http.ResponseWriter, r *http.Request) {
	cid := r.PathValue("cid")
	cRow, err := ac.db.GetChirp(r.Context(), cid)
	if err != nil {
		dataError(fmt.Sprintf("DB: %s", err.Error()), w, 404)
		return
	}
	writeJson(dbChirpToChirpResponse(&cRow), w, 200)
}

func (ac *apiConf) doDeleteOneChirp(w http.ResponseWriter, r *http.Request) {
	_uid := r.Context().Value("Auth-User")
	if _uid == nil {
		panic("expected auth middleware to be called in chain")
	}
	uid := _uid.(string)
	cid := r.PathValue("cid")
	cRow, err := ac.db.GetChirp(r.Context(), cid)
	if err != nil {
		w.WriteHeader(404)
		return
	}
	if cRow.UserID.(string) != uid {
		w.WriteHeader(403)
		return
	}
	err = ac.db.DeleteChirp(r.Context(), cid)
	if err != nil {
		dataError(fmt.Sprintf("DB: %s", err.Error()), w, 500)
		return
	}
	w.WriteHeader(204)
}
