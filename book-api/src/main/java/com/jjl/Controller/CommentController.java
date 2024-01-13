package com.jjl.Controller;

import com.jjl.base.Baseinfo;
import com.jjl.bo.CommentBO;
import com.jjl.enums.MessageEnum;
import com.jjl.grace.result.GraceJSONResult;
import com.jjl.pojo.Comment;
import com.jjl.service.CommentService;
import com.jjl.service.MsgService;
import com.jjl.service.VlogService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Api
@Slf4j
@RequestMapping("comment")
public class CommentController extends Baseinfo {

    @Autowired
    private VlogService vlogService;
    @Autowired
    private MsgService msgService;
    @Autowired
    private CommentService commentService;
    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO) throws Exception {
        return GraceJSONResult.ok(commentService.createComment(commentBO));
    }
    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) {
        String counts = redisOperator.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(counts)){
            return GraceJSONResult.ok(0);
        }
        return GraceJSONResult.ok(Integer.valueOf(counts));
    }
    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId,
                                @RequestParam(defaultValue = "") String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(commentService.queryCommentList(vlogId,userId,page,pageSize));
    }
    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String commentUserId,
                                  @RequestParam String commentId,
                                  @RequestParam String vlogId) {
        commentService.deleteComment(commentUserId,commentId,vlogId);
        return GraceJSONResult.ok();
    }
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                @RequestParam String commentId) {
        //bigkey
        redisOperator.incrementHash(REDIS_VLOG_COMMENT_LIKE_COUNTS,commentId,1);
        redisOperator.setHashValue(REDIS_USER_LIKE_COMMENT,userId+":"+commentId,"1");
        Comment comment = commentService.getComment(commentId);
        Map content = new HashMap<>();
        content.put("commentId",commentId);
        content.put("vlogId",comment.getVlogId());
        content.put("vlogCover",vlogService.getvlog(comment.getVlogId()).getCover());
        msgService.createMsg(userId,comment.getCommentUserId(), MessageEnum.LIKE_COMMENT.type, content);
        return GraceJSONResult.ok();
    }
    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                @RequestParam String commentId) {
        //bigkey
        redisOperator.decrementHash(REDIS_VLOG_COMMENT_LIKE_COUNTS,commentId,1);
        redisOperator.hdel(REDIS_USER_LIKE_COMMENT,userId+":"+commentId);
        return GraceJSONResult.ok();
    }

}

