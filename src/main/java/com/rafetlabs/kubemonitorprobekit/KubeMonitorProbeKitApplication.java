package com.rafetlabs.kubemonitorprobekit;

import com.rafetlabs.kubemonitorprobekit.config.OpenTelemetryConfig;
import com.rafetlabs.kubemonitorprobekit.config.ProbeConfig;
import com.rafetlabs.kubemonitorprobekit.probe.ProbeRunner;

import java.util.logging.Logger;

public class KubeMonitorProbeKitApplication {
    private static final Logger logger = Logger.getLogger(KubeMonitorProbeKitApplication.class.getName());

    public static void main(String[] args) {
        logger.info("Starting KubeMonitorProbeKit Application...");

        try {
            OpenTelemetryConfig.initialize();
            logger.info("OpenTelemetry initialized");

            ProbeConfig probeConfig = new ProbeConfig();
            logger.info("Configuration loaded");

            ProbeRunner probeRunner = new ProbeRunner(probeConfig);
            probeRunner.start();
            logger.info("Probe Runner started");

            logger.info("KubeMonitorProbeKit is fully operational!");
            logger.info("Press Ctrl+C to stop...");

            Thread.currentThread().join();

        } catch (Exception e) {
            logger.severe("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}