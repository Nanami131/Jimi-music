package com.example.jimi.model.request;

import lombok.Data;

import java.util.Date;


@Data
public class RankListRequest {
    private Long id;

    private Long songListId;

    private Long consumerId;

    private Integer score;

    private Date updateTime;
}
