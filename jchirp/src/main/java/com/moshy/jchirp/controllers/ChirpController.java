package com.moshy.jchirp.controllers;

import com.moshy.jchirp.entities.*;
import com.moshy.jchirp.exceptions.ChirpTooLongException;
import com.moshy.jchirp.exceptions.ForbiddenOperationException;
import com.moshy.jchirp.exceptions.NoSuchChirpException;
import com.moshy.jchirp.repositories.ChirpRepository;
import com.moshy.jchirp.repositories.UserRepository;
import com.moshy.jchirp.requests.ChirpRequest;
import com.moshy.jchirp.responses.ChirpResponse;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
@Transactional
public class ChirpController {

    private final UserRepository userRepo;
    private final ChirpRepository chirpRepo;

    @PostMapping("/chirps")
    @ResponseStatus(HttpStatus.CREATED)
    ChirpResponse createChirp(Authentication auth, @RequestBody ChirpRequest req) {
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

    @GetMapping("/chirps")
    List<ChirpResponse> getAllChirps(
            @RequestParam(name = "author_id", required = false) UUID authorId,
            @RequestParam(name = "sort", required = false) String sort
    ) {
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

    @GetMapping("/chirps/{cid}")
    Chirp findChirpById(@PathVariable UUID cid) {
        return chirpRepo.findById(cid)
            .orElseThrow(NoSuchChirpException::new);
    }

    @DeleteMapping("/chirps/{cid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteChirp(Authentication auth, @PathVariable UUID cid) {
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

