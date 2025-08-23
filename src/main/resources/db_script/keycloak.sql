-- Create the Keycloak database with recommended UTF8 settings
CREATE DATABASE IF NOT EXISTS keycloakdb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create a dedicated Keycloak user (never use root in production)
CREATE USER IF NOT EXISTS 'kc_admin'@'%' IDENTIFIED BY 'kc_2025';

-- Grant all privileges on the Keycloak DB to this user
GRANT ALL PRIVILEGES ON keycloakdb.* TO 'kc_admin'@'%';

-- Apply changes
FLUSH PRIVILEGES;
