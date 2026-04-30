FROM eclipse-temurin:17-jdk-alpine
COPY ./notification_service/target/notification_service-jar-with-dependencies.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]