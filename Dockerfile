FROM openjdk:21-slim

# Set the working directory in the container
WORKDIR /app

# Copy the packaged JAR file into the container at /app
COPY build/libs/*.jar app.jar

# Expose the port that your Spring Boot application uses
EXPOSE 8080

#ENTRYPOINT ["top", "-b"]
ENTRYPOINT ["java", "-jar", "app.jar"]