package com.example.jimi.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "rank_list")
@Data
public class RankList implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long songListId;

    private Long consumerId;

    private Integer score;

    private Date updateTime;

}
