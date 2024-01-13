package com.jjl.mapper;

import com.jjl.repository.mapper.MyMapper;
import com.jjl.pojo.Users;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UsersMapper extends MyMapper<Users> {
}