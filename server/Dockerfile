FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
COPY server/build/libs/server.jar app.jar
EXPOSE 9000
RUN apk add --no-cache curl
USER app
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9000/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app.jar"]
