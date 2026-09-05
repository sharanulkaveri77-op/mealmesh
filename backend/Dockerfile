# Stage 1: Build stage with Java 21 JDK
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

ENV MAVEN_OPTS="-Xms128m -Xmx384m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

COPY pom.xml .
COPY src ./src

RUN mvn package -Dmaven.test.skip=true -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

# Stage 2: Runtime stage with Java 21 JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S mealmesh && adduser -S mealmesh -G mealmesh

COPY --from=builder /build/target/*.jar app.jar
RUN chown -R mealmesh:mealmesh /app

USER mealmesh

# Tuned for Render 512MB RAM free tier: SerialGC has lowest memory overhead (<20MB vs 150MB+ for G1GC)
ENV JAVA_OPTS="-Xms128m -Xmx350m -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]