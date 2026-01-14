package com.moshy.jchirp.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.moshy.jchirp.repositories.UserRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.*;

import java.util.UUID;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@Component
public class PolkaController {
    private final UserRepository userRepo;

    @Value("${polka.key}")
    @Getter
    private String polkaKey;

    public PolkaController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Transactional
    void upgrade(Polka request) {
        if (!request.event().equals("user.upgraded"))
            return;
        userRepo.upgradeUserToRed(request.data().id());
    }
}

@Configuration
class PolkaRouter {
    @Bean
    public RouterFunction<ServerResponse> routePolka(PolkaController pc) {
        return route()
                .POST("/api/polka/webhooks", (req) -> {
                    final var pReq = req.body(Polka.class);
                    pc.upgrade(pReq);
                    return ServerResponse.status(HttpStatus.NO_CONTENT).build();
                })
            .build();
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
record Polka(
    String event,
    Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Data(
        @JsonProperty("user_id") UUID id
    ) {

    }
}

