import request from '@/utils/request';

// 用户登录
export const login = (data) => {
  return request.post('/api/user/login', data);
};

// 用户注册
export const register = (data) => {
  return request.post('/api/user/register', data);
};

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/api/user/info');
};

// 更新用户信息
export const updateUserInfo = (data) => {
  return request.put('/api/user/info', data);
};

// 退出登录
export const logout = () => {
  return request.post('/api/user/logout');
};

// 获取文章列表
export const getArticles = () => {
  return request.get('/api/articles')
};
// 上传头像
export const uploadAvatar = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/api/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};