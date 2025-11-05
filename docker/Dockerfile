# Build aşaması
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY ../pom.xml .
COPY ../src ./src
RUN mvn clean package -DskipTests

# Runtime aşaması
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /app/logs
COPY --from=builder /app/target/kube-monitor-probe-kit-1.0.0.jar app.jar
# OTEL Log Bridge KESİNLİKLE KAPALI
ENV OTEL_LOGS_EXPORTER=none
ENV MANAGEMENT_OTLP_LOGS_ENABLED=false
# Sadece metrics ve traces açık
ENV OTEL_TRACES_EXPORTER=otlp
ENV OTEL_METRICS_EXPORTER=otlp

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]