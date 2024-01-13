package com.jjl.mapper;

import com.jjl.repository.mapper.MyMapper;
import com.jjl.pojo.Vlog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VlogMapper extends MyMapper<Vlog> {
}