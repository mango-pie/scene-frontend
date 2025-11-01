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
request.interceptors.request.use(
  (config) => {
    // 添加token认证
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
    
    // 处理token过期或无效的情况
    if (error.response) {
      const { status, data } = error.response;
      
      if (status === 401) {
        // token无效或过期
        console.error('Token无效或已过期，请重新登录');
        localStorage.removeItem('token');
        localStorage.removeItem('currentUser');
        
        // 可以在这里跳转到登录页面
        if (window.location.pathname !== '/user') {
          window.location.href = '/user';
        }
      } else if (status === 403) {
        console.error('权限不足');
      } else if (status >= 500) {
        console.error('服务器内部错误');
      }
    }
    
    // 处理网络错误
    if (error.code === 'ERR_NETWORK') {
      console.error('网络错误，请检查后端服务是否正常运行');
    }
    
    return Promise.reject(error);
  }
);

export default request;