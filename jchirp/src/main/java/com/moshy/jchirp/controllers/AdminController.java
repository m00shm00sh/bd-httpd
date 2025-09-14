package com.moshy.jchirp.controllers;

import com.moshy.jchirp.ApiConfig;
import com.moshy.jchirp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepo;

    @Value("#{T(Boolean).parseBoolean('${isDev}')}")
    private boolean isDev;

    @Autowired
    public AdminController(UserRepository repo) {
        userRepo = repo;
    }

    @PostMapping("/reset")
    void reset() {
        if (!isDev) {
            throw new AccessDeniedException("forbidden on non-dev");
        }
        ApiConfig.fileserverHits.set(0);
        userRepo.deleteAll();
    }
}

