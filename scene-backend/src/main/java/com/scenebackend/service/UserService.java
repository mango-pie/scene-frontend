package com.scenebackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scenebackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scenebackend.model.dto.UserUpdateRequest;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.SessionRepository;

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
     * 根据用户名获取用户
     */
    User getUserByName(String username);

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

    /**
     * 根据用户ID推荐相似用户（支持分页和缓存）
     * @param userId 当前用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param cacheKey 缓存键（用于前端"换一批"功能）
     * @return 分页的推荐用户列表
     */
    Page<User> recommendUsersByUserId(Long userId, int pageNum, int pageSize, String cacheKey);

    /**
     * 根据标签列表推荐相似用户（支持分页和缓存）
     * @param tagList 用户标签列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param cacheKey 缓存键（用于前端"换一批"功能）
     * @return 分页的推荐用户列表
     */
    Page<User> recommendUsersByTags(List<String> tagList, int pageNum, int pageSize, String cacheKey);


    //List<User> searchUserByTags(List<String> tagList);
}