FROM eclipse-temurin:21-jdk
LABEL authors="rajeshkumarm"
WORKDIR /app
COPY target/service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

