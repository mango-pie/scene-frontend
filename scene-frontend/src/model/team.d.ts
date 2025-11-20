export type Team = {
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

export type TeamQuery = {
    id?: number;
    name?: string;
    description?: string;
    maxNum?: number;
    userId?: number;
}