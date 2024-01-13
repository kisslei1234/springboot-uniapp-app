package com.jjl.mapper;

import com.jjl.vo.IndexVlogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface VlogMapperCustom{
    public List<IndexVlogVO> getIndexVlogList(@Param("paramMap")Map<String,Object> map);
    public List<IndexVlogVO> getVlogDetailById(@Param("paramMap")Map<String,Object> map);
    public List<IndexVlogVO> getMyLikedVlogList(@Param("paramMap")Map<String,Object> map);
    public List<IndexVlogVO> getMyFollowVlogList(@Param("paramMap")Map<String,Object> map);
    public List<IndexVlogVO> getMyFriendVlogList(@Param("paramMap")Map<String,Object> map);
}