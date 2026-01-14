package com.moshy.jchirp.controllers;

import com.moshy.jchirp.repositories.RefreshRepository;
import com.moshy.jchirp.exceptions.AuthenticationException;
import com.moshy.jchirp.headers.HeaderSupport;

import com.moshy.jchirp.services.JwtService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@AllArgsConstructor
@Component
@Slf4j
class RefreshController {
    private final RefreshRepository refreshRepo;

    private final JwtService jwt;

    Map<String, String> getAccessToken(List<String> authorizations) {
        String token = HeaderSupport.getByType(authorizations, "Bearer")
            .orElseThrow(AuthenticationException::new);
        var row = refreshRepo.findByTokenAndExpiresAtGreaterThanAndRevokedAtIsNull(token, LocalDateTime.now())
            .orElseThrow(AuthenticationException::new);
        return Map.of("token", jwt.generateToken(row.getUser().getId()));
    }

    @Transactional
    void revokeToken(List<String> authorizations) {
        String token = HeaderSupport.getByType(authorizations, "Bearer")
            .orElseThrow(AuthenticationException::new);
        if (refreshRepo.revokeToken(token, LocalDateTime.now()) == 0)
            log.info("revoke failed to match a row: token={}", token);
    }
}

@Configuration
class RefreshRoute {
    @Bean
    public RouterFunction<ServerResponse> routeRefresh(RefreshController rc) {
        return route()
                .POST("/api/refresh", (req) -> {
                    final var authorization = req.headers().header("Authorization");
                    final var response = rc.getAccessToken(authorization);
                    return ServerResponse.ok().body(response);
                })
                .POST("/api/revoke", (req) -> {
                    final var authorization = req.headers().header("Authorization");
                    rc.revokeToken(authorization);
                    return ServerResponse.status(HttpStatus.NO_CONTENT).build();
                })
            .build();
    }
}

