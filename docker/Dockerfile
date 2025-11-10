# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY ../pom.xml .
COPY ../.mvn .mvn
COPY ../mvnw .
RUN chmod +x mvnw || true
COPY ../src ./src
RUN ./mvnw -q -DskipTests package

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar

# log path (Collector filelog bu dizini okur)
RUN mkdir -p /app/logs && useradd -ms /bin/bash appuser && chown -R appuser:appuser /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
