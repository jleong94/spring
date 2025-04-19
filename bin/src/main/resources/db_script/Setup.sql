USE appDB;
CREATE DATABASE IF NOT EXISTS appDB;

#DROP TABLE users;
CREATE TABLE users(
	id BIGINT NOT NULL AUTO_INCREMENT,
	created_date DATETIME NOT NULL,
	modified_date DATETIME NOT NULL,
	full_name VARCHAR(255) NOT NULL,
	email VARCHAR(100) NOT NULL UNIQUE,
	mobile_num VARCHAR(25) NOT NULL,
	client_id VARCHAR(100) NOT NULL UNIQUE,
	client_secret VARCHAR(255) NOT NULL UNIQUE,
	token_expiration INT NOT NULL DEFAULT 0,
	PRIMARY KEY(id)
);
SELECT * FROM users;

DELIMITER $$
CREATE PROCEDURE create_dummy_user_data()
BEGIN
	DECLARE num INT DEFAULT 1;
	DECLARE max_count INT DEFAULT 1000;
	DECLARE num_string VARCHAR(255) DEFAULT '';
    DECLARE sql_state VARCHAR(5);
    DECLARE sql_message TEXT;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN  
		GET DIAGNOSTICS CONDITION 1
            sql_state = RETURNED_SQLSTATE, 
            sql_message = MESSAGE_TEXT;
        SELECT CONCAT('Error occurred: ', sql_state, ' - ', sql_message) AS 'Exception';
    END;
	WHILE num < max_count DO
		IF num < 10 THEN
			SET num_string = CONCAT('0', num);
		ELSE 
			SET num_string = num;
		END IF;
		INSERT INTO users(created_date, modified_date, full_name, email, mobile_num, client_id, client_secret, token_expiration) VALUES
		(NOW(), NOW(), CONCAT('test ', num_string), CONCAT(num_string, '@gmail.com'), '', CONCAT('test', num_string), CONCAT('test', num_string), 60);
		SET num = num + 1;
    END WHILE;
END$$
DELIMITER ;
CALL create_dummy_user_data;