# ЭТАП 1: СБОРКА (BUILDER)
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY gradle.properties build.gradle settings.gradle ./

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew bootJar --no-daemon

# ЭТАП 2: ЗАПУСК (RUNTIME)
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
