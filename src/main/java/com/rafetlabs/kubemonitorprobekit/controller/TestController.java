package com.rafetlabs.kubemonitorprobekit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.annotation.Timed;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @Timed(value = "test.logs.generate", histogram = true)
    @PostMapping(value = "/logs", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> generateLogs(
            @RequestParam(defaultValue = "info") String level,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "generated from web api") String message
    ) {
        // Basit validasyon
        if (count < 1) count = 1;
        if (count > 1000) count = 1000; // aşırı log patlamasını önle

        // Level normalize
        String lvl = level == null ? "info" : level.toLowerCase();

        for (int i = 1; i <= count; i++) {
            // Yapısal (JSON-friendly) log gövdesi — Logback pattern bunu JSON’a dönüştürür
            // ("{}" placeholder’ı ile sayıyı ayrı alan gibi taşır, Loki’de query kolaylaşır)
            switch (lvl) {
                case "debug":
                    log.debug("{\"event\":\"test_log\",\"seq\":{},\"level\":\"debug\",\"msg\":\"{}\"}", i, message);
                    break;
                case "warn":
                    log.warn("{\"event\":\"test_log\",\"seq\":{},\"level\":\"warn\",\"msg\":\"{}\"}", i, message);
                    break;
                case "error":
                    log.error("{\"event\":\"test_log\",\"seq\":{},\"level\":\"error\",\"msg\":\"{}\"}", i, message);
                    break;
                default:
                    log.info("{\"event\":\"test_log\",\"seq\":{},\"level\":\"info\",\"msg\":\"{}\"}", i, message);
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("ts", Instant.now().toString());
        body.put("level", lvl);
        body.put("count", count);
        body.put("message", message);
        body.put("pipeline", "Spring -> OTel Collector (filelog) -> Loki -> Grafana");
        return ResponseEntity.ok(body);
    }

    // NOT: /health endpoint'i kaldırıldı.
    // Sağlık kontrolü için HealthController'daki /healthz veya actuator /actuator/health kullanılmalıdır.
}
