# Runtime stage with Java 21 JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S mealmesh && adduser -S mealmesh -G mealmesh

COPY target/*.jar app.jar
RUN chown -R mealmesh:mealmesh /app

USER mealmesh

# Tuned for Render 512MB RAM free tier: SerialGC has lowest memory overhead (<20MB vs 150MB+ for G1GC)
ENV JAVA_OPTS="-Xms128m -Xmx320m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]