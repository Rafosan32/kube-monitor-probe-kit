# ============================
# BUILD aşaması (değiştirmedim)
# ============================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Proje kökü bir üstte; mevcut yapıyı koruyoruz
COPY ../pom.xml ./pom.xml
COPY ../src ./src

# Testleri atlayarak paketle
RUN mvn -q -B -DskipTests clean package

# ============================
# RUNTIME aşaması (min değişiklik)
# ============================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root kullanıcı (güvenlik) + log klasörü (Collector filelog için)
RUN addgroup -S app && adduser -S app -G app \
 && mkdir -p /app/logs \
 && chown -R app:app /app
USER app

# Derlenen jar'ı kopyala
# (Jar adı değişirse compose/Docker arg ile override edebilirsin)
COPY --from=builder /app/target/kube-monitor-probe-kit-1.0.0.jar /app/app.jar

# --- OTel Log Bridge KESİNLİKLE KAPALI; sadece metrics+traces açık ---
ENV OTEL_LOGS_EXPORTER=none \
    MANAGEMENT_OTLP_LOGS_ENABLED=false \
    OTEL_TRACES_EXPORTER=otlp \
    OTEL_METRICS_EXPORTER=otlp \
    TZ=Europe/Istanbul \
    LANG=C.UTF-8

# Konteyner dostu JVM varsayılanları (istersen dışarıdan JAVA_OPTS ile override edersin)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 -Duser.timezone=Europe/Istanbul"

EXPOSE 8080

# Docker healthcheck (compose/k8s kullanıyorsan opsiyonel)
HEALTHCHECK --interval=30s --timeout=2s --retries=3 \
  CMD wget -qO- http://localhost:8080/healthz || exit 1

# ENTRYPOINT: mevcut stilini bozmadım;
# JAVA_OPTS geçmek istersen compose'ta environment ile verebilirsin.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
