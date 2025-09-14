package com.moshy.jchirp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="chirp not found")
public class NoSuchChirpException extends RuntimeException { }
