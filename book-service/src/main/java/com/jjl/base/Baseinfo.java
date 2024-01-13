package com.jjl.base;

import com.github.pagehelper.PageInfo;
import com.jjl.utils.PagedGridResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.jjl.utils.RedisOperator;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class Baseinfo {
    @Autowired
    public RabbitTemplate rabbitTemplate;
    @Autowired
    public RedisOperator redisOperator;
    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer COMMON_PAGE_SIZE_ZERO = 0;
    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";
    // 我的关注总数
    public static final String REDIS_MY_FOLLOWS_COUNTS = "redis_my_follows_counts";
    // 我的粉丝总数
    public static final String REDIS_MY_FANS_COUNTS = "redis_my_fans_counts";
    //博主和粉丝的关联关系，用于判断他们是否互粉
    public static final String REDIS_FANS_AND_VLOGER_RELATIONSHIP = "redis_fans_and_vloger_relationship";
    // 视频和发布者获赞数
    public static final String REDIS_VLOG_BE_LIKED_COUNTS = "redis_vlog_be_liked_counts";
    public static final String REDIS_VLOGER_BE_LIKED_COUNTS = "redis_vloger_be_liked_counts";
    public static final String REDIS_USER_LIKE_VLOG = "redis_user_like_vlog";
    public static final String REDIS_VLOG_COMMENT_COUNTS = "redis_vlog_comment_counts";
    public static final String REDIS_VLOG_COMMENT_LIKE_COUNTS = "redis_vlog_comment_like_counts";
    public static final String REDIS_USER_LIKE_COMMENT = "redis_user_like_comment";
    public static PagedGridResult setterPagedGrid(List<?> list,
                                                  Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(page);
        gridResult.setRecords(pageList.getTotal());
        gridResult.setTotal(pageList.getPages());
        return gridResult;
    }
}
