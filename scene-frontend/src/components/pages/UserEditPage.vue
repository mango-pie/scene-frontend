<script setup>
import {computed, onMounted, ref} from 'vue';
import {Button, Cell, CellGroup, Divider, Image as VanImage, showToast, showLoadingToast, Tag} from 'vant';
import {changePassword, getCurrentUser, updateUser, userLogout,updateUserAvatar} from '../../api/user.js'
import {searchTags} from '../../api/tag.js'
import {convertTagsToTree} from '../../utils/tag.js'
import {useRouter} from "vue-router";
// 用户信息数据
const userInfo = ref(null);
const router = useRouter();
// 控制弹出层显示状态
const showPopup = ref(false);
const showEditAccountPopup = ref(false);
const showEditEmailPopup = ref(false);
const showEditPhonePopup = ref(false);
const showEditGenderPopup = ref(false);
const showEditProfilePopup = ref(false);
const showChangePasswordPopup = ref(false);
const showChangeAvatarPopup = ref(false);
const showEditTagsPopup = ref(false);

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
  confirmPassword: '',
  tags: [] // 初始化标签数组
});


const showImageEditPopup = ref(false);
const imageFileList = ref([]);
const imageForm = ref({
  avatarUrl: '' // 用于存储图片URL
});

// 打开图片修改弹窗时初始化数据
const openImageEditPopup = (currentUrl) => {
  imageForm.value.avatarUrl = currentUrl;
  // 初始化已有的图片预览
  if (currentUrl) {
    imageFileList.value = [{ url: currentUrl }];
  } else {
    imageFileList.value = [];
  }
  showImageEditPopup.value = true;
};

// 处理图片上传
// const handleImageUpload = async (file) => {
//   showLoadingToast('上传中...');
//   file.status = 'uploading';
//
//   try {
//     const formData = new FormData();
//     formData.append('file', file.file);
//
//     // 调用后端上传接口
//     const response = await updateUserAvatar(formData, {
//       headers: {
//         'Content-Type': 'multipart/form-data'
//       }
//     });
//
//     if (response.success && response.data) {
//       imageForm.value.avatarUrl = response.data;
//       file.status = 'done';
//       showToast('上传成功');
//     } else {
//       throw new Error('上传失败');
//     }
//   } catch (error) {
//     file.status = 'failed';
//     showToast('上传失败，请重试');
//     console.error('图片上传错误:', error);
//   }
// };
// 处理图片上传
const handleImageUpload = async (file) => {
  showLoadingToast('上传中...');
  file.status = 'uploading';

  try {
    const formData = new FormData();
    formData.append('file', file.file);

    // 调用后端上传接口 - 移除错误的headers参数
    const response = await updateUserAvatar(formData);

    if (response && response.data) {
      imageForm.value.avatarUrl = response.data;
      file.status = 'done';
      showToast('上传成功');
    } else {
      throw new Error('上传失败');
    }
  } catch (error) {
    file.status = 'failed';
    showToast('上传失败，请重试');
    console.error('图片上传错误:', error);
  }
};

// 处理图片删除
const handleImageDelete = () => {
  imageForm.value.avatarUrl = '';
  showToast('已移除图片');
};

// 保存图片修改
const handleSaveImage = async () => {
  try {
    // 调用后端接口保存图片URL（根据实际业务调整）
    // 示例：如果是修改用户头像
    // await updateUser({ avatarUrl: imageForm.value.avatarUrl });
    // 如果是修改队伍头像
    // await updateTeam({ id: currentTeamId, avatarUrl: imageForm.value.avatarUrl });

    showToast('图片修改成功');
    showImageEditPopup.value = false;
    // 刷新数据
    await fetchUserInfo();
  } catch (error) {
    showToast('保存失败，请重试');
    console.error('保存图片失败:', error);
  }
};















// 标签搜索相关变量
const tagSearchInput = ref(''); // 标签搜索输入框
const searchResults = ref([]); // 搜索结果
const showSearchResults = ref(false); // 是否显示搜索结果
const originalTags = ref([]); // 原始标签数据
const hasSearched = ref(false); // 标记是否已经进行过搜索
const searchText = ref(''); // 存储当前搜索文本

const value = ref('');
const activeIds = ref([]);
const activeIndex = ref(0);

// 计算属性：根据搜索文本过滤标签
const filteredTags = computed(() => {
  // 如果没有搜索过或搜索文本为空，显示所有标签
  if (!hasSearched.value || !searchText.value.trim()) {
    return originalTags.value;
  }

  const searchValue = searchText.value.trim().toLowerCase();

  // 对每个父标签进行过滤
  return originalTags.value.map(parentTag => {
    // 如果父标签没有children，保持原样
    if (!parentTag.children || !Array.isArray(parentTag.children)) {
      // 检查父标签文本是否包含搜索词
      if (parentTag.text && parentTag.text.toLowerCase().includes(searchValue)) {
        return parentTag;
      }
      return null; // 不包含搜索词的父标签返回null，稍后会过滤掉
    }

    // 过滤子标签
    const filteredChildren = parentTag.children.filter(child =>
        child && child.text && child.text.toLowerCase().includes(searchValue)
    );

    // 如果父标签有匹配的子标签，返回包含这些子标签的父标签
    if (filteredChildren.length > 0) {
      return {
        ...parentTag,
        children: filteredChildren
      };
    }

    return null; // 没有匹配子标签的父标签返回null
  }).filter(tag => tag !== null); // 过滤掉返回null的标签
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

const fetchUserInfo = async () => {
  try {
    const user = await getCurrentUser();
    if (user) {
      userInfo.value = user;
      // 初始化表单数据
      formData.value = {
        account: user.userAccount || '',
        email: user.email || '',
        phone: user.phone || '',
        gender: user.gender,
        username: user.username || '',
        plantCode: user.plantCode || '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
        tags: user.tagList ? [...user.tagList] : [] // 正确初始化标签
      };
      activeIds.value = formData.value.tags;
    } else {
      console.error('获取用户信息失败：用户未登录');
      showToast('用户未登录，请重新登录');
      router.push('/user');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    showToast('获取用户信息失败，请检查网络连接');
    // 如果获取失败，跳转到登录页面
    router.push('/user');
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
    // 同步用户标签数据
    formData.value.tags = userInfo.value.tagList ? [...userInfo.value.tagList] : [];
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
    case 'tags':
      showEditTagsPopup.value = true;
      break;
  }
};

// 关闭弹出层 - 修复标签弹窗关闭逻辑
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
    case 'tags':
      showEditTagsPopup.value = false; // 修复关闭标签弹窗
      break;
  }
};

// 保存操作 - 修复标签保存逻辑
const handleSave = (popupName) => {
  switch(popupName) {
    case 'account':
      updateUsers({username: formData.value.username, id: userInfo.value.id});
      break;
    case 'email':
      updateUsers({email: formData.value.email, id: userInfo.value.id});
      break;
    case 'phone':
      updateUsers({phone: formData.value.phone, id: userInfo.value.id});
      break;
    case 'gender':
      updateUsers({gender: formData.value.gender, id: userInfo.value.id});
      break;

    case 'password':

         changePasswordResult({
          oldPassword: formData.value.oldPassword,
          newPassword: formData.value.newPassword,
          userId: userInfo.value.id
         }).then(result => {
          if (result === 1) {
            showToast('密码更新成功');
          } else {
            showToast('密码更新失败，请重试');
          }
         });
      break;
    case 'avatar':
      // 头像上传功能待实现
      break;
    case 'tags':
      // 保存用户标签 - 添加用户ID
      updateUsers({
        tagList: activeIds.value,
        id: userInfo.value.id
      });
      break;
  }
  closePopup(popupName);
};
const changePasswordResult = async (data) => {
  if(!formData.value.oldPassword || !formData.value.newPassword || !formData.value.confirmPassword) {
    showToast('请填写完整密码信息');
    return null;
  }
  if (formData.value.newPassword !== formData.value.confirmPassword) {
    showToast('两次输入的新密码不一致');
    return null;
  }
  try {
    const result = await changePassword(data);
    if (result === 1) {
      showToast('密码更新成功，退出登录');
      //退出登录
      try {
        await userLogout();
        // 清除本地存储中的认证信息
        localStorage.removeItem('token');
        localStorage.removeItem('sessionId');
        userInfo.value = null;
        showToast('已退出登录');
        router.push('/');
      } catch (error) {
        showToast('退出登录失败');
        console.error('退出登录错误:', error);
      }
    } else {
      showToast('密码更新失败，请重试');
    }
    return result;
  } catch (error) {
    console.error('修改密码失败:', error);
    showToast('修改密码失败');
    return null;
  }
};
const updateUsers = async (user) => {
  try {
    // 确保性别数据类型正确
    if (user.gender !== undefined) {
      user.gender = Number(user.gender);
    }
    const result = await updateUser(user, localStorage.getItem('sessionId'));
    if (result === 1) {
      // 立即更新本地状态
      if (userInfo.value) {
        userInfo.value = {
          ...userInfo.value,
          ...user
        };
      }
      // 不再保存到localStorage，而是重新从API获取最新数据
      await fetchUserInfo();
      showToast('更新成功');

    } else {
      showToast('更新失败，请重试');
      console.error('更新失败，返回结果:', result);
    }
  } catch (error) {
    console.error('更新用户信息失败:', error);
    showToast('更新用户信息失败');
  }
};

// 头像点击事件
const handleAvatarClick = () => {
  showPopup.value = true;
};

const searchExistingTags = async () => {
  const searchValue = tagSearchInput.value.trim();

  try {
    // 总是获取所有标签数据
    const tags = await searchTags();

    // 转换为树形结构
    const convertedTags = convertTagsToTree(tags);
    originalTags.value = convertedTags;

    // 存储搜索文本并标记已搜索
    if (searchValue) {
      searchText.value = searchValue;
      hasSearched.value = true;
      showToast(`找到 ${getMatchedTagsCount()} 个匹配项`);
    } else {
      hasSearched.value = false;
    }

    showSearchResults.value = true;
  } catch (error) {
    console.error('搜索标签失败:', error);
    showToast('搜索标签失败');
  }
};

// 计算匹配的标签数量
const getMatchedTagsCount = () => {
  return filteredTags.value
      .filter(parent => parent.children && Array.isArray(parent.children))
      .flatMap(parent => parent.children).length;
};

// 添加选中的标签
const addSelectedTag = (tag) => {
  if (!formData.value.tags.includes(tag)) {
    formData.value.tags.push(tag);
  }
};

// 移除标签
const removeTag = (index) => {
  formData.value.tags.splice(index, 1);
};

const onSelectTag = (selectedIds) => {
  // 清空现有标签，防止重复添加
  formData.value.tags = [];
  // 如果没有选中任何标签，直接返回
  if (!selectedIds || selectedIds.length === 0) {
    return;
  }

  // 将选中的标签ID添加到formData.tags中
  // 这里假设activeIds直接就是标签名称数组，如果是ID则需要额外处理
  selectedIds.forEach(id => {
    // 避免重复添加标签
    if (!formData.value.tags.includes(id)) {
      formData.value.tags.push(id);
    }
  });

  console.log('已选择的标签:', formData.value.tags);
};

// 递归查找标签ID对应的标签名
const findTagById = (tags, tagId, callback) => {
  for (const tag of tags) {
    if (tag.children && Array.isArray(tag.children)) {
      const found = tag.children.find(child => child.id === tagId);
      if (found) {
        callback(found.text);
        return;
      }
      findTagById(tag.children, tagId, callback);
    }
  }
};

// 取消搜索
const onCancelSearch = () => {
  hasSearched.value = false;
  searchText.value = '';
  tagSearchInput.value = '';
};

const tagColors = [
  '#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', '#feca57',
  '#ff9ff3', '#54a0ff', '#5f27cd', '#00d2d3', '#ff9f43',
  '#10ac84', '#ee5a24', '#a29bfe', '#fd79a8', '#e17055'
];

// 获取标签颜色
const getTagColor = (index) => {
  return tagColors[index % tagColors.length];
};

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo();
  // 预先加载标签数据
  searchExistingTags();
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
        <van-cell title="昵称" is-link :value="userInfo?.username || '-'" @click="openEditPopup('account')" />
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
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">用户标签</h3>
        <van-button
            type="primary"
            size="small"
            @click="openEditPopup('tags')"
            class="edit-tags-btn"
            icon="edit"
        >
          编辑标签
        </van-button>
      </div>

      <div class="tags-section" v-if="userInfo?.tagList?.length > 0">
        <div class="tags-container">
          <van-tag
              v-for="(tag, index) in userInfo.tagList"
              :key="tag"
              type="primary"
              size="medium"
              class="custom-tag"
              :color="getTagColor(index)"
              text-color="#fff"
          >
            {{ tag }}
          </van-tag>
        </div>
        <p class="tags-count">共 {{ userInfo.tagList.length }} 个标签</p>
      </div>

      <div class="empty-tags" v-else>
        <van-icon name="label-o" size="48" color="#c8c9cc" />
        <p class="empty-text">暂无标签</p>
        <p class="empty-subtext">点击编辑标签添加个性化标签</p>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-buttons">
<!--<van-cell title="编辑个人信息" is-link @click="openEditPopup('profile')"></van-cell>-->
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
              v-model="formData.username"
              label="昵称"
              placeholder="请输入昵称"
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
            <van-radio :name="1">男</van-radio>  <!-- 使用数字类型 -->
            <van-radio :name="0">女</van-radio>  <!-- 使用数字类型 -->
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
<!--        <div class="avatar-upload-container">-->
<!--          <div class="current-avatar">-->
<!--            <VanImage-->
<!--                round-->
<!--                size="100"-->
<!--                fit="cover"-->
<!--                :src="userInfo?.avatarUrl || 'https://img.yzcdn.cn/vant/default-user-image.png'"-->
<!--                class="preview-avatar-img"-->
<!--            />-->
<!--          </div>-->
<!--          <div class="upload-options">-->
<!--            <van-button round type="primary" block>-->
<!--              从相册选择-->
<!--            </van-button>-->
<!--            <van-button round type="default" block style="margin-top: 10px;">-->
<!--              拍摄照片-->
<!--            </van-button>-->
<!--          </div>-->
<!--        </div>-->
<!--        <div class="popup-buttons">-->
<!--          <van-button round type="default" @click="closePopup('avatar')" style="margin-right: 10px;">取消</van-button>-->
<!--          <van-button round type="primary" @click="handleSave('avatar')">确认更换</van-button>-->
<!--        </div>-->
        <van-form @submit="handleSaveImage">
          <van-cell-group inset>
            <!-- 替换原有的URL输入框或新增上传区域 -->
            <van-cell title="图片">
              <template #default>
                <van-uploader
                    v-model="imageFileList"
                    :max-count="1"
                    :after-read="handleImageUpload"
                    accept="image/*"
                    upload-text="从相册选择"
                    @delete="handleImageDelete"
                >
                  <!-- 预览已选中的图片 -->
                  <template #preview="{ file }">
                    <van-image
                        :src="file.url"
                        width="100"
                        height="100"
                        fit="cover"
                        class="image-preview"
                    />
                  </template>
                </van-uploader>
              </template>
            </van-cell>

            <!-- 保留可选的URL输入框（供手动输入） -->
<!--            <van-field-->
<!--                v-model="imageForm.avatarUrl"-->
<!--                label="图片URL"-->
<!--                placeholder="也可直接输入图片链接"-->
<!--                type="url"-->
<!--            />-->
          </van-cell-group>

          <div style="margin: 16px;">
            <van-button round block type="primary" native-type="submit">
              保存修改
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>


    <template>
      <!-- 标签编辑弹出层 - 优化布局 -->
      <van-popup
          v-model:show="showEditTagsPopup"
          position="bottom"
          round
          :overlay-style="{ backgroundColor: 'rgba(0, 0, 0, 0.7)' }"
          teleport="body"
          :style="{ height: 'auto', maxHeight: '80vh' }"
      >
        <div class="popup-content">
          <h3 class="popup-title">编辑用户标签</h3>
          <div class="form-container tag-form-container">
            <div class="tag-input-group">
              <van-search
                  v-model="tagSearchInput"
                  show-action
                  placeholder="请输入搜索关键词"
                  @search="searchExistingTags"
                  @cancel="onCancelSearch"
              />

              <!-- 搜索提示 -->
              <div v-if="hasSearched" style="padding: 10px 16px; color: #999;">
                {{ getMatchedTagsCount() > 0 ? '以下是搜索结果：' : '未找到匹配的标签' }}
              </div>

              <!-- 显示搜索结果 -->
              <div v-if="showSearchResults" class="search-results">
                <van-tree-select
                    v-model:active-id="activeIds"
                    v-model:main-active-index="activeIndex"
                    :items="filteredTags"
                />
              </div>
            </div>

            <div class="selected-tags">
              <div class="selected-tags-title">已选标签：</div>
              <van-row v-if="activeIds.length > 0" gutter="16" style="padding: 16px">
                <van-col v-for="tagId in activeIds" :key="tagId">
                  <van-tag closeable size="medium" type="primary" @close="close(tagId)">
                    {{ tagId }}
                  </van-tag>
                </van-col>
              </van-row>
<!--              <div class="tags-scroll-container">-->
<!--                <van-tag-->
<!--                    v-for="(tag, index) in formData.tags"-->
<!--                    :key="index"-->
<!--                    type="primary"-->
<!--                    closable-->
<!--                    @close="removeTag(index)"-->
<!--                    class="selected-tag"-->
<!--                >-->
<!--                  {{ tag }}-->
<!--                </van-tag>-->
<!--                <div v-if="formData.tags.length === 0" class="no-tags-hint">-->
<!--                  暂无已选标签-->
<!--                </div>-->
<!--              </div>-->
            </div>

            <div class="tag-tips">
              <p>提示：标签可以帮助其他用户更好地了解你</p>
              <p v-if="activeIds.length > 0">已选择 {{ activeIds.length }} 个标签</p>
            </div>
          </div>

          <div class="popup-buttons">
            <van-button round type="default" @click="closePopup('tags')">取消</van-button>
            <van-button round type="primary" @click="handleSave('tags')" :disabled="activeIds.length === 0">保存
            </van-button>
          </div>
        </div>
      </van-popup>
    </template>
  </div>
</template>

<style scoped>

.image-preview {
  border-radius: 8px;
  margin-top: 8px;
}

/* 调整上传组件与其他表单元素的间距 */
:deep(.van-uploader) {
  margin-top: 8px;
}

/* 确保弹窗内图片预览不溢出 */
.popup-content {
  overflow-y: auto;
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

/* 标签搜索相关样式优化 */
.tag-form-container {
  max-height: 50vh;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.tag-input-group {
  margin-bottom: 20px;
}

/* 搜索结果区域样式 */
.search-results {
  margin-top: 10px;
  padding: 12px;
  background-color: #f8f8f8;
  border-radius: 8px;
  border: 1px solid #eee;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-results-title,
.selected-tags-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
  font-weight: bold;
  display: flex;
  align-items: center;
}

.search-results-title::before,
.selected-tags-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 16px;
  background-color: #1989fa;
  margin-right: 8px;
  border-radius: 2px;
}

/* 标签容器滚动优化 */
.tags-scroll-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 5px 0;
}

/* 搜索结果标签样式 */
.search-tag {
  margin: 0 !important;
  padding: 4px 10px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: #fff;
  border: 1px solid #e5e5e5;
}

.search-tag:hover {
  background-color: #f0f7ff;
  border-color: #1989fa;
  color: #1989fa;
}

.search-tag:active {
  transform: scale(0.95);
}

/* 已选标签样式 */
.selected-tags {
  margin-bottom: 20px;
  padding: 12px;
  background-color: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
}

.selected-tag {
  margin: 0 !important;
  padding: 5px 12px;
  font-size: 13px;
  transition: all 0.2s ease;
}

.selected-tag::v-deep(.van-tag__close) {
  margin-left: 5px;
  font-size: 14px;
  opacity: 0.7;
}

.selected-tag::v-deep(.van-tag__close:hover) {
  opacity: 1;
}

/* 无标签提示 */
.no-tags-hint {
  color: #999;
  font-size: 13px;
  padding: 15px 0;
  text-align: center;
  width: 100%;
}

/* 标签提示样式 */
.tag-tips {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin-bottom: 15px;
  line-height: 1.5;
}

.tag-tips p {
  margin: 3px 0;
}

/* 弹出层按钮样式优化 */
.popup-buttons {
  display: flex;
  gap: 12px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.popup-buttons :deep(.van-button) {
  flex: 1;
  height: 44px;
  font-size: 16px;
}

/* 标签最大数量限制提示 */
.tag-limit-hint {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 5px;
  text-align: center;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #323233;
  margin: 0;
}

.edit-tags-btn {
  border-radius: 20px;
  padding: 4px 12px;
  font-size: 12px;
}

.tags-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  margin-top: 8px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.custom-tag {
  border-radius: 16px;
  padding: 6px 12px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.custom-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.tags-count {
  font-size: 12px;
  color: #969799;
  text-align: center;
  margin: 0;
}

.empty-tags {
  text-align: center;
  padding: 40px 20px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-top: 8px;
}

.empty-text {
  font-size: 16px;
  color: #969799;
  margin: 16px 0 8px;
}

.empty-subtext {
  font-size: 14px;
  color: #c8c9cc;
  margin: 0;
}

/* 标签颜色函数 */
:deep(.custom-tag) {
  /* 动态颜色将在JavaScript中设置 */
}
</style>