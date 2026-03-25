FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY anju-backend/pom.xml .
COPY anju-backend/src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

RUN apk add --no-cache curl bash maven

COPY --from=builder /app/target/*.jar /app/app.jar
COPY --from=builder /app/pom.xml /app/pom.xml
COPY --from=builder /app/src /app/src

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "if [ -f /app/pom.xml ]; then \
     echo 'Waiting for Nacos...' && \
     for i in $(seq 1 30); do \
       curl -sf http://nacos:8848/nacos/v1/console/health && break || sleep 2; \
     done && \
     echo 'Starting application...' && \
     java -jar /app/app.jar; \
     else \
     echo 'No application JAR found, exiting...'; \
     fi"]

CMD []
