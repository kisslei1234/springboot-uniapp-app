package com.jjl.service;

import com.jjl.bo.CommentBO;
import com.jjl.pojo.Comment;
import com.jjl.pojo.Fans;
import com.jjl.utils.PagedGridResult;
import com.jjl.vo.CommentVO;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {
public CommentVO createComment(CommentBO commentBO);
public PagedGridResult queryCommentList(String vlogId,String userId, Integer page, Integer pageSize);
public void deleteComment(String commentUserId, String commentId, String vlogId);
public Comment getComment(String id);
}
