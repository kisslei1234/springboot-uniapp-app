package com.jjl.mapper;

import com.jjl.repository.mapper.MyMapper;
import com.jjl.pojo.Fans;
import com.jjl.vo.MyFansVO;
import com.jjl.vo.VlogerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FansMapperCustom extends MyMapper<Fans> {
    public List<VlogerVO> queryMyFollows(@Param("paraMap")Map<String,Object> map);
    public List<MyFansVO> queryMyFans(@Param("paraMap")Map<String,Object> map);
}