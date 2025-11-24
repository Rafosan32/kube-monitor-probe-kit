package com.rafetlabs.kubemonitorprobekit.metrics;

import io.opentelemetry.api.metrics.Meter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class HealthMetrics {
    private static final Logger logger = Logger.getLogger(HealthMetrics.class.getName());

    private final AtomicLong totalProbes = new AtomicLong(0);
    private final AtomicLong successfulProbes = new AtomicLong(0);

    public HealthMetrics() {
        initializeMetrics();
        logger.info("Health metrics initialized");
    }

    private void initializeMetrics() {
        Meter meter = com.rafetlabs.kubemonitorprobekit.config.OpenTelemetryConfig.getMeter();

        meter.gaugeBuilder("probe_success_rate")
                .setDescription("Probe success rate percentage")
                .setUnit("1")
                .buildWithCallback(measurement -> {
                    long total = totalProbes.get();
                    long successful = successfulProbes.get();
                    if (total > 0) {
                        double successRate = (double) successful / total * 100;
                        measurement.record((long) successRate);
                    }
                });
    }

    public void recordProbe(String target, boolean success) {
        totalProbes.incrementAndGet();
        if (success) {
            successfulProbes.incrementAndGet();
        }

        logger.fine("Probe recorded: " + target + " - success: " + success);
    }
}