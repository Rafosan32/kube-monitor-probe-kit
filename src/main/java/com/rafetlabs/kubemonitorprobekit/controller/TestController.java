package com.rafetlabs.kubemonitorprobekit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.annotation.Timed;

@RestController
public class TestController {

    @GetMapping("/test")
    @Timed(value = "test.endpoint", description = "Test endpoint timing")
    public String test() {
        return "OpenTelemetry Test - " + System.currentTimeMillis();
    }

    @GetMapping("/hello")
    @Timed(value = "hello.endpoint", description = "Hello endpoint timing")
    public String hello() {
        return "Hello from Kube Monitor Probe Kit with OpenTelemetry!";
    }
}