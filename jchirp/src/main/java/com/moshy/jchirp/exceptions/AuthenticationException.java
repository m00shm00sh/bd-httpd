package com.moshy.jchirp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason="authentication failed")
public class AuthenticationException extends RuntimeException { }
