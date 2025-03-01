package com.example.duolcmusic.model.request;

import lombok.Data;

import java.util.Date;


@Data
public class CommentRequest {
    private Integer id;

    private Integer userId;

    private Integer songId;

    private Integer songListId;

    private String content;

    private Date createTime;

    private Byte nowType;

    private Integer up;//点赞
}
