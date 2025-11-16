# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY ../pom.xml .
COPY ../.mvn .mvn
COPY ../mvnw .
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY ../src ./src

RUN ./mvnw package -DskipTests -B

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
WORKDIR /app

COPY --from=build /workspace/target/kube-monitor-probe-kit-1.0.0.jar app.jar

RUN mkdir -p /app/logs && \
    useradd -ms /bin/bash appuser && \
    chown -R appuser:appuser /app

USER appuser
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]