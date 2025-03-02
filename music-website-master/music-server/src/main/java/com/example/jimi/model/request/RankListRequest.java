package com.example.jimi.model.request;

import lombok.Data;


@Data
public class RankListRequest {
    private Long id;

    private Long songListId;

    private Long consumerId;

    private Integer score;
}
