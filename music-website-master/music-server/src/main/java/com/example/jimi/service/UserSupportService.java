package com.example.jimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jimi.common.R;
import com.example.jimi.model.domain.UserSupport;
import com.example.jimi.model.request.UserSupportRequest;


public interface UserSupportService {

    R isUserSupportComment(UserSupportRequest userSupportRequest);

    R insertCommentSupport(UserSupportRequest userSupportRequest);

    R deleteCommentSupport(UserSupportRequest userSupportRequest);
}
