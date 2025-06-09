package main

import (
	"fmt"
	"net/http"
)

func (ac *apiConf) polkaWebhook() http.Handler {
	return chain(
		http.HandlerFunc(ac.doPolka),
		authApikey,
		ac.authPolka,
	)
}

type polkaRequest struct {
	Event string `json:"event"`
	Data  struct {
		Uid string `json:"user_id"`
	} `json:"data"`
}

func (ac *apiConf) doPolka(w http.ResponseWriter, r *http.Request) {
	_pk := r.Context().Value("Auth-Polka")
	if _pk == nil {
		panic("missing polka middleware")
	}
	if _pk.(int) != 1 {
		panic("bad polka value")
	}
	var req polkaRequest
	if err := readJson(r, &req); err != nil {
		dataError(fmt.Sprintf("Decoding json: %s", err.Error()), w, 400)
		return
	}
	if req.Event != "user.upgraded" {
		w.WriteHeader(204)
		return
	}
	if _, err := ac.db.UpgradeUser(r.Context(), req.Data.Uid); err != nil {
		w.WriteHeader(404)
		return
	}
	w.WriteHeader(204)
}
