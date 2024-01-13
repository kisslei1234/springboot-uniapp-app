package com.jjl.service;

import com.jjl.bo.UpdatedUserBo;
import com.jjl.pojo.Users;
import org.apache.catalina.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public Users queryMobileIsExist(String mobile);
    public Users createUser(String mobile);
    public Users getUser(String userId);
    public Users UpdateUserInfo(UpdatedUserBo userBo);
    public Users UpdateUserInfo(UpdatedUserBo userBo,Integer type);
}
