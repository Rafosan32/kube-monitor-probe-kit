package com.rafetlabs.kubemonitorprobekit.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;

@Configuration
public class OtelConfig {

    private static final String SERVICE_NAME = "kube-monitor-probe-kit";
    private static final String SERVICE_VERSION = "1.0.0";

    private OpenTelemetry openTelemetry;

    @PostConstruct
    public void init() {
        // OpenTelemetry'i sadece bir kez initialize et
        if (this.openTelemetry == null) {
            this.openTelemetry = initializeOpenTelemetry();
        }
    }

    @Bean
    @Lazy
    public OpenTelemetry openTelemetry() {
        return this.openTelemetry;
    }

    private OpenTelemetry initializeOpenTelemetry() {
        // Resource oluştur - semconv olmadan manuel attribute'lar
        Resource resource = Resource.getDefault()
                .merge(Resource.builder()
                        .put("service.name", SERVICE_NAME)
                        .put("service.version", SERVICE_VERSION)
                        .put("deployment.environment", "development")
                        .put("telemetry.sdk.name", "opentelemetry")
                        .put("telemetry.sdk.language", "java")
                        .put("telemetry.sdk.version", "1.41.0")
                        .build());

        // Span Processor - OTLP HTTP exporter
        BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(
                OtlpHttpSpanExporter.builder()
                        .setEndpoint("http://localhost:4318/v1/traces")
                        .build()
        ).build();

        // Trace Provider
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .addSpanProcessor(BatchSpanProcessor.builder(LoggingSpanExporter.create()).build()) // Logging for debug
                .setResource(resource)
                .build();

        // Metric Reader - OTLP HTTP exporter
        PeriodicMetricReader metricReader = PeriodicMetricReader.builder(
                        OtlpHttpMetricExporter.builder()
                                .setEndpoint("http://localhost:4318/v1/metrics")
                                .build()
                )
                .setInterval(Duration.ofSeconds(30))
                .build();

        // Meter Provider
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(metricReader)
                .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter.create()).build()) // Logging for debug
                .setResource(resource)
                .build();

        // Log Record Processor - OTLP HTTP exporter
        BatchLogRecordProcessor logRecordProcessor = BatchLogRecordProcessor.builder(
                OtlpHttpLogRecordExporter.builder()
                        .setEndpoint("http://localhost:4318/v1/logs")
                        .build()
        ).build();

        // Logger Provider
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(logRecordProcessor)
                .setResource(resource)
                .build();

        // OpenTelemetry SDK oluştur - Global'e register ETME!
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