FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew
RUN gradle dependencies --no-daemon

COPY src src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jdk AS run
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]