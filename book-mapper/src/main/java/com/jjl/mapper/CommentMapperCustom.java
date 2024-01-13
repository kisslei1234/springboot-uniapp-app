package com.jjl.mapper;

import com.jjl.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapperCustom {
    public List<CommentVO> getCommentList(@Param("paramMap") Map<String,Object> map);

}
