package com.moshy.jchirp.filters;

import com.moshy.jchirp.controllers.PolkaController;
import com.moshy.jchirp.headers.HeaderSupport;
import com.moshy.jchirp.services.WebhookInfoDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@AllArgsConstructor
public class PolkaWebhookFilter extends OncePerRequestFilter {

    private final PolkaController polka;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final var authHeaders = Collections.list(request.getHeaders("Authorization"));
        final var key = HeaderSupport.getByType(authHeaders, "ApiKey").orElse(null);
        if (key == null) {
            filterChain.doFilter(request, response);
            return;
        }
        if (key.equals(polka.getPolkaKey())) {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var details = new WebhookInfoDetails(key);
                var authToken = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
