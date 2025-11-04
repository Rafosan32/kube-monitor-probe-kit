package com.rafetlabs.kubemonitorprobekit.task;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class LogAndMetricTask {

    private static final String SERVICE_NAME = "kube-monitor-probe-kit";

    // AttributeKey'leri tanımla
    private static final AttributeKey<String> LOG_TYPE_KEY = AttributeKey.stringKey("log.type");
    private static final AttributeKey<String> SERVICE_NAME_KEY = AttributeKey.stringKey("service.name");
    private static final AttributeKey<Long> COUNT_KEY = AttributeKey.longKey("count");
    private static final AttributeKey<String> METRIC_NAME_KEY = AttributeKey.stringKey("metric.name");
    private static final AttributeKey<String> OPERATION_TYPE_KEY = AttributeKey.stringKey("operation.type");

    private LongCounter customMetricCounter;
    private AtomicLong logCounter = new AtomicLong(0);
    private AtomicLong metricCounter = new AtomicLong(0);
    private Logger logger;

    @Autowired
    private OpenTelemetry openTelemetry;

    @Autowired
    private Meter meter;

    @PostConstruct
    public void init() {
        // Logger'ı al - bean'den gelen OpenTelemetry'i kullan
        this.logger = openTelemetry.getLogsBridge().loggerBuilder(SERVICE_NAME)
                .setInstrumentationVersion("1.0.0")
                .build();

        // Custom metric'i oluştur
        this.customMetricCounter = meter.counterBuilder("custom_operations_total")
                .setDescription("Total number of custom operations")
                .setUnit("1")
                .build();
    }

    // Her saniye log bas
    @Scheduled(fixedRate = 1000)
    public void generateLogs() {
        long count = logCounter.incrementAndGet();

        // Attributes oluştur
        Attributes attributes = Attributes.builder()
                .put(LOG_TYPE_KEY, "heartbeat")
                .put(SERVICE_NAME_KEY, SERVICE_NAME)
                .put(COUNT_KEY, count)
                .build();

        logger.logRecordBuilder()
                .setBody("Probe kit heartbeat - Log count: " + count)
                .setAllAttributes(attributes)
                .emit();

        System.out.println("🔄 Heartbeat log generated - Count: " + count);
    }

    // Her dakika custom metric artır
    @Scheduled(fixedRate = 60000)
    public void generateMetrics() {
        long count = metricCounter.incrementAndGet();

        // Custom metric'i artır - Attributes ile
        Attributes metricAttributes = Attributes.builder()
                .put(OPERATION_TYPE_KEY, "scheduled")
                .put(SERVICE_NAME_KEY, SERVICE_NAME)
                .build();

        customMetricCounter.add(1, metricAttributes);

        // Log için attributes oluştur
        Attributes logAttributes = Attributes.builder()
                .put(LOG_TYPE_KEY, "metric_update")
                .put(METRIC_NAME_KEY, "custom_operations_total")
                .put(COUNT_KEY, count)
                .build();

        // Ayrıca bir log da basabiliriz
        logger.logRecordBuilder()
                .setBody("Custom metric incremented - Total operations: " + count)
                .setAllAttributes(logAttributes)
                .emit();

        System.out.println("📊 Custom metric incremented - Total: " + count);
    }
}