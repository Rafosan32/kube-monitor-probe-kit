package com.rafetlabs.kubemonitorprobekit.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OtelConfig {

    @Value("${OTEL_SERVICE_NAME:kube-monitor-probe-kit}")
    private String serviceName;

    @Value("${OTEL_EXPORTER_PROTOCOL:http}")
    private String exporterProtocol;

    @Value("${OTEL_EXPORTER_OTLP_ENDPOINT:}")
    private String otlpEndpointBase;

    @Value("${OTEL_EXPORTER_OTLP_TRACES_ENDPOINT:}")
    private String tracesEndpoint;

    @Value("${OTEL_EXPORTER_OTLP_METRICS_ENDPOINT:}")
    private String metricsEndpoint;

    @Value("${OTEL_METRIC_EXPORT_INTERVAL_MS:10000}")
    private long metricExportIntervalMs;

    @Value("${OTEL_RESOURCE_ATTRIBUTES:}")
    private String resourceAttributes;

    private String normalizeProtocol(String proto) {
        if (proto == null) return "http/protobuf";
        String p = proto.trim().toLowerCase();
        return p.equals("grpc") ? "grpc" : "http/protobuf";
    }

    @Bean
    public OpenTelemetry openTelemetry() {
        Map<String, String> props = new HashMap<>();

        props.put("otel.service.name", serviceName);
        props.put("otel.exporter.otlp.protocol", normalizeProtocol(exporterProtocol));

        if (otlpEndpointBase != null && !otlpEndpointBase.isBlank()) {
            props.put("otel.exporter.otlp.endpoint", otlpEndpointBase.trim());
        }
        if (tracesEndpoint != null && !tracesEndpoint.isBlank()) {
            props.put("otel.exporter.otlp.traces.endpoint", tracesEndpoint.trim());
        }
        if (metricsEndpoint != null && !metricsEndpoint.isBlank()) {
            props.put("otel.exporter.otlp.metrics.endpoint", metricsEndpoint.trim());
        }
        props.put("otel.metric.export.interval", metricExportIntervalMs + "ms");
        if (resourceAttributes != null && !resourceAttributes.isBlank()) {
            props.put("otel.resource.attributes", resourceAttributes.trim());
        }

        // Autoconfigure kullanımı: argümanlı değil
        OpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
        return sdk;
    }

    @Bean
    @Lazy
    public Meter meter(OpenTelemetry openTelemetry) {
        return openTelemetry.getMeter(serviceName);
    }
}
