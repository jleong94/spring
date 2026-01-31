# ----------------------
# Stage 1: Build stage
# ----------------------
FROM maven:3.9-eclipse-temurin-21 AS build
# Use Maven + JDK 21 base image (includes Maven and JDK)

WORKDIR /app
# Set working directory inside the container

COPY pom.xml .
# Copy POM file first (for dependency caching)

COPY src ./src
# Copy source code

RUN mvn -B clean package -DskipTests
# Run Maven to build JAR (skip tests for speed)

# ----------------------
# Stage 2: Runtime stage
# ----------------------
FROM eclipse-temurin:21-jre
# Use a smaller JRE-only base image (lighter than JDK)

WORKDIR /app
# Working directory for runtime container

COPY --from=build /app/target/*.jar app.jar
# Copy the built JAR from "build" stage into runtime stage

ENTRYPOINT ["java","-jar","/app/app.jar"]
# Run Spring Boot app when container starts
