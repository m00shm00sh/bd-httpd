package main

import (
	"dev.null/m00shm00sh/httpd/internal/database"
	"fmt"
	"github.com/google/uuid"
	"net/http"
)

func (ac *apiConf) registerUser() http.Handler {
	return http.HandlerFunc(ac.doRegisterUser)
}
func (ac *apiConf) modifyUser() http.Handler {
	return chain(
		http.HandlerFunc(ac.doModifyUser),
		authBearer,
		ac.authJwt,
	)
}

func (ac *apiConf) doRegisterUser(w http.ResponseWriter, r *http.Request) {
	var req userRequest
	if err := readJson(r, &req); err != nil {
		dataError(fmt.Sprintf("Decoding json: %s", err.Error()), w, 400)
		return
	}

	uPass, err := ac.passwordEncoder.Encode(req.Password)
	if err != nil {
		dataError(fmt.Sprintf("Hashing: %s", err.Error()), w, 500)
		return
	}

	uRow, err := ac.db.CreateUser(r.Context(), database.CreateUserParams{
		uuid.New(),
		uPass,
		req.Email,
	})
	if err != nil {
		dataError(fmt.Sprintf("DB: %s", err.Error()), w, 500)
		return
	}
	jsonp, err := ac.addNewTokensToResponse(r.Context(), dbUserToUserResponse(&uRow))
	if err != nil {
		dataError(err.Error(), w, 500)
		return
	}
	writeJson(*jsonp, w, 201)
}

func (ac *apiConf) doModifyUser(w http.ResponseWriter, r *http.Request) {
	_uid := r.Context().Value("Auth-User")
	if _uid == nil {
		panic("expected auth middleware to be called in chain")
	}
	uid := _uid.(string)

	var req userRequest
	if err := readJson(r, &req); err != nil {
		dataError(fmt.Sprintf("Decoding json: %s", err.Error()), w, 400)
		return
	}

	uPass, err := ac.passwordEncoder.Encode(req.Password)
	if err != nil {
		dataError(fmt.Sprintf("Hashing: %s", err.Error()), w, 500)
		return
	}
	uRow, err := ac.db.UpdateUser(r.Context(), database.UpdateUserParams{
		req.Email,
		uPass,
		uid,
	})
	if err != nil {
		dataError(fmt.Sprintf("DB: %s", err.Error()), w, 500)
		return
	}
	jsonp, err := ac.addNewTokensToResponse(r.Context(), dbUserToUserResponse(&uRow))
	if err != nil {
		dataError(err.Error(), w, 500)
		return
	}
	writeJson(*jsonp, w, 200)
}
