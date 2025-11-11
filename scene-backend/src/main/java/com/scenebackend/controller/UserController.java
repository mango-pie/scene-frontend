package com.scenebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenebackend.model.domain.User;
import com.scenebackend.model.dto.LoginResponse;
import com.scenebackend.model.dto.UserUpdateRequest;
import com.scenebackend.service.UserService;
import com.scenebackend.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
    /**
     * 根据用户名搜索用户
     * @param username 用户名
     * @return 用户列表
     */
    @GetMapping("/search")
    public List<User> searchUserByName(@RequestParam String username) {
        return userService.searchUserByName(username);
    }

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 用户ID
     */
    @PostMapping("/register")
    public long userRegister(@RequestParam String userAccount,
                             @RequestParam String userPassword,
                             @RequestParam String checkPassword) {
        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 用户信息
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestParam String userAccount,
                               @RequestParam String userPassword,
                               HttpServletRequest request) {

        // 验证用户登录
        User user = userService.userLogin(userAccount, userPassword);

        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }

        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 生成session
        String sessionId = sessionService.createSession(user);

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setUserAccount(user.getUserAccount());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setGender(user.getGender());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setUserStatus(user.getUserStatus());
        response.setUserRole(user.getUserRole());
        response.setTagList(user.getTagList().toArray(new String[0]));
        response.setToken(token);
        response.setSessionId(sessionId); // 返回sessionId
        response.setExpireTime(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 24小时

        return response;
    }

    /**
     * 用户登出
     * @return 登出结果
     */
//    @PostMapping("/logout")
//    public int userLogout() {
//        return userService.userLogout();
//    }
    @PostMapping("/logout")
    public int userLogout(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 清除session
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            sessionService.deleteSession(sessionId);
        }

        // 清除token（在前端清除localStorage中的token）
        return userService.userLogout();
    }

    /**
     * 根据标签搜索用户
     * @param tagList 标签列表
     * @return 用户列表
     */
    @GetMapping("/search/tags")
    public List<User> searchUsersByTags(@RequestParam List<String> tagList) {
        return userService.searchUsersByTags(tagList);
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新结果
     */
    @PostMapping("/update")
    public int updateUser(@RequestBody UserUpdateRequest user) {
        try {
            return userService.updateUser(user);
        } catch (Exception e) {
            System.err.println("更新用户信息时发生错误:");
            e.printStackTrace();
            return -1; // 返回错误代码
        }
    }

    /**
     * 获取用户列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页用户列表
     */
    @GetMapping("/list")
    public Page<User> getUserList(@RequestParam int pageNum,
                                  @RequestParam int pageSize) {
        return userService.getUserList(pageNum, pageSize);
    }

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 用户状态
     * @return 更新结果
     */
    @PostMapping("/status")
    public int updateUserStatus(@RequestParam Long id,
                                @RequestParam Integer status) {
        return userService.updateUserStatus(id, status);
    }

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    @GetMapping("/current")
    public User getCurrentUser(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 优先使用session认证
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            User user = sessionService.getUserBySession(sessionId);
            if (user != null) {
                return user;
            }
        }

        // 如果session认证失败，使用token认证
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                return userService.getById(userId);
            }
        }

        throw new RuntimeException("未提供有效的认证信息");
    }
    /**
     * 修改密码
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @param userId 用户ID
     * @return 修改结果
     */
    @PostMapping("/changePassword")
    public int changePassword(@RequestParam String oldPassword,
                              @RequestParam String newPassword,
                              @RequestParam Long userId) {
        return userService.changePassword(userId, oldPassword, newPassword);
    }
}