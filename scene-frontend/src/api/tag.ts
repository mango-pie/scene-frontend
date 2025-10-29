import request from './request';
export const searchTags = () => {
  return request.get('/tag/search');
}
