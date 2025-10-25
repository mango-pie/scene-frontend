<script setup>
import { ref } from 'vue'
import {showToast, Toast} from "vant";
import Team from "../pages/Team.vue";
import Index from "../pages/index.vue";
import {useRouter} from "vue-router";

const onclickLeft = () => {
  history.back();
}
const onclickRight = () => {
  router.push('/search');
}
const router = useRouter();
const active = ref("home");
const onChange = (index) => showToast(`标签 ${index}`)

</script>

<template>

  <van-nav-bar title="导航栏"
               left-text="返回"
               left-arrow
               @click-left="onclickLeft"
               @click-right="onclickRight">

    <template #right>
      <van-icon name="search" size="24px" @click="onclickRight"/>
    </template>

  </van-nav-bar>

  <slot name="content">
    <div id="content">
      <router-view></router-view>
    </div>
  </slot>


  <van-tabbar route  @change="onChange">
    <van-tabbar-item to="/" icon="home-o" name = "home">主页</van-tabbar-item>
    <van-tabbar-item to="/team" icon="friends-o" name = "friends">队伍</van-tabbar-item>
    <van-tabbar-item to="/user" icon="setting-o" name = "setting">个人</van-tabbar-item>
  </van-tabbar>

</template>

<style scoped>

</style>


