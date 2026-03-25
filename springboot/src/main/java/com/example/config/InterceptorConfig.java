package com.example.config;

import com.example.interceptor.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    // 定义不需要拦截的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/login",
            "/register",
            "/",
            "/error",
            "/home/data",
            "/categories",

            // 商品相关
            "/goods/**",

            // 用户相关 - 确保currentUser完全放行
            "/user/currentUser",
            "/user/selectById/**",
            "/user/selectAll",
            "/user/selectPage",
            // 新增：放行上传头像接口（但接口内会验证Token）
            "/user/uploadAvatar",

            // 订单相关
            "/orders/**",

            // 地址相关
            "/address/**",

            // 分类相关
            "/category/**",

            // 管理员相关
            "/admin/**",

            // 公告相关
            "/notice/**",

            // 库存相关
            "/goodsStock/**",

            // 文件相关
            "/files/**",
            "/files/preview/**",
            "/files/download/**",
            "/files/upload"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }
}