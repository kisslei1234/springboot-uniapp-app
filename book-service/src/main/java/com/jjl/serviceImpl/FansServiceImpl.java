package com.jjl.serviceImpl;

import com.github.pagehelper.PageHelper;
import com.jjl.base.Baseinfo;
import com.jjl.base.RabbitMQConfig;
import com.jjl.enums.MessageEnum;
import com.jjl.enums.YesOrNo;
import com.jjl.mapper.FansMapper;
import com.jjl.mapper.FansMapperCustom;
import com.jjl.mo.MessageMO;
import com.jjl.pojo.Fans;
import com.jjl.service.FansService;
import com.jjl.service.MsgService;
import com.jjl.utils.JsonUtils;
import com.jjl.utils.PagedGridResult;
import com.jjl.utils.RedisOperator;
import com.jjl.vo.MyFansVO;
import com.jjl.vo.VlogerVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static com.jjl.enums.MessageEnum.FOLLOW_YOU;

@Service
public class FansServiceImpl extends Baseinfo implements FansService {

    @Autowired
    private FansMapper fansMapper;
    @Autowired
    private FansMapperCustom fansMapperCustom;
    @Autowired
    private Sid sid;
    @Autowired
    private RedisOperator redisOperator;
    @Transactional
    @Override
    public void doFollow(String myId, String vlogerId) {
        String id = sid.nextShort();
        Fans fans = new Fans();
        fans.setId(id);
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);
        fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        Fans follow = isFollow(vlogerId, myId);
        if (follow!=null){
            follow.setIsFanFriendOfMine(YesOrNo.YES.type);
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKeySelective(follow);
        }
        fansMapper.insert(fans);
//        msgService.createMsg(myId,vlogerId, FOLLOW_YOU.type,null);
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(myId);
        messageMO.setToUserId(vlogerId);
        //使用mq
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg."+FOLLOW_YOU.enValue,
                Objects.requireNonNull(JsonUtils.objectToJson(messageMO)));
    }

    @Override
    public Fans isFollow(String fansId, String vlogerId) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId",vlogerId);
        criteria.andEqualTo("fanId",fansId);
        List<Fans> fans = fansMapper.selectByExample(example);
        Fans fan = null;
        if (fans != null && fans.size()>0&&!fans.isEmpty()){
            fan = fans.get(0);
        }
        return fan;
    }
    @Override
    public void doCancel(String myId,String vlogerId){
        Fans follow = isFollow(myId, vlogerId);
        Fans pendingFan = isFollow(vlogerId, myId);
        if (pendingFan!=null){
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(pendingFan);
        }
        fansMapper.delete(follow);
    }

    @Override
    public Boolean queryDoIFollow(String myId, String vlogerId) {
        Fans follow = isFollow(myId, vlogerId);
        return follow != null;
    }

    @Override
    public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("myId",myId);
        List<VlogerVO> vlogerVOS = fansMapperCustom.queryMyFollows(map);
        PageHelper.startPage(page,pageSize);
        return setterPagedGrid(vlogerVOS,page);
    }

    @Override
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("myId",myId);
        List<MyFansVO> myFansVOS = fansMapperCustom.queryMyFans(map);
        PageHelper.startPage(page,pageSize);
        for(MyFansVO f : myFansVOS){
            String s = redisOperator.get(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + myId + ":" + f.getFanId());
            if (StringUtils.isNotBlank(s)&&s.equalsIgnoreCase("1")){
                f.setFriend(true);
            }
        }
        return setterPagedGrid(myFansVOS,page);
    }

}
