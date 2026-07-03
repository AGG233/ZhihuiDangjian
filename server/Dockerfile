FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /app
COPY server/build/libs/server.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination /extracted


FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S -g 1000 app && adduser -S -u 1000 -G app app
WORKDIR /app

COPY --from=extractor /extracted/dependencies/ ./
COPY --from=extractor /extracted/spring-boot-loader/ ./
COPY --from=extractor /extracted/snapshot-dependencies/ ./
COPY --from=extractor /extracted/application/ ./


LABEL com.rauio.zhihuidangjian=true
EXPOSE 9000

RUN apk upgrade --no-cache && apk add --no-cache curl && mkdir -p /app/logs /app/uploads && chown -R app:app /app
USER app

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9000/actuator/health || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
