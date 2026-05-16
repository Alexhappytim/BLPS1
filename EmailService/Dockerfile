FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle gradle
COPY settings.gradle build.gradle ./
COPY EmailService EmailService

RUN chmod +x ./gradlew && ./gradlew --no-daemon :EmailService:clean :EmailService:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/EmailService/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
