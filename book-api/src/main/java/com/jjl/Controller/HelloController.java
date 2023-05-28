package com.jjl.Controller;

import com.jjl.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api( tags = {"测试接口"})
public class HelloController {
    @ApiOperation(value = "这是一个hello的测试路由")
    @GetMapping("hello")
    public Object hello() {
        return GraceJSONResult.ok("hello");
    }
}
