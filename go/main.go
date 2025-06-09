package main

import (
	"bytes"
	"database/sql"
	"dev.null/m00shm00sh/httpd/internal/database"
	"github.com/joho/godotenv"
	_ "github.com/mattn/go-sqlite3"
	"log/slog"
	"net/http"
	"os"
	"strings"
	"sync/atomic"
)

func main() {
	godotenv.Load()
	dbUrl := os.Getenv("DB_URL")
	isDev := os.Getenv("PLATFORM") == "dev"
	jwtSecret := []byte(os.Getenv("JWT_SECRET"))
	polka := os.Getenv("POLKA_KEY")

	if strings.Contains(polka, "insert your own") {
		panic("unset POLKA_KEY")
	}
	if bytes.Contains(jwtSecret, []byte("insert your own")) {
		panic("unset JWT_SECRET")
	}

	dbConn, err := sql.Open("sqlite3", dbUrl)
	if err != nil {
		panic(err)
	}
	dbQueries := database.New(dbConn)

	sm := http.NewServeMux()
	ac := apiConf{
		hits:            atomic.Int32{},
		db:              dbQueries,
		isDev:           isDev,
		logger:          slog.Default(),
		polka:           polka,
		jwtIssuer:       "chirpy",
		jwtSecret:       jwtSecret,
		passwordEncoder: bcryptEncoder{},
	}
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
	s := http.Server{Handler: sm, Addr: ":8080"}
	s.ListenAndServe()
}
