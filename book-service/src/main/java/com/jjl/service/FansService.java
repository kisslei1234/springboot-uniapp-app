package com.jjl.service;

import com.github.pagehelper.PageHelper;
import com.jjl.base.Baseinfo;
import com.jjl.pojo.Fans;
import com.jjl.pojo.Vlog;
import com.jjl.utils.PagedGridResult;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public interface FansService {
    public void doFollow(String myId,String vlogerId);
    public Fans isFollow(String fansId, String vlogerId);
    public void doCancel(String myId,String vlogerId);
    //查询我是否关注了这个用户
    Boolean queryDoIFollow(String myId, String vlogerId);
    public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize);

    PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize);

}
