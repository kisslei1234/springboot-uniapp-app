package com.jjl.serviceImpl;

import com.github.pagehelper.PageHelper;
import com.jjl.base.Baseinfo;
import com.jjl.base.RabbitMQConfig;
import com.jjl.bo.CommentBO;
import com.jjl.enums.MessageEnum;
import com.jjl.enums.YesOrNo;
import com.jjl.mapper.CommentMapper;
import com.jjl.mapper.CommentMapperCustom;
import com.jjl.mo.MessageMO;
import com.jjl.pojo.Comment;
import com.jjl.service.CommentService;
import com.jjl.service.MsgService;
import com.jjl.service.VlogService;
import com.jjl.utils.JsonUtils;
import com.jjl.utils.PagedGridResult;
import com.jjl.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends Baseinfo implements CommentService {
    @Autowired
    private Sid sid;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private CommentMapperCustom commentMapperCustom;
    @Autowired
    private MsgService msgService;
    @Autowired
    private VlogService vlogService;
    @Override
    public CommentVO createComment(CommentBO commentBO) {

        String id = sid.nextShort();
        Comment comment = new Comment();
        comment.setId(id);
        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());
        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());
        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());
        commentMapper.insert(comment);
        //redis操作放在service中，评论总数的累加
        redisOperator.increment(REDIS_VLOG_COMMENT_COUNTS+":"+commentBO.getVlogId(),1);
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment,commentVO);
        Integer type = MessageEnum.COMMENT_VLOG.type;
        String RouteKeyValue = MessageEnum.COMMENT_VLOG.enValue;
        if (StringUtils.isNotBlank(commentBO.getFatherCommentId())&&!commentBO.getFatherCommentId().equalsIgnoreCase("0")){
           type = MessageEnum.REPLY_YOU.type;
           RouteKeyValue = MessageEnum.REPLY_YOU.enValue;
        }
        Map content = new HashMap<>();
        content.put("commentId",commentBO.getCommentUserId());
        content.put("commentContent",commentBO.getContent());
        content.put("vlogId",commentBO.getVlogId());
        content.put("vlogCover",vlogService.getvlog(commentBO.getVlogId()).getCover());
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(commentBO.getCommentUserId());
        messageMO.setToUserId(commentBO.getVlogerId());
        messageMO.setMsgContent(content);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,"sys.msg."+RouteKeyValue, JsonUtils.objectToJson(messageMO));
//        msgService.createMsg(commentBO.getCommentUserId(),commentBO.getVlogerId(), type,content);
        return commentVO;

    }

    @Override
    public PagedGridResult queryCommentList(String vlogId, String userId,Integer page, Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("vlogId",vlogId);
        PageHelper.startPage(page,pageSize);
        List<CommentVO> list = commentMapperCustom.getCommentList(map);
        for (CommentVO cv : list){
            String commentId = cv.getCommentId();
            //当前短视频某个评论的点赞总数
            String countsStr = redisOperator.getHashValue(REDIS_VLOG_COMMENT_LIKE_COUNTS, commentId);
            Integer counts = 0;
            if (StringUtils.isNotBlank(countsStr)){
                counts = Integer.valueOf(countsStr);
            }
            cv.setLikeCounts(counts);
            //当前用户是否点赞过该评论
            String isLike = redisOperator.getHashValue(REDIS_USER_LIKE_COMMENT, userId+":"+commentId );
            if (StringUtils.isNotBlank(isLike)&&isLike.equals("1")){
                cv.setIsLike(YesOrNo.YES.type);
        }
      }
        return setterPagedGrid(list,page);
    }
    @Override
    public void deleteComment(String commentUserId, String commentId, String vlogId) {
        Comment comment = new Comment();
        comment.setId(commentId);
        commentMapper.deleteByPrimaryKey(comment);
        //redis操作放在service中，评论总数的累加
        redisOperator.decrement(REDIS_VLOG_COMMENT_COUNTS+":"+vlogId,1);
    }

    @Override
    public Comment getComment(String id) {
        return commentMapper.selectByPrimaryKey(id);
    }
}



