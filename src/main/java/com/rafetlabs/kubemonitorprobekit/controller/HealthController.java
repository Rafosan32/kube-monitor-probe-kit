package com.rafetlabs.kubemonitorprobekit.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping(value = "/healthz", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> healthz() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");
        return body;
    }
}
