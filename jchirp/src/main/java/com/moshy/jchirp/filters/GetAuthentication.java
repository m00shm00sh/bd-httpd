package com.moshy.jchirp.filters;

import com.moshy.jchirp.exceptions.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class GetAuthentication {

    public static Authentication getOrThrow() throws AuthenticationException {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            throw new AuthenticationException();
        return auth;
    }
}
