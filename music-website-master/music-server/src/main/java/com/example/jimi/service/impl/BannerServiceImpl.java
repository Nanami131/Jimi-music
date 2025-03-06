package com.example.jimi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.jimi.common.R;
import com.example.jimi.mapper.BannerMapper;
import com.example.jimi.model.domain.Banner;
import com.example.jimi.service.BannerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;


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
    public R getAllBanner() {
        String key = stringRedisTemplate.opsForValue().get("banner:all");
        if(key!=null){
            try {
                List<Banner> banners = objectMapper.readValue(key, new TypeReference<List<Banner>>() {});
                return R.success("走了缓存",banners);
            } catch (JsonProcessingException e) {
                return R.error(e.getMessage());
            }
        }
        List<Banner> banners = bannerMapper.selectList(null);
        try {
            stringRedisTemplate.opsForValue().set("banner:all", objectMapper.writeValueAsString(banners),30, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("缓存建立失败: {}",  e);
        }
        return R.success("没有走缓存",banners);
    }
}
