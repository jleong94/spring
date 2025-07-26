CREATE DATABASE keycloakdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'kc_admin'@'localhost' IDENTIFIED BY 'kc_2025';
GRANT ALL PRIVILEGES ON keycloakdb.* TO 'kc_admin'@'localhost';
FLUSH PRIVILEGES;