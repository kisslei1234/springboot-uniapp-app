package com.jjl.serviceImpl;

import com.github.pagehelper.PageHelper;
import com.jjl.base.Baseinfo;
import com.jjl.base.RabbitMQConfig;
import com.jjl.bo.VlogBo;
import com.jjl.enums.MessageEnum;
import com.jjl.enums.YesOrNo;
import com.jjl.mapper.MyLikedVlogMapper;
import com.jjl.mapper.VlogMapper;
import com.jjl.mapper.VlogMapperCustom;
import com.jjl.mo.MessageMO;
import com.jjl.pojo.MyLikedVlog;
import com.jjl.pojo.Vlog;
import com.jjl.service.FansService;
import com.jjl.service.MsgService;
import com.jjl.service.VlogService;
import com.jjl.utils.JsonUtils;
import com.jjl.utils.PagedGridResult;
import com.jjl.utils.RedisOperator;
import com.jjl.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jjl.base.Baseinfo.*;

@Service
public class VlogServiceImpl extends Baseinfo implements VlogService {
    @Autowired
    private FansService fansService;
    @Autowired
    private RedisOperator redisOperator;
    @Autowired
    private VlogMapper vlogMapper;
    @Autowired
    private VlogMapperCustom vlogMapperCustom;
    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;
    @Autowired
    private MsgService msgService;
    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public void createvlog(VlogBo vlogBo) {
        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBo,vlog);
        String vid = sid.nextShort();
        vlog.setId(vid);
        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);
        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());
        vlogMapper.insertSelective(vlog);
    }

    @Override
    public PagedGridResult getIndexVlog(String search, Integer page, Integer pageSize,String userId) {
        PageHelper.startPage(page,pageSize);
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(search)){
            map.put("search",search);
        }
        List<IndexVlogVO> indexVlogList = vlogMapperCustom.getIndexVlogList(map);
        for (IndexVlogVO v : indexVlogList){
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();
            if (StringUtils.isNotBlank(userId)){
                v.setDoILikeThisVlog(doILikeVlog(userId,vlogId));
            }
            if (StringUtils.isNotBlank(vlogerId)){
                v.setDoIFollowVloger(doIFollowVloger(userId,vlogerId));
            }
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }
        return Baseinfo.setterPagedGrid(indexVlogList,page);
    }
    @Override
    public Integer getVlogBeLikedCounts(String vlogId){
        String counts = redisOperator.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(counts)){
            return 0;
        }
        return Integer.valueOf(counts);
    }

    @Override
    public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {

        PageHelper.startPage(page,pageSize);
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        List<IndexVlogVO> indexVlogVOList = vlogMapperCustom.getMyLikedVlogList(map);
        return setterPagedGrid(indexVlogVOList,page);
    }

    @Override
    public PagedGridResult getMyFollowVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        Map<String,Object> map = new HashMap<>();
        map.put("myId",userId);
        List<IndexVlogVO> indexVlogVOList = vlogMapperCustom.getMyFollowVlogList(map);
        for (IndexVlogVO v : indexVlogVOList){
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();
            if (StringUtils.isNotBlank(userId)){
                v.setDoILikeThisVlog(doILikeVlog(userId,vlogId));
            }
            v.setDoIFollowVloger(true);
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }
        return setterPagedGrid(indexVlogVOList,page);
    }

    @Override
    public PagedGridResult getMyFriendVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        Map<String,Object> map = new HashMap<>();
        map.put("myId",userId);
        List<IndexVlogVO> indexVlogVOList = vlogMapperCustom.getMyFriendVlogList(map);
        for (IndexVlogVO v : indexVlogVOList){
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();
            if (StringUtils.isNotBlank(userId)){
                v.setDoILikeThisVlog(doILikeVlog(userId,vlogId));
            }
            v.setDoIFollowVloger(true);
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }
        return setterPagedGrid(indexVlogVOList,page);
    }
    @Transactional
    @Override
    public void flushLikeCounts(String vlogId, Integer counts) {
        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);
        vlogMapper.updateByPrimaryKeySelective(vlog);
    }

    public Boolean doILikeVlog(String myId,String vlogId){
        String like = redisOperator.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);
        if (StringUtils.isNotBlank(like)){
            return true;
        }
        return false;
    }
    private Boolean doIFollowVloger(String myId,String vlogerId){
        return fansService.queryDoIFollow(myId, vlogerId);
    }
    @Override
    public IndexVlogVO getVlogDetailById(String userId, String vlogId) {
        Map<String,Object> map = new HashMap<>();
        map.put("vlogId",vlogId);
        List<IndexVlogVO> indexVlogVOList = vlogMapperCustom.getVlogDetailById(map);
        if (indexVlogVOList != null&&indexVlogVOList.size()>0&&!indexVlogVOList.isEmpty()){
            IndexVlogVO indexVlogVO = indexVlogVOList.get(0);
            indexVlogVO.setDoILikeThisVlog(doILikeVlog(userId,vlogId));
            indexVlogVO.setDoIFollowVloger(doIFollowVloger(userId,indexVlogVO.getVlogerId()));
            indexVlogVO.setLikeCounts(getVlogBeLikedCounts(vlogId));
            return indexVlogVO;
        }
        return null;
    }
    @Transactional
    @Override
    public void changeToPrivateOrPublic(String userId, String vlogId, Integer isPrivate) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId",userId);
        criteria.andEqualTo("id",vlogId);
        Vlog vlog = new Vlog();
        vlog.setIsPrivate(isPrivate);
        vlogMapper.updateByExampleSelective(vlog,example);
    }

    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer YesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId",userId);
        criteria.andEqualTo("isPrivate",YesOrNo);
        PageHelper.startPage(page,pageSize);
        List<Vlog> vlogList = vlogMapper.selectByExample(example);
        return Baseinfo.setterPagedGrid(vlogList,page);
    }
    @Transactional
    @Override
    public void UserLikeVlog(String userId, String vlogId) {
        String id = sid.nextShort();
        MyLikedVlog myLikeVlog = new MyLikedVlog();
        myLikeVlog.setId(id);
        myLikeVlog.setVlogId(vlogId);
        myLikeVlog.setUserId(userId);
        myLikedVlogMapper.insert(myLikeVlog);
        //把点赞消息存储到mongodb
        //TODO：用mq解耦
        Vlog vlog = getvlog(vlogId);
        Map msgContent = new HashMap<>();
        msgContent.put("vlogId",vlogId);
        msgContent.put("vlogCover",vlog.getCover());
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userId);
        messageMO.setToUserId(vlog.getVlogerId());
        messageMO.setMsgContent(msgContent);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,"sys.msg."+ MessageEnum.LIKE_VLOG.enValue, JsonUtils.objectToJson(messageMO));
//        msgService.createMsg(userId,vlogId, MessageEnum.LIKE_VLOG.type,msgContent);
    }
    @Override
    public Vlog getvlog(String id){
        return vlogMapper.selectByPrimaryKey(id);
    }
    @Transactional
    @Override
    public void UserUnLikeVlog(String userId, String vlogId) {
        Example example = new Example(MyLikedVlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        criteria.andEqualTo("vlogId",vlogId);
        myLikedVlogMapper.deleteByExample(example);
    }
}
