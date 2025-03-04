package com.example.jimi.config;

import com.example.jimi.interceptor.CorsInterceptor;
import com.example.jimi.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public CorsInterceptor corsInterceptor() {
        return new CorsInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //这里先把登录和注册排除登陆验证，其余不需要登录也应该能访问的接口后续添加

        registry.addInterceptor(corsInterceptor())
                .addPathPatterns("/**");
        registry.addInterceptor(new LoginInterceptor()).
                excludePathPatterns(
                        "/user/add",
                        "/user/login/status",
                        "/user/email/status",
                        "/user/resetPassword",
                        "/user/sendVerificationCode",
                        "/user/logout",
                        "/admin/login/status"
                );
    }

}