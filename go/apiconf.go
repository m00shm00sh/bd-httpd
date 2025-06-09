package main

import (
	"dev.null/m00shm00sh/httpd/internal/database"
	"log/slog"
	"sync/atomic"
)

type apiConf struct {
	hits      atomic.Int32
	db        *database.Queries
	isDev     bool
	jwtIssuer string
	jwtSecret []byte
	logger    *slog.Logger
	polka     string
	passwordEncoder
}
