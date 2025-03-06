package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.mapper.BannerMapper;
import com.example.jimi.model.domain.Banner;
import com.example.jimi.service.BannerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner>
        implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    @Override
    public List<Banner> getAllBanner() {
        String s = stringRedisTemplate.opsForValue().get("banner:all");
        if(s!=null){


        }
        System.out.println("没有走缓存");
        return bannerMapper.selectList(null);
    }
}
