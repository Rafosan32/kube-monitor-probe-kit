package com.rafetlabs.kubemonitorprobekit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    @GetMapping(value = "/healthz", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> healthz() {
        log.debug("Health check endpoint called");

        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");
        body.put("service", "kube-monitor-probe-kit");
        body.put("timestamp", Instant.now().toString());
        body.put("version", "1.0.0");
        return body;
    }

    @GetMapping(value = "/ready", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> readiness() {
        log.debug("Readiness probe called");

        Map<String, Object> body = new HashMap<>();
        body.put("status", "READY");
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    @GetMapping(value = "/live", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> liveness() {
        log.debug("Liveness probe called");

        Map<String, Object> body = new HashMap<>();
        body.put("status", "ALIVE");
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}