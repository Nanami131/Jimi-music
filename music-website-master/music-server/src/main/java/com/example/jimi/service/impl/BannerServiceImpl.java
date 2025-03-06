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
        String s = stringRedisTemplate.opsForValue().get("banner:all");
        if(s!=null){
            try {
                List<Banner> banners = objectMapper.readValue(s, new TypeReference<List<Banner>>() {});
                return R.success("走了缓存",banners);
            } catch (JsonProcessingException e) {
                return R.error(e.getMessage());
            }
        }
        List<Banner> banners = bannerMapper.selectList(null);
        try {
            stringRedisTemplate.opsForValue().set("banner:all", objectMapper.writeValueAsString(banners));
        } catch (JsonProcessingException e) {
            return R.error(e.getMessage());
        }
        return R.success("没有走缓存",banners);
    }
}
