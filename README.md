# 🧩 Kube Monitor Probe Kit — OpenTelemetry Collector & Observability Stack
Bu proje, **OpenTelemetry Collector Contrib** kullanarak uygulamalardan gelen **log**, **metrik** ve **trace** verilerini merkezi biçimde toplayan bir gözlemlenebilirlik (observability) altyapısı sağlar.  
Toplanan veriler **Prometheus**, **Loki** ve **Tempo** bileşenlerine yönlendirilir; tüm veriler ise **Grafana** üzerinden tek bir arayüzde görüntülenir.

Collector, `docker-compose` ortamında bağımsız bir konteyner olarak konumlandırılmıştır ve sistemin **temel observability katmanını** oluşturur.

## 🚀 Kurulum ve Çalıştırma
```bash
git clone https://github.com/Rafosan32/kube-monitor-probe-kit.git
cd kube-monitor-probe-kit/docker
docker compose up -d --build
```

## 🧱 Mimari Genel Bakış
```test
                    ┌───────────────────────────────┐
                    │        Java / Spring App      │
                    │       (OTLP Exporter)         │
                    └──────────────┬────────────────┘
                                   │
                         OTLP gRPC │ HTTP (4317 / 4318)
                                   ▼
                      ┌────────────────────────────┐
                      │  OpenTelemetry Collector   │
                      │   (otelcol-contrib)        │
                      ├────────────────────────────┤
                      │ Receivers:                 │
                      │   • otlp                   │
                      │ Processors:                │
                      │   • resource               │
                      │   • batch                  │
                      │ Exporters:                 │
                      │   • prometheus             │
                      │   • tempo                  │
                      │   • loki                   │
                      └───────────┬────────────────┘
                                  │
          ┌───────────────────────┼─────────────────────────┐
          │                       │                         │
          ▼                       ▼                         ▼
    ┌────────────┐        ┌──────────────┐          ┌─────────────┐
    │ Prometheus │        │   Tempo      │          │     Loki    │
    │  (Metrics) │        │   (Traces)   │          │    (Logs)   │
    └────────────┘        └──────────────┘          └─────────────┘
          │                       │                         │
          └──────────────┬────────┴────────────┬────────────┘
                         ▼                     ▼
                ┌─────────────────────────────────────────┐
                │            Grafana Dashboard            │
                │ (Metrics + Logs + Traces visualization) │
                └─────────────────────────────────────────┘
```

# Kube Monitor Probe Kit

Kubernetes ortamları için gRPC health check ve monitoring probe'ları sağlayan OpenTelemetry tabanlı Java uygulaması.

## Özellikler

- **gRPC Health Checks**: gRPC servislerinin bağlantı durumunu kontrol eder
- **TCP Connectivity Checks**: TCP port bağlantılarını test eder
- **OpenTelemetry Integration**: Metrik ve trace'leri OTLP/gRPC formatında export eder
- **Configurable Targets**: Environment variables ile yapılandırılabilir target'lar

## Quick Start

### Build
```bash
mvn clean package
