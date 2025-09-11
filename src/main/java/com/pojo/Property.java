package com.pojo;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/*
 * Here is to load all the parameter & it's value created in all properties file located in classpath properties folder
 * */
@Getter//Auto generate getter method
@Configuration
@Slf4j
public class Property {

	//Email SMTP
	@Value("${spring.mail.host}")
    private String spring_mail_host;

	@Value("${spring.mail.port}")
    private int spring_mail_port;

	@Value("${spring.mail.username}")
    private String spring_mail_username;

	@Value("${spring.mail.password}")
    private String spring_mail_password;

	@Value("${spring.mail.properties.mail.smtp.auth}")
    private String spring_mail_properties_mail_smtp_auth;

	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String spring_mail_properties_mail_smtp_starttls_enable;

	@Value("${spring.mail.protocol}")
    private String spring_mail_protocol;
	
	@Bean
    JavaMailSender javaMailSender() {
        JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
        javaMailSenderImpl.setHost(this.spring_mail_host);
        javaMailSenderImpl.setPort(this.spring_mail_port);
        javaMailSenderImpl.setUsername(this.spring_mail_username);
        javaMailSenderImpl.setPassword(this.spring_mail_password);

        Properties properties = javaMailSenderImpl.getJavaMailProperties();
        properties.put("mail.smtp.auth", this.spring_mail_properties_mail_smtp_auth);
        properties.put("mail.smtp.starttls.enable", this.spring_mail_properties_mail_smtp_starttls_enable);

        return javaMailSenderImpl;
    }
	
	//CORS
	@Value("#{'${allowed.origins}'.split(',')}")
    private List<String> allowed_origins;
	
	//Server
	@Value("${server.ssl.enabled-protocols}")
	private String[] server_ssl_enabled_protocols;
	
	@Value("${server.ssl.protocol}")
	private String server_ssl_protocol;
	
	@Value("${server.ssl.key-store}")
    private String server_ssl_key_store;

	@Value("${server.ssl.key-store-password}")
    private String server_ssl_key_store_password;

	@Value("${server.ssl.key-store-type}")
    private String server_ssl_key_store_type;

	@Value("${server.ssl.trust-store}")
    private String server_ssl_trust_store;

	@Value("${server.ssl.trust-store-password}")
    private String server_ssl_trust_store_password;

	@Value("${server.ssl.trust-store-type}")
    private String server_ssl_trust_store_type;
	
	// Firebase
	@Value("${fcm.credentialsClasspathPath}")
    private String fcm_credentialsClasspathPath;
	
	@PostConstruct
    public void initFirebase() throws Throwable {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase already initialized");
                return;
            }

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(this.fcm_credentialsClasspathPath);
            if (inputStream != null) {
            	GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream);
            	log.info("Initializing Firebase with classpath credentials: {}", this.fcm_credentialsClasspathPath);
            	FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                        .setCredentials(googleCredentials)
                        .build();
                FirebaseApp.initializeApp(firebaseOptions);
                log.info("Firebase initialized");
            } else {
            	log.info("Couldn't find {} on classpath.", this.fcm_credentialsClasspathPath);
            }
        } catch (Throwable e) {
        	// Get the current stack trace element
        	StackTraceElement currentElement = Thread.currentThread().getStackTrace()[1];
        	// Find matching stack trace element from exception
        	for (StackTraceElement element : e.getStackTrace()) {
        		if (currentElement.getClassName().equals(element.getClassName())
        				&& currentElement.getMethodName().equals(element.getMethodName())) {
        			log.error("Error in {} at line {}: {} - {}",
        					element.getClassName(),
        					element.getLineNumber(),
        					e.getClass().getName(),
        					e.getMessage());
        			break;
        		}
        	}
        	throw e;
        }
    }
	
	//Slack
	@Value("${alert.slack.webhook-url}")
	private String alert_slack_webhook_url;
}
