package com.moshy.jchirp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason="operation forbidden for user")
public class ForbiddenOperationException extends RuntimeException { }
