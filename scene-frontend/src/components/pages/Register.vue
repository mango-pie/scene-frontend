<template>
  <div class="register-page">
    <!-- 页面头部 -->
    <van-nav-bar
        title="用户注册"
        left-text="返回"
        left-arrow
        @click-left="handleBack"
    />

    <!-- 注册表单 -->
    <div class="register-container">
      <!-- 注册标题 -->
      <div class="register-header">
        <h2 class="register-title">创建新账号</h2>
        <p class="register-subtitle">加入我们，开启精彩旅程</p>
      </div>

      <!-- 表单区域 -->
      <van-form @submit="handleRegister" class="register-form">
        <!-- 用户名 -->
        <van-cell-group inset>
          <van-field
              v-model="formData.userAccount"
              name="userAccount"
              label="用户名"
              placeholder="请输入用户名"
              :rules="[{ required: true, message: '请输入用户名' }]"
              left-icon="user-o"
          />
        </van-cell-group>

        <!-- 密码 -->
        <van-cell-group inset>
          <van-field
              v-model="formData.userPassword"
              type="password"
              name="userPassword"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
              left-icon="lock"
          />
        </van-cell-group>

        <!-- 确认密码 -->
        <van-cell-group inset>
          <van-field
              v-model="formData.checkPassword"
              type="password"
              name="checkPassword"
              label="确认密码"
              placeholder="请再次输入密码"
              :rules="[{ required: true, message: '请确认密码' }, { validator: validatePassword, message: '两次密码输入不一致' }]"
              left-icon="lock"
          />
        </van-cell-group>

        <!-- 注册按钮 -->
        <div style="margin: 16px;">
          <van-button
              round
              block
              type="primary"
              native-type="submit"
              :loading="loading"
              class="register-btn"
          >
            {{ loading ? '注册中...' : '立即注册' }}
          </van-button>
        </div>

        <!-- 登录链接 -->
        <div class="login-link">
          <span>已有账号？</span>
          <van-button type="primary" size="small" text="立即登录" @click="handleLogin" />
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userRegister } from '../../api/user.js'
import { showToast, showSuccessToast, showFailToast } from 'vant'

const router = useRouter()
const loading = ref(false)

// 表单数据
const formData = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

// 密码验证
const validatePassword = (val) => {
  return val === formData.userPassword
}

// 返回处理
const handleBack = () => {
  router.back()
}

// 跳转到登录页面
const handleLogin = () => {
  router.push('/user')
}

// 注册处理
const handleRegister = async () => {
  if (formData.userPassword !== formData.checkPassword) {
    showFailToast('两次密码输入不一致')
    return
  }

  loading.value = true

  try {
    const result = await userRegister({
      userAccount: formData.userAccount,
      userPassword: formData.userPassword,
      checkPassword: formData.checkPassword
    })

    if (result) {
      showSuccessToast('注册成功！')
      // 注册成功后跳转到登录页面
      setTimeout(() => {
        router.push('/user')
      }, 1500)
    } else {
      showFailToast('注册失败，请重试')
    }
  } catch (error) {
    console.error('注册失败:', error)
    showFailToast('注册失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-container {
  padding: 20px;
  margin-top: 20px;
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
  color: white;
}

.register-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.register-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.register-form {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

:deep(.van-cell-group--inset) {
  margin: 16px 0;
}

:deep(.van-field__label) {
  width: 80px;
  color: #333;
  font-weight: 500;
}

:deep(.van-field__control) {
  color: #333;
}

.register-btn {
  height: 50px;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 25px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.register-btn:active {
  transform: translateY(2px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.login-link {
  text-align: center;
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 12px;
  font-size: 14px;
  color: #666;
}

.login-link span {
  margin-right: 8px;
}

:deep(.van-nav-bar) {
  background: transparent;
}

:deep(.van-nav-bar__title) {
  color: white;
  font-weight: 600;
}

:deep(.van-nav-bar__text) {
  color: white;
}

:deep(.van-nav-bar .van-icon) {
  color: white;
}
</style>