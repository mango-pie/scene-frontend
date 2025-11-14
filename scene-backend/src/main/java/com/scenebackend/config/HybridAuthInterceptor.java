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
            // 检查是否是特殊标识，如果是则跳过session验证
            if ("redis-unavailable".equals(sessionId) || "session-creation-failed".equals(sessionId)) {
                // 特殊标识表示session不可用，直接进入token验证
                System.out.println("检测到session不可用标识，跳过session验证，使用token认证");
            } else {
                try {
                    if (sessionService.validateSession(sessionId)) {
                        // session验证通过，刷新session有效期
                        sessionService.refreshSession(sessionId);
                        return true;
                    }
                } catch (Exception e) {
                    System.err.println("session验证过程中发生异常，将回退到token认证: " + e.getMessage());
                    // 继续执行token验证
                }
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
                System.out.println("token验证通过，用户ID: " + userId);
                return true;
            }
        }

        // 两种认证方式都失败
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\": 401, \"message\": \"认证失败，请重新登录\"}");
        return false;
    }
}