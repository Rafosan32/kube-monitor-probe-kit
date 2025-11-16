package com.rafetlabs.kubemonitorprobekit.task;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.common.Attributes;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class LogAndMetricTask {

    private static final Logger logger = LoggerFactory.getLogger(LogAndMetricTask.class);

    private final Meter meter;
    private LongCounter operationsCounter;
    private final AtomicLong counter = new AtomicLong(0);

    public LogAndMetricTask(Meter meter) {
        this.meter = meter;
    }

    @PostConstruct
    public void init() {
        this.operationsCounter = meter.counterBuilder("custom_operations_total")
                .setDescription("Total number of custom operations")
                .setUnit("1")
                .build();

        logger.info("LogAndMetricTask initialized with custom_operations_total metric");
    }

    @Scheduled(fixedRate = 30000)
    public void updateMetricAndLog() {
        long count = counter.incrementAndGet();

        // Metric'i güncelle
        operationsCounter.add(1, Attributes.builder()
                .put("operation", "scheduled_update")
                .put("service", "kube-monitor-probe-kit")
                .build());

        // JSON log
        logger.info("Scheduled metric update completed - count: {}", count);

        // Farklı log seviyelerinden örnekler
        if (count % 5 == 0) {
            logger.warn("This is a warning message - count: {}", count);
        }

        if (count % 10 == 0) {
            logger.debug("Debug level log - count: {}", count);
        }
    }
}