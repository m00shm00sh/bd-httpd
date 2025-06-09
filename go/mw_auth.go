package main

import (
	"context"
	"github.com/golang-jwt/jwt/v5"
	"net/http"
	"strings"
)

// authentication middleware:
// authApiKey injects Auth-ApiKey into context if an 'Authorization: ApiKey <...>' header exists
// authBearer inject Auth-Bearer into context if an 'Authorization: Bearer <..." header exists
// (*apiConf).authJwt expects Auth-Bearer in context and injects an Auth-User if the jwt decode is successful
// (*apiConf).authPolka expects Auth-ApiKeyr in context and injects an Auth-Polka if the apikey is valid

func authApikey(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		vs := r.Header.Values("Authorization")
		var apikey string
		for _, v := range vs {
			fs := strings.Fields(v)
			if len(fs) == 2 && fs[0] == "ApiKey" {
				apikey = fs[1]
				break
			}
		}
		if apikey == "" {
			w.WriteHeader(401)
			w.Write([]byte("no Authorization: ApiKey"))
			return
		}

		ctx := context.WithValue(r.Context(), "Auth-ApiKey", apikey)
		newRequest := r.WithContext(ctx)
		next.ServeHTTP(w, newRequest)
	})
}

func authBearer(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		vs := r.Header.Values("Authorization")
		var bearer string
		for _, v := range vs {
			fs := strings.Fields(v)
			if len(fs) == 2 && fs[0] == "Bearer" {
				bearer = fs[1]
				break
			}
		}
		if bearer == "" {
			w.WriteHeader(401)
			w.Write([]byte("no Authorization: Bearer"))
			return
		}

		ctx := context.WithValue(r.Context(), "Auth-Bearer", bearer)
		newRequest := r.WithContext(ctx)
		next.ServeHTTP(w, newRequest)
	})
}

func (ac *apiConf) authJwt(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		var err error
		var token *jwt.Token
		var idStr string
		var iss string
		tokenStr_ := r.Context().Value("Auth-Bearer")
		if tokenStr_ == nil {
			panic("expected Auth-Bearer")
		}
		tokenStr := tokenStr_.(string)
		token, err = jwt.ParseWithClaims(
			tokenStr, &jwt.RegisteredClaims{},
			func(_ *jwt.Token) (any, error) { return ac.jwtSecret, nil },
		)
		if err != nil {
			w.WriteHeader(401)
			w.Write([]byte("bad token"))
			return
		}
		idStr, err = token.Claims.GetSubject()
		if err != nil || idStr == "" {
			w.WriteHeader(401)
			w.Write([]byte("invalid user"))
			return
		}
		iss, err = token.Claims.GetIssuer()
		if iss != ac.jwtIssuer {
			w.WriteHeader(401)
			w.Write([]byte("invalid issuer"))
			return
		}
		ctx := context.WithValue(r.Context(), "Auth-User", idStr)
		newRequest := r.WithContext(ctx)
		next.ServeHTTP(w, newRequest)
	})
}

func (ac *apiConf) authPolka(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		token := r.Context().Value("Auth-ApiKey")
		if token == nil {
			panic("expected Auth-ApiKey")
		}
		if token.(string) != ac.polka {
			w.WriteHeader(401)
			w.Write([]byte("invalid api-key"))
			return
		}
		ctx := context.WithValue(r.Context(), "Auth-Polka", 1)
		newRequest := r.WithContext(ctx)
		next.ServeHTTP(w, newRequest)
	})
}
