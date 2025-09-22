## 🏗️ Spring Boot Base Project Template

> Java 21 + Spring Boot + MySQL

This project serves as a fully working **base template** for Java Spring Boot application. Easily clone and duplicate this project as a solid starting point for building microservices or monoliths.

---

🛠 Tech Stack
- **Java:** JDK 21  
- **Framework:** Spring Boot  
- **Database:** MySQL 

---

⚙️ Setup Instructions

```bash
📡 Fluentbit Setup:
N/A

🔍 OpenSearch Setup:
N/A

🔐 Keycloak Setup:
# Step 1: Download keycloak from https://www.keycloak.org/
# Step 2: Download the mysql jar from https://mvnrepository.com/artifact/com.mysql/mysql-connector-j & put at <Installation path>\keycloak-<version>\providers
# Step 3: Copy the content keycloak.conf(git version) & replace with keycloak.conf at path, <Installation path>\keycloak-<version>\conf.  
# Step 4: Navigate cmd to path, <Installation path>\keycloak-<version>\bin & run, kc.bat start --optimized
# Step 5: Access url, localhost:<port> on browser to test whether keycloak is accessible. For further setup, please refer to https://www.keycloak.org/guides.

🌱 Spring Boot Application Setup
# Step 1: Make sure running environment installed JDK 21 or above
# Step 2: Download the latest JAR file from git release
# Step 3: Open Command Prompt as Administrator (Windows)
# Step 4: Navigate to the folder containing spring.jar
cd path\to\your\jar\directory
# Step 5: Run the JAR with custom JVM options
java -Xms256m -Xmx2048m -XX:+PrintGCDetails -jar spring.jar

Note: Unlock & set password for file import into .p12 same as .p12 file 
```
📊 Roadmap<br>
- [ ] Setup fluentbit & opensearch to ship log & perform analysis.

🤝 Contributing<br>
1. Contributions are welcome! 🚀
2. Fork this repo
3. Create your feature branch
4. Commit changes
5. Push branch
6. Open a Pull Request

📚 Resources<br>
- [Spring Boot Docs](https://docs.spring.io/spring-boot/index.html)↗
- [Keycloak Docs](https://www.keycloak.org/documentation)↗
- [MySQL Docs](https://dev.mysql.com/doc/)↗
- [FluentBit Docs](https://docs.fluentbit.io/manual)↗
- [OpenSearch Docs](https://docs.opensearch.org/latest/)↗

📝 License<br>
Licensed under the [Apache-2.0 license](https://github.com/jleong94/spring?tab=Apache-2.0-1-ov-file).