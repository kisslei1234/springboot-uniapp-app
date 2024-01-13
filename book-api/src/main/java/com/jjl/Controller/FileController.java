package com.jjl.Controller;

import com.jjl.base.Baseinfo;
import com.jjl.config.MinIOConfig;
import com.jjl.grace.result.GraceJSONResult;
import com.jjl.utils.MinIOUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@Api("文件上传")
public class FileController extends Baseinfo {
    @Autowired
    private MinIOConfig minIOConfig;
    @PostMapping("upload")
    public GraceJSONResult upload(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),originalFilename,file.getInputStream());
        String imgurl =minIOConfig.getFileHost()+"/"+minIOConfig.getBucketName()+"/"+originalFilename;
        return GraceJSONResult.ok(imgurl);
    }
}
