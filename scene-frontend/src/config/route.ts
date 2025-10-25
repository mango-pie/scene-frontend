import Index from "../components/pages/index.vue"; // 添加Index组件导入
import Team from "../components/pages/Team.vue";
import User from "../components/pages/User.vue";
import Search from "../components/pages/Search.vue";
import UserEdit from "../components/pages/UserEditPage.vue";

const routes = [
    {path:'/',component:Index},
    {path: '/team',component: Team},
    {path: '/user',component: User},
    {path: '/search',component: Search},
    {path: '/user/edit',component: UserEdit}
]

export default routes