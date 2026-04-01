FROM eclipse-temurin:21-jre-alpine
COPY server/build/libs/*.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "/app.jar"]
