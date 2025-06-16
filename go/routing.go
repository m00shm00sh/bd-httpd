package main

import (
	"net/http"
)

func (ac *apiConf) configureRoutes() *http.ServeMux {
	sm := http.NewServeMux()
	handle := func(pat string, h http.Handler) {
		sm.Handle(pat, ac.withLog(h))
	}
	handle("/app/", ac.serveStatic())
	handle("GET /api/healthz", ac.healthz())
	handle("POST /api/users", ac.registerUser())
	handle("PUT /api/users", ac.modifyUser())
	handle("POST /api/login", ac.loginUser())
	handle("POST /api/refresh", ac.refresh())
	handle("POST /api/revoke", ac.revoke())
	handle("POST /api/polka/webhooks", ac.polkaWebhook())
	handle("POST /api/chirps", ac.postChirp())
	handle("GET /api/chirps", ac.getAllChirps())
	handle("GET /api/chirps/{cid}", ac.getOneChirp())
	handle("DELETE /api/chirps/{cid}", ac.deleteChirp())
	handle("GET /admin/metrics", ac.metrics())
	handle("POST /admin/reset", ac.resetDb())
	return sm
}

