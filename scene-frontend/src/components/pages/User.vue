<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { showToast } from 'vant';
import { userLogin, getCurrentUser, userLogout } from '../../api/user';
import CaptchaInput from '../../components/layouts/CaptchaInput.vue';
import captchaMobileMixin from '../../mixins/captcha-mobile.js';
defineOptions({
  mixins: [captchaMobileMixin]
});
const router = useRouter();
const userInfo = ref(null);
const userAccount = ref('');
const userPassword = ref('');
const isLoggedIn = ref(false);

const loginCaptcha = ref(null);
const loginInput = ref('');


const handleLogin = async () => {
  // 验证验证码
  loginCaptcha.value?.handleVerify();
  if (!loginCaptcha.value?.isVerified) {
    showToast('验证码错误');
    return;
  }

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

    // 保存sessionId到localStorage（如果存在）
    if (result.sessionId) {
      localStorage.setItem('sessionId', result.sessionId);
      console.log('SessionId已保存到localStorage');
    } else {
      // 如果sessionId为null，清除可能存在的旧sessionId
      localStorage.removeItem('sessionId');
      console.log('Redis不可用，仅使用token认证');
    }

    // 直接设置登录状态
    isLoggedIn.value = true;
    userInfo.value = result;

    await checkLoginStatus();
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

    // 如果有sessionId或token，尝试通过API获取当前用户信息
    if (sessionId || token) {
      try {
        const user = await getCurrentUser();
        if (user) {
          userInfo.value = user;
          isLoggedIn.value = true;
          // 不再保存到localStorage
          // localStorage.setItem('currentUser', JSON.stringify(user));
          console.log('通过混合认证获取用户信息成功');
        } else {
          console.warn('API返回空用户信息');
          isLoggedIn.value = false;
          // 清除所有认证信息
          localStorage.removeItem('token');
          localStorage.removeItem('sessionId');
          // localStorage.removeItem('currentUser');
        }
      } catch (error) {
        console.error('通过混合认证获取用户信息失败:', error);
        isLoggedIn.value = false;
        // 清除所有认证信息
        localStorage.removeItem('token');
        localStorage.removeItem('sessionId');
        // localStorage.removeItem('currentUser');
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
    // 清除本地存储中的认证信息，不再清除用户信息
    // localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    localStorage.removeItem('sessionId');
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



<template>
  <div class="user-container">
    <!-- 登录状态检查 -->
    <div v-if="isLoggedIn" class="user-header">
      <div class="avatar-container" @click="handleAvatarClick">
        <van-image
            round
            :size="100"
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

        <!-- 新增用户信息卡片 -->
        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-value">{{ userInfo?.postCount || 0 }}</div>
            <div class="stat-label">帖子</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userInfo?.followerCount || 0 }}</div>
            <div class="stat-label">关注</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ userInfo?.followingCount || 0 }}</div>
            <div class="stat-label">粉丝</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮区域 -->
    <div v-if="isLoggedIn" class="action-buttons">
      <van-button
          round
          type="primary"
          block
          @click="handleAvatarClick"
          class="edit-profile-btn"
      >
        编辑资料
      </van-button>
      <van-button
          round
          type="info"
          block
          @click="handleLogout"
          class="logout-btn"
      >
        退出登录
      </van-button>
    </div>

    <!-- 登录表单 - 移动端优化版 -->
    <div v-else class="login-container">
      <div class="login-header">
        <h2 class="login-title">欢迎回来</h2>
        <p class="login-subtitle">请登录账号继续使用</p>
      </div>

      <div class="form-group">
        <label>账号：</label>
        <input
            v-model="userAccount"
            type="text"
            placeholder="请输入账号"
            class="form-input"
        />
      </div>

      <div class="form-group">
        <label>密码：</label>
        <input
            v-model="userPassword"
            type="password"
            placeholder="请输入密码"
            class="form-input"
        />
      </div>

      <!-- 验证码部分保持原样 -->
      <div class="mobile-form-container">
        <div class="form-group captcha-form-group">
          <label class="captcha-form-label">登录验证码：</label>
          <div class="captcha-wrapper">
            <CaptchaInput
                ref="loginCaptcha"
                class="captcha-mobile"
                v-model="loginInput"
                :length="4"
                placeholder="请输入验证码"
                :hide-refresh-btn="true"
                :hide-verify-btn="true"
            />
            <div class="captcha-tip">点击验证码图片可刷新</div>
          </div>
        </div>
      </div>

      <button @click="handleLogin" class="login-btn">登录</button>
      <button @click="handleRegister" class="register-btn">注册新账号</button>

      <div class="other-options">
        <a class="forgot-password">忘记密码？</a>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import '../../styles/variables.scss';
@import '../../styles/captcha-mobile.scss';

.user-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 登录状态样式 */
.user-header {
  background-color: #fff;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.avatar-container {
  margin-bottom: 20px;
  position: relative;
}

.avatar-img {
  width: 100px !important;
  height: 100px !important;
  border: 3px solid #f0f0f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-basic-info {
  width: 100%;
}

.user-name {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.user-account {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
}

/* 用户统计信息 */
.user-stats {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}

/* 操作按钮 */
.action-buttons {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.edit-profile-btn {
  background-color: #646cff;
}

.logout-btn {
  background-color: #f56c6c;
}

/* 未登录状态样式 */
.login-container {
  padding: 30px 20px;
  max-width: 380px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.2s;

  &:focus {
    border-color: #646cff;
    outline: none;
  }
}

.login-btn, .register-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  box-sizing: border-box;
}

.login-btn {
  background-color: #646cff;
  color: white;
  margin-bottom: 12px;
}

.register-btn {
  background-color: white;
  color: #646cff;
  border: 1px solid #646cff;
  margin-bottom: 20px;
}

.other-options {
  text-align: center;
}

.forgot-password {
  font-size: 14px;
  color: #646cff;
  text-decoration: none;
}

/* 响应式调整 */
@media (max-width: 375px) {
  .user-header {
    padding: 20px 16px;
  }

  .login-container {
    padding: 20px 16px;
  }

  .user-stats {
    gap: 20px;
  }
}
</style>