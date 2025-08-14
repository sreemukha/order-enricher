# Use a slim base image with OpenJDK 21
FROM openjdk:21-jdk-slim

# Set a working directory
WORKDIR /app

# Copy the compiled JAR file from the Maven build
COPY target/order-enricher-*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]