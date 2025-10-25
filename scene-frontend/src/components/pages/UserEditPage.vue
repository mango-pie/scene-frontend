<script setup>
import { ref, onMounted, computed } from 'vue';
import { Cell, CellGroup, Tag, Button, Divider, Image as VanImage, Popup, Field, RadioGroup, Radio } from 'vant';
import userService from '../../services/userService';

// 用户信息数据
const userInfo = ref(null);
// 控制弹出层显示状态
const showPopup = ref(false);
const showEditAccountPopup = ref(false);
const showEditEmailPopup = ref(false);
const showEditPhonePopup = ref(false);
const showEditGenderPopup = ref(false);
const showEditProfilePopup = ref(false);
const showChangePasswordPopup = ref(false);
const showChangeAvatarPopup = ref(false);

// 表单数据
const formData = ref({
  account: '',
  email: '',
  phone: '',
  gender: 1,
  username: '',
  plantCode: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 计算属性：格式化显示字段
const userGender = computed(() => {
  return userInfo.value?.gender === 1 ? '男' : '女';
});

const userStatus = computed(() => {
  return userInfo.value?.userStatus === 1 ? '正常' : '禁用';
});

const userRole = computed(() => {
  return userInfo.value?.userRole === 1 ? '管理员' : '普通用户';
});

const formattedCreateTime = computed(() => {
  if (!userInfo.value?.createTime) return '';
  const date = new Date(userInfo.value.createTime);
  return date.toLocaleString('zh-CN');
});

// 获取用户信息（模拟数据）
const fetchUserInfo = () => {
  try {
    const user = JSON.parse(localStorage.getItem('currentUser'));
    userInfo.value = user;
    // 初始化表单数据
    formData.value = {
      account: user.userAccount || '',
      email: user.email || '',
      phone: user.phone || '',
      gender: user.gender || 1,
      username: user.username || '',
      plantCode: user.plantCode || '',
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }
};

// 打开修改弹窗时同步数据
const openEditPopup = (popupName) => {
  if (userInfo.value) {
    formData.value.account = userInfo.value.userAccount || '';
    formData.value.email = userInfo.value.email || '';
    formData.value.phone = userInfo.value.phone || '';
    formData.value.gender = userInfo.value.gender || 1;
    formData.value.username = userInfo.value.username || '';
    formData.value.plantCode = userInfo.value.plantCode || '';
  }
  switch(popupName) {
    case 'account':
      showEditAccountPopup.value = true;
      break;
    case 'email':
      showEditEmailPopup.value = true;
      break;
    case 'phone':
      showEditPhonePopup.value = true;
      break;
    case 'gender':
      showEditGenderPopup.value = true;
      break;
    case 'profile':
      showEditProfilePopup.value = true;
      break;
    case 'password':
      showChangePasswordPopup.value = true;
      break;
    case 'avatar':
      showChangeAvatarPopup.value = true;
      break;
  }
};

// 关闭弹出层
const closePopup = (popupName) => {
  if (!popupName) {
    showPopup.value = false;
    return;
  }

  switch(popupName) {
    case 'account':
      showEditAccountPopup.value = false;
      break;
    case 'email':
      showEditEmailPopup.value = false;
      break;
    case 'phone':
      showEditPhonePopup.value = false;
      break;
    case 'gender':
      showEditGenderPopup.value = false;
      break;
    case 'profile':
      showEditProfilePopup.value = false;
      break;
    case 'password':
      showChangePasswordPopup.value = false;
      break;
    case 'avatar':
      showChangeAvatarPopup.value = false;
      break;
  }
};

// 模拟保存操作
const handleSave = (popupName) => {
  // 仅做演示，不实际修改数据
  closePopup(popupName);
};

// 头像点击事件
const handleAvatarClick = () => {
  showPopup.value = true;
};

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo();
});
</script>

<template>
  <div class="user-profile-page">
    <!-- 用户信息头部 -->
    <div class="user-header">
      <div class="avatar-container" @click="handleAvatarClick">
        <van-image
            round
            size="100"
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
        <h2 class="user-name">{{ userInfo?.username }}</h2>
        <p class="user-account">账号：{{ userInfo?.userAccount }}</p>
        <div class="user-meta">
          <Tag type="primary" size="small">{{ userStatus }}</Tag>
          <Tag type="default" size="small" style="margin-left: 8px">{{ userRole }}</Tag>
        </div>
      </div>
    </div>

    <Divider />

    <!-- 基本信息区域 -->
    <div class="section">
      <h3 class="section-title">基本信息</h3>
      <van-cell-group inset>
        <van-cell title="用户ID" :value="userInfo?.id || '-'" />
        <van-cell title="账号" is-link :value="userInfo?.userAccount || '-'" @click="openEditPopup('account')" />
        <van-cell title="邮箱" is-link :value="userInfo?.email || '-'" @click="openEditPopup('email')" />
        <van-cell title="手机号码" is-link :value="userInfo?.phone || '-'" @click="openEditPopup('phone')" />
        <van-cell title="性别" is-link :value="userGender" @click="openEditPopup('gender')" />
        <van-cell title="工厂代码" :value="userInfo?.plantCode || '-'" />
      </van-cell-group>
    </div>

    <Divider />

    <!-- 系统信息区域 -->
    <div class="section">
      <h3 class="section-title">系统信息</h3>
      <CellGroup inset>
        <Cell title="创建时间" :value="formattedCreateTime || '-'" />
        <Cell title="用户状态" :value="userStatus" />
        <Cell title="用户角色" :value="userRole" />
      </CellGroup>
    </div>

    <Divider />

    <!-- 用户标签区域 -->
    <div class="section" v-if="userInfo?.tags?.length > 0">
      <h3 class="section-title">用户标签</h3>
      <div class="tags-container">
        <Tag v-for="tag in userInfo.tags" :key="tag" type="default" style="margin: 8px 8px 0 0;">
          {{ tag }}
        </Tag>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
      <van-cell title="编辑个人信息" is-link @click="openEditPopup('profile')"></van-cell>
      <van-cell title="修改密码" is-link @click="openEditPopup('password')"></van-cell>
      <van-cell></van-cell>
    </div>

    <!-- 头像预览弹出层 -->
    <van-popup
        v-model:show="showPopup"
        position="bottom"
        round
        :overlay-style="{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }"
        teleport="body"
    >
      <div class="popup-content">
        <h3 class="popup-title">头像预览</h3>
        <div class="avatar-preview">
          <VanImage
              round
              size="180"
              fit="cover"
              :src="userInfo?.avatarUrl || 'https://img.yzcdn.cn/vant/default-user-image.png'"
              class="preview-avatar-img"
          />
        </div>
        <div class="action-buttons">
          <van-cell title="更换头像" is-link @click="() => { closePopup(); openEditPopup('avatar'); }"></van-cell>
          <van-cell title="关闭" is-link @click="closePopup"></van-cell>
        </div>
      </div>
    </van-popup>

    <!-- 账号修改弹出层 - 移除固定高度 -->
    <van-popup
        v-model:show="showEditAccountPopup"
        position="bottom"
        round
        :overlay-style="{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }"
        teleport="body"
    >
      <div class="popup-content">
        <h3 class="popup-title">修改账号</h3>
        <div class="form-container">
          <van-field
              v-model="formData.account"
              label="账号"
              placeholder="请输入账号"
              clearable
          />
        </div>
        <div class="popup-buttons">
          <van-button round type="default" @click="closePopup('account')" style="margin-right: 10px;">取消</van-button>
          <van-button round type="primary" @click="handleSave('account')">保存</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 邮箱修改弹出层 - 移除固定高度 -->
    <van-popup
        v-model:show="showEditEmailPopup"
        position="bottom"
        round
        :overlay-style="{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }"
        teleport="body"
    >
      <div class="popup-content">
        <h3 class="popup-title">修改邮箱</h3>
        <div class="form-container">
          <van-field
              v-model="formData.email"
              label="邮箱"
              placeholder="请输入邮箱"
              type="email"
              clearable
          />
          <van-field
              label="验证码"
              placeholder="请输入验证码"
              clearable
          >
            <template #button>
              <van-button size="small" type="primary">获取验证码</van-button>
            </template>
          </van-field>
        </div>
        <div class="popup-buttons">
          <van-button round type="default" @click="closePopup('email')" style="margin-right: 10px;">取消</van-button>
          <van-button round type="primary" @click="handleSave('email')">保存</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 手机号修改弹出层 - 移除固定高度 -->
    <van-popup
        v-model:show="showEditPhonePopup"
        position="bottom"
        round
        :overlay-style="{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }"
        teleport="body"
    >
      <div class="popup-content">
        <h3 class="popup-title">修改手机号码</h3>
        <div class="form-container">
          <van-field
              v-model="formData.phone"
              label="手机号"
              placeholder="请输入手机号"
              type="tel"
              clearable
          />
          <van-field
              label="验证码"
              placeholder="请输入验证码"
              clearable
          >
            <template #button>
              <van-button size="small" type="primary">获取验证码</van-button>
            </template>
          </van-field>
        </div>
        <div class="popup-buttons">
          <van-button round type="default" @click="closePopup('phone')" style="margin-right: 10px;">取消</van-button>
          <van-button round type="primary" @click="handleSave('phone')">保存</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 性别修改弹出层 - 移除固定高度 -->
    <van-popup
        v-model:show="showEditGenderPopup"
        position="bottom"
        round
        :overlay-style="{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }"
        teleport="body"
    >
      <div class="popup-content">
        <h3 class="popup-title">选择性别</h3>
        <div class="form-container">
          <van-radio-group v-model="formData.gender" direction="horizontal">
            <van-radio name="1">男</van-radio>
            <van-radio name="0">女</van-radio>
          </van-radio-group>
        </div>
        <div class="popup-buttons">
          <van-button round type="default" @click="closePopup('gender')" style="margin-right: 10px;">取消</van-button>
          <van-button round type="primary" @click="handleSave('gender')">保存</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 编辑个人信息弹出层 - 移除固定高度 -->
    <van-popup
        v-model:show="showEditProfilePopup"
        position="bottom"
        round
        :overlay-style="{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }"
        teleport="body"
    >
      <div class="popup-content">
        <h3 class="popup-title">编辑个人信息</h3>
        <div class="form-container">
          <van-field
              v-model="formData.username"
              label="用户名"
              placeholder="请输入用户名"
              clearable
          />
          <van-field
              v-model="formData.plantCode"
              label="工厂代码"
              placeholder="请输入工厂代码"
              clearable
          />
          <van-field
              v-model="formData.email"
              label="邮箱"
              placeholder="请输入邮箱"
              type="email"
              clearable
          />
          <van-field
              v-model="formData.phone"
              label="手机号"
              placeholder="请输入手机号"
              type="tel"
              clearable
          />
        </div>
        <div class="popup-buttons">
          <van-button round type="default" @click="closePopup('profile')" style="margin-right: 10px;">取消</van-button>
          <van-button round type="primary" @click="handleSave('profile')">保存</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 修改密码弹出层 - 移除固定高度 -->
    <van-popup
        v-model:show="showChangePasswordPopup"
        position="bottom"
        round
        :overlay-style="{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }"
        teleport="body"
    >
      <div class="popup-content">
        <h3 class="popup-title">修改密码</h3>
        <div class="form-container">
          <van-field
              v-model="formData.oldPassword"
              label="原密码"
              placeholder="请输入原密码"
              type="password"
              clearable
          />
          <van-field
              v-model="formData.newPassword"
              label="新密码"
              placeholder="请输入新密码"
              type="password"
              clearable
          />
          <van-field
              v-model="formData.confirmPassword"
              label="确认密码"
              placeholder="请再次输入新密码"
              type="password"
              clearable
          />
        </div>
        <div class="popup-buttons">
          <van-button round type="default" @click="closePopup('password')" style="margin-right: 10px;">取消</van-button>
          <van-button round type="primary" @click="handleSave('password')">保存</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 更换头像弹出层 - 移除固定高度 -->
    <van-popup
        v-model:show="showChangeAvatarPopup"
        position="bottom"
        round
        :overlay-style="{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }"
        teleport="body"
    >
      <div class="popup-content">
        <h3 class="popup-title">更换头像</h3>
        <div class="avatar-upload-container">
          <div class="current-avatar">
            <VanImage
                round
                size="100"
                fit="cover"
                :src="userInfo?.avatarUrl || 'https://img.yzcdn.cn/vant/default-user-image.png'"
                class="preview-avatar-img"
            />
          </div>
          <div class="upload-options">
            <van-button round type="primary" block>
              从相册选择
            </van-button>
            <van-button round type="default" block style="margin-top: 10px;">
              拍摄照片
            </van-button>
          </div>
        </div>
        <div class="popup-buttons">
          <van-button round type="default" @click="closePopup('avatar')" style="margin-right: 10px;">取消</van-button>
          <van-button round type="primary" @click="handleSave('avatar')">确认更换</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<style scoped>
.user-profile-page {
  background-color: #f8f8f8;
  min-height: 100vh;
}

.user-header {
  background-color: #fff;
  padding: 30px 20px;
  display: flex;
  align-items: center;
}

.avatar-container {
  cursor: pointer;
  transition: transform 0.2s;
}

.avatar-container:active {
  transform: scale(0.95);
}

/* 头像样式修复 */
.avatar-img {
  width: 100px !important;
  height: 100px !important;
  object-fit: cover;
}

.user-basic-info {
  margin-left: 20px;
}

.user-name {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 8px 0;
}

.user-account {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px 0;
}

.user-meta {
  display: flex;
  align-items: center;
}

.section {
  background-color: #fff;
  margin-bottom: 10px;
  padding: 16px 20px 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 12px 0;
  color: #333;
}

.tags-container {
  padding: 8px 0;
  display: flex;
  flex-wrap: wrap;
}

.action-buttons {
  margin: 16px;
  /* 垂直排列单元格 */
  display: flex;
  flex-direction: column;
  gap: 4px; /* 两行之间的间距 */
}

/* 确保文字靠左并调整内边距 */
.action-cell {
  /* 强制文字左对齐 */
  text-align: left !important;
  /* 可根据需要调整内边距 */
  padding: 12px 16px !important;
}

/* 适配不同屏幕尺寸 */
@media screen and (max-width: 768px) {
  .user-header {
    flex-direction: column;
    text-align: center;
  }

  .user-basic-info {
    margin-left: 0;
    margin-top: 16px;
  }
}

/* 弹出层核心样式优化 - 自适应高度 */
.popup-content {
  padding: 20px;
  background-color: #fff;
  border-radius: 16px 16px 0 0;
  /* 移除固定高度和flex布局限制，让内容自然撑开 */
}

.popup-title {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

/* 移除表单容器的滚动和flex限制 */
.form-container {
  margin-bottom: 20px;
}

.popup-buttons {
  display: flex;
  gap: 10px; /* 按钮间距 */
}

.popup-buttons :deep(.van-button) {
  flex: 1; /* 按钮平均分配宽度 */
}

.avatar-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  margin-bottom: 20px;
  background-color: #f8f8f8;
  border-radius: 8px;
}

/* 预览头像样式修复 */
.preview-avatar-img {
  width: 180px !important;
  height: 180px !important;
  object-fit: cover;
}

/* 头像上传区域样式 */
.avatar-upload-container {
  margin-bottom: 20px;
}

.current-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.upload-options {
  width: 100%;
}

/* 强制圆形显示样式 */
:deep(.van-image--round) {
  border-radius: 50% !important;
  overflow: hidden !important;
}

:deep(.van-image__img) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
}

/* 确保底部弹窗自适应高度，不被截断 */
:deep(.van-popup--bottom) {
  position: fixed !important;
  bottom: 0 !important;
  left: 0 !important;
  right: 0 !important;
  max-height: 90vh !important; /* 限制最大高度为屏幕的90%，避免内容过多溢出屏幕 */
  height: auto !important; /* 强制高度自适应 */
}

:deep(.van-radio-group) {
  padding: 10px 0;
}

:deep(.van-radio) {
  margin-right: 20px;
  font-size: 16px;
}

/* 调整表单字段间距 */
:deep(.van-field) {
  margin-bottom: 8px;
}

.user-profile-page {
  background-color: #f8f8f8;
  min-height: 100vh;
}

.user-header {
  background-color: #fff;
  padding: 30px 20px;
  display: flex;
  align-items: center;
}

.avatar-container {
  cursor: pointer;
  transition: transform 0.2s;
}

.avatar-container:active {
  transform: scale(0.95);
}

/* 头像样式修复 */
.avatar-img {
  width: 100px !important;
  height: 100px !important;
  object-fit: cover;
}

.user-basic-info {
  margin-left: 20px;
}

.user-name {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 8px 0;
}

.user-account {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px 0;
}

.user-meta {
  display: flex;
  align-items: center;
}

.section {
  background-color: #fff;
  margin-bottom: 10px;
  padding: 16px 20px 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 12px 0;
  color: #333;
}

.tags-container {
  padding: 8px 0;
  display: flex;
  flex-wrap: wrap;
}

.action-buttons {
  margin: 16px;
  /* 垂直排列单元格 */
  display: flex;
  flex-direction: column;
  gap: 4px; /* 两行之间的间距 */
}

/* 确保文字靠左并调整内边距 */
.action-cell {
  /* 强制文字左对齐 */
  text-align: left !important;
  /* 可根据需要调整内边距 */
  padding: 12px 16px !important;
}

/* 适配不同屏幕尺寸 */
@media screen and (max-width: 768px) {
  .user-header {
    flex-direction: column;
    text-align: center;
  }

  .user-basic-info {
    margin-left: 0;
    margin-top: 16px;
  }
}

/* 弹出层样式 */
.popup-content {
  padding: 20px;
  background-color: #fff;
  border-radius: 16px 16px 0 0;
}

.popup-title {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.avatar-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 240px;
  margin-bottom: 30px;
  background-color: #f8f8f8;
  border-radius: 8px;
}

/* 预览头像样式修复 */
.preview-avatar-img {
  width: 180px !important;
  height: 180px !important;
  object-fit: cover;
}

.popup-actions {
  margin-top: 10px;
}

/* 强制圆形显示样式 */
:deep(.van-image--round) {
  border-radius: 50% !important;
  overflow: hidden !important;
}

:deep(.van-image__img) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
}

:deep(.van-popup--bottom) {
  position: fixed !important;
  bottom: 0 !important;
  left: 0 !important;
  right: 0 !important;
}
</style>