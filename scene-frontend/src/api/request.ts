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

import { BaseResponse } from '../model/BaseResponse';

request.interceptors.response.use(
    (response) => {
      const baseResponse: BaseResponse = response.data;
      
      // 检查是否为BaseResponse格式
      if ('code' in baseResponse && 'message' in baseResponse && 'data' in baseResponse) {
        // 成功响应（假设code为0表示成功）
        if (baseResponse.code === 0) {
          // 只返回data部分，保持现有代码兼容性
          return baseResponse.data;
        } else {
          // 业务错误，抛出异常供上层处理
          const error = new Error(baseResponse.message || '请求失败');
          // 将错误码和详情附加到错误对象
          (error as any).code = baseResponse.code;
          (error as any).detail = baseResponse.data;
          return Promise.reject(error);
        }
      }
      
      // 兼容非BaseResponse格式的响应
      return response.data;
    },
    (error) => {
      // 处理HTTP错误
      console.error('API请求错误:', error);

      if (error.response) {
        const { status, data } = error.response;
        
        // 如果后端返回了BaseResponse格式的错误
        if (data && 'code' in data && 'message' in data) {
          const errorMsg = data.message || `请求失败(${status})`;
          const errorWithDetails = new Error(errorMsg);
          (errorWithDetails as any).code = data.code;
          (errorWithDetails as any).detail = data.data;
          return Promise.reject(errorWithDetails);
        }

        if (status === 401) {
          console.error('认证无效或已过期，请重新登录');
          localStorage.removeItem('token');
          localStorage.removeItem('sessionId');
          if (window.location.pathname !== '/user') {
            window.location.href = '/user';
          }
        } else if (status === 403) {
          console.error('权限不足');
        } else if (status >= 500) {
          console.error('服务器内部错误');
        }
      }

      if (error.code === 'ERR_NETWORK') {
        console.error('网络错误，请检查后端服务是否正常运行');
      }

      return Promise.reject(error);
    }
);

export default request;