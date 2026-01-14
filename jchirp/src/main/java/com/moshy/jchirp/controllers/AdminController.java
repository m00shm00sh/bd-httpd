package com.moshy.jchirp.controllers;

import com.moshy.jchirp.ApiConfig;
import com.moshy.jchirp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.*;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@Component
class AdminController {

    private final UserRepository userRepo;

    @Value("#{T(Boolean).parseBoolean('${isDev}')}")
    private boolean isDev;

    @Autowired
    public AdminController(UserRepository repo) {
        userRepo = repo;
    }

    void reset() {
        if (!isDev) {
            throw new AccessDeniedException("forbidden on non-dev");
        }
        ApiConfig.fileserverHits.set(0);
        userRepo.deleteAll();
    }
}

@Configuration
class AdminRouting {
    @Bean
    public RouterFunction<ServerResponse> routeAdmin(AdminController ac) {
        return route()
            .POST("/admin/reset", (_req) -> {
                ac.reset();
                return ServerResponse.ok().body("");
            })
            .build();
    }
}

