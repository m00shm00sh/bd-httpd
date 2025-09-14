package com.moshy.jchirp.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MiscController {
    @GetMapping("/healthz")
    String healthCheck() {
        return "OK";
    }
}

