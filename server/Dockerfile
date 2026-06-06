FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew settings.gradle build.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY buildSrc ./buildSrc
COPY server ./server
COPY services ./services

RUN chmod +x ./gradlew && ./gradlew :server:bootJar --no-daemon \
    -x test -x integrationTest -x spotlessCheck \
    -x jacocoRootReport -x jacocoRootCoverageVerification


FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /app
COPY --from=builder /app/server/build/libs/server.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination /extracted


FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app

COPY --from=extractor /extracted/dependencies/ ./
COPY --from=extractor /extracted/spring-boot-loader/ ./
COPY --from=extractor /extracted/snapshot-dependencies/ ./
COPY --from=extractor /extracted/application/ ./


LABEL com.rauio.zhihuidangjian=true
EXPOSE 9000

RUN apk add --no-cache curl
USER app

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9000/actuator/health || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
