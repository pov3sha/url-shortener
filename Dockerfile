# Stage 1: Build the Maven application
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and source code directly
COPY pom.xml .
COPY src ./src

# Build application JAR
RUN mvn package -DskipTests

# Stage 2: Runtime environment
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/url-shortener-0.0.1-SNAPSHOT.jar app.jar

# Expose default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
