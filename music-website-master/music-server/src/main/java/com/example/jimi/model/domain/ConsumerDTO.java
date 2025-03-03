package com.example.jimi.model.domain;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true) // 启用链式编程支持
public class ConsumerDTO {
    private Integer id;

    private String username;

    private String phoneNum;

    private String email;
}
