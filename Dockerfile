# Imagen base con Java 21
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]