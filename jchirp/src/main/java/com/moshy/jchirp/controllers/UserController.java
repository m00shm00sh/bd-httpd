package com.moshy.jchirp.controllers;

import com.moshy.jchirp.entities.*;
import com.moshy.jchirp.repositories.RefreshRepository;
import com.moshy.jchirp.requests.UserRequest;
import com.moshy.jchirp.responses.UserResponse;
import com.moshy.jchirp.responses.UserWithRefreshToken;
import com.moshy.jchirp.services.JwtService;
import com.moshy.jchirp.services.UserService;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
@Transactional
public class UserController {

    // the Service acts between Controller and Repository to encode/decode passwords
    private final UserService service;

    private final RefreshRepository refreshRepo;

    private final JwtService jwt;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse newUser(@RequestBody UserRequest newUser) {
        var u = new User();
        u.setEmail(newUser.email());
        u.setPassword(newUser.password());
        u = service.addUser(u);
        // force a round trip to get created-at time
        u = service.findUserById(u.getId());
        var response = new UserResponse();
        response.setId(u.getId());
        response.setCreatedAt(u.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(u.getUpdatedAt().atOffset(ZoneOffset.UTC));
        response.setEmail(u.getEmail());
        response.setIsChirpyRed(u.isChirpyRed());
        return response;
    }

    @PostMapping("/login")
    UserWithRefreshToken loginUser(@RequestBody UserRequest login) {
        var u = service.findUserByEmail(login.email());
        var refreshToken = getRandomHexString(32*2);
        var refresh = new Refresh();
        refresh.setToken(refreshToken);
        refresh.setUser(u);
        refresh.setExpiresAt(LocalDateTime.now().plusDays(60));
        refreshRepo.save(refresh);
        UserWithRefreshToken response = new UserWithRefreshToken();
        response.setId(u.getId());
        response.setCreatedAt(u.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(u.getUpdatedAt().atOffset(ZoneOffset.UTC));
        response.setEmail(u.getEmail());
        response.setToken(jwt.generateToken(u.getId()));
        response.setRefreshToken(refreshToken);
        response.setIsChirpyRed(u.isChirpyRed());
        return response;
    }


    @PutMapping("/users")
    UserResponse modifyUser(Authentication auth, @RequestBody UserRequest newDetails) {
        var u = service.findUserById(UUID.fromString(auth.getName()));
        u.setEmail(newDetails.email());
        u.setPassword(newDetails.password());
        service.addUser(u);
        var response = new UserResponse();
        response.setId(u.getId());
        response.setCreatedAt(u.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(u.getUpdatedAt().atOffset(ZoneOffset.UTC));
        response.setEmail(u.getEmail());
        response.setIsChirpyRed(u.isChirpyRed());
        return response;
    }

    private static String getRandomHexString(int n) {
        var bytes = new byte[n/2];
        try {
            SecureRandom.getInstanceStrong().nextBytes(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return Hex.encodeHexString(bytes);
    }
}

