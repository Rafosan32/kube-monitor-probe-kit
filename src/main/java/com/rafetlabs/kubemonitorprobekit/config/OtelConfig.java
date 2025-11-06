package com.rafetlabs.kubemonitorprobekit.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.logging.LoggingMetricExporter; // (opsiyonel: debug)
import io.opentelemetry.exporter.logging.LoggingSpanExporter;  // (opsiyonel: debug)
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class OtelConfig {

    private static final String SERVICE_NAME = "kube-monitor-probe-kit";

    @Bean
    public OpenTelemetry openTelemetry() {
        // semconv yok: service.name'i manuel veriyoruz
        Resource resource = Resource.getDefault().merge(
                Resource.create(
                        Attributes.of(AttributeKey.stringKey("service.name"), SERVICE_NAME)
                )
        );

        // Traces -> OTLP HTTP (Collector:4318)
        OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint("http://otel-collector:4318/v1/traces")
                .build();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                // Debug istersen aç:
                // .addSpanProcessor(BatchSpanProcessor.builder(LoggingSpanExporter.create()).build())
                .setResource(resource)
                .build();

        // Metrics -> OTLP HTTP (Collector → Prometheus reader)
        OtlpHttpMetricExporter metricExporter = OtlpHttpMetricExporter.builder()
                .setEndpoint("http://otel-collector:4318/v1/metrics")
                .build();

        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .setResource(resource)
                .registerMetricReader(PeriodicMetricReader.builder(metricExporter).build())
                // Debug metric export istersen:
                // .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter.create()).build())
                .build();

        // Logs: exporter/processor YOK (Collector filelog tail ediyor)
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder().build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setMeterProvider(meterProvider)
                .setLoggerProvider(loggerProvider)
                .build();
    }

    @Bean
    @Lazy
    public Meter meter(OpenTelemetry openTelemetry) {
        return openTelemetry.getMeter(SERVICE_NAME);
    }
}
