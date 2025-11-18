package com.rafetlabs.kubemonitorprobekit.config;

import java.util.*;
import java.util.logging.Logger;

public class ProbeConfig {
    private static final Logger logger = Logger.getLogger(ProbeConfig.class.getName());

    private final List<GrpcProbeTarget> grpcTargets;
    private final List<ProbeTarget> tcpTargets;
    private final int probeIntervalSeconds;

    public ProbeConfig() {
        this.grpcTargets = loadGrpcTargets();
        this.tcpTargets = loadTcpTargets();
        this.probeIntervalSeconds = Integer.parseInt(
                System.getenv().getOrDefault("PROBE_INTERVAL_SECONDS", "30")
        );

        logger.info("Probe konfigürasyonu yüklendi: " +
                grpcTargets.size() + " gRPC, " +
                tcpTargets.size() + " TCP target");
    }

    private List<GrpcProbeTarget> loadGrpcTargets() {
        List<GrpcProbeTarget> targets = new ArrayList<>();

        // Environment variables'dan gRPC target'larını yükle
        String grpcTargetsEnv = System.getenv().getOrDefault("GRPC_TARGETS", "");
        if (!grpcTargetsEnv.isEmpty()) {
            String[] targetConfigs = grpcTargetsEnv.split(",");
            for (String config : targetConfigs) {
                String[] parts = config.split(";");
                if (parts.length >= 2) {
                    String target = parts[0].trim();
                    String service = parts[1].trim();
                    String method = parts.length > 2 ? parts[2].trim() : "health.check";
                    targets.add(new GrpcProbeTarget(target, service, method));
                }
            }
        } else {
            // Default gRPC target'lar (örnek)
            targets.add(new GrpcProbeTarget("localhost:50051", "grpc.health.v1.Health", "Check"));
            targets.add(new GrpcProbeTarget("localhost:8080", "helloworld.Greeter", "SayHello"));
        }

        return targets;
    }

    private List<ProbeTarget> loadTcpTargets() {
        List<ProbeTarget> targets = new ArrayList<>();

        String tcpTargetsEnv = System.getenv().getOrDefault("TCP_TARGETS", "");
        if (!tcpTargetsEnv.isEmpty()) {
            String[] hosts = tcpTargetsEnv.split(",");
            for (String host : hosts) {
                targets.add(new ProbeTarget(host.trim(), "TCP"));
            }
        } else {
            targets.add(new ProbeTarget("google.com:80", "TCP"));
            targets.add(new ProbeTarget("github.com:443", "TCP"));
        }

        return targets;
    }

    public List<GrpcProbeTarget> getGrpcTargets() {
        return Collections.unmodifiableList(grpcTargets);
    }

    public List<ProbeTarget> getTcpTargets() {
        return Collections.unmodifiableList(tcpTargets);
    }

    public int getProbeIntervalSeconds() {
        return probeIntervalSeconds;
    }

    public static class ProbeTarget {
        private final String target;
        private final String type;

        public ProbeTarget(String target, String type) {
            this.target = target;
            this.type = type;
        }

        public String getTarget() {
            return target;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return type + "->" + target;
        }
    }

    public static class GrpcProbeTarget {
        private final String target;
        private final String serviceName;
        private final String methodName;

        public GrpcProbeTarget(String target, String serviceName, String methodName) {
            this.target = target;
            this.serviceName = serviceName;
            this.methodName = methodName;
        }

        public String getTarget() {
            return target;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getMethodName() {
            return methodName;
        }

        @Override
        public String toString() {
            return "gRPC->" + target + "[" + serviceName + "/" + methodName + "]";
        }
    }
}