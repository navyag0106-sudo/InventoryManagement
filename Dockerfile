# ==========================================================
# Inventory Management System - Dockerfile
# Spring Boot 2.7.18 / Java 8 / Maven
# ==========================================================

# ---------- Build stage ----------
FROM maven:3.8.8-eclipse-temurin-8 AS build

WORKDIR /app

# Copy pom first for better Docker layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:8-jre

WORKDIR /app

# Copy generated JAR
COPY --from=build /app/target/*.jar app.jar

# Render provides the PORT environment variable
EXPOSE 8080

# Start Spring Boot application
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
