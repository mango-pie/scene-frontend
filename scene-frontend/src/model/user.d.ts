
export type User = {
    id: number;
    username: string;
    userAccount: string;
    avatarUrl?: string;
    gender: number;
    phone: string;
    email: string;
    userStatus: number;
    userRole: number;
    plantCode: string;
    tags: string[];
    createTime: Date;
}
