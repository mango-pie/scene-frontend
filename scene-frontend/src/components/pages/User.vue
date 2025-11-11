<template>
  <div class="user-container">
    <!-- 登录状态检查 -->
    <div v-if="isLoggedIn" class="user-header">
      <div class="avatar-container" @click="handleAvatarClick">
        <van-image
            round
            size="80"
            :src="userInfo?.avatarUrl"
            class="avatar-img"
        >
          <!-- 头像加载失败时显示用户名首字母 -->
          <template #default>
            {{ userInfo?.username?.charAt(0) || '用' }}
          </template>
        </van-image>
      </div>
      <div class="user-basic-info">
        <h2 class="user-name" @click="handleAvatarClick">{{ userInfo?.username }}</h2>
        <p class="user-account" @click="handleAvatarClick">账号：{{ userInfo?.userAccount }}</p>
      </div>
      <button @click="handleLogout" class="logout-btn">退出登录</button>
    </div>
    
    <!-- 登录表单 -->
    <div v-else class="login-container">
      <h2>用户登录</h2>
      <div class="form-group">
        <label>账号：</label>
        <input v-model="userAccount" type="text" placeholder="请输入账号" />
      </div>
      <div class="form-group">
        <label>密码：</label>
        <input v-model="userPassword" type="password" placeholder="请输入密码" />
      </div>
      <button @click="handleLogin" class="login-btn">登录</button>
      <button @click="handleRegister" class="register-btn" style="margin-top: 10px">注册</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { userLogin, getCurrentUser, userLogout } from '../../api/user';

const router = useRouter();
const userInfo = ref(null);
const userAccount = ref('');
const userPassword = ref('');
const isLoggedIn = ref(false);

const handleLogin = async () => {
  if (!userAccount.value || !userPassword.value) {
    showToast('请输入账号和密码');
    return;
  }

  try {
    const result = await userLogin({
      userAccount: userAccount.value,
      userPassword: userPassword.value
    });

    // 添加结果检查
    if (!result) {
      showToast('登录失败，账号或密码错误');
      return;
    }

    // 登录成功后的处理
    console.log('登录结果:', result);

    // 保存token到localStorage
    if (result.token) {
      localStorage.setItem('token', result.token);
      console.log('Token已保存到localStorage');
    }

    // 保存sessionId到localStorage（新增）
    if (result.sessionId) {
      localStorage.setItem('sessionId', result.sessionId);
      console.log('SessionId已保存到localStorage');
    }

    // 保存用户信息
    localStorage.setItem('currentUser', JSON.stringify(result));

    // 直接设置登录状态
    isLoggedIn.value = true;
    userInfo.value = result;

    showToast('登录成功');

    // 可以跳转到首页或保持当前页面
    // router.push('/');
  } catch (error) {
    showToast('登录失败，请检查账号密码或网络连接');
    console.error('登录错误:', error);
  }
};
const handleRegister = () => {
  router.push('/register');
}

const checkLoginStatus = async () => {
  try {
    // 先检查本地存储中的认证信息
    const sessionId = localStorage.getItem('sessionId');
    const token = localStorage.getItem('token');
    const storedUser = localStorage.getItem('currentUser');

    // 如果有sessionId或token，尝试通过API获取当前用户信息
    if (sessionId || token) {
      try {
        const user = await getCurrentUser();
        if (user) {
          userInfo.value = user;
          isLoggedIn.value = true;
          localStorage.setItem('currentUser', JSON.stringify(user));
          console.log('通过混合认证获取用户信息成功');
        } else {
          console.warn('API返回空用户信息');
          isLoggedIn.value = false;
          // 清除所有认证信息
          localStorage.removeItem('token');
          localStorage.removeItem('sessionId');
          localStorage.removeItem('currentUser');
        }
      } catch (error) {
        console.error('通过混合认证获取用户信息失败:', error);
        isLoggedIn.value = false;
        // 清除所有认证信息
        localStorage.removeItem('token');
        localStorage.removeItem('sessionId');
        localStorage.removeItem('currentUser');
      }
    } else {
      console.log('未找到认证信息，用户未登录');
      isLoggedIn.value = false;
    }
  } catch (error) {
    console.error('检查登录状态失败:', error);
    isLoggedIn.value = false;
    if (error.code === 'ERR_NETWORK') {
      showToast('无法连接到服务器，请检查网络连接');
    }
  }
};

const handleLogout = async () => {
  try {
    await userLogout();
    // 清除本地存储中的用户信息、token和sessionId
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    localStorage.removeItem('sessionId'); // 新增：清除sessionId
    userInfo.value = null;
    isLoggedIn.value = false;
    showToast('已退出登录');
    router.push('/');
  } catch (error) {
    showToast('退出登录失败');
    console.error('退出登录错误:', error);
  }
};


// 处理头像点击
const handleAvatarClick = () => {
  router.push('/user/edit');
};

// 页面加载时检查登录状态
onMounted(() => {
  checkLoginStatus();
});
</script>

<style scoped>
.user-container {
  padding: 20px;
}

/* 登录表单样式 */
.login-container {
  max-width: 400px;
  margin: 0 auto;
}
.form-group {
  margin-bottom: 15px;
}
input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}
.login-btn {
  width: 100%;
  padding: 10px;
  background-color: #646cff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.register-btn {
  width: 100%;
  padding: 10px;
  background-color: white;
  color: blue;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}



/* 用户信息展示样式 */
.user-header {
  display: flex;
  align-items: center;
  padding: 20px;
  background-color: #fff;
}

.avatar-container {
  cursor: pointer;
  margin-right: 20px;
}

.avatar-img {
  width: 80px !important;
  height: 80px !important;
  object-fit: cover;
}

.user-basic-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
}

.user-name {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #333;
}

.user-account {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.logout-btn {
  padding: 8px 16px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>