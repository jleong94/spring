package com.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import com.modal.Email;

@Mapper
public interface EmailMapper {

	//int offset = (page - 1) * size;
	@Transactional(readOnly = true)
    List<Email> findEmailByMailId(@Param("mail_id") long mail_id, @Param("limit") int limit, @Param("offset") int offset);
}
