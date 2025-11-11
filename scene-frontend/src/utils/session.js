// Session管理工具
export const SessionManager = {
    // 保存session
    setSession(sessionId) {
        localStorage.setItem('sessionId', sessionId);
    },

    // 获取session
    getSession() {
        return localStorage.getItem('sessionId');
    },

    // 清除session
    clearSession() {
        localStorage.removeItem('sessionId');
    },

    // 检查session是否存在
    hasSession() {
        return !!localStorage.getItem('sessionId');
    },

    // 清除所有认证信息
    clearAllAuth() {
        localStorage.removeItem('token');
        localStorage.removeItem('sessionId');
        // localStorage.removeItem('currentUser');
    }
};

export default SessionManager;