package com.rafetlabs.kubemonitorprobekit.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

import java.util.logging.Logger;

public class OpenTelemetryConfig {
    private static final Logger logger = Logger.getLogger(OpenTelemetryConfig.class.getName());

    private static OpenTelemetry openTelemetry;
    private static Tracer tracer;
    private static Meter meter;

    public static void initialize() {
        logger.info("Initializing OpenTelemetry...");

        openTelemetry = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
        GlobalOpenTelemetry.set(openTelemetry);

        tracer = openTelemetry.getTracer("kube-monitor-probe-kit");
        meter = openTelemetry.getMeter("kube-monitor-probe-kit");

        logger.info("OpenTelemetry configured successfully");
    }

    public static Tracer getTracer() {
        return tracer;
    }

    public static Meter getMeter() {
        return meter;
    }
}