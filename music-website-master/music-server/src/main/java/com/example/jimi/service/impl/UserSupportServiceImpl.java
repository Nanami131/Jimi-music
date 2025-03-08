package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.common.R;
import com.example.jimi.model.domain.UserSupport;
import com.example.jimi.model.request.UserSupportRequest;
import com.example.jimi.service.UserSupportService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
public class UserSupportServiceImpl
        implements UserSupportService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public R isUserSupportComment(UserSupportRequest req) {
        String key = "support:" + req.getCommentId() + ":" + req.getUserId();
        Boolean exists = stringRedisTemplate.hasKey(key);
        return R.success(exists ? "取消点赞成功" : "点赞成功", exists);
    }

    public R insertCommentSupport(UserSupportRequest req) {
        String key = "support:" + req.getCommentId() + ":" + req.getUserId();
        stringRedisTemplate.opsForValue().set(key, "1", 7, TimeUnit.DAYS); // TTL 72小时
        return R.success("点赞成功");
    }

    public R deleteCommentSupport(UserSupportRequest req) {
        String key = "support:" + req.getCommentId() + ":" + req.getUserId();
        stringRedisTemplate.delete(key);
        return R.success("取消点赞成功");
    }
}
