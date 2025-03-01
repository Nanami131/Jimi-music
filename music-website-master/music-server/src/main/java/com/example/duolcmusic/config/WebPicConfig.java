package com.example.duolcmusic.config;

import com.example.duolcmusic.constant.LoadConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebPicConfig implements WebMvcConfigurer {

    //TODO 这个配置类的目的是什么  就是注册了一个类似于拦截器吧  看到对应的资源 会将其修改成相应的地址
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/avatorImages/**")
                .addResourceLocations(LoadConstants.AVATOR_IMAGES_PATH);
        registry.addResourceHandler("/img/singerPic/**")
                .addResourceLocations(LoadConstants.SINGER_PIC_PATH);
        registry.addResourceHandler("/img/songPic/**")
                .addResourceLocations(LoadConstants.SONG_PIC_PATH);
        registry.addResourceHandler("/song/**")
                .addResourceLocations(LoadConstants.SONG_PATH);
        registry.addResourceHandler("/img/songListPic/**")
                .addResourceLocations(LoadConstants.SONGLIST_PIC_PATH);
        registry.addResourceHandler("/img/swiper/**")
                .addResourceLocations(LoadConstants.BANNER_PIC_PATH);
        System.out.println(LoadConstants.SONG_PIC_PATH);
    }



}
