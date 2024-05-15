# Use an official OpenJDK runtime as a parent image
FROM openjdk:21

# Set the working directory in the container
WORKDIR /app

# Copy the project JAR file to the container
COPY target/perpustakaan-0.0.1-SNAPSHOT.jar app.jar

# Expose ports for gRPC and web server
EXPOSE 9091
EXPOSE 8081

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
