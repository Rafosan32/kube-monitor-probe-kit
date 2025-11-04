package com.rafetlabs.kubemonitorprobekit.controller;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class DemoController {

    private static final String SERVICE_NAME = "kube-monitor-probe-kit";
    private final Tracer tracer;
    private final Random random = new Random();

    @Autowired
    public DemoController(OpenTelemetry openTelemetry) {
        // Bean olarak enjekte edilen OpenTelemetry'i kullan
        this.tracer = openTelemetry.getTracer(SERVICE_NAME);
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        // Manuel span oluştur
        Span span = tracer.spanBuilder("hello-endpoint")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Span'a attribute ekle - doğrudan string key ile
            span.setAttribute("http.method", "GET");
            span.setAttribute("http.route", "/api/hello");
            span.setAttribute("service.name", SERVICE_NAME);

            // İşlem yap (simülasyon)
            simulateWork();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Hello from Kube Monitor Probe Kit!");
            response.put("status", "success");
            response.put("timestamp", java.time.Instant.now().toString());

            // Response attribute'larını da ekleyebiliriz
            span.setAttribute("response.status", "success");

            return response;
        } catch (Exception e) {
            span.recordException(e);
            span.setAttribute("error", true);
            throw e;
        } finally {
            span.end();
        }
    }

    @GetMapping("/users/{id}")
    public Map<String, Object> getUser(@PathVariable String id) {
        Span span = tracer.spanBuilder("get-user-endpoint")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Attribute'lar
            span.setAttribute("http.method", "GET");
            span.setAttribute("http.route", "/api/users/{id}");
            span.setAttribute("user.id", id);
            span.setAttribute("service.name", SERVICE_NAME);

            // Database benzeri işlem simülasyonu
            simulateDatabaseQuery();

            // Event ekle
            span.addEvent("user.data.retrieved");

            Map<String, Object> user = new HashMap<>();
            user.put("id", id);
            user.put("name", "User " + id);
            user.put("email", "user" + id + "@example.com");
            user.put("status", "active");
            user.put("retrievedAt", java.time.Instant.now().toString());

            span.setAttribute("user.found", true);

            return user;
        } catch (Exception e) {
            span.recordException(e);
            span.setAttribute("error", true);
            throw e;
        } finally {
            span.end();
        }
    }

    @GetMapping("/metrics")
    public Map<String, Object> getMetricsInfo() {
        Span span = tracer.spanBuilder("metrics-info-endpoint")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("http.method", "GET");
            span.setAttribute("http.route", "/api/metrics");

            simulateWork();

            Map<String, Object> info = new HashMap<>();
            info.put("service", SERVICE_NAME);
            info.put("version", "1.0.0");
            info.put("otel.enabled", true);
            info.put("metrics.export", "OTLP/HTTP");
            info.put("timestamp", java.time.Instant.now().toString());

            span.addEvent("metrics.info.provided");

            return info;
        } finally {
            span.end();
        }
    }

    private void simulateWork() {
        try {
            // Kısa bir gecikme simülasyonu
            Thread.sleep(random.nextInt(50) + 10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulateDatabaseQuery() {
        try {
            // Database query simülasyonu
            Thread.sleep(random.nextInt(100) + 20);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}