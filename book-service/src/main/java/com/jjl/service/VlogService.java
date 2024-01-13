package com.jjl.service;

import com.jjl.bo.VlogBo;
import com.jjl.pojo.Fans;
import com.jjl.pojo.Vlog;
import com.jjl.utils.PagedGridResult;
import com.jjl.vo.IndexVlogVO;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface VlogService {
    public Vlog getvlog(String id);
    public void createvlog(VlogBo vlogBo);
    public PagedGridResult getIndexVlog(String search, Integer page, Integer pageSize,String userId);
    public IndexVlogVO getVlogDetailById(String userId,String vlogId);
    public void  changeToPrivateOrPublic(String userId,String vlogId,Integer isPrivate);
    public PagedGridResult queryMyVlogList(String userId,Integer page,Integer pageSize,Integer YesOrNo);
    public void UserLikeVlog(String userId,String vlogId);
    public void UserUnLikeVlog(String userId,String vlogId);
    public Integer getVlogBeLikedCounts(String vlogId);
    public PagedGridResult getMyLikedVlogList(String userId,Integer page,Integer pageSize);
    public PagedGridResult getMyFollowVlogList(String userId,Integer page,Integer pageSize);
    public PagedGridResult getMyFriendVlogList(String userId,Integer page,Integer pageSize);
    public void flushLikeCounts(String vlogId,Integer counts);
}
