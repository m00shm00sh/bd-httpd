package com.moshy.jchirp.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.*;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
class MiscRouting {
    @Bean
    public RouterFunction<ServerResponse> routeMisc() {
        return route()
                .GET("/api/healthz", (_req) -> ServerResponse.ok().body("OK"))
            .build();
    }
}

