# 第一阶段：编译环境
FROM gradle:8.12-jdk21 AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle --no-daemon clean bootJar -x test

# 第二阶段：运行环境
FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/server/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
