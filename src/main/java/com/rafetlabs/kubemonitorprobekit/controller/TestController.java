package com.rafetlabs.kubemonitorprobekit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.annotation.Timed;

@RestController
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/test-trace")
    @Timed(value = "test.trace.endpoint", description = "Test trace endpoint")
    public String testTrace() {
        logger.info("Trace test endpoint called - generating span");
        return "Trace test completed - " + System.currentTimeMillis();
    }

    @GetMapping("/test-metric")
    @Timed(value = "test.metric.endpoint", description = "Test metric endpoint")
    public String testMetric() {
        logger.info("Metric test endpoint called - generating metrics");
        return "Metric test completed - " + System.currentTimeMillis();
    }

    @GetMapping("/test-log")
    public String testLog(@RequestParam(defaultValue = "5") int count) {
        logger.info("Starting log generation test with {} logs", count);

        for (int i = 0; i < count; i++) {
            logger.info("Generated log entry #{}/{}", i + 1, count);
            logger.debug("Debug log entry - processing data");
        }

        logger.warn("Completed generating {} log entries", count);
        logger.error("Simulated error log for testing");

        return String.format("Generated %d log entries for Fluent Bit -> Loki", count);
    }

    @GetMapping("/health")
    public String health() {
        logger.debug("Health check endpoint called");
        return "{\"status\": \"UP\", \"service\": \"kube-monitor-probe-kit\"}";
    }
}