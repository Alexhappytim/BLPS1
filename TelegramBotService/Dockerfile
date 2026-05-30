FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle gradle
COPY settings.gradle build.gradle ./
COPY TelegramBotService TelegramBotService

RUN chmod +x ./gradlew && ./gradlew --no-daemon :TelegramBotService:clean :TelegramBotService:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/TelegramBotService/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
