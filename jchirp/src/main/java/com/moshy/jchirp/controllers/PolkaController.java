package com.moshy.jchirp.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.moshy.jchirp.repositories.UserRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PolkaController {
    private final UserRepository userRepo;

    @Value("${polka.key}")
    @Getter
    private String polkaKey;

    public PolkaController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/polka/webhooks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void upgrade(@RequestBody Polka request) {
        if (!request.event().equals("user.upgraded"))
            return;
        userRepo.upgradeUserToRed(request.data().id());
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

