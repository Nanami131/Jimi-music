package com.example.duolcmusic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.duolcmusic.common.R;
import com.example.duolcmusic.model.domain.Admin;
import com.example.duolcmusic.model.request.AdminRequest;

import javax.servlet.http.HttpSession;

public interface AdminService extends IService<Admin> {

    R verityPasswd(AdminRequest adminRequest, HttpSession session);
}
