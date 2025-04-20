package com.service;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.modal.EMail;
import com.modal.EMailAttachment;
import com.properties.Property;
import com.repo.EMailRepo;
import com.utilities.Tool;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@Service
public class EMailService {

	@Autowired
	Property property;
	
	@Autowired
	Tool tool;
	
	@Autowired
	EMailRepo emailRepo;
	
	/*
	 * Send email
	 * @param log Logger instance for logging
	 * @param email request body
	 * */
	public EMail sendEMail(Logger log, EMail email) {
        try {
        	Properties properties = new Properties();
        	properties.put("mail.smtp.auth", "true");
        	properties.put("mail.smtp.host", property.getSmtpHost());
        	properties.put("mail.smtp.port", property.getSmtpPort());
        	if (property.isSmtpSSL()) {
        		properties.put("mail.smtp.socketFactory.port", property.getSmtpPort());
        		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            } if (property.isSmtpTLS()) {
            	properties.put("mail.smtp.starttls.enable", "true");
            }
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(property.getSmtpUsername(), property.getSmtpPassword());
                }
            });
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email.getFrom()));
            for (String to : email.getTo().split("[,;]")) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }
            for (String cc : email.getCc().split("[,;]")) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
            }
            for (String bcc : email.getBcc().split("[,;]")) {
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));
            }
            message.setSubject(email.getSubject());
            Multipart multipart = new MimeMultipart();
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            if (email.isHTML()) {
            	mimeBodyPart.setContent(email.getBody(), "text/html; charset=utf-8");
            } else {
            	mimeBodyPart.setText(email.getBody());
            }
            multipart.addBodyPart(mimeBodyPart);
            for (EMailAttachment emailAttachment : email.getAttachments()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(new File(emailAttachment.getFile_path()));
                multipart.addBodyPart(attachmentPart);
            }
            message.setContent(multipart);
            Transport.send(message);
            email.setIsSend(1);
        } catch(Exception e) {
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
        } finally{
            try {
            	emailRepo.save(email);
            }catch(Exception e) {}
        }
        return email;
    }
}
