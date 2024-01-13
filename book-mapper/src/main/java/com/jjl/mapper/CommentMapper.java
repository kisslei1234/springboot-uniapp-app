package com.jjl.mapper;

import com.jjl.repository.mapper.MyMapper;
import com.jjl.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends MyMapper<Comment> {
}