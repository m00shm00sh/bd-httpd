package main

import (
	"net/http"
)

func (ac *apiConf) incHits(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		ac.hits.Add(1)
		next.ServeHTTP(w, r)
	})
}
