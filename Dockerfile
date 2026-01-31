# ----------------------
# Stage 1: Build stage
# ----------------------
FROM eclipse-temurin:21-jdk AS build
# Use JDK 21 (full JDK, includes compiler & tools)

WORKDIR /app
# Set working directory inside the container

COPY . .
# Copy all project files into /app

RUN ./mvnw -B clean package -DskipTests
# Run Maven Wrapper (mvnw) to build JAR (skip tests for speed)

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
