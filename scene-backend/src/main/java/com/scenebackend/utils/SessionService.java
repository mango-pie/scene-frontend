package com.scenebackend.utils;

import com.scenebackend.model.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private static final String SESSION_PREFIX = "session:";
    private static final long SESSION_EXPIRE_HOURS = 24; // session有效期24小时

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建session
     */
    public String createSession(User user) {
        String sessionId = java.util.UUID.randomUUID().toString();
        String sessionKey = SESSION_PREFIX + sessionId;

        // 存储用户信息到session
        redisTemplate.opsForValue().set(sessionKey, user, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

        return sessionId;
    }

    /**
     * 验证session是否有效
     */
    public boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
    }

    /**
     * 根据sessionId获取用户信息
     */
    public User getUserBySession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        return (User) redisTemplate.opsForValue().get(sessionKey);
    }

    /**
     * 删除session
     */
    public void deleteSession(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            String sessionKey = SESSION_PREFIX + sessionId;
            redisTemplate.delete(sessionKey);
        }
    }

    /**
     * 刷新session有效期
     */
    public void refreshSession(String sessionId) {
        if (validateSession(sessionId)) {
            String sessionKey = SESSION_PREFIX + sessionId;
            User user = getUserBySession(sessionId);
            if (user != null) {
                redisTemplate.opsForValue().set(sessionKey, user, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
            }
        }
    }
}