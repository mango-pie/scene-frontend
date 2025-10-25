// 模拟用户服务，返回符合 User 类型的数据
import user from "../components/pages/User.vue";

export default {

  // setCurrentUser(user) {
  //   // 模拟设置当前登录用户，实际中可能会使用 localStorage 或 sessionStorage
  //   localStorage.setItem('currentUser', JSON.stringify(user));
  // },



  // 获取当前登录用户信息

  getCurrentUser() {
    // 返回符合 User 类型的模拟数据
    return {
      id: user.id,
      username: user.username,
      userAccount: user.userAccount,
      avatarUrl: user.avatarUrl,
      gender: user.gender, // 1表示男性，0表示女性
      phone: user.phone,
      email: user.email,
      userStatus: user.userStatus, // 1表示正常，0表示禁用
      userRole: user.userRole, // 0表示普通用户，1表示管理员
      plantCode: user.plantCode,
      tags: user.tags,
      createTime: user.createTime
    };
  }
};