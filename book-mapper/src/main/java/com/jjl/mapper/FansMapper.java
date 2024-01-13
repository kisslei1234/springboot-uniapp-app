package com.jjl.mapper;

import com.jjl.repository.mapper.MyMapper;
import com.jjl.pojo.Fans;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FansMapper extends MyMapper<Fans> {
}