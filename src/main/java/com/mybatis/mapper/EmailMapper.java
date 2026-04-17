package com.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pojo.EmailDTO;

@Mapper
public interface EmailMapper {

    List<EmailDTO> findEmailByMailId(@Param("mailId") long mailId);
}
