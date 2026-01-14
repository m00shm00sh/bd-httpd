package com.moshy.jchirp.controllers;

import com.moshy.jchirp.entities.*;
import com.moshy.jchirp.exceptions.*;
import com.moshy.jchirp.filters.GetAuthentication;
import com.moshy.jchirp.repositories.ChirpRepository;
import com.moshy.jchirp.repositories.UserRepository;
import com.moshy.jchirp.requests.ChirpRequest;
import com.moshy.jchirp.responses.ChirpResponse;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.*;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.web.servlet.function.RouterFunctions.route;

@AllArgsConstructor
@Component
class ChirpController {

    private final UserRepository userRepo;
    private final ChirpRepository chirpRepo;

    @Transactional
    ChirpResponse createChirp(Authentication auth, ChirpRequest req) {
        var uid = UUID.fromString(auth.getName());
        if (req.body().length() > 140)
            throw new ChirpTooLongException();
        User u = userRepo.findById(uid).orElseThrow(() -> new IllegalArgumentException("bad uuid " + uid));

        Chirp c = new Chirp();
        c.setUser(u);
        c.setBody(cleanChirp(req.body()));
        c = chirpRepo.saveAndFlush(c);
        chirpRepo.refresh(c);
        var resp = new ChirpResponse();
        resp.setId(c.getId());
        resp.setBody(c.getBody());
        resp.setUserId(uid);
        resp.setCreatedAt(c.getCreatedAt().atOffset(ZoneOffset.UTC));
        resp.setUpdatedAt(c.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return resp;
    }

    List<ChirpResponse> getAllChirps(@Nullable UUID authorId, @Nullable String sort) {
        var dbRows = (authorId != null)
                ? chirpRepo.findByUserIdOrderByCreatedAt(authorId)
                : chirpRepo.findByOrderByCreatedAt();
        var rows = dbRows.stream().map(c -> {
            var resp = new ChirpResponse();
            resp.setId(c.getId());
            resp.setBody(c.getBody());
            resp.setUserId(c.getUser().getId());
            resp.setCreatedAt(c.getCreatedAt().atOffset(ZoneOffset.UTC));
            resp.setUpdatedAt(c.getUpdatedAt().atOffset(ZoneOffset.UTC));
            return resp;
        }).toList();

        return "desc".equals(sort) ? rows.reversed() : rows;
    }

    Chirp findChirpById(UUID cid) {
        return chirpRepo.findById(cid).orElseThrow(NoSuchChirpException::new);
    }

    @Transactional
    void deleteChirp(Authentication auth, UUID cid) {
        var uid = UUID.fromString(auth.getName());
        Chirp c = chirpRepo.findById(cid).orElseThrow(NoSuchChirpException::new);
        if (!c.getUser().getId().equals(uid))
            throw new ForbiddenOperationException();
        chirpRepo.deleteById(cid);
    }

    private final static Set<String> profane = Set.of("kerfuffle", "sharbert", "fornax");
    private static String cleanChirp(String chirp) {
        var toks = chirp.split(" +");

        for (int i = 0; i < toks.length; ++i) {
            if (profane.contains(toks[i].toLowerCase()))
                toks[i] = "****";
        }
        return String.join(" ", toks);
    }
}

@Configuration
class ChirpRouter {
    @Bean
    public RouterFunction<ServerResponse> routeChirps(ChirpController cc) {
        return route()
                .POST("/api/chirps", (req) -> {
                    final var auth = GetAuthentication.getOrThrow();
                    final var chirpReq = req.body(ChirpRequest.class);
                    final var response = cc.createChirp(auth, chirpReq);
                    return ServerResponse.status(HttpStatus.CREATED).body(response);
                })
                .GET("/api/chirps", (req) -> {
                    final var authorId = req.param("author_id")
                            .map(UUID::fromString)
                            .orElse(null);
                    final var sort = req.param("sort").orElse(null);
                    final var response = cc.getAllChirps(authorId, sort);
                    return ServerResponse.ok().body(response);
                })
                .GET("/api/chirps/{cid}", (req) -> {
                    final var cid = UUID.fromString(req.pathVariable("cid"));
                    final var response = cc.findChirpById(cid);
                    try {
                        return ServerResponse.ok().body(response);
                    } catch (NoSuchChirpException _404) {
                        return ServerResponse.notFound().build();
                    }
                })
                .DELETE("/api/chirps/{cid}", (req) -> {
                    final var auth = GetAuthentication.getOrThrow();
                    final var cid = UUID.fromString(req.pathVariable("cid"));
                    cc.deleteChirp(auth, cid);
                    return ServerResponse.status(HttpStatus.NO_CONTENT).build();
                })
            .build();
    }

}