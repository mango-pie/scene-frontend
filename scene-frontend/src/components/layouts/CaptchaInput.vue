<template>
  <!-- 验证码组件容器，支持样式穿透 -->
  <div class="captcha-container" :style="containerStyle">
    <!-- 验证码画布 -->
    <canvas
        ref="captchaCanvas"
        class="captcha-canvas"
        :width="width"
        :height="height"
        @click="refreshCaptcha"
        :style="canvasStyle"
    ></canvas>

    <!-- 输入区域 -->
    <div class="captcha-input-wrap" :style="inputWrapStyle">
      <input
          v-model="userInput"
          type="text"
          class="captcha-input"
          :placeholder="placeholder"
          @keyup.enter="handleVerify"
          :style="inputStyle"
      />
      <button
          class="captcha-btn captcha-refresh-btn"
          @click="refreshCaptcha"
          :style="refreshBtnStyle"
      >
        {{ refreshText }}
      </button>
      <button
          class="captcha-btn captcha-verify-btn"
          @click="handleVerify"
          :style="verifyBtnStyle"
      >
        {{ verifyText }}
      </button>
    </div>

    <!-- 验证结果提示 -->
    <div class="captcha-result" :style="resultStyle">
      {{ resultText }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue';

// 1. 定义Props：支持自定义配置，覆盖默认值
const props = defineProps({
  // 验证码长度
  length: {
    type: Number,
    default: 4,
  },
  // 画布宽度
  width: {
    type: Number,
    default: 240,
  },
  // 画布高度
  height: {
    type: Number,
    default: 80,
  },
  // 验证码字符集（数字/字母/自定义）
  charSet: {
    type: String,
    default: '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz',
  },
  // 输入框占位符
  placeholder: {
    type: String,
    default: '请输入验证码',
  },
  // 按钮文本自定义
  verifyText: {
    type: String,
    default: '验证',
  },
  refreshText: {
    type: String,
    default: '刷新',
  },
  // 样式自定义（通过CSS变量/内联样式）
  containerClass: {
    type: String,
    default: '',
  },
  // v-model 绑定用户输入（可选）
  modelValue: {
    type: String,
    default: '',
  },
  // 是否验证失败后自动刷新
  autoRefreshOnFail: {
    type: Boolean,
    default: true,
  },
  // 失败后刷新延迟（ms）
  refreshDelay: {
    type: Number,
    default: 1000,
  },
});

// 2. 定义Emit：向父组件传递事件
const emit = defineEmits([
  'update:modelValue', // v-model 双向绑定
  'verify-success',    // 验证成功
  'verify-fail',       // 验证失败
  'refresh',           // 刷新验证码
]);

// 3. 响应式数据（组件内部状态，每个实例独立）
const captchaCanvas = ref(null); // canvas ref
const userInput = ref(props.modelValue); // 用户输入
const currentCaptcha = ref(''); // 当前正确验证码
const resultText = ref(''); // 验证结果提示
const ctx = ref(null); // canvas 上下文

// 4. 计算属性：样式整合（支持父组件自定义）
const containerStyle = computed(() => ({
  width: `${props.width}px`,
  margin: '10px 0',
  ...(props.containerClass ? {} : {})
}));
const canvasStyle = computed(() => ({
  border: '1px solid #ddd',
  borderRadius: '4px',
  cursor: 'pointer',
  backgroundColor: '#f8f8f8',
}));
const inputWrapStyle = computed(() => ({
  display: 'flex',
  gap: '8px',
  alignItems: 'center',
  marginTop: '8px',
}));
const inputStyle = computed(() => ({
  flex: 1,
  padding: '6px 10px',
  border: '1px solid #ddd',
  borderRadius: '4px',
  outline: 'none',
  '&:focus': {
    borderColor: '#409eff',
  },
}));
const verifyBtnStyle = computed(() => ({
  padding: '6px 12px',
  border: 'none',
  borderRadius: '4px',
  backgroundColor: '#409eff',
  color: '#fff',
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: '#66b1ff',
  },
}));
const refreshBtnStyle = computed(() => ({
  ...verifyBtnStyle.value,
  backgroundColor: '#909399',
  '&:hover': {
    backgroundColor: '#a6a9ad',
  },
}));
const resultStyle = computed(() => ({
  marginTop: '8px',
  fontSize: '14px',
  height: '20px',
  color: resultText.value.includes('成功') ? 'green' : 'red',
}));

// 5. 核心方法：生成随机验证码
const generateCaptcha = () => {
  let captcha = '';
  for (let i = 0; i < props.length; i++) {
    const randomIndex = Math.floor(Math.random() * props.charSet.length);
    captcha += props.charSet[randomIndex];
  }
  return captcha;
};

// 6. 核心方法：绘制验证码
const drawCaptcha = () => {
  if (!ctx.value) return;
  // 清空画布
  ctx.value.clearRect(0, 0, props.width, props.height);

  // 绘制干扰线
  for (let i = 0; i < 5; i++) {
    ctx.value.strokeStyle = `rgb(${Math.floor(Math.random() * 150)}, ${Math.floor(Math.random() * 150)}, ${Math.floor(Math.random() * 150)})`;
    ctx.value.beginPath();
    ctx.value.moveTo(Math.random() * props.width, Math.random() * props.height);
    ctx.value.lineTo(Math.random() * props.width, Math.random() * props.height);
    ctx.value.stroke();
  }

  // 绘制干扰点
  for (let i = 0; i < 50; i++) {
    ctx.value.fillStyle = `rgb(${Math.floor(Math.random() * 150)}, ${Math.floor(Math.random() * 150)}, ${Math.floor(Math.random() * 150)})`;
    ctx.value.beginPath();
    ctx.value.arc(
        Math.random() * props.width,
        Math.random() * props.height,
        Math.random() * 2,
        0,
        2 * Math.PI
    );
    ctx.value.fill();
  }

  // 绘制验证码文本
  for (let i = 0; i < currentCaptcha.value.length; i++) {
    ctx.value.fillStyle = `rgb(${Math.floor(Math.random() * 150)}, ${Math.floor(Math.random() * 150)}, ${Math.floor(Math.random() * 150)})`;
    ctx.value.font = `${Math.floor(Math.random() * 10) + 24}px Arial`;
    ctx.value.fillText(
        currentCaptcha.value[i],
        30 + i * (props.width / props.length - 10), // 自适应字符间距
        props.height / 2 + (Math.random() * 20 - 10) // 垂直居中+随机偏移
    );
  }
};

// 7. 公开方法：刷新验证码（组件内部/父组件均可调用）
const refreshCaptcha = () => {
  currentCaptcha.value = generateCaptcha();
  drawCaptcha();
  userInput.value = '';
  resultText.value = '';
  emit('refresh', currentCaptcha.value); // 通知父组件刷新
  emit('update:modelValue', ''); // 同步v-model
};

// 8. 公开方法：验证验证码
const handleVerify = () => {
  const inputVal = userInput.value.trim().toLowerCase();
  const correctVal = currentCaptcha.value.toLowerCase();

  if (!inputVal) {
    resultText.value = '请输入验证码';
    return;
  }

  if (inputVal === correctVal) {
    resultText.value = '验证成功';
    emit('verify-success', currentCaptcha.value); // 通知父组件验证成功
  } else {
    resultText.value = '验证失败';
    emit('verify-fail', inputVal); // 通知父组件验证失败
    // 失败后自动刷新
    if (props.autoRefreshOnFail) {
      setTimeout(refreshCaptcha, props.refreshDelay);
    }
  }
};

// 9. 监听v-model变化（父组件修改modelValue时同步）
watch(
    () => props.modelValue,
    (newVal) => {
      userInput.value = newVal;
    },
    { immediate: true }
);

// 10. 监听用户输入，同步v-model到父组件
watch(userInput, (newVal) => {
  emit('update:modelValue', newVal);
});

// 11. 初始化：获取canvas上下文 + 生成第一个验证码
onMounted(() => {
  if (captchaCanvas.value) {
    ctx.value = captchaCanvas.value.getContext('2d');
    refreshCaptcha();
  }
});

// 12. 暴露方法给父组件（父组件可通过ref调用）
defineExpose({
  refreshCaptcha,
  handleVerify,
  getCurrentCaptcha: () => currentCaptcha.value, // 获取当前正确验证码（谨慎使用）
});
</script>

<style scoped>
/* 基础样式，支持父组件通过style穿透覆盖 */
.captcha-container {
  box-sizing: border-box;
}

.captcha-canvas {
  display: block;
}

.captcha-input {
  box-sizing: border-box;
}

.captcha-btn {
  box-sizing: border-box;
}

/* 样式穿透：父组件可通过 .custom-class >>> .captcha-canvas 覆盖样式 */
:deep(.captcha-canvas) {
  /* 基础样式，可被覆盖 */
}
</style>