# Step 1: Use Java 8 JRE Runtime Base Image
FROM eclipse-temurin:8-jre-alpine

# Set working directory inside container
WORKDIR /app

# Copy built JAR artifact from target folder
COPY target/inventory-management-system.jar app.jar

# Expose server port
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
