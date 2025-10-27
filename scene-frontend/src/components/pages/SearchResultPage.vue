<script setup>
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {searchUsersByTags} from "../../api/user.js";

const router = useRouter();

const query = router.currentRoute.value.query;
const tags = query.tags ? query.tags.split(',') : [];
const userList = ref([]);

const fetchUserList = async () => {
  try {
    const result = await searchUsersByTags(tags);
    console.log('搜索结果:', result);

    // 直接使用result，不需要访问result.data
    // 检查result是否为数组，如果是则直接赋值，否则根据实际数据结构处理
    if (Array.isArray(result)) {
      userList.value = result;
    } else if (result && typeof result === 'object') {
      // 如果后端返回的是对象，可能需要根据实际结构调整
      userList.value = Object.values(result);
    }

    // 如果结果为空，给出提示
    if (userList.value.length === 0) {
      console.log('未找到匹配的用户');
    }
  } catch (error) {
    console.error('获取用户列表失败:', error);
  }
};

onMounted(() => {
  fetchUserList();
});
</script>

<template>
  <van-card v-for = "user in userList" :key = "user.id"
    :title = "`${user.username} (${user.userAccount})`"
    :desc = "user.profile"
    :thumb = "user.avatarUrl"

  >
    <template #tags>
      <van-tag v-for="tag in user.tagList" :key="tag" plain type="danger">{{ tag }}</van-tag>
    </template>
    <template #footer>
      <van-button type="primary" size="small">关注</van-button>
    </template>
  </van-card>
</template>

<style scoped>

</style>