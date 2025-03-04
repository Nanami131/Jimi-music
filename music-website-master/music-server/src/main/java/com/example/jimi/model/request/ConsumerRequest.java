package com.example.jimi.model.request;

import com.example.jimi.common.validation.ValidationGroups;
import lombok.Data;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;


@Data
public class ConsumerRequest {
    @Null(message = "不能手动指定id", groups = ValidationGroups.CreateValidationGroup.class)
    private Integer id;

    @NotBlank(message = "用户名不能为空", groups = ValidationGroups.CreateValidationGroup.class)
    private String username;

    private String oldPassword; //因为会用到用户旧密码 这无所谓的对应即可

    @Null(message = "不能在更新时修改密码", groups = ValidationGroups.UpdateValidationGroup.class)
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

    private Date updateTime;

}

