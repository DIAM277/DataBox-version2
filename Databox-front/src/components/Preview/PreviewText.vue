<template>
  <!-- Apple Quick Look 沉浸式文本容器 -->
  <div
    class="w-full h-full bg-white dark:bg-[#1e1e1e] rounded-2xl shadow-2xl overflow-hidden flex flex-col transition-colors duration-300">

    <!-- 顶部操作栏 (融合 Tailwind) -->
    <div
      class="flex flex-wrap items-center justify-between px-5 py-3 border-b border-gray-100 dark:border-[#2e2e2e] bg-gray-50/70 dark:bg-black/20 shrink-0 gap-4 select-none">
      <div class="flex items-center gap-4 md:gap-6 flex-wrap">

        <!-- 编码选择区：加入 popper-class 突破遮罩层级 -->
        <div class="flex items-center gap-2">
          <el-select v-model="encode" placeholder="请选择编码" @change="changeEncode" class="w-[105px] mac-select"
            popper-class="preview-dropdown">
            <el-option :value="utf8" label="UTF-8" />
            <el-option :value="gbk" label="GBK" />
          </el-select>
          <span class="text-[12px] text-gray-400 dark:text-gray-500 font-medium hidden sm:block">切换编码修复乱码</span>
        </div>

        <!-- 主题切换：加入 popper-class 突破遮罩层级 -->
        <div class="flex items-center gap-2">
          <el-select v-model="currentTheme" placeholder="选择主题" @change="changeTheme" class="w-[145px] mac-select"
            popper-class="preview-dropdown">
            <el-option v-for="theme in themes" :key="theme.value" :label="theme.label" :value="theme.value" />
          </el-select>
        </div>

        <!-- 换行与否 -->
        <div class="flex items-center mt-0.5">
          <el-switch v-model="wordWrap" active-text="自动换行" inactive-text="原定格式" @change="toggleWordWrap"
            style="--el-switch-on-color: #34C759;" />
        </div>

      </div>

      <!-- 复制动作 -->
      <div class="flex items-center shrink-0">
        <button @click="copy"
          class="px-4 py-1.5 rounded-lg bg-[#007AFF] hover:bg-[#0066cc] text-white text-[13px] font-medium transition-all shadow-sm active:scale-95 flex items-center gap-1.5 focus:outline-none">
          <span class="iconfont icon-copy text-[13px]"></span>立即复制
        </button>
      </div>
    </div>

    <!-- 文本主体展示区：外层动态绑定换行模式 -->
    <div class="flex-1 w-full h-full overflow-auto p-5 md:p-8 bg-transparent transition-all"
      :class="wordWrap ? 'wrap-mode' : 'nowrap-mode'">
      <highlightjs language="" :code="txtContent"
        class="font-mono text-[13.5px] leading-[1.65] text-gray-800 dark:text-[#d4d4d4]"></highlightjs>
    </div>

  </div>
</template>

<script setup>
import useClipboard from "vue-clipboard3"
const { toClipboard } = useClipboard()
import { ref, reactive, getCurrentInstance, nextTick, onMounted, watch } from "vue"
import hljs from 'highlight.js' // 导入 highlight.js
const { proxy } = getCurrentInstance();

// 动态导入主题CSS
const loadThemeCSS = (themeName) => {
  // 移除之前的主题样式
  const prevTheme = document.getElementById('highlight-theme');
  if (prevTheme) {
    prevTheme.remove();
  }

  // 创建新的样式标签
  const link = document.createElement('link');
  link.id = 'highlight-theme';
  link.rel = 'stylesheet';
  link.href = `https://cdn.jsdelivr.net/npm/highlight.js@11.7.0/styles/${themeName}.min.css`;
  document.head.appendChild(link);
};

const props = defineProps({
  url: {
    type: String,
  },
})

const txtContent = ref("")
const blobResult = ref()
const encode = ref("utf8")
const utf8 = "utf8" // 定义utf8变量
const gbk = "gbk"   // 定义gbk变量
const wordWrap = ref(true) // 默认不自动换行

// 主题列表
const themes = [
  { value: 'github', label: 'GitHub' },
  { value: 'atom-one-dark', label: 'Atom One Dark' },
  { value: 'atom-one-light', label: 'Atom One Light' },
  { value: 'monokai', label: 'Monokai' },
  { value: 'vs', label: 'Visual Studio' },
];

const currentTheme = ref('github');

// 切换主题
const changeTheme = (themeName) => {
  loadThemeCSS(themeName);
};

// 切换换行模式
const toggleWordWrap = (value) => {
  wordWrap.value = value;
};

const readTxt = async () => {
  let result = await proxy.Request({
    url: props.url,
    responseType: "blob",
  })
  if (!result) {
    return;
  }
  blobResult.value = result
  showTxt()
}

// 切换编码
const changeEncode = (e) => {
  encode.value = e
  showTxt()
}

// 显示文本
const showTxt = () => {
  const reader = new FileReader()
  reader.onload = (e) => {
    let txt = reader.result
    txtContent.value = txt
    // 手动触发高亮刷新
    // nextTick(() => {
    //   document.querySelectorAll('pre code').forEach((block) => {
    //     hljs.highlightElement(block);
    //   });
    // })
  }
  reader.readAsText(blobResult.value, encode.value)
}

// 复制文本
const copy = async () => {
  await toClipboard(txtContent.value)
  proxy.Message.success("复制成功")
}

onMounted(() => {
  // 初始化加载默认主题
  loadThemeCSS(currentTheme.value);
  readTxt()
})
</script>

<style scoped>
:deep(.hljs) {
  background: transparent !important;
  padding: 0 !important;
  margin: 0 !important;
  font-family: inherit;
  color: inherit !important;
}

.wrap-mode :deep(pre),
.wrap-mode :deep(code) {
  white-space: pre-wrap !important;
  word-break: break-all !important;
  overflow-wrap: anywhere !important;
}

.nowrap-mode :deep(pre),
.nowrap-mode :deep(code) {
  white-space: pre !important;
  word-break: normal !important;
}

/* 覆写下沉式控制栏表单组件 */
:deep(.mac-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08) inset !important;
  background-color: rgba(255, 255, 255, 0.5) !important;
  border-radius: 8px;
  height: 32px;
}

html.dark :deep(.mac-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
  background-color: rgba(0, 0, 0, 0.2) !important;
}

:deep(.mac-select .el-input__inner) {
  font-size: 13px;
  font-weight: 500;
}
</style>

<style>
/* 突破外层 Preview.vue 的 z-[9999] 遮罩层级 */
.preview-dropdown.el-popper {
  z-index: 10000 !important;
  border-radius: 12px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2) !important;
}

/* 适配暗黑模式下拉面板颜色 */
html.dark .preview-dropdown.el-popper {
  background-color: #1c1c1e !important;
  border-color: #38383a !important;
}

html.dark .preview-dropdown .el-select-dropdown__item {
  color: #f5f5f7 !important;
}

html.dark .preview-dropdown .el-select-dropdown__item.hover,
html.dark .preview-dropdown .el-select-dropdown__item:hover {
  background-color: #2c2c2e !important;
}
</style>
