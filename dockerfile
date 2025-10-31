# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set the jar file name (adjust if your jar has a different version)
ARG JAR_FILE=target/order-service-1.0.0.jar

# Copy the jar into the container
COPY ${JAR_FILE} app.jar

# Run the jar
ENTRYPOINT ["java","-jar","/app.jar"]
