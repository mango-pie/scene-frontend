import { showToast } from 'vant';

// 处理API错误
export const handleApiError = (error: any): void => {
    console.error('API错误:', error);

    // 获取错误信息
    const errorMessage = error.message || '操作失败，请稍后重试';

    // 显示错误提示
    showToast(errorMessage);

    // 根据错误码进行特殊处理
    switch (error.code) {
        case 401:
            // 未授权，跳转到登录页
            if (window.location.pathname !== '/user') {
                window.location.href = '/user';
            }
            break;
        case 403:
            // 权限不足，可以显示更详细的提示
            showToast('您没有权限执行此操作');
            break;
        // 可以添加更多错误码的处理
    }
};
