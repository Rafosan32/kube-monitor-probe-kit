# 🧩 Kube Monitor Probe Kit — OpenTelemetry Collector & Observability Stack

Bu yapılandırma, **OpenTelemetry Collector Contrib v0.104.0** kullanarak  
uygulama loglarını, metriklerini ve trace’lerini merkezi olarak toplar ve  
**Prometheus**, **Loki** ve **Tempo**’ya iletir.  

Collector, `docker-compose` ortamında bağımsız bir konteyner olarak konumlandırılmıştır  
ve aşağıdaki sistemin **temel observability katmanını** oluşturur.

---

## 🧱 Mimari Genel Bakış

```shell
                    ┌───────────────────────────────┐
                    │        Java / Spring App      │
                    │  (OTLP Exporter entegreli)    │
                    └──────────────┬────────────────┘
                                   │
             OTLP (gRPC/HTTP) 4317 │ 4318
                                   ▼
                      ┌────────────────────────┐
                      │  OpenTelemetry Collector │
                      │  (otelcol-contrib:0.104) │
                      ├────────────────────────┤
                      │ Receivers:              │
                      │  • otlp (metrics/traces)│
                      │  • filelog (logs)       │
                      │ Processors:             │
                      │  • resource, batch      │
                      │ Exporters:              │
                      │  • prometheus           │
                      │  • tempo (traces)       │
                      │  • loki (logs)          │
                      └──────────┬──────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
              ▼                  ▼                  ▼
        ┌────────────┐     ┌──────────────┐    ┌─────────────┐
        │ Prometheus │     │ Grafana Tempo│    │ Grafana Loki│
        │   (Metrics)│     │   (Traces)   │    │    (Logs)   │
        └────────────┘     └──────────────┘    └─────────────┘
                                │                     │
                                └──────┬──────────────┘
                                       ▼
                                 ┌──────────┐
                                 │ Grafana  │
                                 │(Dashboard│
                                 │ + Explore│
                                 └──────────┘
```
