package com.scenebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenebackend.model.domain.User;
import com.scenebackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

//import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

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
    public User userLogin(@RequestParam String userAccount,
                          @RequestParam String userPassword) {
        return userService.userLogin(userAccount, userPassword);
    }

    /**
     * 用户登出
     * @return 登出结果
     */
    @PostMapping("/logout")
    public int userLogout() {
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
    public int updateUser(@RequestBody User user) {
        return userService.updateUser(user);
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
    public User getCurrentUser() {
        // 实际项目中这里应该从会话或Token中获取当前登录用户ID，再调用getUserById
        // 这里返回null，需要根据实际认证机制实现
        return null;
    }
}