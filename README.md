# 🚀 Spring Boot Base Project Template

> Java 21 + Spring Boot + MySQL

This project serves as a fully working **base template** for Java Spring Boot developers. Easily clone and duplicate this project as a solid starting point for building microservices or monoliths.

Download latest build: [![Download Latest Build](https://img.shields.io/badge/Download-Build-blue?style=for-the-badge&logo=github)](RELEASE_URL)

---

## 🧩 Features

### 🔐 Security
- [x] JWT-based Authentication
- [x] Retrieval JWT (username & password)
- [x] User Registration
- [x] Rate Limiter Control

### 🌐 API
- [x] RESTful API using Spring
- [x] JAX-RS Compatible Endpoints
- [x] Apache HTTP Client for API Calls
- [x] TCP Socket Communication

### ⚙️ Scheduler
- [x] Spring Batch Scheduler

### 📦 Utility
- [x] Multi-threaded Operations
- [x] SSH SFTP File Sync
- [x] Date/Time Formatter (`yyyy-MM-dd'T'HH:mm:ss.SSS`)
- [x] Centralized Exception Handling
- [x] Send Mail via SMTP

---

## 🛠 Tech Stack

- **Java:** JDK 21  
- **Framework:** Spring Boot  
- **Database:** MySQL  
- **HTTP Client:** Apache HTTP Client  
- **Security:** OAuth2, JWT  
- **Other:** SFTP, TCP Socket, Multi-threading, Spring Batch  

---

## 🚀 Setup

```bash
# Step 1: Make sure running environment installed JDK 21 or above.

# Step 2: Download the latest JAR file

# Step 3: Open Command Prompt as Administrator (Windows)

# Step 4: Navigate to the folder containing spring.jar
cd path\to\your\jar\directory

# Step 5: Run the JAR with custom JVM options
java -Xms256m -Xmx2048m -XX:+PrintGCDetails -enableassertions -jar spring.jar
