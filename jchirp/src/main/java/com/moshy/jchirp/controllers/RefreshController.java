package com.moshy.jchirp.controllers;

import com.moshy.jchirp.repositories.RefreshRepository;
import com.moshy.jchirp.exceptions.AuthenticationException;
import com.moshy.jchirp.headers.HeaderSupport;

import com.moshy.jchirp.services.JwtService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api")
public class RefreshController {
    private final RefreshRepository refreshRepo;

    private final JwtService jwt;

    @PostMapping("/refresh")
    Map<String, String> getAccessToken(@RequestHeader("Authorization") List<String> authorizations) {
        String token = HeaderSupport.getByType(authorizations, "Bearer")
            .orElseThrow(AuthenticationException::new);
        var row = refreshRepo.findByTokenAndExpiresAtGreaterThanAndRevokedAtIsNull(token, LocalDateTime.now())
            .orElseThrow(AuthenticationException::new);
        return Map.of("token", jwt.generateToken(row.getUser().getId()));
    }

    @PostMapping("/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void revokeToken(@RequestHeader("Authorization") List<String> authorizations) {
        String token = HeaderSupport.getByType(authorizations, "Bearer")
            .orElseThrow(AuthenticationException::new);
        if (refreshRepo.revokeToken(token, LocalDateTime.now()) == 0)
            log.info("revoke failed to match a row: token={}", token);
    }
}

