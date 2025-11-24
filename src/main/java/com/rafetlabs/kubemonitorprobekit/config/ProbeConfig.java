package com.rafetlabs.kubemonitorprobekit.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProbeConfig {
    private static final Logger logger = Logger.getLogger(ProbeConfig.class.getName());

    private final List<String> tcpTargets;
    private final int probeIntervalSeconds;

    public ProbeConfig() {
        this.tcpTargets = loadTcpTargets();
        this.probeIntervalSeconds = Integer.parseInt(
                System.getenv().getOrDefault("PROBE_INTERVAL_SECONDS", "30")
        );

        logger.info("Loaded " + tcpTargets.size() + " TCP targets");
        logger.info("Probe interval: " + probeIntervalSeconds + " seconds");
    }

    private List<String> loadTcpTargets() {
        List<String> targets = new ArrayList<>();

        String tcpTargetsEnv = System.getenv().getOrDefault("TCP_TARGETS", "");
        if (!tcpTargetsEnv.isEmpty()) {
            String[] hostConfigs = tcpTargetsEnv.split(",");
            for (String config : hostConfigs) {
                targets.add(config.trim());
            }
        }

        if (targets.isEmpty()) {
            targets.add("google.com:80");
            targets.add("github.com:443");
        }

        return targets;
    }

    public List<String> getTcpTargets() {
        return tcpTargets;
    }

    public int getProbeIntervalSeconds() {
        return probeIntervalSeconds;
    }
}