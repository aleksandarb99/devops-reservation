# Use a base image with Java 17 and Maven installed
FROM maven:3.8.4-openjdk-17 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the project's pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the application source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Use a minimal base image for running the application
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the builder stage to the container
COPY --from=builder /app/target/*.jar ./app.jar

# Set the command to run the Spring Boot application
CMD ["java", "-jar", "app.jar"]