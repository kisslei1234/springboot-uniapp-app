package com.jjl.serviceImpl;

import com.jjl.base.Baseinfo;
import com.jjl.enums.MessageEnum;
import com.jjl.mo.MessageMO;
import com.jjl.pojo.Users;
import com.jjl.repository.MessageRepository;
import com.jjl.service.MsgService;
import com.jjl.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class MsgServiceImpl extends Baseinfo implements MsgService {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserService userService;
    @Override
    public void createMsg(String fromUserId, String toUserId, Integer msgType, Map msgContent) {
        MessageMO messageMO = new MessageMO();
        Users user = userService.getUser(fromUserId);
        messageMO.setFromUserId(fromUserId);
        messageMO.setFromNickname(user.getNickname());
        messageMO.setFromFace(user.getFace());
        messageMO.setToUserId(toUserId);
        messageMO.setMsgType(msgType);
        if (msgContent != null){
            messageMO.setMsgContent(msgContent);
        }
        messageMO.setCreateTime(new Date());
        messageRepository.save(messageMO);
    }

    @Override
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.DESC, "createTime");
        List<MessageMO> list = messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toUserId,pageable);
        for (MessageMO msg : list){
            if (msg.getMsgType() == MessageEnum.FOLLOW_YOU.type){
                Map msgContent = msg.getMsgContent();
                if (msgContent == null){
                    msgContent = new HashMap<>();
                }
                String relationship = redisOperator.get(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
                if (StringUtils.isNotBlank(relationship)&&relationship.equals("1")){
                    msgContent.put("isFriend",true);
                }else {
                    msgContent.put("isFriend",false);
                }
                msg.setMsgContent(msgContent);
            }
        }
        return list;
    }
}
