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

//请求拦截器
request.interceptors.request.use(
    (config) => {
      try {
        const sessionId = localStorage.getItem('sessionId');
        const token = localStorage.getItem('token');

        // 如果sessionId存在且不为空，优先使用session认证
        if (sessionId && sessionId.trim() !== '') {
          config.headers['X-Session-Id'] = sessionId;
        }
        
        // 总是添加token到请求头，作为备用认证方式
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        
        // 如果两者都不存在
        if (!sessionId && !token) {
          console.log('未找到认证信息');
        }
      } catch (error) {
        console.error('获取认证信息失败:', error);
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
);

request.interceptors.response.use(
    (response) => {
      // 只返回响应数据
      return response.data;
    },
    (error) => {
      // 处理错误情况
      console.error('API请求错误:', error);

      // 处理认证过期或无效的情况
      if (error.response) {
        const { status, data } = error.response;

        if (status === 401) {
          // 认证无效或过期
          console.error('认证无效或已过期，请重新登录');
          // 清除所有认证信息
          localStorage.removeItem('token');
          localStorage.removeItem('sessionId');
          // localStorage.removeItem('currentUser');

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