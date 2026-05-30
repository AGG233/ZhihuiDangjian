# ── Stage 1: Extract layered JAR ──
FROM eclipse-temurin:21-jre-alpine AS extractor
COPY server/build/libs/server.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination /extracted

# ── Stage 2: Runtime ──
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app

# Layer: dependencies (rarely changes, ~80MB)
COPY --from=extractor /extracted/dependencies/ ./

# Layer: spring-boot-loader (never changes, ~400KB)
COPY --from=extractor /extracted/spring-boot-loader/ ./

# Layer: snapshot-dependencies (usually empty)
COPY --from=extractor /extracted/snapshot-dependencies/ ./

# Layer: application code (changes every release, ~10MB)
COPY --from=extractor /extracted/application/ ./

LABEL com.rauio.zhihuidangjian=true
EXPOSE 9000
RUN apk add --no-cache curl
USER app
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9000/actuator/health || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
