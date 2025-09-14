package com.moshy.jchirp.requests;

public record UserRequest(
    String email,
    String password
) { }
