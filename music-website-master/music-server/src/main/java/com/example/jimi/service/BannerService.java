package com.example.jimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.jimi.model.domain.Banner;

import java.util.List;


public interface BannerService extends IService<Banner> {

    List<Banner> getAllBanner();

}
