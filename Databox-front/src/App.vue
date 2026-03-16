<template>
  <el-config-provider :locale="locale" :message="config">
    <router-view></router-view>
  </el-config-provider>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const locale = zhCn
const config = reactive({
  max: 1
})

// === 全局初始化主题配置 ===
const initTheme = () => {
  const savedTheme = localStorage.getItem('theme');

  if (savedTheme === 'dark') {
    document.documentElement.classList.add('dark');
  } else if (savedTheme === 'light') {
    document.documentElement.classList.remove('dark');
  } else {
    // 如果没有任何缓存，则尝试读取设备操作系统的偏好设置
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    if (prefersDark) {
      document.documentElement.classList.add('dark');
      // 可选：将操作系统的首选默认也存入本地
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }
}

onMounted(() => {
  initTheme()
})
</script>

<style lang="scss" scoped></style>
