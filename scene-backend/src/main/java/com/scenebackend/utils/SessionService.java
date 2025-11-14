package com.scenebackend.utils;

import com.scenebackend.common.ErrorCode;
import com.scenebackend.exception.BusinessException;
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
     * 检查Redis是否可用
     */
    private boolean isRedisAvailable() {
        try {
            redisTemplate.hasKey("test");
            return true;
        } catch (Exception e) {
            System.err.println("Redis不可用，将回退到token认证: " + e.getMessage());
            return false;
        }
    }

    /**
     * 创建session
     */
    public String createSession(User user) {
        // 如果Redis不可用，抛出业务异常
        if (!isRedisAvailable()) {
            throw new BusinessException(ErrorCode.REDIS_UNAVAILABLE_ERROR);
        }

        try {
            String sessionId = java.util.UUID.randomUUID().toString();
            String sessionKey = SESSION_PREFIX + sessionId;

            // 存储用户信息到session
            redisTemplate.opsForValue().set(sessionKey, user, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);

            return sessionId;
        } catch (Exception e) {
            System.err.println("创建session失败: " + e.getMessage());
            throw new BusinessException(ErrorCode.REDIS_UNAVAILABLE_ERROR);
        }
    }

    /**
     * 验证session是否有效
     */
    public boolean validateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }

        if (!isRedisAvailable()) {
            return false;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
        } catch (Exception e) {
            System.err.println("验证session失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 根据sessionId获取用户信息
     */
    public User getUserBySession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }

        if (!isRedisAvailable()) {
            return null;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            return (User) redisTemplate.opsForValue().get(sessionKey);
        } catch (Exception e) {
            System.err.println("获取session用户信息失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 删除session
     */
    public void deleteSession(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            if (!isRedisAvailable()) {
                return;
            }

            try {
                String sessionKey = SESSION_PREFIX + sessionId;
                redisTemplate.delete(sessionKey);
            } catch (Exception e) {
                System.err.println("删除session失败: " + e.getMessage());
            }
        }
    }

    /**
     * 刷新session有效期
     */
    public void refreshSession(String sessionId) {
        if (validateSession(sessionId)) {
            if (!isRedisAvailable()) {
                return;
            }

            try {
                String sessionKey = SESSION_PREFIX + sessionId;
                User user = getUserBySession(sessionId);
                if (user != null) {
                    redisTemplate.opsForValue().set(sessionKey, user, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
                }
            } catch (Exception e) {
                System.err.println("刷新session失败: " + e.getMessage());
            }
        }
    }

    public boolean updateSessionUser(String sessionId, User user) {
        if (sessionId == null || sessionId.trim().isEmpty() || user == null) {
            return false;
        }

        if (!isRedisAvailable()) {
            return false;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                redisTemplate.opsForValue().set(sessionKey, user, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("更新session用户信息失败: " + e.getMessage());
            return false;
        }
    }
}