package main

import (
	"context"
	"crypto/rand"
	"dev.null/m00shm00sh/httpd/internal/database"
	"encoding/hex"
	"fmt"
	"github.com/golang-jwt/jwt/v5"
	"time"
)

type userResponseWithTokens struct {
	userResponse
	Token        string `json:"token"`
	RefreshToken string `json:"refresh_token"`
}

func (ac *apiConf) addNewTokensToResponse(ctx context.Context, u userResponse) (*userResponseWithTokens, error) {
	jwt, err := ac.makeJwt(u.ID, time.Hour)
	if err != nil {
		return nil, fmt.Errorf("JWT: %w", err)
	}
	refreshToken, err := ac.makeRefreshToken(ctx, u.ID)
	if err != nil {
		return nil, fmt.Errorf("DB: %w", err)
	}
	return &userResponseWithTokens{
		u,
		jwt,
		refreshToken,
	}, nil
}

func (ac *apiConf) makeJwt(uuidStr string, exp time.Duration) (string, error) {
	now := time.Now().UTC()
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, jwt.RegisteredClaims{
		Issuer:    ac.jwtIssuer,
		Subject:   uuidStr,
		IssuedAt:  &jwt.NumericDate{now},
		ExpiresAt: &jwt.NumericDate{now.Add(exp)},
	})
	return token.SignedString(ac.jwtSecret)
}

func (ac *apiConf) makeRefreshToken(ctx context.Context, uuidStr string) (string, error) {
	k := make([]byte, 32)
	rand.Read(k)
	token := hex.EncodeToString(k)
	now := time.Now().UTC()
	err := ac.db.CreateRefresh(ctx, database.CreateRefreshParams{
		token,
		uuidStr,
		now.Add(time.Duration(60*24) * time.Hour),
	})
	if err != nil {
		return "", err
	}
	return token, nil
}
