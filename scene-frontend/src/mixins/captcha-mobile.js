// 验证码移动端默认配置混入
export default {
    props: {
        // 移动端默认尺寸
        length: {
            type: Number,
            default: 4
        },
        width: {
            type: Number,
            default: 280 // 对应样式变量中的canvas-width
        },
        height: {
            type: Number,
            default: 70 // 对应样式变量中的canvas-height
        },
        // 移动端默认文本
        placeholder: {
            type: String,
            default: '输入验证码后按回车验证'
        },
        // 移动端默认隐藏按钮
        hideRefreshBtn: {
            type: Boolean,
            default: true
        },
        hideVerifyBtn: {
            type: Boolean,
            default: true
        },
        // 移动端默认样式
        containerStyle: {
            type: Object,
            default: () => ({
                width: '100%'
            })
        },
        canvasStyle: {
            type: Object,
            default: () => ({
                borderRadius: '12px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
                backgroundColor: '#f9fafb'
            })
        }
    }
};