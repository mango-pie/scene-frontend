import request from './request';

// 用户类型定义
interface User {
  id: number;
  username: string;
  userAccount: string;
  email: string;
  phone: string;
  gender: number;
  profile: string;
  tagList: string[];
  avatarUrl?: string;
  userStatus?: number;
  userRole?: number;
  planetCode?: string;
  // 其他用户属性...
}

// 登录响应类型定义
interface LoginResponse {
  id: number;
  username: string;
  userAccount: string;
  avatarUrl: string;
  gender: number;
  phone: string;
  email: string;
  userStatus: number;
  userRole: number;
  planetCode: string;
  token: string;
  expireTime: number;
}

// 用户注册
export const userRegister = (data: {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
}) => {
  return request.post('/user/register', data);
};

// 用户登录 - 修改为使用查询参数传递
export const userLogin = (data: {
  userAccount: string;
  userPassword: string;
}): Promise<LoginResponse> => {
  return request.post('/user/login', {}, { params: data });
};

// 用户登出
export const userLogout = () => {
  return request.post('/user/logout');
};

// 根据用户名搜索用户
export const searchUserByName = (username: string) => {
  return request.get('/user/search', { params: { username } });
};

// 根据标签搜索用户
export const searchUsersByTags = (tagList: string[]) => {
  return request.get('/user/search/tags', { params: { tagList } });
};

// 获取用户列表（分页）
export const getUserList = (pageNum: number, pageSize: number) => {
  return request.get('/user/list', { params: { pageNum, pageSize } });
};

// 获取当前登录用户
export const getCurrentUser = (): Promise<User> => {
  return request.get('/user/current');
};

// 更新用户信息
export const updateUser = (user: Partial<User>) => {
  console.log('更新用户信息:', user);
  return request.post('/user/update', user);
};