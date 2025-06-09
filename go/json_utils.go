package main

import (
	"encoding/json"
	"fmt"
	"net/http"
)

func internalError(e error, w http.ResponseWriter) {
	fmt.Printf("Encoding json: %s\n", e)
	w.WriteHeader(500)
}
func dataError(errorMsg string, w http.ResponseWriter, rc int) {
	type errT struct {
		Error string `json:"error"`
	}
	data, err := json.Marshal(errT{Error: errorMsg})
	if err != nil {
		internalError(err, w)
		return
	}
	w.Header().Set("Content-type", "application/json")
	w.WriteHeader(rc)
	w.Write(data)
}

func readJson(r *http.Request, v any) error {
	decoder := json.NewDecoder(r.Body)
	return decoder.Decode(v)
}

func writeJson(v any, w http.ResponseWriter, rc int) error {
	ret, err := json.Marshal(v)
	if err != nil {
		internalError(err, w)
		return err
	}
	w.Header().Set("Content-type", "application/json")
	w.WriteHeader(rc)
	_, err = w.Write(ret)
	return err
}
