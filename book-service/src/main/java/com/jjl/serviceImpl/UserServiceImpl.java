package com.jjl.serviceImpl;

import com.jjl.bo.UpdatedUserBo;
import com.jjl.enums.Sex;
import com.jjl.enums.UserInfoModifyType;
import com.jjl.enums.YesOrNo;
import com.jjl.exceptions.GraceException;
import com.jjl.grace.result.ResponseStatusEnum;
import com.jjl.mapper.UsersMapper;
import com.jjl.pojo.Users;
import com.jjl.service.UserService;
import com.jjl.utils.DateUtil;
import com.jjl.utils.DesensitizationUtil;
import org.apache.ibatis.annotations.Mapper;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private Sid sid;
    private static final String USER_FACE1 = "https://i.mji.rip/2023/08/06/2bf6439df26596e6da784d27ab998922.jpeg";
    @Override
    public Users queryMobileIsExist(String mobile) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("mobile", mobile);
        Users user = usersMapper.selectOneByExample(userExample);
        return user;
    }
    @Transactional
    @Override
    public Users createUser(String mobile) {
        //获取全局唯一id
        String userId = sid.nextShort();
        Users user = new Users();
        user.setId(userId);
        user.setMobile(mobile);
        user.setNickname("用户:"+ DesensitizationUtil.commonDisplay(mobile));
        user.setImoocNum("用户:"+ DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);
        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);
        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setCanImoocNumBeUpdated(YesOrNo.YES.type);
        user.setDescription("这家伙很懒,什么都没有留下...");
        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());
        usersMapper.insert(user);
        return user;
    }

    @Override
    public Users getUser(String userId) {
        return usersMapper.selectByPrimaryKey(userId);
    }
    @Transactional
    @Override
    public Users UpdateUserInfo(UpdatedUserBo userBo) {
        Users user = new Users();
        BeanUtils.copyProperties(userBo,user);
        //设置更新内容
        int result = usersMapper.updateByPrimaryKeySelective(user);
        //如果更新失败
        if (result!=1){
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }
        return getUser(userBo.getId());
    }

    @Override
    public Users UpdateUserInfo(UpdatedUserBo userBo, Integer type) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        if (type == UserInfoModifyType.NICKNAME.type){
            criteria.andEqualTo("nickname",userBo.getNickname());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }
        if (type == UserInfoModifyType.IMOOCNUM.type){
            criteria.andEqualTo("imoocNum",userBo.getImoocNum());
            Users user = usersMapper.selectOneByExample(example);
            if (user != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_IMOOCNUM_EXIST_ERROR);
            }
            Users u = getUser(userBo.getId());
            if (u.getCanImoocNumBeUpdated() == YesOrNo.NO.type){
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_IMOOCNUM_ERROR);
            }
            userBo.setCanImoocNumBeUpdated(YesOrNo.NO.type);
        }
        return UpdateUserInfo(userBo);
    }
}
