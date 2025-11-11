package com.scenebackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS配置类
 * 解决前端跨域请求问题
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

//    @Autowired
//    private JwtInterceptor jwtInterceptor;
    @Autowired
    private HybridAuthInterceptor hybridAuthInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(hybridAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/tag/search",  // 添加标签查询接口排除
                        "/error",
                        // Swagger UI 相关路径
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/swagger-ui.html/**",
                        "/swagger-ui/index.html",
                        "/doc.html"

                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有路径都支持跨域
        registry.addMapping("/**")
                // 允许的源，这里配置为允许localhost:5173访问，也可以使用*允许所有源
                .allowedOrigins("http://localhost:5173")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带凭证（cookies, HTTP认证等）
                .allowCredentials(true)
                // 预检请求的有效期（秒）
                .maxAge(3600);
    }
}