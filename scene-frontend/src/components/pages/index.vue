<script setup>
import {onMounted, ref} from 'vue';
import {recommendUsersByUserId, refreshRecommendations} from '../../api/search';
import {getCurrentUser} from "../../api/user.js";

// 移除 userStore 导入
// import { useUserStore } from '@/stores/userStore';

// 响应式数据
const recommendedUsers = ref([]);
const loading = ref(false);
const error = ref('');
const pageNum = ref(1);
const totalPages = ref(0);
const pageSize = ref(5);
const currentUserId = ref(2); // 默认用户ID

// 加载推荐用户
const loadRecommendations = async () => {
  loading.value = true;
  error.value = '';
  try {
    getCurrentUser().then(user => {
      currentUserId.value = user.id;
    });
    const userId = currentUserId.value;
    const users = await recommendUsersByUserId(userId, pageNum.value, pageSize.value);
    totalPages.value = users.pages;
    console.log('推荐用户:', users);
    recommendedUsers.value = users.records;
  } catch (err) {
    error.value = '加载推荐用户失败，请稍后重试';
    console.error('加载推荐用户失败:', err);
  } finally {
    loading.value = false;
  }
};

// 换一批功能
const changeRecommendations = async () => {
  loading.value = true;
  error.value = '';
  try {
    // 修改这里：直接使用currentUserId.value
    console.log('当前用户ID:', currentUserId.value);
    const userId = currentUserId.value;
    pageNum.value++;
    if (pageNum.value > totalPages.value) {
      pageNum.value = 1;
    }
    if (pageNum.value <= 0) {
      pageNum.value = 1;
    }
    console.log('换一批用户ID:', userId);
    console.log('换一批页码:', pageNum.value);
    const response= await refreshRecommendations(userId, pageNum.value, pageSize.value);
    recommendedUsers.value = response.records;
  } catch (err) {
    error.value = '更换推荐失败，请稍后重试';
    console.error('更换推荐失败:', err);
  } finally {
    loading.value = false;
  }
};

// 组件挂载时加载推荐
onMounted(() => {
  loadRecommendations();

  getCurrentUser().then(user => {
    console.log('当前用户:', user);
    currentUserId.value = user.id;
  });
});
</script>

<!-- 模板和样式部分保持不变 -->
<template>
  <div class="home-container">
    <header class="page-header">
      <h1>用户推荐</h1>
      <p>根据你的兴趣，我们为你推荐了以下用户</p>
    </header>

    <main class="recommendation-section">
      <div class="section-header">
        <h2>推荐用户</h2>
        <button
            class="refresh-btn"
            @click="changeRecommendations"
            :disabled="loading"
        >
          {{ loading ? '加载中...' : '换一批' }}
        </button>
      </div>

      <div v-if="loading && recommendedUsers.length === 0" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在为你寻找合适的用户...</p>
      </div>

      <div v-else-if="error" class="error-message">
        {{ error }}
        <button @click="loadRecommendations" class="retry-btn">重试</button>
      </div>

      <div v-else-if="recommendedUsers.length === 0" class="empty-state">
        <p>暂无推荐用户</p>
      </div>

      <div v-else class="users-grid">
        <div
            v-for="user in recommendedUsers"
            :key="user.id"
            class="user-card"
        >
          <div class="user-avatar">
            <img
                :src="user.avatarUrl || 'https://img.remit.ee/api/file/BQACAgUAAyEGAASHRsPbAAEL5GNpLs8302yQpRoi3s1iQQrxFtrlHwACPyIAAsBAeFW389IGDnhLvjYE.png'"
                :alt="user.username"
                class="avatar-image"
            >
          </div>
          <div class="user-info">
            <h3 class="user-name">{{ user.username }}</h3>
            <p class="user-profile">{{ user.profile || '暂无简介' }}</p>
            <div class="user-tags">
              <span
                  v-for="(tag, index) in user.tagList"
                  :key="index"
                  class="tag"
              >
                {{ tag }}
              </span>
            </div>
          </div>
          <button class="connect-btn">联系TA</button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 样式部分保持不变 */
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  color: #333;
  margin-bottom: 10px;
}

.page-header p {
  color: #666;
  font-size: 16px;
}

.recommendation-section {
  background: #fafafa;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  color: #333;
  margin: 0;
}

.refresh-btn {
  background: #1890ff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  background: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
}

.refresh-btn:disabled {
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
}

.loading-container {
  text-align: center;
  padding: 60px 0;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  background: #fff2f0;
  border: 1px solid #ffccc7;
  color: #ff4d4f;
  padding: 16px;
  border-radius: 6px;
  text-align: center;
}

.retry-btn {
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 8px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.user-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  text-align: center;
}

.avatar-image {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #f0f0f0;
}

.user-info {
  flex: 1;
}

.user-name {
  margin: 0 0 8px;
  color: #333;
  font-size: 18px;
  text-align: center;
}

.user-profile {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.tag {
  background: #f0f0f0;
  color: #666;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.connect-btn {
  background: #52c41a;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  margin-top: auto;
}

.connect-btn:hover {
  background: #73d13d;
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .users-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>