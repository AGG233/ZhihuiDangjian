FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
COPY server/build/libs/*.jar app.jar
EXPOSE 9000
USER app
ENTRYPOINT ["java", "-jar", "/app.jar"]
