package com.scenebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenebackend.common.ErrorCode;
import com.scenebackend.exception.BusinessException;
import com.scenebackend.model.domain.User;
import com.scenebackend.model.dto.BaseResponse;
import com.scenebackend.model.dto.LoginResponse;
import com.scenebackend.model.dto.UserUpdateRequest;
import com.scenebackend.service.UserService;
import com.scenebackend.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.scenebackend.utils.SessionService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private SessionService sessionService;

    // 在UserController类中添加
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 根据用户名搜索用户
     * @param username 用户名
     * @return 用户列表
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUserByName(@RequestParam String username) {
        return BaseResponse.success(userService.searchUserByName(username));
    }

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 用户ID
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestParam String userAccount,
                             @RequestParam String userPassword,
                             @RequestParam String checkPassword) {
        return BaseResponse.success(userService.userRegister(userAccount, userPassword, checkPassword));
    }

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 用户信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginResponse> login(@RequestParam String userAccount,
                               @RequestParam String userPassword,
                               HttpServletRequest request) {

        // 验证用户登录
        User user = userService.userLogin(userAccount, userPassword);

        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        String sessionId = null;
        try {
            // 尝试创建session，如果Redis不可用会抛出异常
            sessionId = sessionService.createSession(user);
        } catch (BusinessException e) {
            if (e.getCode() == ErrorCode.REDIS_UNAVAILABLE_ERROR.getCode()) {
                // Redis不可用，只返回token，sessionId设为null
                System.out.println("Redis不可用，登录时仅返回token");
                sessionId = null;
            } else {
                // 其他异常继续抛出
                throw e;
            }
        }

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setSessionId(sessionId); // 如果Redis不可用，sessionId为null
        response.setExpireTime(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 24小时

        return BaseResponse.success(response);
    }

    /**
     * 用户登出
     * @return 登出结果
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 清除session
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            sessionService.deleteSession(sessionId);
        }

        // 清除token（在前端清除localStorage中的token）
        return BaseResponse.success(userService.userLogout());
    }

    /**
     * 根据标签搜索用户
     * @param tagList 标签列表
     * @return 用户列表
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam List<String> tagList) {
        return BaseResponse.success(userService.searchUsersByTags(tagList));
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody UserUpdateRequest user,
                          @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        try {
            int result = userService.updateUser(user);

            // 如果更新成功且提供了sessionId，则同步更新Redis中的session数据
            if (result == 1 && sessionId != null && !sessionId.trim().isEmpty()) {
                // 获取更新后的用户信息
                User updatedUser = userService.getById(user.getId());
                if (updatedUser != null) {
                    // 使用SessionService更新session数据
                    sessionService.updateSessionUser(sessionId, updatedUser);
                }
            }

            return BaseResponse.success(result);
        } catch (Exception e) {
            System.err.println("更新用户信息时发生错误:");
            e.printStackTrace();
            throw new BusinessException(ErrorCode.UPDATE_ERROR, "更新用户信息时发生错误"); // 返回错误代码
        }
    }

    /**
     * 获取用户列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页用户列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<User>> getUserList(@RequestParam int pageNum,
                                  @RequestParam int pageSize) {
        return BaseResponse.success(userService.getUserList(pageNum, pageSize));
    }

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public BaseResponse<User> getUserById(@PathVariable Long id) {
        return BaseResponse.success(userService.getUserById(id));
    }

    @GetMapping("/name/{name}")
    public BaseResponse<User> getUserByName(@PathVariable String name) {
        return BaseResponse.success(userService.getUserByName(name));
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 用户状态
     * @return 更新结果
     */
    @PostMapping("/status")
    public BaseResponse<Integer> updateUserStatus(@RequestParam Long id,
                                @RequestParam Integer status) {
        return BaseResponse.success(userService.updateUserStatus(id, status));
    }

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 检查sessionId是否为特殊标识符（Redis不可用）
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            if (sessionId.equals("redis-unavailable") || sessionId.equals("session-creation-failed")) {
                System.out.println("检测到Redis不可用特殊标识符，跳过session验证，直接使用token认证");
                // 跳过session验证，直接进入token认证
            } else {
                try {
                    User user = sessionService.getUserBySession(sessionId);
                    if (user != null) {
                        return BaseResponse.success(user);
                    }
                } catch (Exception e) {
                    System.err.println("Session认证失败，将回退到token认证: " + e.getMessage());
                    // 继续执行token认证
                }
            }
        }

        // 如果session认证失败或检测到特殊标识符，使用token认证
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                return BaseResponse.success(userService.getById(userId));
            }
        }

        throw new BusinessException(ErrorCode.AUTHENTICATION_ERROR, "未提供有效的认证信息");
    }
    /**
     * 修改密码
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @param userId 用户ID
     * @return 修改结果
     */
    @PostMapping("/changePassword")
    public BaseResponse<Integer> changePassword(@RequestParam String oldPassword,
                              @RequestParam String newPassword,
                              @RequestParam Long userId) {
        return BaseResponse.success(userService.changePassword(userId, oldPassword, newPassword));
    }


    /**
     * 根据用户ID推荐相似用户（支持分页和缓存）
     * @param userId 当前用户ID
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认10
     * @param cacheKey 缓存键（用于前端"换一批"功能），默认"default"
     * @return 分页的推荐用户列表
     */
    @GetMapping("/recommend/byUserId")
    public BaseResponse<Page<User>> recommendUsersByUserId(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "default") String cacheKey) {
        System.out.println("根据用户ID推荐相似用户（支持分页和缓存）");
        System.out.println("userId: " + userId);
        System.out.println("pageNum: " + pageNum);
        System.out.println("pageSize: " + pageSize);
        System.out.println("cacheKey: " + cacheKey);
        return BaseResponse.success(userService.recommendUsersByUserId(userId, pageNum, pageSize, cacheKey));
    }

    /**
     * 根据标签列表推荐相似用户（支持分页和缓存）
     * @param tags 标签列表，多个标签用逗号分隔
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认10
     * @param cacheKey 缓存键（用于前端"换一批"功能），默认"default"
     * @return 分页的推荐用户列表
     */
    @GetMapping("/recommend/byTags")
    public BaseResponse<Page<User>> recommendUsersByTags(
            @RequestParam String tags,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "default") String cacheKey) {
        // 解析标签字符串为列表
        List<String> tagList = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toList());
        return BaseResponse.success(userService.recommendUsersByTags(tagList, pageNum, pageSize, cacheKey));
    }

    /**
     * 清除特定用户ID的推荐缓存（用于"换一批"功能）
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/recommend/cache/byUserId")
    public BaseResponse<Boolean> clearRecommendCacheByUserId(@RequestParam Long userId) {
        try {
            // 使用模式匹配删除所有相关缓存
            String pattern = "user:recommend:userId:" + userId + ":cache:*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            return BaseResponse.success(true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "清除推荐缓存失败");
        }
    }

    /**
     * 清除特定标签列表的推荐缓存（用于"换一批"功能）
     * @param tags 标签列表，多个标签用逗号分隔
     * @return 操作结果
     */
    @DeleteMapping("/recommend/cache/byTags")
    public BaseResponse<Boolean> clearRecommendCacheByTags(@RequestParam String tags) {
        try {
            // 解析标签并排序以匹配缓存键格式
            List<String> tagList = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList());
            Collections.sort(tagList);
            String tagsHash = String.join(",", tagList);

            // 使用模式匹配删除所有相关缓存
            String pattern = "user:recommend:tags:" + tagsHash + ":cache:*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            return BaseResponse.success(true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "清除推荐缓存失败");
        }
    }
}