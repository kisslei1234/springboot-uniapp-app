package com.jjl.Controller;

import com.jjl.base.Baseinfo;
import com.jjl.bo.RedisLoginBO;
import com.jjl.grace.result.GraceJSONResult;
import com.jjl.grace.result.ResponseStatusEnum;
import com.jjl.pojo.Users;
import com.jjl.service.UserService;
import com.jjl.utils.IPUtil;
import com.jjl.utils.ValidateCodeUtils;
import com.jjl.vo.UsersVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("passport")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PassportController extends Baseinfo {
    @Autowired
    private UserService userService;

    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile, HttpServletRequest request) {
        if(StringUtils.isEmpty(mobile)) {
            return GraceJSONResult.ok();
        }
        //TODO 获得ip限制，防止恶意刷短信验证码，在六十秒内智能获得一次验证码
        String userIp = IPUtil.getRequestIp(request);
        redisOperator.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);
        // 生成随机验证码并且发送短信
        String code = String.valueOf(ValidateCodeUtils.generateValidateCode(6));
        log.info("生成的随机验证码为：{}", code);
        //TODO 把验证码存入到redis中
        redisOperator.set(MOBILE_SMSCODE + ":" + mobile, code,60*30);
        return GraceJSONResult.ok();
    }
    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody RedisLoginBO redisLoginBO, HttpServletRequest request){
        String mobile = redisLoginBO.getMobile();
        String smsCode = redisLoginBO.getSmsCode();
        //从redis中获得验证码进行校验是否匹配
        String rediscode = redisOperator.get(MOBILE_SMSCODE+":"+mobile);
        if (StringUtils.isEmpty(rediscode) || !rediscode.equalsIgnoreCase(smsCode)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        //查询数据库
        Users user = userService.queryMobileIsExist(mobile);
        //如果数据库中不存在，直接注册
        if(user == null) {
                user = userService.createUser(mobile);
        }
        //如果不为空，继续下方业务
        String utoken = UUID.randomUUID().toString();
        redisOperator.set(REDIS_USER_TOKEN+":"+user.getId(),utoken,60*30);
        //用户登录注册成功后，删除redis的短信验证码
        redisOperator.del(MOBILE_SMSCODE+":"+mobile);
        //返回用户信息，包含token
        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(user,usersVo);
        usersVo.setUserToken(utoken);
        return GraceJSONResult.ok(usersVo);
    }
    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId,HttpServletRequest request){
        //后端只需要清除redis的token
        redisOperator.del(REDIS_USER_TOKEN+":"+userId);
        return GraceJSONResult.ok();
    }
}
