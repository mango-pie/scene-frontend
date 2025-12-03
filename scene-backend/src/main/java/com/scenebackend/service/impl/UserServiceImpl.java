package com.scenebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scenebackend.common.ErrorCode;
import com.scenebackend.exception.BusinessException;
import com.scenebackend.model.domain.User;
import com.scenebackend.model.dto.UserUpdateRequest;
import com.scenebackend.service.UserService;
import com.scenebackend.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.session.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
/**
* @author 芒果派
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-10-16 22:03:48
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    // 盐值，用于密码加密
    private static final String SALT = "scene";
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户列表
     */
    @Override
    public List<User> searchUserByName(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = this.list(queryWrapper);
        return userList.stream().map(this::getSafetyUser).toList();
    }

    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setProfile(originUser.getProfile());
        return safetyUser;
    }
    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 用户ID
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isBlank(userAccount) || StringUtils.isBlank(userPassword) || StringUtils.isBlank(checkPassword)) {
            return -1;
        }
        if (userAccount.length() < 4) {
            return -1;
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            return -1;
        }
        // 账户不能包含特殊字符
        String validPattern = "^[\\w]+$";
        if (!Pattern.matches(validPattern, userAccount)) {
            return -1;
        }
        // 密码和确认密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            return -1;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StringUtils.isBlank(userAccount) || StringUtils.isBlank(userPassword)) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "^[\\w]+$";
        if (!Pattern.matches(validPattern, userAccount)) {
            return null;
        }
        // 2. 加密 - 取消注释这行代码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 查询用户是否存在 - 使用加密后的密码进行查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword); // 修改为使用加密后的密码
        User user = this.getOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            return null;
        }
        // 4. 返回安全的用户信息
        return this.getSafetyUser(user);
    }

    @Override
    public int userLogout() {
        // 这里可以实现用户注销逻辑，如清除会话、缓存等
        // 示例实现
        return 1; // 表示成功
    }
@Override
public int updateUser(UserUpdateRequest request) {
    if (request == null || request.getId() == null) {
        return 0;
    }
    //创建用户实体
    User user = new User();
    user.setId(request.getId());

    // 设置可更新字段
    if (StringUtils.isNotBlank(request.getUsername())) {
        user.setUsername(request.getUsername());
    }
    if (StringUtils.isNotBlank(request.getAvatarUrl())) {
        user.setAvatarUrl(request.getAvatarUrl());
    }
    if (request.getGender() != null) {
        user.setGender(request.getGender());
    }
    if (StringUtils.isNotBlank(request.getPhone())) {
        user.setPhone(request.getPhone());
    }
    if (StringUtils.isNotBlank(request.getEmail())) {
        user.setEmail(request.getEmail());
    }
    if (StringUtils.isNotBlank(request.getPlantCode())) {
        user.setPlanetCode(request.getPlantCode());
    }
    if (request.getTagList() != null) {
        System.out.println("标签列表: " + request.getTagList());
        user.setTagList(request.getTagList());
    }
    try {
        boolean updateResult = this.updateById(user);

        return updateResult ? 1 : 0;
    } catch (Exception e) {
        System.err.println("执行数据库更新时发生错误:");
        e.printStackTrace();
        return -1;
    }
}
    @Override
    public Page<User> getUserList(int pageNum, int pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 可以添加排序等条件
        queryWrapper.orderByDesc("createTime");
        Page<User> userPage = this.page(page, queryWrapper);
        // 对结果进行安全处理
        userPage.setRecords(userPage.getRecords().stream()
                .map(this::getSafetyUser)
                .collect(Collectors.toList()));
        return userPage;
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            return null;
        }
        User user = this.getById(id);
        return this.getSafetyUser(user);
    }

    @Override
    public int updateUserStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return 0;
        }
        User user = new User();
        user.setId(id);
        user.setUserStatus(status);
        boolean updateResult = this.updateById(user);
        return updateResult ? 1 : 0;
    }

    @Override
    public int changePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 校验
        if (userId == null || oldPassword == null || newPassword == null) {
            return 0;
        }
        // 2. 校验旧密码是否匹配
        User user = this.getById(userId);
        if (user == null || !user.getUserPassword().equals(DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes()))) {
            return 0;
        }
        // 3. 加密新密码
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        // 4. 更新密码
        user.setUserPassword(encryptNewPassword);
        boolean updateResult = this.updateById(user);
        return updateResult ? 1 : 0;
    }


    @Override
    public List<User> searchUsersByTags(List<String> tagNameList)
    {
        if (tagNameList == null || tagNameList.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签列表不能为空");
        }

        // 修改为使用JSON数组格式进行查询
        long sqlStartTime = System.currentTimeMillis();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for(String tagName : tagNameList) {
            System.out.println("tagName: " + tagName);
            // 修改为搜索JSON数组格式的标签
            queryWrapper = queryWrapper.like("tags", "\"" + tagName + "\"");
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        long sqlEndTime = System.currentTimeMillis();
        System.out.println("SQL查询方式执行时间: " + (sqlEndTime - sqlStartTime) + "ms");

        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public User getUserByName(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().like(User::getUsername, username));
        return this.getSafetyUser(user);
    }

    @Override
    public Page<User> recommendUsersByUserId(Long userId, int pageNum, int pageSize, String cacheKey) {
        if (userId == null || pageNum < 1 || pageSize < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 生成Redis缓存键
        String redisKey = "user:recommend:userId:" + userId + ":cache:" + cacheKey;

        System.out.println("根11）");
        try {
            // 尝试从缓存获取预计算的所有推荐用户
            List<User> allRecommendedUsers = (List<User>) redisTemplate.opsForValue().get(redisKey);
            System.out.println("allRecommendedUsers: " + allRecommendedUsers);
            // 如果缓存不存在，计算推荐用户并缓存
            if (allRecommendedUsers == null) {
                // 获取当前用户
                User currentUser = this.getById(userId);
                if (currentUser == null) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
                }
System.out.println("currentUser: " + currentUser);
                // 获取当前用户的标签列表
                List<String> currentUserTags = currentUser.getTagList();
                if (currentUserTags.isEmpty()) {
                    return new Page<>();
                }

                // 获取所有其他用户
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.ne("id", userId); // 排除当前用户
                queryWrapper.ne("userStatus", 0); // 排除禁用用户
                List<User> otherUsers = userMapper.selectList(queryWrapper);

                System.out.println("otherUsers: " + otherUsers);
                // 计算相似度并排序
                List<UserSimilarity> similarities = new ArrayList<>();
                for (User user : otherUsers) {
                    List<String> userTags = user.getTagList();
                    if (!userTags.isEmpty()) {
                        double similarity = calculateTagSimilarity(currentUserTags, userTags);
                        if (similarity > 0) { // 只添加有相似度的用户
                            similarities.add(new UserSimilarity(user, similarity));
                        }
                    }
                }

                // 按相似度降序排序
                similarities.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

                // 转换为安全用户列表
                allRecommendedUsers = similarities.stream()
                        .map(us -> getSafetyUser(us.getUser()))
                        .collect(Collectors.toList());

                // 缓存推荐结果，设置1小时过期
                redisTemplate.opsForValue().set(redisKey, allRecommendedUsers, 1, TimeUnit.HOURS);
            }

            // 执行分页
            Page<User> resultPage = new Page<>(pageNum, pageSize);
            int total = allRecommendedUsers.size();
            resultPage.setTotal(total);

            // 计算分页数据
            int startIndex = (int) ((pageNum - 1) * pageSize);
            int endIndex = Math.min(startIndex + pageSize, total);

            if (startIndex < total) {
                List<User> pageData = allRecommendedUsers.subList(startIndex, endIndex);
                resultPage.setRecords(pageData);
            } else {
                resultPage.setRecords(new ArrayList<>());
            }

            return resultPage;
        } catch (Exception e) {
            // 如果Redis操作失败，记录异常但继续执行
            log.error("Redis缓存操作失败: " + e.getMessage(), e);
            // 降级处理：直接返回空分页
            return new Page<>();
        }
    }

    @Override
    public Page<User> recommendUsersByTags(List<String> tagList, int pageNum, int pageSize, String cacheKey) {
        if (tagList == null || tagList.isEmpty() || pageNum < 1 || pageSize < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }

        // 生成Redis缓存键（对标签列表进行排序以确保一致性）
        List<String> sortedTags = new ArrayList<>(tagList);
        Collections.sort(sortedTags);
        String tagsHash = String.join(",", sortedTags);
        String redisKey = "user:recommend:tags:" + tagsHash + ":cache:" + cacheKey;

        try {
            // 尝试从缓存获取预计算的所有推荐用户
            List<User> allRecommendedUsers = (List<User>) redisTemplate.opsForValue().get(redisKey);

            // 如果缓存不存在，计算推荐用户并缓存
            if (allRecommendedUsers == null) {
                // 获取所有用户
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.ne("userStatus", 0); // 排除禁用用户
                List<User> allUsers = userMapper.selectList(queryWrapper);

                // 计算相似度并排序
                List<UserSimilarity> similarities = new ArrayList<>();
                for (User user : allUsers) {
                    List<String> userTags = user.getTagList();
                    if (!userTags.isEmpty()) {
                        double similarity = calculateTagSimilarity(tagList, userTags);
                        if (similarity > 0) { // 只添加有相似度的用户
                            similarities.add(new UserSimilarity(user, similarity));
                        }
                    }
                }

                // 按相似度降序排序
                similarities.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

                // 转换为安全用户列表
                allRecommendedUsers = similarities.stream()
                        .map(us -> getSafetyUser(us.getUser()))
                        .collect(Collectors.toList());

                // 缓存推荐结果，设置1小时过期
                redisTemplate.opsForValue().set(redisKey, allRecommendedUsers, 1, TimeUnit.HOURS);
            }

            // 执行分页
            Page<User> resultPage = new Page<>(pageNum, pageSize);
            int total = allRecommendedUsers.size();
            resultPage.setTotal(total);

            // 计算分页数据
            int startIndex = (int) ((pageNum - 1) * pageSize);
            int endIndex = Math.min(startIndex + pageSize, total);

            if (startIndex < total) {
                List<User> pageData = allRecommendedUsers.subList(startIndex, endIndex);
                resultPage.setRecords(pageData);
            } else {
                resultPage.setRecords(new ArrayList<>());
            }

            return resultPage;
        } catch (Exception e) {
            // 如果Redis操作失败，记录异常但继续执行
            log.error("Redis缓存操作失败: " + e.getMessage(), e);
            // 降级处理：直接返回空分页
            return new Page<>();
        }
    }
    /**
     * 计算两个标签列表的余弦相似度
     * @param tags1 标签列表1
     * @param tags2 标签列表2
     * @return 相似度，范围[0,1]
     */
    private double calculateTagSimilarity(List<String> tags1, List<String> tags2) {
        // 创建标签集合
        Set<String> allTags = new HashSet<>();
        allTags.addAll(tags1);
        allTags.addAll(tags2);

        // 计算点积
        int dotProduct = 0;
        for (String tag : tags1) {
            if (tags2.contains(tag)) {
                dotProduct++;
            }
        }

        // 计算向量长度
        double magnitude1 = Math.sqrt(tags1.size());
        double magnitude2 = Math.sqrt(tags2.size());

        // 计算余弦相似度
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    /**
     * 用户相似度内部类，用于存储用户和相似度
     */
    private static class UserSimilarity {
        private User user;
        private double similarity;

        public UserSimilarity(User user, double similarity) {
            this.user = user;
            this.similarity = similarity;
        }

        public User getUser() {
            return user;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}