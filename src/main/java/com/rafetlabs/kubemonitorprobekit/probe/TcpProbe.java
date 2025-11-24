package com.rafetlabs.kubemonitorprobekit.probe;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

import java.net.Socket;
import java.util.logging.Logger;

public class TcpProbe {
    private static final Logger logger = Logger.getLogger(TcpProbe.class.getName());
    private final Tracer tracer;

    public TcpProbe(Tracer tracer) {
        this.tracer = tracer;
    }

    public boolean check(String hostPort) {
        String host = hostPort;
        int port = 80;

        if (hostPort.contains(":")) {
            String[] parts = hostPort.split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        }

        Span span = tracer.spanBuilder("tcp.probe")
                .setAttribute("net.peer.name", host)
                .setAttribute("net.peer.port", port)
                .startSpan();

        try {
            long startTime = System.currentTimeMillis();

            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(host, port), 5000);
                long responseTime = System.currentTimeMillis() - startTime;

                span.setAttribute("tcp.response_time_ms", responseTime);
                span.setAttribute("probe.success", true);
                span.setStatus(StatusCode.OK);

                logger.info("TCP probe SUCCESS: " + host + ":" + port + " (" + responseTime + "ms)");
                return true;
            }

        } catch (Exception e) {
            span.setAttribute("probe.success", false);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            logger.warning("TCP probe FAILED: " + host + ":" + port + " - " + e.getMessage());
            return false;
        } finally {
            span.end();
        }
    }
}