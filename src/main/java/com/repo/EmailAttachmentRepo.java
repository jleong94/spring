package com.repo;

import org.springframework.data.repository.CrudRepository;

import com.modal.EmailAttachment;

public interface EmailAttachmentRepo extends CrudRepository<EmailAttachment, Long> {

}
