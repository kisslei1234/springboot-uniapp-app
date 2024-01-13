package com.jjl.intercepter;
import com.jjl.base.Baseinfo;
import com.jjl.exceptions.GraceException;
import com.jjl.grace.result.ResponseStatusEnum;
import com.jjl.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Slf4j
public class PassportIntercepter extends Baseinfo implements HandlerInterceptor {
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        Long count = redisTemplate.opsForZSet().count("phb", currentTimeMillis - 5 * 60 * 1000, currentTimeMillis);
        if(count > 3){
            return false;
        }
        //获得用户ip
        String userip = IPUtil.getRequestIp(request);
        boolean keyIsExist = redisOperator.keyIsExist(MOBILE_SMSCODE + ":" + userip);
        //如果一分钟内访问超过一次，则不再放行
        if(keyIsExist) {
            //抛出异常，不能再继续操作
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            log.info("短信发送过于频繁，访问ip为：{}", userip);
            return false;
        }
        //请求放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
