package com.rafetlabs.kubemonitorprobekit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppController {

    private static final Logger log = LoggerFactory.getLogger(AppController.class);

    @GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> ping() {
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("ts", Instant.now().toString());

        // JSON log (Logback pattern JSON'a dönüştürür)
        log.info("{\"event\":\"ping\",\"ok\":true}");

        return body;
    }

    @PostMapping(value = "/log", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> createLog(@RequestParam(defaultValue = "info") String level,
                                         @RequestParam(defaultValue = "web-ui test log") String message) {

        switch (level.toLowerCase()) {
            case "debug": log.debug("{}", message); break;
            case "warn":  log.warn("{}", message);  break;
            case "error": log.error("{}", message); break;
            default:      log.info("{}", message);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("logged", true);
        body.put("level", level);
        body.put("message", message);
        body.put("ts", Instant.now().toString());
        return body;
    }
}
