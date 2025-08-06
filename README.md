# 🚀 Spring Boot Base Project Template

> Java 21 + Spring Boot + MySQL

This project serves as a fully working **base template** for Java Spring Boot application. Easily clone and duplicate this project as a solid starting point for building microservices or monoliths.

---

## 🛠 Tech Stack

- **Java:** JDK 21  
- **Framework:** Spring Boot  
- **Database:** MySQL  
- **HTTP Client:** Apache HTTP Client  
- **Security:** OAuth2, JWT, Keycloak  
- **Other:** SFTP, TCP Socket, Multi-threading, Spring Batch, SMTP Mail  

---

## 🚀 Setup

```bash
# Step 1: Make sure running environment installed JDK 21 or above
# Step 2: Download keycloak from https://www.keycloak.org/
# Step 3: Download the mysql jar from https://mvnrepository.com/artifact/com.mysql/mysql-connector-j & put at <Installation path>\keycloak-<version>\providers
# Step 4: Copy the content keycloak.conf(git version) & replace with keycloak.conf at path, <Installation path>\keycloak-<version>\conf
# Step 5: Navigate cmd to path, <Installation path>\keycloak-<version>\bin & run, kc.bat start
# Step 6: Access url, localhost:<port> on browser to test whether keycloak is accessible
# Step 7: Download the latest JAR file from git release
# Step 8: Open Command Prompt as Administrator (Windows)
# Step 9: Navigate to the folder containing spring.jar
cd path\to\your\jar\directory
# Step 10: Run the JAR with custom JVM options
java -Xms256m -Xmx2048m -XX:+PrintGCDetails -jar spring.jar
