package com.jjl.Controller;

import com.jjl.base.Baseinfo;
import com.jjl.grace.result.GraceJSONResult;
import com.jjl.mo.MessageMO;
import com.jjl.service.MsgService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api
@Slf4j
@RestController
@RequestMapping("msg")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MsgController extends Baseinfo {
    @Autowired
    private MsgService msgService;
    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize){
        if (page == null){
            page = COMMON_PAGE_SIZE_ZERO;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }
        List<MessageMO> messageMOS = msgService.queryList(userId, page, pageSize);
        return GraceJSONResult.ok(messageMOS);
    }
}
