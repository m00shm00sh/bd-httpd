package main

import (
	"net/http"
)

func chain(endpoint http.Handler, middleware ...func(http.Handler) http.Handler) http.Handler {
	if len(middleware) < 1 {
		return endpoint
	}
	chain := endpoint
	for i := len(middleware) - 1; i >= 0; i-- {
		chain = middleware[i](chain)
	}
	return chain
}
