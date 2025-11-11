package com.scenebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenebackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scenebackend.model.dto.UserUpdateRequest;

import java.util.List;

/**
* @author 芒果派
* @description 针对表【user】的数据库操作Service
* @createDate 2025-10-16 22:03:48
*/
public interface UserService extends IService<User> {
    /**
     * 根据用户名搜索用户
     */
    List<User> searchUserByName(String userAccount);

    /**
     * 用户注册
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     */
    User userLogin(String userAccount, String userPassword);

    /**
     * 获取安全的用户信息（隐藏敏感信息）
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     */
    int userLogout();

    /**
     * 根据标签搜索用户
     */
    List<User> searchUsersByTags(List<String> tagList);

    /**
     * 更新用户信息
     */
    int updateUser(UserUpdateRequest request);

    /**
     * 获取用户列表（带分页）
     */
    Page<User> getUserList(int pageNum, int pageSize);

    /**
     * 根据ID获取用户详情
     */
    User getUserById(Long id);

    /**
     * 启用/禁用用户
     */
    int updateUserStatus(Long id, Integer status);

    int changePassword(Long userId, String oldPassword, String newPassword);

    //List<User> searchUserByTags(List<String> tagList);
}