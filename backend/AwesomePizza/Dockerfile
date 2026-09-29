# Multi-stage build for AwesomePizza backend
# Stage 1: Build the application
FROM openjdk:21-jdk AS builder
WORKDIR /app
# Copy pom.xml first for better Docker layer caching
COPY pom.xml .
# Download dependencies offline for faster subsequent builds
RUN mvn -q -e -DskipTests dependency:go-offline
# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:21-jre
WORKDIR /app
# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar
# Expose the application port
EXPOSE 8080
# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
