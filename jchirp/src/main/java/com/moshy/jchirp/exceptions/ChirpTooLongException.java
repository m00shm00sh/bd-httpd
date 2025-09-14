package com.moshy.jchirp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason="chirp too long")
public class ChirpTooLongException extends RuntimeException { }
