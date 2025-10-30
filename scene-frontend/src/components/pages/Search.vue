<script setup>
import {ref, computed, onMounted} from 'vue';
import { showToast } from 'vant';
import {useRouter} from "vue-router";
import {searchTags} from "../../api/tag.js";
import {convertTagsToTree} from "../../utils/tag.js";
const router = useRouter();

const value = ref('');
const activeIds = ref([]);
const activeIndex = ref(0);
const hasSearched = ref(false); // 标记是否已经进行过搜索
const searchText = ref(''); // 存储当前搜索文本

// 后端返回的标签数据结构示例
// {id: 1, isDelete: 0, isParent: 0, parentId: 3, tagName: "熊"}

// 转换函数：将后端返回的扁平Tag列表转换为树形结构


// 原始标签数据（现在从后端获取）
const originalTags = ref([
]);

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



const onSearch = (val) => {
  const searchValue = value.value.trim().toLowerCase();

// 如果搜索框为空，提示用户
  if (!searchValue) {
    showToast('请输入搜索关键词');
    return;
  }

// 存储搜索文本并标记已搜索
  searchText.value = value.value;
  hasSearched.value = true;

// 统计搜索结果数量
  const allMatchedTags = filteredTags.value
      .filter(parent => parent.children && Array.isArray(parent.children))
      .flatMap(parent => parent.children);

  showToast(`找到 ${allMatchedTags.length} 个匹配项`);
}

const onCancel = () => {
// 取消搜索时重置状态，显示所有标签
  hasSearched.value = false;
  searchText.value = '';
  showToast('取消搜索');
}

const close = (tagId) => {
// 直接过滤掉要关闭的标签id
  activeIds.value = activeIds.value.filter(id => id !== tagId);
}

const handleSearch = () => {
  if (activeIds.value.length === 0) {
    showToast('请选择标签');
    return;
  }
  // 构建查询参数
  const queryParams = activeIds.value.join(',');
  // 导航到搜索结果页面

  router.push({ path: '/user/list', query: { tags: queryParams } });
}

const searchByTags = async () => {
  try {
    const tags = await searchTags();
    console.log('后端返回的标签数据:', tags);
    
    // 转换后端数据为前端需要的格式
    if (tags && Array.isArray(tags)) {
      const convertedTags = convertTagsToTree(tags);
      console.log('转换后的标签数据:', convertedTags);
      
      // 更新原始标签数据
      originalTags.value = convertedTags;
    } else {
      console.warn('后端返回的标签数据格式不正确');
    }
  } catch (error) {
    console.error('搜索标签失败:', error);
    showToast('搜索标签失败');
  }
}

onMounted(() => {
  searchByTags();
});
</script>

<template>
  <form action="/">
    <van-search
        v-model="value"
        show-action
        placeholder="请输入搜索关键词"
        @search="onSearch"
        @cancel="onCancel"
    />
  </form>

  <!-- 已选标签区域 -->
  <van-divider v-if="activeIds.length > 0" content-position="left">已选标签</van-divider>
  <van-row v-if="activeIds.length > 0" gutter="16" style="padding: 16px">
    <van-col v-for="tagId in activeIds" :key="tagId">
      <van-tag closeable size="medium" type="primary" @close="close(tagId)">
        {{ tagId }}
      </van-tag>
    </van-col>
  </van-row>

  <!-- 搜索提示 -->
  <div v-if="hasSearched" style="padding: 10px 16px; color: #999;">
    {{ filteredTags.length > 0 ? '以下是搜索结果：' : '未找到匹配的标签' }}
  </div>

  <van-divider content-position="left"/>
  <van-tree-select
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="filteredTags"
  />

  <van-button type="primary" @click="handleSearch">搜索</van-button>
</template>

<style scoped>

</style>