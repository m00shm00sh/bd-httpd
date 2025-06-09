package main

import (
	"golang.org/x/crypto/bcrypt"
)

type passwordEncoder interface {
	Encode(string) (string, error)
	Check(string, string) bool
}

type bcryptEncoder struct{}

func (_ bcryptEncoder) Encode(password string) (string, error) {
	uPass, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	return string(uPass), err
}
func (_ bcryptEncoder) Check(encodedPassword string, checkPassword string) bool {
	return bcrypt.CompareHashAndPassword([]byte(encodedPassword), []byte(checkPassword)) == nil
}
