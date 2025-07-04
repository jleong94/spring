package com.repo;

import org.springframework.data.repository.CrudRepository;

import com.modal.EMailAttachment;

public interface EMailAttachmentRepo extends CrudRepository<EMailAttachment, Long> {

}
