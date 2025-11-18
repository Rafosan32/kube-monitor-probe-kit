package com.rafetlabs.kubemonitorprobekit.probe;

import com.rafetlabs.kubemonitorprobekit.config.ProbeConfig.GrpcProbeTarget;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.grpc.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class GrpcProbe {
    private static final Logger logger = Logger.getLogger(GrpcProbe.class.getName());
    private final Tracer tracer;

    public GrpcProbe(Tracer tracer) {
        this.tracer = tracer;
    }

    public ProbeResult check(GrpcProbeTarget grpcTarget) {
        Span span = tracer.spanBuilder("grpc.probe")
                .setAttribute("rpc.system", "grpc")
                .setAttribute("rpc.service", grpcTarget.getServiceName())
                .setAttribute("rpc.method", grpcTarget.getMethodName())
                .setAttribute("net.peer.name", extractHost(grpcTarget.getTarget()))
                .setAttribute("net.peer.port", extractPort(grpcTarget.getTarget()))
                .startSpan();

        ManagedChannel channel = null;
        try {
            logger.info("gRPC probe checking: " + grpcTarget);

            long startTime = System.currentTimeMillis();

            // gRPC channel oluştur
            channel = ManagedChannelBuilder.forTarget(grpcTarget.getTarget())
                    .usePlaintext() // SSL olmadan
                    .enableRetry()
                    .maxRetryAttempts(3)
                    .build();

            // Channel'ın bağlantı durumunu kontrol et
            boolean isReady = channel.awaitTermination(5, TimeUnit.SECONDS);

            if (!isReady) {
                // Active connection kontrolü
                ConnectivityState state = channel.getState(true);
                isReady = state == ConnectivityState.READY;

                if (!isReady) {
                    // Force connect
                    channel.getState(true);
                    isReady = channel.awaitTermination(3, TimeUnit.SECONDS);
                }
            }

            long responseTime = System.currentTimeMillis() - startTime;

            span.setAttribute("grpc.connected", isReady);
            span.setAttribute("grpc.response_time_ms", responseTime);
            span.setAttribute("probe.success", isReady);

            if (isReady) {
                span.setStatus(StatusCode.OK);
                logger.info("gRPC probe SUCCESS: " + grpcTarget + " (" + responseTime + "ms)");
                return new ProbeResult(true, responseTime, "gRPC channel connected successfully");
            } else {
                span.setStatus(StatusCode.ERROR, "gRPC channel not ready");
                logger.warning("gRPC probe FAILED: " + grpcTarget + " - Channel not ready");
                return new ProbeResult(false, responseTime, "gRPC channel not ready");
            }

        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);

            logger.severe("gRPC probe ERROR: " + grpcTarget + " - " + e.getMessage());
            return new ProbeResult(false, 0, e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException ie) {
                    channel.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            span.end();
        }
    }

    private String extractHost(String target) {
        if (target.contains(":")) {
            return target.split(":")[0];
        }
        return target;
    }

    private long extractPort(String target) {
        if (target.contains(":")) {
            try {
                return Long.parseLong(target.split(":")[1]);
            } catch (NumberFormatException e) {
                return 50051; // default gRPC port
            }
        }
        return 50051; // default gRPC port
    }
}