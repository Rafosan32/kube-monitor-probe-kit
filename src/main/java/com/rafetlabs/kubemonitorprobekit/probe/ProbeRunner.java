package com.rafetlabs.kubemonitorprobekit.probe;

import com.rafetlabs.kubemonitorprobekit.config.ProbeConfig;
import com.rafetlabs.kubemonitorprobekit.metrics.HealthMetrics;
import io.opentelemetry.api.trace.Tracer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProbeRunner {
    private static final Logger logger = Logger.getLogger(ProbeRunner.class.getName());

    private final ProbeConfig probeConfig;
    private final TcpProbe tcpProbe;
    private final HealthMetrics healthMetrics;
    private final ScheduledExecutorService scheduler;

    public ProbeRunner(ProbeConfig probeConfig) {
        this.probeConfig = probeConfig;
        Tracer tracer = com.rafetlabs.kubemonitorprobekit.config.OpenTelemetryConfig.getTracer();
        this.tcpProbe = new TcpProbe(tracer);
        this.healthMetrics = new HealthMetrics();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        logger.info("Probe runner starting. Interval: " + probeConfig.getProbeIntervalSeconds() + "s");

        scheduler.scheduleAtFixedRate(
                this::runAllProbes,
                0,
                probeConfig.getProbeIntervalSeconds(),
                TimeUnit.SECONDS
        );
    }

    private void runAllProbes() {
        int successfulProbes = 0;

        for (String target : probeConfig.getTcpTargets()) {
            try {
                boolean success = tcpProbe.check(target);
                healthMetrics.recordProbe(target, success);
                if (success) successfulProbes++;
            } catch (Exception e) {
                logger.severe("Probe error: " + target + " - " + e.getMessage());
            }
        }

        logger.info("Probe cycle completed: " + successfulProbes + "/" + probeConfig.getTcpTargets().size() + " successful");
    }

    public void stop() {
        logger.info("Stopping probe runner...");
        scheduler.shutdown();
    }
}