package com.example.jimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jimi.common.R;
import com.example.jimi.model.domain.Admin;
import com.example.jimi.model.request.AdminRequest;

import javax.servlet.http.HttpSession;

public interface AdminService extends IService<Admin> {

    R verityPasswd(AdminRequest adminRequest, HttpSession session);
}
