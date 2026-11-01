# Etapa 1: Compilar la aplicación con Maven y Java 21
FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


# Etapa 2: Ejecutar la aplicación con Java 21
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
RUN useradd --system --uid 10001 --create-home appuser

COPY --from=build /app/target/*.jar app.jar

USER 10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]