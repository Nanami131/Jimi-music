package com.example.jimi.controller;

import com.example.jimi.common.R;
import com.example.jimi.model.request.CommentRequest;
import com.example.jimi.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;


    // 提交评论
    @PostMapping("/comment/add")
    public R addComment(@RequestBody CommentRequest addCommentRequest) {
        return commentService.addComment(addCommentRequest);
    }

    // 删除评论
    @GetMapping("/comment/delete")
    public R deleteComment(@RequestParam Integer id) {
        return commentService.deleteComment(id);
    }

    // 获得指定歌曲 ID 的评论列表
    @GetMapping("/comment/song/detail")
    public R commentOfSongId(@RequestParam Integer songId,
                             @RequestParam("pageNum") Integer pageNum,
                             @RequestParam("pageSize") Integer pageSize) {
        return commentService.commentOfSongId(songId, pageNum, pageSize);
    }

    // 获得指定歌单 ID 的评论列表
    @GetMapping("/comment/songList/detail")
    public R commentOfSongListId(@RequestParam("songListId") Integer songListId,
                                 @RequestParam("pageNum") Integer pageNum,
                                 @RequestParam("pageSize") Integer pageSize) {
        return commentService.commentOfSongListId(songListId, pageNum, pageSize);
    }

    @PostMapping("/comment/supports")
    public R getCommentSupports(@RequestBody List<Integer> commentIds) {
        return commentService.getCommentSupports(commentIds);
    }

    // 点赞
    @PostMapping("/comment/like")
    public R commentOfLike(@RequestBody CommentRequest upCommentRequest) {
        return commentService.updateCommentMsg(upCommentRequest);
    }
}
