package com.rafetlabs.kubemonitorprobekit.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class HealthMetrics {
    private static final Logger logger = Logger.getLogger(HealthMetrics.class.getName());

    private final AtomicLong totalGrpcProbes;
    private final AtomicLong successfulGrpcProbes;
    private final AtomicLong totalTcpProbes;
    private final AtomicLong successfulTcpProbes;

    public HealthMetrics() {
        this.totalGrpcProbes = new AtomicLong(0);
        this.successfulGrpcProbes = new AtomicLong(0);
        this.totalTcpProbes = new AtomicLong(0);
        this.successfulTcpProbes = new AtomicLong(0);

        initializeMetrics();
    }

    private void initializeMetrics() {
        Meter meter = com.rafetlabs.kubemonitorprobekit.config.OpenTelemetryConfig.getMeter();

        // gRPC probe metrikleri
        meter.gaugeBuilder("grpc_probe_duration_ms")
                .setDescription("gRPC probe connection time in milliseconds")
                .setUnit("ms")
                .buildWithCallback(measurement -> {
                    // Bu callback periyodik olarak çağrılır
                });

        meter.gaugeBuilder("grpc_probe_success_rate")
                .setDescription("gRPC probe success rate")
                .setUnit("1")
                .buildWithCallback(this::recordGrpcSuccessRate);

        // TCP probe metrikleri
        meter.gaugeBuilder("tcp_probe_duration_ms")
                .setDescription("TCP probe response time in milliseconds")
                .setUnit("ms")
                .buildWithCallback(measurement -> {
                    // Bu callback periyodik olarak çağrılır
                });

        meter.gaugeBuilder("tcp_probe_success_rate")
                .setDescription("TCP probe success rate")
                .setUnit("1")
                .buildWithCallback(this::recordTcpSuccessRate);

        // Counter metrikleri
        meter.counterBuilder("grpc_probes_total")
                .setDescription("Total number of gRPC probes")
                .build();

        meter.counterBuilder("tcp_probes_total")
                .setDescription("Total number of TCP probes")
                .build();

        logger.info("Health metrics başlatıldı");
    }

    public void recordGrpcProbe(String target, boolean success, long responseTime) {
        totalGrpcProbes.incrementAndGet();
        if (success) {
            successfulGrpcProbes.incrementAndGet();
        }

        logger.fine("gRPC probe recorded: " + target + " - success: " + success + " - responseTime: " + responseTime + "ms");
    }

    public void recordTcpProbe(String target, boolean success, long responseTime) {
        totalTcpProbes.incrementAndGet();
        if (success) {
            successfulTcpProbes.incrementAndGet();
        }

        logger.fine("TCP probe recorded: " + target + " - success: " + success + " - responseTime: " + responseTime + "ms");
    }

    private void recordGrpcSuccessRate(ObservableLongMeasurement measurement) {
        long total = totalGrpcProbes.get();
        long successful = successfulGrpcProbes.get();

        if (total > 0) {
            double successRate = (double) successful / total;
            measurement.record((long)(successRate * 100)); // Yüzde olarak
        }
    }

    private void recordTcpSuccessRate(ObservableLongMeasurement measurement) {
        long total = totalTcpProbes.get();
        long successful = successfulTcpProbes.get();

        if (total > 0) {
            double successRate = (double) successful / total;
            measurement.record((long)(successRate * 100)); // Yüzde olarak
        }
    }
}