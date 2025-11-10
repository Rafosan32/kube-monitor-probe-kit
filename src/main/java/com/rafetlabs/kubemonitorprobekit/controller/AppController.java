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

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> info() {
        Map<String, Object> body = new HashMap<>();
        body.put("app", "kube-monitor-probe-kit");
        body.put("ts", Instant.now().toString());
        return body;
    }

    @PostMapping(value = "/log", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> log(@RequestParam(defaultValue = "info") String level,
                                   @RequestParam(defaultValue = "hello from api") String message) {

        switch (level.toLowerCase()) {
            case "trace": log.trace("{}", message); break;
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
