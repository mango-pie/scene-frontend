<script setup>
import { computed, onMounted, ref } from 'vue';
import { showToast } from 'vant';
// API 导入
import { addTeam, deleteTeam, listTeams, updateTeam } from '../../api/team';
import { getCurrentUser, getUserById } from '../../api/user';

// ==========================================
// 响应式数据定义
// ==========================================

// 队伍列表核心数据
const teamList = ref([]);
const loading = ref(false);
const currentUser = ref(null);
const captainInfoMap = ref({}); // 队长信息映射表（userId -> 用户信息）

// 搜索相关
const searchValue = ref('');
const showSearch = ref(false);
// 新增筛选相关数据
const showFilter = ref(false);
const filterOptions = ref({
  status: -1, // -1表示全部，0-公开 1-私有 2-加密
  captainName: '',
  description: ''
});

// 创建队伍相关
const showCreatePopup = ref(false);
const createForm = ref({
  name: '',
  description: '',
  maxNum: 6,
  status: 0, // 0-公开 1-私有 2-加密
  password: '',
  avatarUrl: ''
});

// 编辑队伍相关
const showEditPopup = ref(false);
const editingTeam = ref(null);
const showStatusPicker = ref(false);

// ==========================================
// 计算属性
// ==========================================

// 创建队伍状态显示文本
const createStatusText = computed(() => {
  return getStatusText(createForm.value.status);
});

// 编辑队伍状态显示文本
const editStatusText = computed(() => {
  return editingTeam.value ? getStatusText(editingTeam.value.status) : '';
});

// ==========================================
// 工具函数
// ==========================================

/**
 * 判断是否为当前用户创建的队伍
 * @param {Object} team - 队伍信息
 * @returns {boolean} 是否为当前用户的队伍
 */
const isMyTeam = (team) => {
  return currentUser.value && team.userId === currentUser.value.id;
};

/**
 * 获取队伍卡片样式类
 * @param {Object} team - 队伍信息
 * @returns {Object} 样式类对象
 */
const getTeamCardClass = (team) => {
  return {
    'team-card': true,
    'my-team': isMyTeam(team),
    'other-team': !isMyTeam(team)
  };
};

/**
 * 获取队伍标签样式
 * @param {Object} team - 队伍信息
 * @returns {Object} 样式对象
 */
const getTeamTagStyle = (team) => {
  if (isMyTeam(team)) {
    return {
      backgroundColor: '#1989fa',
      color: 'white'
    };
  }
  return {};
};

/**
 * 获取队伍头像（含默认值）
 * @param {string} avatarUrl - 头像URL
 * @returns {string} 处理后的头像URL
 */
const getTeamAvatar = (avatarUrl) => {
  if (avatarUrl && avatarUrl.trim() !== '') {
    return avatarUrl;
  }
  return 'https://img.yzcdn.cn/vant/default-user-image.png'; // 默认头像
};

/**
 * 获取状态文本（0-公开 1-私有 2-加密）
 * @param {number} status - 状态值
 * @returns {string} 状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    '-1': '全部',
    '0': '公开',
    '1': '私有',
    '2': '加密'
  };
  return statusMap[status] || '未知';
};

/**
 * 获取当前状态选择器默认索引
 * @returns {number} 状态索引
 */
const getCurrentStatusIndex = () => {
  if (showCreatePopup.value) {
    return createForm.value.status;
  } else if (showEditPopup.value && editingTeam.value) {
    return editingTeam.value.status;
  }
  return 0;
};

/**
 * 格式化时间为本地日期格式
 * @param {string} timeStr - 时间字符串
 * @returns {string} 格式化后的日期
 */
const formatTime = (timeStr) => {
  return new Date(timeStr).toLocaleDateString('zh-CN');
};

/**
 * 获取队长信息（含默认值）
 * @param {Object} team - 队伍信息
 * @returns {Object} 队长信息
 */
const getCaptainInfo = (team) => {
  if (!team.userId) return null;
  return captainInfoMap.value[team.userId] || {
    username: '加载中...',
    userAccount: `用户${team.userId}`
  };
};

// ==========================================
// 数据加载函数
// ==========================================

/**
 * 加载队伍列表
 */
const loadTeams = async () => {
  try {
    loading.value = true;
    // 构建搜索查询参数
    const query = {};

    // 基础搜索
    if (searchValue.value.trim()) {
      query.name = searchValue.value.trim();
    }

    // 筛选条件
    if (filterOptions.value.status !== -1) {
      query.status = filterOptions.value.status;
    }

    if (filterOptions.value.captainName.trim()) {
      query.captainName = filterOptions.value.captainName.trim();
    }

    if (filterOptions.value.description.trim()) {
      query.description = filterOptions.value.description.trim();
    }

    const result = await listTeams(query);
    teamList.value = result || [];
    await loadCaptainInfo(); // 加载队长信息
  } catch (error) {
    console.error('加载队伍列表失败:', error);
    showToast('加载队伍列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 加载所有队伍的队长信息
 */
const loadCaptainInfo = async () => {
  try {
    // 去重收集所有队长ID
    const captainIds = [...new Set(teamList.value.map(team => team.userId))];

    // 批量查询队长信息
    for (const captainId of captainIds) {
      if (captainId && !captainInfoMap.value[captainId]) {
        try {
          const captainInfo = await getUserById(captainId);
          captainInfoMap.value[captainId] = captainInfo;
        } catch (error) {
          console.error(`查询队长信息失败 (ID: ${captainId}):`, error);
          captainInfoMap.value[captainId] = {
            username: '未知用户',
            userAccount: `用户${captainId}`
          };
        }
      }
    }
  } catch (error) {
    console.error('加载队长信息失败:', error);
  }
};

// ==========================================
// 事件处理函数
// ==========================================

/**
 * 处理状态选择确认
 * @param {Object} item - 选择器返回结果
 */
const handleStatusConfirm = (item) => {
  const statusValue = item.selectedOptions[0].value;
  if (showCreatePopup.value) {
    createForm.value.status = statusValue;
  } else if (showEditPopup.value && editingTeam.value) {
    editingTeam.value.status = statusValue;
  } else if (showFilter.value) {
    filterOptions.value.status = statusValue;
  }
  showStatusPicker.value = false;
};

/**
 * 执行搜索
 */
const handleSearch = () => {
  loadTeams();
};

/**
 * 执行筛选
 */
const handleFilter = () => {
  loadTeams();
  showFilter.value = false;
};

/**
 * 重置筛选条件
 */
const resetFilter = () => {
  filterOptions.value = {
    status: -1,
    captainName: '',
    description: ''
  };
};

/**
 * 创建队伍
 */
const handleCreateTeam = async () => {
  if (!createForm.value.name.trim()) {
    showToast('请输入队伍名称');
    return;
  }

  try {
    const teamData = {
      ...createForm.value,
      id: 0, // 后端自动生成
      userId: currentUser.value?.id || 0,
      expireTime: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(), // 默认30天后过期
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
      isDelete: 0
    };

    await addTeam(teamData);
    showToast('创建队伍成功');
    showCreatePopup.value = false;
    resetCreateForm();
    loadTeams();
  } catch (error) {
    console.error('创建队伍失败:', error);
    showToast('创建队伍失败');
  }
};

/**
 * 编辑队伍
 */
const handleEditTeam = async () => {
  if (!editingTeam.value) return;

  try {
    await updateTeam(editingTeam.value);
    showToast('更新队伍成功');
    showEditPopup.value = false;
    editingTeam.value = null;
    loadTeams();
  } catch (error) {
    console.error('更新队伍失败:', error);
    showToast('更新队伍失败');
  }
};

/**
 * 删除队伍
 * @param {Object} team - 队伍信息
 */
const handleDeleteTeam = async (team) => {
  try {
    await deleteTeam(team.id);
    showToast('删除队伍成功');
    loadTeams();
  } catch (error) {
    console.error('删除队伍失败:', error);
    showToast('删除队伍失败');
  }
};

/**
 * 查看队伍详情（非当前用户的队伍）
 * @param {Object} team - 队伍信息
 */
const handleViewTeam = (team) => {
  showToast(`查看队伍: ${team.name}`);
  // 可添加跳转到队伍详情页的逻辑
};

/**
 * 打开编辑弹窗（含权限检查）
 * @param {Object} team - 队伍信息
 */
const openEditPopup = (team) => {
  if (!isMyTeam(team)) {
    handleViewTeam(team);
    return;
  }
  editingTeam.value = { ...team };
  showEditPopup.value = true;
};

/**
 * 重置创建表单
 */
const resetCreateForm = () => {
  createForm.value = {
    name: '',
    description: '',
    maxNum: 6,
    status: 0,
    password: '',
    avatarUrl: ''
  };
};

// ==========================================
// 生命周期钩子
// ==========================================

/**
 * 页面初始化
 */
onMounted(async () => {
  try {
    currentUser.value = await getCurrentUser();
    await loadTeams();
  } catch (error) {
    console.error('初始化失败:', error);
  }
});
</script>

<template>
  <div class="team-page">
    <!-- 搜索栏区域 -->
    <van-sticky>
      <div class="search-bar">
        <van-search
            v-model="searchValue"
            placeholder="搜索队伍名称"
            @search="handleSearch"
            @clear="handleSearch"
            shape="round"
            background="#f7f8fa"
        />
        <van-button
            type="primary"
            size="small"
            @click="showFilter = true"
            class="filter-btn"
            icon="filter"
        >
          筛选
        </van-button>
      </div>
    </van-sticky>

    <!-- 队伍列表区域 -->
    <div class="team-list">
      <van-pull-refresh v-model="loading" @refresh="loadTeams">
        <van-list
            v-model:loading="loading"
            :finished="!loading"
            finished-text="没有更多队伍了"
            @load="loadTeams"
        >
          <!-- 空状态显示 -->
          <div v-if="teamList.length === 0 && !loading" class="empty-state">
            <van-icon name="friends-o" size="48" color="#c8c9cc"/>
            <p class="empty-text">暂无队伍</p>
            <p class="empty-subtext">点击右下角按钮创建第一个队伍</p>
          </div>

          <!-- 队伍列表 -->
          <van-cell-group v-else inset>
            <van-swipe-cell
                v-for="team in teamList"
                :key="team.id"
                :disabled="!isMyTeam(team)"
            >
              <van-card
                  :title="team.name"
                  :desc="team.description"
                  :thumb="getTeamAvatar(team.avatarUrl)"
                  :class="getTeamCardClass(team)"
                  @click="openEditPopup(team)"
              >
                <!-- 队伍标签 -->
                <template #tags>
                  <van-tag
                      v-if="isMyTeam(team)"
                      type="primary"
                      size="small"
                      :style="getTeamTagStyle(team)"
                  >
                    我的队伍
                  </van-tag>
                  <van-tag type="primary" size="small">{{ getStatusText(team.status) }}</van-tag>
                  <van-tag type="success" size="small" style="margin-left: 5px">
                    最大{{ team.maxNum }}人
                  </van-tag>
                  <van-tag type="warning" size="small" style="margin-left: 5px">
                    过期: {{ formatTime(team.expireTime) }}
                  </van-tag>
                </template>

                <!-- 队伍底部信息 -->
                <template #footer>
                  <div class="team-footer">
                    <div class="captain-info">
                      <van-icon name="user-circle-o" size="14"/>
                      <span class="captain-name">{{ getCaptainInfo(team).username }}</span>
                      <span class="captain-account">({{ getCaptainInfo(team).userAccount }})</span>
                    </div>
                    <van-button
                        v-if="isMyTeam(team)"
                        size="mini"
                        type="primary"
                        plain
                        @click.stop="openEditPopup(team)"
                    >
                      编辑
                    </van-button>
                    <van-button
                        v-else
                        size="mini"
                        type="default"
                        plain
                        @click.stop="handleViewTeam(team)"
                    >
                      查看详情
                    </van-button>
                  </div>
                </template>
              </van-card>

              <!-- 滑动删除按钮 -->
              <template #right>
                <van-button
                    v-if="isMyTeam(team)"
                    square
                    type="danger"
                    text="删除"
                    @click="handleDeleteTeam(team)"
                    class="delete-btn"
                />
              </template>
            </van-swipe-cell>
          </van-cell-group>
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 圆形加号创建按钮 - 右下角固定位置 -->
    <div class="create-button-container">
      <van-button
          type="primary"
          round
          @click="showCreatePopup = true"
          class="floating-create-btn"
          icon="plus"
          size="large"
      >
      </van-button>
    </div>

    <!-- 创建队伍弹窗 -->
    <van-popup
        v-model:show="showCreatePopup"
        position="bottom"
        round
        :style="{ height: '70%' }"
    >
      <div class="popup-content">
        <h3 class="popup-title">创建队伍</h3>
        <van-form @submit="handleCreateTeam">
          <van-cell-group inset>
            <van-field
                v-model="createForm.name"
                label="队伍名称"
                placeholder="请输入队伍名称"
                :rules="[{ required: true, message: '请输入队伍名称' }]"
            />
            <van-field
                v-model="createForm.description"
                label="队伍描述"
                placeholder="请输入队伍描述"
                type="textarea"
                autosize
            />
            <van-field
                v-model="createForm.maxNum"
                label="最大人数"
                type="digit"
                placeholder="请输入最大人数"
                :rules="[{ required: true, message: '请输入最大人数' }]"
            />
            <van-field
                :model-value="createStatusText"
                label="队伍状态"
                readonly
                is-link
                @click="showStatusPicker = true"
                placeholder="请选择队伍状态"
            />
            <van-field
                v-if="createForm.status === 2"
                v-model="createForm.password"
                label="密码"
                type="password"
                placeholder="请输入队伍密码"
                :rules="[{ required: true, message: '加密队伍必须设置密码' }]"
            />
            <van-field
                v-model="createForm.avatarUrl"
                label="队伍头像URL"
                placeholder="请输入队伍头像链接（可选）"
                type="url"
            />
          </van-cell-group>
          <div style="margin: 16px;">
            <van-button round block type="primary" native-type="submit">
              创建队伍
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- 编辑队伍弹窗 -->
    <van-popup
        v-model:show="showEditPopup"
        position="bottom"
        round
        :style="{ height: '70%' }"
        v-if="editingTeam"
    >
      <div class="popup-content">
        <h3 class="popup-title">编辑队伍</h3>
        <van-form @submit="handleEditTeam">
          <van-cell-group inset>
            <van-field
                v-model="editingTeam.name"
                label="队伍名称"
                placeholder="请输入队伍名称"
                :rules="[{ required: true, message: '请输入队伍名称' }]"
            />
            <van-field
                v-model="editingTeam.description"
                label="队伍描述"
                placeholder="请输入队伍描述"
                type="textarea"
                autosize
            />
            <van-field
                v-model="editingTeam.maxNum"
                label="最大人数"
                type="digit"
                placeholder="请输入最大人数"
                :rules="[{ required: true, message: '请输入最大人数' }]"
            />
            <van-field
                :model-value="editStatusText"
                label="队伍状态"
                readonly
                is-link
                @click="showStatusPicker = true"
                placeholder="请选择队伍状态"
            />
            <van-field
                v-if="editingTeam.status === 2"
                v-model="editingTeam.password"
                label="密码"
                type="password"
                placeholder="请输入队伍密码"
            />
            <van-field
                v-model="editingTeam.avatarUrl"
                label="队伍头像URL"
                placeholder="请输入队伍头像链接"
                type="url"
            />
          </van-cell-group>
          <div style="margin: 16px;">
            <van-button round block type="primary" native-type="submit">
              保存修改
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- 筛选弹窗 -->
    <van-popup
        v-model:show="showFilter"
        position="bottom"
        round
        :style="{ height: '70%' }"
    >
      <div class="popup-content">
        <h3 class="popup-title">筛选队伍</h3>
        <van-form @submit="handleFilter">
          <van-cell-group inset>
            <van-field
                :model-value="getStatusText(filterOptions.status)"
                label="队伍状态"
                readonly
                is-link
                @click="showStatusPicker = true"
                placeholder="请选择队伍状态"
            />
            <van-field
                v-model="filterOptions.captainName"
                label="队长名称"
                placeholder="请输入队长名称"
            />
            <van-field
                v-model="filterOptions.description"
                label="队伍描述"
                placeholder="请输入描述关键词"
                type="textarea"
                autosize
            />
          </van-cell-group>
          <div style="margin: 16px; display: flex; gap: 10px;">
            <van-button block @click="resetFilter">重置</van-button>
            <van-button block type="primary" native-type="submit">确定筛选</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- 状态选择器弹窗 -->
    <van-popup v-model:show="showStatusPicker" position="bottom">
      <van-picker
          :columns="[
      { text: '全部', value: -1 },
      { text: '公开', value: 0 },
      { text: '私有', value: 1 },
      { text: '加密', value: 2 }
    ]"
          :default-index="showFilter.value ? 0 : getCurrentStatusIndex()"
          @confirm="handleStatusConfirm"
          @cancel="showStatusPicker = false"
      />
    </van-popup>
  </div>
</template>

<style scoped>
.team-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  position: relative;
  padding-bottom: 80px; /* 为底部按钮留出空间 */
}

/* 搜索栏样式 */
.search-bar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  background: white;
  border-bottom: 1px solid #ebedf0;
}

.search-bar :deep(.van-search) {
  flex: 1;
  margin-right: 10px;
}

.filter-btn {
  white-space: nowrap;
}

/* 队伍列表样式 */
.team-list {
  padding: 10px;
}

/* 队伍卡片样式 */
.team-card {
  margin-bottom: 10px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.team-card.my-team {
  border-left: 4px solid #1989fa;
  background: linear-gradient(135deg, #f8faff 0%, #f0f7ff 100%);
  box-shadow: 0 2px 8px rgba(25, 137, 250, 0.1);
}

.team-card.other-team {
  border-left: 4px solid #ebedf0;
  background: white;
}

.team-card.my-team :deep(.van-card__header) {
  border-bottom: 1px solid #e8f4ff;
}


.team-card.my-team :deep(.van-card__title) {
  color: #1989fa;
  font-weight: 600;
}

.team-card :deep(.van-card__content) {
  padding: 12px;
}

.team-card :deep(.van-card__thumb) {
  width: 60px;
  height: 60px;
  margin-right: 12px;
  border-radius: 8px;
}

/* 队伍底部信息样式 */
.team-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.captain-info {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #969799;
}

.captain-name {
  margin-left: 4px;
  font-weight: 500;
}

.captain-account {
  margin-left: 4px;
  color: #c8c9cc;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #969799;
}

.empty-text {
  font-size: 16px;
  margin: 16px 0 8px;
}

.empty-subtext {
  font-size: 14px;
  color: #c8c9cc;
}

/* 弹窗样式 */
.popup-content {
  padding: 20px;
}

.popup-title {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 20px;
  color: #323233;
}

.delete-btn {
  height: 100%;
}

/* 圆形加号创建按钮样式 */
.create-button-container {
  position: fixed;
  bottom: 60px;
  right: 30px;
  z-index: 100;
}


.floating-create-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(25, 137, 250, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 24px;
  background-color: #1989fa;
}

.floating-create-btn::after {
  border-radius: 50%;
}
</style>