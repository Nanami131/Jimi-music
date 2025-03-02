package com.example.jimi.model.request;

import lombok.Data;

import java.util.Date;


@Data
public class SingerRequest {
    private Integer id;

    private String name;

    private Byte sex;

    private String pic;

    private Date birth;

    private String location;

    private String introduction;
}
