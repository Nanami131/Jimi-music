package com.example.jimi.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Object user =session.getAttribute("username");
        if(user==null){
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"会话已过期，请重新登录\",\"success\":false}");
            return false;
        }
        session.setMaxInactiveInterval(6400);
        //System.out.println("2"+user.toString());
        return true;
    }
}
