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
💻 Maven Build(Own workstation) Setup:
# Step 1: Open eclipse marketplace & search for word, spring.
# Step 2: Click install, wait for it finish & restart eclipse.
# Step 3: Right click target project, select run as->run configurations.
# Step 4: Select maven build.
# Step 5: Put in, clean {goal} on goals input box.
  - {goal}: validate → compile → test → package → integration-test → verify → install → deploy
  - Most right side will cover the job from right to left.
# Step 6: On jre tab, pick the right jdk on alternate jre dropdown.
# Step 7: Click run & wait for it finish.
# Step 8: JAR file will output at {project directory}/target

📡 Fluentbit Setup(Window):
N/A

🔍 OpenSearch Setup(Window):
N/A

🔐 Keycloak Setup(Window):
# Step 1: Download keycloak from https://www.keycloak.org/
# Step 2: Download the mysql jar from https://mvnrepository.com/artifact/com.mysql/mysql-connector-j & put at <Installation path>\keycloak-<version>\providers
# Step 3: Copy the content keycloak.conf(git version) & replace with keycloak.conf at path, <Installation path>\keycloak-<version>\conf.  
# Step 4: Navigate cmd to path, <Installation path>\keycloak-<version>\bin & run, kc.bat start --optimized
# Step 5: Access url, localhost:<port> on browser to test whether keycloak is accessible. For further setup, please refer to https://www.keycloak.org/guides.

🌱 Spring Boot Application Setup(Window):
# Step 1: Make sure running environment installed JDK 21 or above
# Step 2: Download the latest JAR file from git release
# Step 3: Open Command Prompt as Administrator (Windows)
# Step 4: Navigate to the folder containing spring.jar
cd path\to\your\jar\directory
# Step 5: Run the JAR with custom JVM options
java -Xms256m -Xmx2048m -XX:+PrintGCDetails -jar spring.jar

🌱 Spring Boot Application Setup(Linux):
# Step 1: Login to the linux VM
# Step 2: Press left ctrl + p key within winscp screen.
# Step 3: Key in correct password & press enter.
# Step 4: Paste, sudo dnf update -y & press enter to update system.
# Step 5: Paste, sudo dnf search openjdk & press enter to search available Java versions.
# Step 6: Paste, sudo dnf install -y java-21-openjdk java-21-openjdk-devel & press enter to install Java.
# Step 7: Paste, java -version, javac -version & press enter to verify Java version.
# Step 8: Paste, sudo alternatives --config java, sudo alternatives --config javac & press enter to configure which Java version to use if there are multiple of it.
# Step 9: Paste, sudo useradd -r -m -d /opt/spring -s /sbin/nologin spring & press enter.
	* r: system account (no login shell, no password).
	* m -d /opt/myapp: creates a home directory at /opt/myapp for app files.
	* s /sbin/nologin: prevents login access (security).
# Step 10: Copy the jar into /opt/spring.
# Step 11: Paste, sudo chown spring:spring /opt/spring/spring.jar & press enter.
# Step 12: Paste, sudo vi /etc/systemd/system/spring.service & press enter.
# Step 13: Paste below full script, key in :wq at the end of line & press enter. It will save as file.
sudo tee /etc/systemd/system/spring.service > /dev/null <<'EOF'
[Unit]
Description=Spring Boot Base Project Template
After=network.target

[Service]
User=spring
Group=spring
WorkingDirectory=/opt/spring
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/spring/spring.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
# Step 14: Copt & paste below CLI to perform necessary action.
To refresh the updated script to system: sudo systemctl daemon-reload
Start the service: sudo systemctl start spring
Stop the service: sudo systemctl stop spring
Restart the service: sudo systemctl restart spring
Check the service status: sudo systemctl status spring & journalctl -u spring -f
Disable the service from starting at boot: sudo systemctl disable spring
Enable the service from starting at boot: sudo systemctl enable spring

🔑 Keystore Setup:
# Step 1: Install keystore explorer & launch it. 
# Step 2: Click on, open an existing keystore & select the target .p12 file.
# Step 3: Import the key, signed cert & intermediate cert.
# Step 4: Right click on cert, select unlock & set password(password same as .p12 itself).
# Step 5: Save it.
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