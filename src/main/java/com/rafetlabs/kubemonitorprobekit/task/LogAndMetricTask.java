package com.rafetlabs.kubemonitorprobekit.task;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributeKey;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LogAndMetricTask {

    private static final Logger logger = LoggerFactory.getLogger(LogAndMetricTask.class);

    private final OpenTelemetry openTelemetry;
    private Meter meter;
    private LongCounter operationsCounter;
    private final AtomicLong counter = new AtomicLong(0);

    private static final AttributeKey<String> LOG_TYPE_KEY   = AttributeKey.stringKey("log_type");
    private static final AttributeKey<String> METRIC_NAME_KEY= AttributeKey.stringKey("metric_name");
    private static final AttributeKey<Long>   COUNT_KEY      = AttributeKey.longKey("count");

    @Autowired
    public LogAndMetricTask(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @PostConstruct
    public void init() {
        this.meter = openTelemetry.getMeter("kube-monitor-probe-kit");
        this.operationsCounter = meter.counterBuilder("custom_operations_total")
                .setDescription("Custom operations counter")
                .setUnit("1")
                .build();
    }

    @Scheduled(fixedRate = 5000)
    public void updateMetricAndLog() {
        long count = counter.incrementAndGet();

        Attributes attributes = Attributes.builder()
                .put(METRIC_NAME_KEY, "custom_operations_total")
                .put(LOG_TYPE_KEY, "metric_update")
                .put(COUNT_KEY, count)
                .build();

        // Metric artışı
        operationsCounter.add(1, attributes);

        // JSON log (Logback pattern JSON'a dönüştürür)
        logger.info("{\"event\":\"metric_update\",\"metric\":\"custom_operations_total\",\"count\":{}}", count);
    }
}
