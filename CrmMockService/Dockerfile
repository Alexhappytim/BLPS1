FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app
COPY settings.gradle build.gradle ./
COPY CrmMockService/build.gradle CrmMockService/
# Only download dependencies if there are no code changes
RUN gradle :CrmMockService:dependencies --no-daemon

COPY CrmMockService/src CrmMockService/src
RUN gradle :CrmMockService:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/CrmMockService/build/libs/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
