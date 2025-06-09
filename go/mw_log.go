package main

import (
	"fmt"
	"log/slog"
	"net/http"
)

func (ac *apiConf) log(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		rp := fmt.Sprintf("%p", r)
		ac.logger.Info("request", "@", rp, "from", r.RemoteAddr, "url", r.URL, "method", r.Method)
		loggingW := httpWriterProxy{
			logger: ac.logger.With("@", rp),
			w:      w,
		}
		next.ServeHTTP(&loggingW, r)
	})
}

func (ac *apiConf) withLog(next http.Handler) http.Handler {
	return chain(next, ac.log)
}

// wrap http.ResponseWriter to intercept WriteHeader(int)
type httpWriterProxy struct {
	logger    *slog.Logger
	didHeader bool
	w         http.ResponseWriter
}

func (p *httpWriterProxy) Header() http.Header {
	return p.w.Header()
}
func (p *httpWriterProxy) Write(b []byte) (int, error) {
	if !p.didHeader {
		p.logger.Info("w", "result", 200)
		p.didHeader = true
	}
	return p.w.Write(b)
}
func (p *httpWriterProxy) WriteHeader(status int) {
	p.didHeader = true
	p.logger.Info("wh", "result", status)
	p.w.WriteHeader(status)
}
