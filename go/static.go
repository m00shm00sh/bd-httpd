package main

import (
	"net/http"
)

func (ac *apiConf) serveStatic() http.Handler {
	return chain(
		http.FileServer(http.Dir("static")),
		ac.incHits,
		func(h http.Handler) http.Handler { return http.StripPrefix("/app/", h) },
	)
}
