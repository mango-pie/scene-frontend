// import { defineConfig } from 'vite'
// import vue from '@vitejs/plugin-vue'
// // 引入新的插件
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'

// export default defineConfig({
//   plugins: [
//     vue(),
//     Components({
//       resolvers: [
//         VantResolver({
//           importStyle: true // 关键：自动导入样式
//         })
//       ]
//     })
//   ]
// })
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(),
  Components({
       resolvers: [
        VantResolver({
          importStyle: true // 关键：自动导入样式
        })]
  })],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端服务地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '') // 如果后端API没有/api前缀，需要重写路径
      }
    }
  }
});

