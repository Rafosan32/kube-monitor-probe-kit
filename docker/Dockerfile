# Build - Maven with Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Önce pom.xml için dependency cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sonra kaynak kodlar
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime - Alpine JRE 21
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

WORKDIR /app

COPY --from=builder /app/target/kube-monitor-probe-kit-1.0.0.jar app.jar

EXPOSE 8080

# Java 21 optimizasyonları
ENTRYPOINT ["java", "-Xmx512m", "-XX:+UseZGC", "-jar", "app.jar"]