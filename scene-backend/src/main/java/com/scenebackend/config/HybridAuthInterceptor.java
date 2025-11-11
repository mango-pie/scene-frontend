package com.scenebackend.config;

import com.scenebackend.utils.SessionService;
import com.scenebackend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//混合认证拦截器
@Component
public class HybridAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SessionService sessionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求（CORS预检请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 放行登录、注册等公开接口
        String uri = request.getRequestURI();
        if (uri.contains("/user/login") || uri.contains("/user/register") || uri.contains("/tag/search")) {
            return true;
        }

        // 优先检查session认证
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            if (sessionService.validateSession(sessionId)) {
                // session验证通过，刷新session有效期
                sessionService.refreshSession(sessionId);
                return true;
            }
        }

        // 如果session认证失败，检查token认证
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                // token验证通过，将用户ID存入request属性
                Long userId = jwtUtil.getUserIdFromToken(token);
                request.setAttribute("userId", userId);
                return true;
            }
        }

        // 两种认证方式都失败
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\": 401, \"message\": \"认证失败，请重新登录\"}");
        return false;
    }
}