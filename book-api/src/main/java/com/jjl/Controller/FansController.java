package com.jjl.Controller;

import com.jjl.base.Baseinfo;
import com.jjl.bo.VlogBo;
import com.jjl.enums.YesOrNo;
import com.jjl.grace.result.GraceJSONResult;
import com.jjl.grace.result.ResponseStatusEnum;
import com.jjl.pojo.Users;
import com.jjl.service.FansService;
import com.jjl.service.MsgService;
import com.jjl.service.UserService;
import com.jjl.service.VlogService;
import com.jjl.utils.PagedGridResult;
import com.jjl.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api
@RequestMapping("/fans")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FansController extends Baseinfo {

    @Autowired
    private MsgService msgService;
    @Autowired
    private FansService fansService;
    @Autowired
    private UserService userService;

    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId,@RequestParam String vlogerId){
        if (StringUtils.isBlank(myId)||StringUtils.isBlank(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //判断当前用户，自己不能关注自己
        if (myId.equals(vlogerId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }
        //判断两个id对应的用户是否存在
        Users user = userService.getUser(myId);
        Users vloger = userService.getUser(vlogerId);
        if ( user == null || vloger == null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }
        //保存粉丝关系到数据库
        fansService.doFollow(myId,vlogerId);
        //博主的粉丝数增加，我的关注数增加
        redisOperator.increment(REDIS_MY_FOLLOWS_COUNTS+":"+myId,1);
        redisOperator.increment(REDIS_MY_FANS_COUNTS+":"+vlogerId,1);
        //我和博主的关注关系放入redis
        redisOperator.set(REDIS_FANS_AND_VLOGER_RELATIONSHIP+":"+myId+":"+vlogerId, String.valueOf(YesOrNo.YES.type));
        return GraceJSONResult.ok();
    }
    @PostMapping("cancel")
    public GraceJSONResult cancel(@RequestParam String myId,@RequestParam String vlogerId){
        fansService.doCancel(myId,vlogerId);
        redisOperator.decrement(REDIS_MY_FANS_COUNTS+":"+vlogerId,1);
        redisOperator.decrement(REDIS_MY_FOLLOWS_COUNTS+":"+myId,1);
        redisOperator.del(REDIS_FANS_AND_VLOGER_RELATIONSHIP+":"+myId+":"+vlogerId);
        return GraceJSONResult.ok();
    }
    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId,@RequestParam String vlogerId){
        boolean flag = fansService.queryDoIFollow(myId,vlogerId);
        return GraceJSONResult.ok(flag);
    }
    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId,@RequestParam Integer page,@RequestParam Integer pageSize){
        return GraceJSONResult.ok(fansService.queryMyFollows(myId,page,pageSize));
    }
    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId,@RequestParam Integer page,@RequestParam Integer pageSize){
        return GraceJSONResult.ok(fansService.queryMyFans(myId,page,pageSize));
    }
}
