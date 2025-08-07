package com.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.modal.EMail;
import com.modal.EMailAttachment;
import com.pojo.Property;
import com.repo.EMailRepo;
import jakarta.mail.internet.MimeMessage;
@Service
public class EMailService {
	
	@Autowired
	JavaMailSender javaMailSender;
	
	@Autowired
	EMailRepo emailRepo;
	
	@Autowired
	Property property;
	
	/*
	 * Send email
	 * @param email
	 * */
	@Transactional
	public EMail sendEMail(Logger log, EMail email) throws Exception {
		try {
			email.setSender(property.getSpring_mail_username());
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			mimeMessageHelper.setFrom(email.getSender()); // must match sender
			mimeMessageHelper.setTo(email.getReceiver() == null ? "" : email.getReceiver());
			mimeMessageHelper.setCc(email.getCc() == null ? "" : email.getCc());
			mimeMessageHelper.setBcc(email.getBcc() == null ? "" : email.getBcc());
			mimeMessageHelper.setSubject(email.getSubject());
			mimeMessageHelper.setText(email.getBody(), email.isHTML()); // ✅ `true` for HTML
			for(EMailAttachment emailAttachment : email.getAttachments()) {
				Path path = Paths.get(emailAttachment.getFile_path());
				if(Files.exists(path) && Files.isRegularFile(path)) {
					mimeMessageHelper.addAttachment(path.getFileName().toString(), path.toFile());
				}
			}
			javaMailSender.send(mimeMessage);
			email.setSend(true);
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
			throw e;
		} finally {
        	emailRepo.save(email);
        }
        return email;
    }
}
