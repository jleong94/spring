CREATE DATABASE keycloakdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
DROP USER IF EXISTS 'kc_admin'@'localhost';
DROP USER IF EXISTS 'kc_admin'@'%';
CREATE USER 'kc_admin'@'%' IDENTIFIED BY 'kc_2025';
GRANT ALL PRIVILEGES ON keycloakdb.* TO 'kc_admin'@'%';
FLUSH PRIVILEGES;
SHOW GRANTS FOR 'kc_admin'@'%';
SELECT user, host FROM mysql.user WHERE user = 'kc_admin';
SELECT user(), current_user();

