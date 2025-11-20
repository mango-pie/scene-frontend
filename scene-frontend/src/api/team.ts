import request from './request';

// 队伍类型定义 - 基于后端Team类
export interface Team {
    id: number;
    name: string;
    description: string;
    maxNum: number;
    expireTime: string;
    userId: number;
    status: number; // 0-公开，1-私有，2-加密
    password?: string;
    createTime: string;
    updateTime: string;
    isDelete: number;
    /**
     * 队伍头像
     */
    avatarUrl?: string;
}

// 队伍查询参数 - 基于后端TeamQuery类
export interface TeamQuery {
    id?: number;
    name?: string;
    description?: string;
    maxNum?: number;
    userId?: number;
    status?: number; // 队伍状态（0-公开，1-私有，2-加密）
    captainName?: string; // 队长名称
}

// 创建队伍
export const addTeam = (team: Team) => {
    return request.post('/team/add', team);
};

// 删除队伍
export const deleteTeam = (id: number) => {
    return request.delete(`/team/delete/${id}`);
};

// 更新队伍
export const updateTeam = (team: Team) => {
    return request.post('/team/update', team);
};

// 获取队伍详情
export const getTeam = (id: number) => {
    return request.get(`/team/get/${id}`);
};

// 查询队伍列表
export const listTeams = (query: TeamQuery) => {
    return request.post('/team/list', query);
};