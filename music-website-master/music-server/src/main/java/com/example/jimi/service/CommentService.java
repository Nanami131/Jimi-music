package com.example.jimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jimi.common.R;
import com.example.jimi.model.domain.Comment;
import com.example.jimi.model.request.CommentRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CommentService extends IService<Comment> {

    R addComment(CommentRequest addCommentRequest);

    R updateCommentMsg(CommentRequest upCommentRequest);

    R deleteComment(Integer id);

    R commentOfSongId(Integer songId,Integer pageNum, Integer pageSize);

    R commentOfSongListId(Integer songListId, Integer pageNum, Integer pageSize);

    R getCommentSupports(List<Integer> commentIds);
}
