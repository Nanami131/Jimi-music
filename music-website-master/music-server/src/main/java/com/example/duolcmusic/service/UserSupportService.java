package com.example.duolcmusic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.duolcmusic.common.R;
import com.example.duolcmusic.model.domain.UserSupport;
import com.example.duolcmusic.model.request.UserSupportRequest;


public interface UserSupportService extends IService<UserSupport> {

    R isUserSupportComment(UserSupportRequest userSupportRequest);

    R insertCommentSupport(UserSupportRequest userSupportRequest);

    R deleteCommentSupport(UserSupportRequest userSupportRequest);
}
