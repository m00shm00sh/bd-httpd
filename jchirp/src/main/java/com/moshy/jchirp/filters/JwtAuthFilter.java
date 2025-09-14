package com.moshy.jchirp.filters;

import com.moshy.jchirp.headers.HeaderSupport;
import com.moshy.jchirp.services.JwtService;
import com.moshy.jchirp.util.ThrowableToOptional;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        var authHeaders = Collections.list(request.getHeaders("Authorization"));
        var bearer = HeaderSupport.getByType(authHeaders, "Bearer");
        var token = bearer.orElse(null);
        if (token != null) {
            var user = jwtService.getUserFromToken(token);
            if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                ThrowableToOptional.orEmpty(
                    () -> userDetailsService.loadUserByUsername(user), UsernameNotFoundException.class
                ).ifPresent(details -> {
                    var authToken = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}
