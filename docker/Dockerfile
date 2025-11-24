# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Önce pom.xml ve gerekli dosyaları kopyala
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Dependency'leri indir
RUN ./mvnw dependency:go-offline -B

# Kaynak kodları kopyala ve paketle
COPY src ./src
RUN ./mvnw package -DskipTests -B

# JAR dosyasının ismini kontrol et (debug için)
RUN find /workspace/target -name "*.jar" -type f

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
USER root

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        iputils-ping \
        net-tools && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
WORKDIR /app

# JAR dosyasını kopyala - isim kontrolü yapıldıktan sonra
COPY --from=build /workspace/target/*.jar app.jar

RUN mkdir -p /app/logs && \
    useradd -ms /bin/bash appuser && \
    chown -R appuser:appuser /app

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]