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

	mux := ac.configureRoutes()
	s := http.Server{Handler: mux, Addr: ":8080"}
	s.ListenAndServe()
}
