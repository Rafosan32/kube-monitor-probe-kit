package com.rafetlabs.kubemonitorprobekit.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogTestController {

    private static final Logger logger = LoggerFactory.getLogger(LogTestController.class);

    @GetMapping("/log-test")
    public String logTest() {
        logger.info("Test log message - Fluent Bit should capture this");
        logger.warn("Warning log message with some data: {}", System.currentTimeMillis());
        logger.error("Error log message for testing");

        return "Log test completed - check Fluent Bit logs";
    }
}