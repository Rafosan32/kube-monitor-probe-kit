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
    private final GrpcProbe grpcProbe;
    private final TcpProbe tcpProbe;
    private final HealthMetrics healthMetrics;
    private final ScheduledExecutorService scheduler;

    public ProbeRunner(ProbeConfig probeConfig) {
        this.probeConfig = probeConfig;
        Tracer tracer = com.rafetlabs.kubemonitorprobekit.config.OpenTelemetryConfig.getTracer();
        this.grpcProbe = new GrpcProbe(tracer);
        this.tcpProbe = new TcpProbe(tracer);
        this.healthMetrics = new HealthMetrics();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        logger.info("Probe runner başlatılıyor. Interval: " +
                probeConfig.getProbeIntervalSeconds() + "s");

        scheduler.scheduleAtFixedRate(this::runAllProbes, 0,
                probeConfig.getProbeIntervalSeconds(), TimeUnit.SECONDS);
    }

    private void runAllProbes() {
        logger.info("Tüm probe'lar çalıştırılıyor...");

        // gRPC probe'larını çalıştır
        for (ProbeConfig.GrpcProbeTarget target : probeConfig.getGrpcTargets()) {
            try {
                ProbeResult result = grpcProbe.check(target);
                healthMetrics.recordGrpcProbe(target.getTarget(), result.isSuccess(), result.getResponseTime());
            } catch (Exception e) {
                logger.severe("gRPC probe hatası: " + target + " - " + e.getMessage());
            }
        }

        // TCP probe'larını çalıştır
        for (ProbeConfig.ProbeTarget target : probeConfig.getTcpTargets()) {
            try {
                ProbeResult result = tcpProbe.check(target.getTarget());
                healthMetrics.recordTcpProbe(target.getTarget(), result.isSuccess(), result.getResponseTime());
            } catch (Exception e) {
                logger.severe("TCP probe hatası: " + target + " - " + e.getMessage());
            }
        }

        logger.info("Tüm probe'lar tamamlandı");
    }

    public void stop() {
        logger.info("Probe runner durduruluyor...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}