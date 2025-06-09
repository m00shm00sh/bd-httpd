package main

import (
	"fmt"
	"net/http"
)

func (_ *apiConf) healthz() http.Handler {
	return http.HandlerFunc(doHealthz)
}
func (ac *apiConf) metrics() http.Handler {
	return http.HandlerFunc(ac.doMetrics)
}
func (ac *apiConf) resetDb() http.Handler {
	return http.HandlerFunc(ac.doResetDb)
}

func doHealthz(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	w.WriteHeader(200)
	w.Write([]byte("OK"))
}

func (ac *apiConf) doMetrics(w http.ResponseWriter, _ *http.Request) {
	hits := ac.hits.Load()
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.WriteHeader(200)
	msg := fmt.Sprintf("<html><body><h1>Welcome, Chirpy Admin</h1><p>Chirpy has been visited %d times!</p></body></html>", hits)
	w.Write([]byte(msg))
}

func (ac *apiConf) doResetDb(w http.ResponseWriter, r *http.Request) {
	if !ac.isDev {
		w.WriteHeader(403)
		return
	}
	ac.hits.Store(0)
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	err := ac.db.DeleteUsers(r.Context())
	if err != nil {
		dataError(fmt.Sprintf("DB: %s", err.Error()), w, 500)
		return
	}
	err = ac.db.DeleteChirps(r.Context())
	if err != nil {
		dataError(fmt.Sprintf("DB: %s", err.Error()), w, 500)
		return
	}
	w.WriteHeader(200)
	w.Write([]byte(""))
}
