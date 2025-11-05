# Build aşaması - Maven with Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Context proje root'u olduğu için direkt erişebiliyoruz
COPY ../pom.xml .
COPY ../src ./src

RUN mvn clean package -DskipTests

# Runtime aşaması - Eclipse Temurin JRE 21
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

WORKDIR /app

COPY --from=builder /app/target/kube-monitor-probe-kit-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]