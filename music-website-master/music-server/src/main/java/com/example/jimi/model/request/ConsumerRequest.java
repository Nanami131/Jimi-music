package com.example.jimi.model.request;

import com.example.jimi.validation.ValidationGroups;
import lombok.Data;

import java.util.Date;
import javax.validation.constraints.NotBlank;


@Data
public class ConsumerRequest {
    private Integer id;

    @NotBlank(message = "用户名不能为空", groups = ValidationGroups.CreateValidationGroup.class)
    private String username;

    private String oldPassword; //因为会用到用户旧密码 这无所谓的对应即可

    @NotBlank(message = "密码不能为空", groups = ValidationGroups.CreateValidationGroup.class)
    private String password;

    private Byte sex;

    private String phoneNum;

    private String email;

    private Date birth;

    private String introduction;

    private String location;

    private String avator;

    private Date createTime;
}

