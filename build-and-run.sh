#!/bin/bash

# Perform Gradle build
echo "Performing Gradle build..."
./gradlew build

# Check if Gradle build was successful
if [ $? -ne 0 ]; then
  echo "Gradle build failed. Exiting..."
  exit 1
fi

# Build Docker image
echo "Building Docker image..."
docker build -t twitter-analog .

# Check if Docker image build was successful
if [ $? -ne 0 ]; then
  echo "Docker image build failed. Exiting..."
  exit 1
fi

# Run Docker Compose
echo "Running Docker Compose..."
docker-compose up -d

# Check if Docker Compose was successful
if [ $? -ne 0 ]; then
  echo "Docker Compose failed. Exiting..."
  exit 1
fi

echo "Application deployed successfully."
