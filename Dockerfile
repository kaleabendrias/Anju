FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY anju-backend/pom.xml .
COPY anju-backend/src ./src

RUN apt-get update && apt-get install -y curl wget && \
    wget -q https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz && \
    tar -xzf apache-maven-3.9.6-bin.tar.gz -C /usr/local --strip-components=1 && \
    rm apache-maven-3.9.6-bin.tar.gz && \
    mvn dependency:resolve clean package -DskipTests && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

FROM eclipse-temurin:17-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y curl wget && \
    wget -q https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz && \
    tar -xzf apache-maven-3.9.6-bin.tar.gz -C /usr/local --strip-components=1 && \
    rm apache-maven-3.9.6-bin.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

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
