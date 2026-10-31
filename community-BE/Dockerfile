FROM gradle:8.14.3-jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src src
RUN gradle test bootJar --no-daemon --max-workers=2
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
