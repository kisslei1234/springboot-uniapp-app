package com.jjl.Controller;

import com.jjl.base.Baseinfo;
import com.jjl.bo.VlogBo;
import com.jjl.enums.YesOrNo;
import com.jjl.grace.result.GraceJSONResult;
import com.jjl.service.VlogService;
import com.jjl.utils.PagedGridResult;
import com.jjl.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
@RefreshScope
@RestController
@Api
@RequestMapping("/vlog")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class VlogController extends Baseinfo {
    @Value("${nacos.counts}")
    private Integer nacosCounts;
    @Autowired
    private VlogService vlogService;

    @PostMapping("publish")
    public GraceJSONResult publish(@RequestBody VlogBo vlogBo) {
        vlogService.createvlog(vlogBo);
        return GraceJSONResult.ok();
    }
    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String search,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize,
                                     @RequestParam String userId){
        if (page == null){
            page = COMMON_START_PAGE;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        return GraceJSONResult.ok(vlogService.getIndexVlog(search,page,pageSize,userId));
    }
    @GetMapping("detail")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "") String userId,
                                     @RequestParam String vlogId){
        IndexVlogVO indexVlogVO = vlogService.getVlogDetailById(userId,vlogId);
        return GraceJSONResult.ok(indexVlogVO);
    }
    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                                   @RequestParam String vlogId){
        vlogService.changeToPrivateOrPublic(userId,vlogId, YesOrNo.YES.type);
        return GraceJSONResult.ok();
    }
    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                                   @RequestParam String vlogId){
        vlogService.changeToPrivateOrPublic(userId,vlogId, YesOrNo.NO.type);
        return GraceJSONResult.ok();
    }
    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                        @RequestParam(defaultValue = "") Integer page,
                                        @RequestParam(defaultValue = "") Integer pageSize){
        if (page == null){
            page = COMMON_START_PAGE;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId,page,pageSize,YesOrNo.NO.type);
        return GraceJSONResult.ok(pagedGridResult);
    }
    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                        @RequestParam(defaultValue = "") Integer page,
                                        @RequestParam(defaultValue = "") Integer pageSize){
        if (page == null){
            page = COMMON_START_PAGE;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = vlogService.queryMyVlogList(userId,page,pageSize,YesOrNo.YES.type);
        return GraceJSONResult.ok(pagedGridResult);
    }
    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                        @RequestParam(defaultValue = "") Integer page,
                                        @RequestParam(defaultValue = "") Integer pageSize){
        if (page == null){
            page = COMMON_START_PAGE;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = vlogService.getMyLikedVlogList(userId,page,pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }
    @GetMapping("followList")
    public GraceJSONResult followList(@RequestParam String myId,
                                       @RequestParam(defaultValue = "") Integer page,
                                       @RequestParam(defaultValue = "") Integer pageSize){
        if (page == null){
            page = COMMON_START_PAGE;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult pagedGridResult = vlogService.getMyFollowVlogList(myId,page,pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }
    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam String myId,
                                      @RequestParam(defaultValue = "") Integer page,
                                      @RequestParam(defaultValue = "") Integer pageSize){
        if (page == null){
            page = COMMON_START_PAGE;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult myFriendVlogList = vlogService.getMyFriendVlogList(myId, page, pageSize);
        return GraceJSONResult.ok(myFriendVlogList);
    }
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,@RequestParam String vlogId,@RequestParam String vlogerId){
        vlogService.UserLikeVlog(userId,vlogId);
        //点赞后，视频和视频发布者的获赞都会+1
        redisOperator.increment(REDIS_VLOG_BE_LIKED_COUNTS+":"+vlogId,1);
        redisOperator.increment(REDIS_VLOGER_BE_LIKED_COUNTS+":"+vlogerId,1);
        redisOperator.set(REDIS_USER_LIKE_VLOG+":"+userId+":"+vlogId,"1");
        //点赞完毕，获得点赞数
        //假定阈值为100，当点赞数达到100时，发送消息给视频发布者
        String countsStr = redisOperator.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        log.info("==============================当前视频点赞数为：{}",countsStr);
        Integer oldCounts = 0;
        Integer counts = 0;
        if (StringUtils.isNotBlank(countsStr)){
            counts = Integer.valueOf(countsStr);
            if (counts-oldCounts>=nacosCounts){
                vlogService.flushLikeCounts(vlogerId,counts);
                oldCounts = counts;
            }
        }
        return GraceJSONResult.ok();
    }
    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,@RequestParam String vlogId,@RequestParam String vlogerId){
        vlogService.UserUnLikeVlog(userId,vlogId);
        //取消点赞后，视频和视频发布者的获赞都会-1
        redisOperator.decrement(REDIS_VLOG_BE_LIKED_COUNTS+":"+vlogId,1);
        redisOperator.decrement(REDIS_VLOGER_BE_LIKED_COUNTS+":"+vlogerId,1);
        redisOperator.del(REDIS_USER_LIKE_VLOG+":"+userId+":"+vlogId);
        return GraceJSONResult.ok();
    }
    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId){
        return GraceJSONResult.ok(vlogService.getVlogBeLikedCounts(vlogId));
    }

}
