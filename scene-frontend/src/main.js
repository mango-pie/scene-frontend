import { createApp } from 'vue'
//import './style.css'
import App from './components/layouts/BasicLayout.vue'
import {Button} from 'vant';
import * as VueRouter from 'vue-router'


// 修复路由文件导入路径，从route.js改为route.ts
import routes from './config/route.ts'



const app = createApp(App);
app.use(Button);



const router = VueRouter.createRouter({
    history: VueRouter.createWebHashHistory(),
    routes
})
app.use(router)
app.mount('#app')