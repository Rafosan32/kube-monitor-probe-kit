package com.rafetlabs.kubemonitorprobekit.controller;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);
    private final Tracer tracer;

    public TestController(Tracer tracer) {
        this.tracer = tracer;
    }

    @PostMapping(value = "/logs",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> generateLogs(
            @RequestParam(defaultValue = "info") String level,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "generated from web api") String message
    ) {
        Span span = tracer.spanBuilder("api.test.logs").startSpan();
        try {
            if (count < 1) count = 1;
            if (count > 1000) count = 1000;

            String lvl = (level == null ? "info" : level.toLowerCase());
            log.info("Generating {} {} logs with message: {}", count, lvl, message);

            for (int i = 0; i < count; i++) {
                String msg = message + " #" + (i + 1);
                switch (lvl) {
                    case "trace":
                        log.trace("{}", msg);
                        break;
                    case "debug":
                        log.debug("{}", msg);
                        break;
                    case "warn":
                        log.warn("{}", msg);
                        break;
                    case "error":
                        log.error("{}", msg);
                        break;
                    default:
                        log.info("{}", msg);
                }
            }

            Map<String, Object> body = new HashMap<>();
            body.put("ok", true);
            body.put("level", lvl);
            body.put("count", count);
            body.put("timestamp", Instant.now().toString());
            body.put("traceId", span.getSpanContext().getTraceId());
            return ResponseEntity.ok(body);
        } finally {
            span.end();
        }
    }

    @GetMapping("/metrics-test")
    public Map<String, Object> metricsTest() {
        Span span = tracer.spanBuilder("api.test.metrics").startSpan();
        try {
            log.info("Metrics test endpoint called");

            Map<String, Object> response = new HashMap<>();
            response.put("test", "metrics");
            response.put("timestamp", Instant.now().toString());
            response.put("traceId", span.getSpanContext().getTraceId());
            return response;
        } finally {
            span.end();
        }
    }
}