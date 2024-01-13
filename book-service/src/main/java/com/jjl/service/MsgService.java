package com.jjl.service;

import com.jjl.mo.MessageMO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
public interface MsgService {
    public void createMsg(String fromUserId, String toUserId, Integer msgType, Map msgContent);
    public List<MessageMO> queryList(String toUserId,Integer page,Integer pageSize);

}
