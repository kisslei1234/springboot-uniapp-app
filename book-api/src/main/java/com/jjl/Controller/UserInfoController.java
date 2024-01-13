package com.jjl.Controller;

import com.jjl.base.Baseinfo;
import com.jjl.bo.UpdatedUserBo;
import com.jjl.config.MinIOConfig;
import com.jjl.enums.FileTypeEnum;
import com.jjl.enums.UserInfoModifyType;
import com.jjl.grace.result.GraceJSONResult;
import com.jjl.grace.result.ResponseStatusEnum;
import com.jjl.pojo.Users;
import com.jjl.service.UserService;
import com.jjl.utils.MinIOUtils;
import com.jjl.vo.UsersVo;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Api("用户信息相关")
@RestController
@RequestMapping("/userInfo")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserInfoController extends Baseinfo {
    @Autowired
    private UserService userService;
    @Autowired
    private MinIOConfig minIOConfig;
    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId){
        Users user = userService.getUser(userId);
        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(user,usersVo);
        //我的关注博主总数量
        String myfollows = redisOperator.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        //我的粉丝总数
        String myfans = redisOperator.get(REDIS_MY_FANS_COUNTS + ":" + userId);

        //用户获赞总数，视频+评论综合
        String likedVlogCountsStr = redisOperator.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String likedVlogerCountsStr = redisOperator.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);
        Integer follows = 0;
        Integer fans = 0;
        Integer totalLikeMeCounts = 0;
        Integer likedVlogCounts = 0;
        Integer likedVlogerCounts = 0;
        if (StringUtils.isNotBlank(myfollows)){
            follows = Integer.valueOf(myfollows);
        }
        if (StringUtils.isNotBlank(myfans)){
            fans = Integer.valueOf(myfans);
        }
        if (StringUtils.isNotBlank(likedVlogCountsStr)){
            likedVlogCounts = Integer.valueOf(likedVlogCountsStr);
        }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)){
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }
        totalLikeMeCounts = likedVlogCounts + likedVlogerCounts;
        usersVo.setMyFansCounts(fans);
        usersVo.setMyFollowsCounts(follows);
        usersVo.setMyLikedVlogerCounts(likedVlogerCounts);
        usersVo.setTotalLikeMeCounts(totalLikeMeCounts);
        return GraceJSONResult.ok(usersVo);
    }
    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBo user,@RequestParam Integer type){
        UserInfoModifyType.checkUserInfoTypeIsRight(type);
        Users updateUserInfo = userService.UpdateUserInfo(user,type);
        return GraceJSONResult.ok(updateUserInfo);
    }
    @PostMapping("modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId, @RequestParam Integer type, MultipartFile file) throws Exception {
        if(type != FileTypeEnum.BGIMG.type&&type != FileTypeEnum.FACE.type){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        String originalFilename = file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),originalFilename,file.getInputStream());
        String imgurl = minIOConfig.getFileHost()+"/"+minIOConfig.getBucketName()+"/"+originalFilename;
        UpdatedUserBo userBo = new UpdatedUserBo();
        userBo.setId(userId);
        if (type == FileTypeEnum.BGIMG.type){
            userBo.setBgImg(imgurl);}
        else if (type == FileTypeEnum.FACE.type){
            userBo.setFace(imgurl);
        }
        Users users = userService.UpdateUserInfo(userBo);
        return GraceJSONResult.ok(users);
    }
}
