package com.example.jimi.interceptor;

import com.example.jimi.handler.ConsumerDTOHandler;
import com.example.jimi.model.domain.ConsumerDTO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // 直接通过，不检查 session
        }
        HttpSession session = request.getSession();
        Object user =session.getAttribute("user");
        if(user==null){
            response.setStatus(401);
            System.out.println(111);
            response.getWriter().write("{\"code\":401,\"message\":\"会话已过期，请重新登录\",\"success\":false}");
            return false;
        }

        //将用户信息存到ThreadLocal
        ConsumerDTOHandler.setConsumerInfo((ConsumerDTO) user);
        //更新会话时间
        session.setMaxInactiveInterval(6400);
        return true;
    }

    // 在请求完成后清理 ThreadLocal
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理 ThreadLocal，防止内存泄漏
        ConsumerDTOHandler.removeConsumerInfo();
    }
}
