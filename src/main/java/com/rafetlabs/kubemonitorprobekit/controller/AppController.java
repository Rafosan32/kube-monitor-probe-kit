package com.rafetlabs.kubemonitorprobekit.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
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
    private final Tracer tracer;

    public AppController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> info() {
        Span span = tracer.spanBuilder("api.info").startSpan();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("app", "kube-monitor-probe-kit");
            body.put("version", "1.0.0");
            body.put("timestamp", Instant.now().toString());
            body.put("status", "healthy");

            log.info("API info endpoint called");
            return body;
        } finally {
            span.end();
        }
    }

    @PostMapping(value = "/log",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> logMessage(
            @RequestParam(defaultValue = "info") String level,
            @RequestParam(defaultValue = "Hello from OpenTelemetry!") String message) {

        Span span = tracer.spanBuilder("api.log").startSpan();
        try {
            // Log seviyesine göre loglama
            switch (level.toLowerCase()) {
                case "trace":
                    log.trace("TRACE: {}", message);
                    break;
                case "debug":
                    log.debug("DEBUG: {}", message);
                    break;
                case "warn":
                    log.warn("WARN: {}", message);
                    break;
                case "error":
                    log.error("ERROR: {}", message);
                    break;
                default:
                    log.info("INFO: {}", message);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("level", level);
            response.put("message", message);
            response.put("timestamp", Instant.now().toString());
            response.put("traceId", span.getSpanContext().getTraceId());

            return response;
        } finally {
            span.end();
        }
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        log.debug("Health check endpoint called");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "kube-monitor-probe-kit");
        response.put("timestamp", Instant.now().toString());
        return response;
    }
}