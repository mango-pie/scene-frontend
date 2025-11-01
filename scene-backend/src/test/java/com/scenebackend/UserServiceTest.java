//package com.scenebackend;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.scenebackend.model.domain.User;
//import com.scenebackend.service.UserService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * UserService集成测试类
// * 测试所有用户相关接口功能
// */
//@SpringBootTest(classes = SceneBackendApplication.class)
//public class UserServiceTest {
//
//    @Autowired
//    private UserService userService;
//
//    // 测试用例中使用的测试账号
//    private static final String TEST_USER_ACCOUNT = "test" + UUID.randomUUID().toString().substring(0, 5);
//    private static final String TEST_USER_PASSWORD = "12345678";
//    private static final List<String> TEST_USER_TAG = Arrays.asList("Python", "Java");
//
//    // 测试搜索用户方法
//    @Test
//    public void testSearchUserByName_WithValidName() {
//        System.out.println("测试1: 搜索存在的用户名...");
//
//        // 搜索包含特定字符的用户名
//        List<User> users = userService.searchUserByName("scene");
//
//        assertNotNull(users, "搜索结果不应为null");
//        System.out.println("✅ 搜索到 " + users.size() + " 个用户");
//
//        // 验证返回的用户数据是安全的（不包含敏感信息）
//        for (User user : users) {
//            assertNotNull(user.getId(), "用户ID不应为null");
//            assertNotNull(user.getUsername(), "用户名不应为null");
//            assertNull(user.getUserPassword(), "用户密码应为null（安全过滤）");
//            System.out.println("   用户: " + user.getUsername() + " (ID: " + user.getId() + ")");
//        }
//    }
//
//    @Test
//    public void testSearchUserByName_WithEmptyName() {
//        System.out.println("测试2: 搜索空用户名...");
//
//        List<User> users = userService.searchUserByName("");
//
//        assertNotNull(users, "搜索结果不应为null");
//        System.out.println("✅ 搜索到 " + users.size() + " 个用户");
//
//        // 空搜索应该返回所有用户或空列表
//        if (!users.isEmpty()) {
//            for (User user : users) {
//                assertNull(user.getUserPassword(), "用户密码应为null（安全过滤）");
//            }
//        }
//    }
//
//    @Test
//    public void testSearchUserByName_WithNullName() {
//        System.out.println("测试3: 搜索null用户名...");
//
//        List<User> users = userService.searchUserByName(null);
//
//        assertNotNull(users, "搜索结果不应为null");
//        System.out.println("✅ 搜索到 " + users.size() + " 个用户");
//
//        // null搜索应该返回所有用户或空列表
//        if (!users.isEmpty()) {
//            for (User user : users) {
//                assertNull(user.getUserPassword(), "用户密码应为null（安全过滤）");
//            }
//        }
//    }
//
//    @Test
//    public void testSearchUserByName_WithNonExistentName() {
//        System.out.println("测试4: 搜索不存在的用户名...");
//
//        List<User> users = userService.searchUserByName("nonexistentuser12345");
//
//        assertNotNull(users, "搜索结果不应为null");
//        assertEquals(0, users.size(), "不存在的用户名应该返回空列表");
//        System.out.println("✅ 正确返回空列表");
//    }
//
//    @Test
//    public void testSearchUserByName_SecurityFilter() {
//        System.out.println("测试5: 验证安全过滤功能...");
//
//        List<User> users = userService.searchUserByName("a");
//
//        if (!users.isEmpty()) {
//            User firstUser = users.get(0);
//
//            // 验证敏感字段被过滤
//            assertNull(firstUser.getUserPassword(), "用户密码应该被过滤");
//            assertNotNull(firstUser.getUsername(), "用户名应该保留");
//            assertNotNull(firstUser.getId(), "用户ID应该保留");
//            assertNotNull(firstUser.getAvatarUrl(), "头像URL应该保留");
//
//            System.out.println("✅ 安全过滤验证通过");
//            System.out.println("   保留字段: ID, 用户名, 头像等");
//            System.out.println("   过滤字段: 密码");
//        } else {
//            System.out.println("⚠️ 没有用户数据，跳过安全过滤测试");
//        }
//    }
//
//    // 测试用户注册方法
//    @Test
//    public void testUserRegister_Success() {
//        System.out.println("测试6: 测试用户注册成功场景...");
//
//        // 清理可能存在的测试用户
//        User existingUser = new User();
//        existingUser.setUserAccount(TEST_USER_ACCOUNT);
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>(existingUser);
//        userService.remove(queryWrapper);
//
//        // 执行注册
//        long userId = userService.userRegister(TEST_USER_ACCOUNT, TEST_USER_PASSWORD, TEST_USER_PASSWORD);
//
//        // 验证注册成功
//        assertTrue(userId > 0, "用户注册应返回正整数用户ID");
//        System.out.println("✅ 用户注册成功，用户ID: " + userId);
//
//        // 验证用户确实存在
//        User user = userService.getById(userId);
//        assertNotNull(user, "注册后应能查询到用户");
//        assertEquals(TEST_USER_ACCOUNT, user.getUserAccount(), "用户名应一致");
//        assertNotNull(user.getUserPassword(), "密码应被加密保存");
//        assertNotEquals(TEST_USER_PASSWORD, user.getUserPassword(), "密码不应明文保存");
//    }
//
//    @Test
//    public void testUserRegister_Failure_InvalidAccount() {
//        System.out.println("测试7: 测试用户注册失败场景 - 无效账号...");
//
//        // 账号长度不足
//        long userId1 = userService.userRegister("abc", TEST_USER_PASSWORD, TEST_USER_PASSWORD);
//        assertEquals(-1, userId1, "账号长度不足应返回-1");
//
//        // 账号包含特殊字符
//        long userId2 = userService.userRegister("test@123", TEST_USER_PASSWORD, TEST_USER_PASSWORD);
//        assertEquals(-1, userId2, "账号包含特殊字符应返回-1");
//
//        System.out.println("✅ 无效账号测试通过");
//    }
//
//    @Test
//    public void testUserRegister_Failure_InvalidPassword() {
//        System.out.println("测试8: 测试用户注册失败场景 - 无效密码...");
//
//        // 密码长度不足
//        long userId1 = userService.userRegister("testuser", "1234567", "1234567");
//        assertEquals(-1, userId1, "密码长度不足应返回-1");
//
//        // 两次密码不一致
//        long userId2 = userService.userRegister("testuser", TEST_USER_PASSWORD, "differentpassword");
//        assertEquals(-1, userId2, "两次密码不一致应返回-1");
//
//        System.out.println("✅ 无效密码测试通过");
//    }
//
//    // 测试用户登录方法
////    @Test
////    public void testUserLogin_Success() {
////        System.out.println("测试9: 测试用户登录成功场景...");
////
//////        // 先注册一个测试用户
//////        long userId = userService.userRegister(TEST_USER_ACCOUNT, TEST_USER_PASSWORD, TEST_USER_PASSWORD);
//////        assertTrue(userId > 0, "测试用户注册应成功");
////
////        // 执行登录
////        User user = userService.userLogin("testd460a", "12345678");
////
////        // 验证登录成功
////        assertNotNull(user, "登录成功应返回用户信息");
////        assertEquals("testd460a", user.getUserAccount(), "返回的用户名应一致");
////        assertNull(user.getUserPassword(), "返回的用户信息不应包含密码");
////        System.out.println("✅ 用户登录成功");
////    }
//
//    @Test
//    public void testUserLogin_Failure() {
//        System.out.println("测试10: 测试用户登录失败场景...");
//
//        // 密码错误
//        User user1 = userService.userLogin(TEST_USER_ACCOUNT, "wrongpassword");
//        assertNull(user1, "密码错误应返回null");
//
//        // 账号不存在
//        User user2 = userService.userLogin("nonexistentuser", TEST_USER_PASSWORD);
//        assertNull(user2, "账号不存在应返回null");
//
//        // 无效账号
//        User user3 = userService.userLogin("abc", TEST_USER_PASSWORD);
//        assertNull(user3, "无效账号应返回null");
//
//        System.out.println("✅ 登录失败测试通过");
//    }
//
//    // 测试用户注销方法
//    @Test
//    public void testUserLogout() {
//        System.out.println("测试11: 测试用户注销...");
//
//        int result = userService.userLogout();
//
//        // 验证注销成功
//        assertEquals(1, result, "注销成功应返回1");
//        System.out.println("✅ 用户注销测试通过");
//    }
//
//    // 测试根据标签搜索用户方法
//    @Test
//    public void testSearchUsersByTags() {
//        System.out.println("测试12: 测试根据标签搜索用户...");
//
//         //创建一个带有标签的测试用户
////        User testUser = new User();
////        testUser.setUsername("TagTestUser");
////        testUser.setUserAccount("tagtest" + System.currentTimeMillis());
////        testUser.setUserPassword(TEST_USER_PASSWORD);
////        testUser.setTags(TEST_USER_TAG);
////        userService.save(testUser);
//
//        // 使用标签搜索
//        List<String> tags = Arrays.asList("Python", "Java");
//        List<User> users = userService.searchUsersByTags(tags);
//
//        // 验证搜索结果
//        assertNotNull(users, "搜索结果不应为null");
//        boolean found = false;
//        for (User user : users) {
//            if (user.getUsername().equals("scene")) {
//                found = true;
//                break;
//            }
//        }
//        assertTrue(found, "应能搜索到带有指定标签的用户");
//        System.out.println("✅ 标签搜索测试通过");
//
//        // 清理测试数据
////       userService.removeById(testUser.getId());
//    }
//
//    // 测试更新用户信息方法
////    @Test
////    public void testUpdateUser() {
////        System.out.println("测试13: 测试更新用户信息...");
////
////        // 创建测试用户
////        User testUser = new User();
////        testUser.setUsername("UpdateTestUser");
////        testUser.setUserAccount("updatetest" + System.currentTimeMillis());
////        testUser.setUserPassword(TEST_USER_PASSWORD);
////        userService.save(testUser);
////
////        // 更新用户信息
////        User updateUser = new User();
////        updateUser.setId(testUser.getId());
////        updateUser.setUsername("UpdatedUser");
////        updateUser.setAvatarUrl("http://example.com/avatar.jpg");
////
//////        int result = userService.updateUser(updateUser);
////
////        // 验证更新成功
////        assertEquals(1, result, "更新成功应返回1");
////
////        // 验证更新结果
////        User updatedUser = userService.getById(testUser.getId());
////        assertEquals("UpdatedUser", updatedUser.getUsername(), "用户名应更新成功");
////        assertEquals("http://example.com/avatar.jpg", updatedUser.getAvatarUrl(), "头像URL应更新成功");
////        System.out.println("✅ 用户信息更新测试通过");
////
////        // 清理测试数据
////        userService.removeById(testUser.getId());
////    }
//
//    // 测试分页获取用户列表方法
//    @Test
//    public void testGetUserList() {
//        System.out.println("测试14: 测试分页获取用户列表...");
//
//        // 获取第一页，每页10条数据
//        Page<User> userPage = userService.getUserList(1, 10);
//
//        // 验证结果
//        assertNotNull(userPage, "分页结果不应为null");
//        assertNotNull(userPage.getRecords(), "用户列表不应为null");
//        assertTrue(userPage.getSize() <= 10, "每页数据量不应超过10条");
//        System.out.println("✅ 分页获取用户列表测试通过");
//        System.out.println("   当前页: " + userPage.getCurrent());
//        System.out.println("   每页大小: " + userPage.getSize());
//        System.out.println("   总记录数: " + userPage.getTotal());
//        System.out.println("   总页数: " + userPage.getPages());
//    }
//
//    // 测试根据ID获取用户详情方法
//    @Test
//    public void testGetUserById() {
//        System.out.println("测试15: 测试根据ID获取用户详情...");
//
//        // 创建测试用户
//        User testUser = new User();
//        testUser.setUsername("GetByIdTestUser");
//        testUser.setUserAccount("getidtest" + System.currentTimeMillis());
//        testUser.setUserPassword(TEST_USER_PASSWORD);
//        userService.save(testUser);
//
//        // 根据ID获取用户
//        User user = userService.getUserById(testUser.getId());
//
//        // 验证结果
//        assertNotNull(user, "应能根据ID获取用户");
//        assertEquals(testUser.getId(), user.getId(), "用户ID应一致");
//        assertEquals(testUser.getUsername(), user.getUsername(), "用户名应一致");
//        assertNull(user.getUserPassword(), "返回的用户信息不应包含密码");
//        System.out.println("✅ 根据ID获取用户详情测试通过");
//
//        // 清理测试数据
//        userService.removeById(testUser.getId());
//    }
//
//    // 测试更新用户状态方法
//    @Test
//    public void testUpdateUserStatus() {
//        System.out.println("测试16: 测试更新用户状态...");
//
//        // 创建测试用户
//        User testUser = new User();
//        testUser.setUsername("StatusTestUser");
//        testUser.setUserAccount("statustest" + System.currentTimeMillis());
//        testUser.setUserPassword(TEST_USER_PASSWORD);
//        testUser.setUserStatus(0); // 正常状态
//        userService.save(testUser);
//
//        // 更新用户状态为禁用
//        int result = userService.updateUserStatus(testUser.getId(), 1);
//
//        // 验证更新成功
//        assertEquals(1, result, "更新状态成功应返回1");
//
//        // 验证状态已更新
//        User updatedUser = userService.getById(testUser.getId());
//        assertEquals(1, updatedUser.getUserStatus().intValue(), "用户状态应更新为1");
//        System.out.println("✅ 更新用户状态测试通过");
//
//        // 清理测试数据
//        userService.removeById(testUser.getId());
//    }
//
//    @Test
//    public void testSearchUserByTags() {
//        // 测试根据标签搜索用户
//        List<String> tagNameList = Arrays.asList("Java","Python");
//        List<User> users = userService.searchUsersByTags(tagNameList);
//        for (User user : users) {
//            System.out.println(user);
//        }
//        assertNotNull(users);
//        assertTrue(users.stream().anyMatch(user -> user.getTags().contains("Java")), "结果中应包含Java标签的用户");
//        //assertTrue(users.stream().anyMatch(user -> user.getTags().contains("Python")), "结果中应包含Python标签的用户");
//
//        System.out.println("✅ 标签搜索用户测试通过");
//    }
//}