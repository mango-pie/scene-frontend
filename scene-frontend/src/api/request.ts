import axios from 'axios';

// 创建axios实例
const request = axios.create({
  // 使用相对路径，这样请求会通过Vite代理转发到后端
  baseURL: 'http://localhost:8080/api',
  // 请求超时时间
  timeout: 10000,
  // 允许携带Cookie
  withCredentials: true,
});

// 请求拦截器
// request.interceptors.request.use(
//   (config) => {
//     // 添加token认证
//     const token = localStorage.getItem('token');
//     if (token) {
//       config.headers.Authorization = `Bearer ${token}`;
//     }
//     return config;
//   },
//   (error) => {
//     return Promise.reject(error);
//   }
// );

request.interceptors.request.use(
    (config) => {
        // 添加token认证 - 增加错误处理
        try {
            const token = localStorage.getItem('token');
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
                console.log('添加token到请求头');
            } else {
                console.log('未找到token');
            }
        } catch (error) {
            console.error('获取token失败:', error);
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 只返回响应数据
    return response.data;
  },
  (error) => {
    // 处理错误情况
    console.error('API请求错误:', error);
    
    // 处理网络错误
    if (error.code === 'ERR_NETWORK') {
      console.error('网络错误，请检查后端服务是否正常运行');
    }
    
    return Promise.reject(error);
  }
);

export default request;