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

import request from './request';

export const recommendUsersByUserId = async (userId : number , pageNum = 1, pageSize = 10) =>
{
    try {
        const response = await request.get('/user/recommend/byUserId', {
            params: {
                userId,
                pageNum,
                pageSize
            }
        });
        console.log('推荐用户11:', response);
        return response;
    } catch (error) {
        console.error('推荐用户失败:', error);
        throw error;
    }
}

export const recommendUsersByTags = async (tags : string, pageNum = 1, pageSize = 10) =>
{
    try {
        // 标签参数需要转成JSON字符串格式
        const response = await request.get('/user/recommend/byTags', {
            params: {
                tags: JSON.stringify(tags),
                pageNum,
                pageSize
            }
        });
        return response.data;
    } catch (error) {
        console.error('按标签推荐用户失败:', error);
        throw error;
    }
}

// 清除推荐结果缓存，用于"换一批"功能
export const clearRecommendationCache = async (userId : number) =>
{
    try {
        const response = await request.delete('/user/recommend/cache/byUserId', {
            params: {
                userId
            }
        });
        return response.data;
    } catch (error) {
        console.error('清除缓存失败:', error);
        throw error;
    }
}

// export const updateRecommendationsUI = (freshRecommendations: axios.AxiosResponse<any>) =>
// {
//     // 假设这里是更新推荐列表的UI逻辑
//     // 例如，更新推荐用户列表的DOM元素
//     // 这里只是简单地打印推荐用户
//     console.log('更新推荐用户:', freshRecommendations);
// }

// "换一批"功能实现
export const refreshRecommendations = async (userId : number, pageNum = 1, pageSize = 5) =>
{
    try {
        // 1. 先清除缓存
        await clearRecommendationCache(userId);
        // 2. 重新获取推荐结果
        const freshRecommendations = await recommendUsersByUserId(userId, pageNum, pageSize);
        // 3. 更新UI
       // updateRecommendationsUI(freshRecommendations);
        return freshRecommendations;
    } catch (error) {
        console.error('刷新推荐失败:', error);
        throw error;
    }
}

export const refreshRecommendationsByTags = async (tags : string, pageNum = 1, pageSize = 10) =>
{
    try {
        // 1. 先清除缓存
        await clearRecommendationCache(2);
        // 2. 重新获取推荐结果
        const freshRecommendations = await recommendUsersByTags(tags, pageNum, pageSize);
        // 3. 更新UI
        //updateRecommendationsUI(freshRecommendations);
        return freshRecommendations;
    } catch (error) {
        console.error('刷新推荐失败:', error);
        throw error;
    }
}
