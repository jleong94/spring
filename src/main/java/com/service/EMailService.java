package com.service;

import java.io.File;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.enums.ResponseCode;
import com.modal.EMail;
import com.modal.EMailAttachment;
import com.pojo.ApiResponse;
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
	 * @param email
	 * */
	public EMail sendEMail(EMail email) throws Exception {
        try {
        	email.setSender(property.getSmtp_mail());
        	Properties properties = new Properties();
        	properties.put("mail.smtp.auth", "true");
        	properties.put("mail.smtp.host", property.getSmtp_host());
        	properties.put("mail.smtp.port", property.getSmtpPort());
        	if (property.isSmtp_ssl()) {
        		properties.put("mail.smtp.socketFactory.port", property.getSmtpPort());
        		properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            } if (property.isSmtp_tls()) {
            	properties.put("mail.smtp.starttls.enable", "true");
            }
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(property.getSmtp_username(), property.getSmtp_password());
                }
            });
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email.getSender()));
            for (String to : email.getReceiver().split("[,;]")) {
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
        } finally{
        	emailRepo.save(email);
        }
        return email;
    }
	
	/*
	 * Get email details by mail_id
	 * @param mail_id 
	 * */
	public ApiResponse getMerchantDetailByMerchant_Id(Long mail_id) {
		return ApiResponse.builder().resp_code(ResponseCode.SUCCESS.getResponse_code())
				.resp_msg(ResponseCode.SUCCESS.getResponse_desc())
				.datetime(tool.getTodayDateTimeInString())
				.email(emailRepo.findById(mail_id).orElseThrow(() -> new RuntimeException("EMail details not found for mail id: " + mail_id)))
				.build();
	}
	
	public EMail saveUploadFileToPath(MultipartFile[] multipartFiles, EMail email) throws Exception {
		for (MultipartFile multipartFile : multipartFiles) {
			email.getAttachments().add(EMailAttachment.builder()
					.file_path(tool.saveUploadFileToPath(multipartFile, property.getMail_upload_path()))
					.build());
		}
		return email;
	}
}
