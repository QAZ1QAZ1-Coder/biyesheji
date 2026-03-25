package com.example.interceptor;

import com.example.utils.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行公开接口
        String uri = request.getRequestURI();
        System.out.println("🔍 拦截器处理请求: " + uri);

        // 特别放行 currentUser 接口 - 完全放行，不验证token
        if (uri.equals("/user/currentUser")) {
            System.out.println("✅ 完全放行 currentUser 接口，不验证Token");
            return true;
        }

        // 其他公开接口
        if (uri.equals("/login") || uri.equals("/register") || uri.equals("/")) {
            System.out.println("✅ 放行公开接口: " + uri);
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        System.out.println("📨 Authorization头: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            System.out.println("🔐 提取后的Token: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));
        } else {
            System.out.println("❌ 未找到有效的Token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"401\",\"msg\":\"Token无效或已过期\"}");
            return false;
        }

        // 验证token
        if (!jwtTokenUtil.validateToken(token)) {
            System.out.println("❌ Token验证失败");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"401\",\"msg\":\"Token无效或已过期\"}");
            return false;
        }

        if (jwtTokenUtil.isTokenExpired(token)) {
            System.out.println("❌ Token已过期");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"401\",\"msg\":\"Token无效或已过期\"}");
            return false;
        }

        // 将用户信息存入request
        Integer userId = jwtTokenUtil.getUserIdFromToken(token);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        String role = jwtTokenUtil.getRoleFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);

        System.out.println("✅ 用户信息设置到request: userId=" + userId + ", username=" + username + ", role=" + role);

        return true;
    }
}