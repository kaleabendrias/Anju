FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY anju-backend/pom.xml ./pom.xml
RUN mvn -B -ntp dependency:go-offline

COPY anju-backend/src ./src
RUN mvn -B -ntp clean package -DskipTests

FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY --from=builder /app/target/*.jar /app/app.jar
COPY --from=builder /app/pom.xml /app/pom.xml
COPY --from=builder /app/src /app/src
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +x /usr/local/bin/docker-entrypoint.sh

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

CMD []
