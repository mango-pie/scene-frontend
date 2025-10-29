<!--<script setup>-->
<!--// 修改导入语句-->
<!--import { ref } from 'vue';-->
<!--import { showToast } from 'vant';-->

<!--const value = ref('');-->
<!--const onSearch = (val) => {-->
<!--  const searchValue = value.value.trim().toLowerCase();-->
<!--  -->
<!--  // 如果搜索框为空，清空已选标签并提示-->
<!--  if (!searchValue) {-->
<!--    showToast('请输入搜索关键词');-->
<!--    return;-->
<!--  }-->
<!--  -->
<!--  // 将所有的子标签提取出来，过滤掉没有children属性的标签-->
<!--  const searchResults = Tags-->
<!--      .filter(parentTag => parentTag.children && Array.isArray(parentTag.children))-->
<!--      .flatMap(parentTag => parentTag.children)-->
<!--      .filter(tag => -->
<!--        tag && -->
<!--        tag.text && -->
<!--        tag.text.toLowerCase().includes(searchValue)-->
<!--      );-->
<!--  -->
<!--  // 保留现有标签，并添加新的搜索结果（避免重复）-->
<!--  // 注意：这里我们只需要id，而不是整个标签对象-->
<!--  const currentIds = activeIds.value.map(item => -->
<!--    typeof item === 'object' ? item.id : item-->
<!--  );-->
<!--  -->
<!--  const newTagIds = searchResults-->
<!--    .filter(tag => !currentIds.includes(tag.id))-->
<!--    .map(tag => tag.id); // 只取id值-->
<!--  -->
<!--  // 更新activeIds为id数组，而不是对象数组-->
<!--  const updatedActiveIds = [...currentIds, ...newTagIds];-->
<!--  activeIds.value = updatedActiveIds;-->
<!--  -->
<!--  // 显示搜索结果数量-->
<!--  showToast(`找到 ${newTagIds.length} 个匹配项`);-->
<!--}-->
<!--const onCancel = () => showToast('取消');-->
<!--const show = ref(true);-->
<!--const close = (tag) => {-->
<!--  // 如果tag是对象，比较id；如果是字符串，直接比较-->
<!--  activeIds.value = activeIds.value.filter(item => {-->
<!--    if (typeof tag === 'object' && typeof item === 'object') {-->
<!--      return item.id !== tag.id;-->
<!--    } else if (typeof tag === 'string' && typeof item === 'string') {-->
<!--      return item !== tag;-->
<!--    } else if (typeof tag === 'object' && typeof item === 'string') {-->
<!--      return item !== tag.id;-->
<!--    } else if (typeof tag === 'string' && typeof item === 'object') {-->
<!--      return item.id !== tag;-->
<!--    }-->
<!--    return true;-->
<!--  })-->
<!--}-->

<!--const activeIds = ref([]);-->
<!--const activeIndex = ref(0);-->
<!--const Tags = [-->
<!--  {-->
<!--    text: '浙江',-->
<!--    children: [-->
<!--      { text: '杭州', id: '杭州' },-->
<!--      { text: '温州', id: '温州' },-->
<!--      { text: '宁波', id: '宁波', disabled: true },-->
<!--    ],-->
<!--  },-->
<!--  {-->
<!--    text: '江苏',-->
<!--    children: [-->
<!--      { text: '南京', id: '南京' },-->
<!--      { text: '无锡', id: '无锡' },-->
<!--      { text: '徐州', id: '徐州'},-->
<!--    ],-->
<!--  },-->
<!--  { text: '福建', disabled: true },-->
<!--];-->
<!--</script>-->

<!--<template>-->
<!--  <form action="/">-->
<!--    <van-search-->
<!--        v-model="value"-->
<!--        show-action-->
<!--        placeholder="请输入搜索关键词"-->
<!--        @search="onSearch"-->
<!--        @cancel="onCancel"-->
<!--    />-->
<!--  </form>-->
<!--  <van-divider content-position="left">已选标签</van-divider>-->
<!--  <div v-if="activeIds.length === 0">暂无标签</div>-->
<!--  <van-row gutter="16" style="padding: 16px ">-->
<!--    <van-col v-for="(tagId, index) in activeIds" :key="tagId">-->
<!--    <van-tag closeable size="medium" type="primary" @close="close(tagId)">-->
<!--      {{ tagId }}-->
<!--    </van-tag>-->
<!--    </van-col>-->
<!--  </van-row>-->

<!--  <van-divider content-position="left"/>-->
<!--  <van-tree-select-->
<!--      v-model:active-id="activeIds"-->
<!--      v-model:main-active-index="activeIndex"-->
<!--      :items="Tags"-->
<!--  />-->
<!--</template>-->

<!--<style scoped>-->

<!--</style>-->


<script setup>
import {ref, computed, onMounted} from 'vue';
import { showToast } from 'vant';
import {useRouter} from "vue-router";
import {searchTags} from "../../api/tag.js";

const router = useRouter();

const value = ref('');
const activeIds = ref([]);
const activeIndex = ref(0);
const hasSearched = ref(false); // 标记是否已经进行过搜索
const searchText = ref(''); // 存储当前搜索文本

// 原始标签数据
const originalTags = [
  {
    text: '浙江',
    children: [
      { text: '杭州', id: '杭州' },
      { text: '温州', id: '温州' },
      { text: '宁波', id: '宁波', disabled: true },
    ],
  },
  {
    text: '江苏',
    children: [
      { text: '南京', id: '南京' },
      { text: '无锡', id: '无锡' },
      { text: '徐州', id: '徐州'},
    ],
  },
  { text: '代码',
    children: [
      { text: 'Java', id: 'Java' },
      { text: 'Python', id: 'Python' },
      { text: 'C++', id: 'C++'},
    ],
  },
];

// 计算属性：根据搜索文本过滤标签
const filteredTags = computed(() => {
// 如果没有搜索过或搜索文本为空，显示所有标签
  if (!hasSearched.value || !searchText.value.trim()) {
    return originalTags;
  }

  const searchValue = searchText.value.trim().toLowerCase();

// 对每个父标签进行过滤
  return originalTags.map(parentTag => {
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
  // try{
    const tags = await searchTags();
    console.log(tags);
  // }catch (error) {
  //   showToast('搜索标签失败');
  // }
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