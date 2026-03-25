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

        <!-- 🚨 新增：Markdown 动态预览切换器（当侦测到扩展名为 md 时才显示） -->
        <div class="flex items-center mt-0.5" v-if="isMdFile">
          <el-switch v-model="mdPreviewMode" active-text="MD预览" inactive-text="查看源码"
            style="--el-switch-on-color: #007AFF;" />
        </div>

        <!-- 换行与否 (当正在预览渲染好的 MD 时隐藏此项无意义控制) -->
        <div class="flex items-center mt-0.5" v-show="!isMdFile || !mdPreviewMode">
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

    <!-- 文本主体展示区：外层动态绑定换行模式 (🚨修复了txt默认不换行的漏洞) -->
    <div class="flex-1 w-full h-full overflow-auto p-5 md:p-8 bg-transparent transition-all"
      :class="(!isMdFile || !mdPreviewMode) && wordWrap ? 'wrap-mode' : 'nowrap-mode'">

      <!-- 🚨 核心：Markdown 沉浸式富文本渲染态 -->
      <div v-if="isMdFile && mdPreviewMode"
        class="custom-markdown max-w-4xl mx-auto text-[15px] leading-[1.75] text-gray-800 dark:text-gray-200 selection:bg-blue-200 dark:selection:bg-blue-900 tracking-wide select-text"
        v-html="parsedMarkdown">
      </div>

      <!-- 原有原生文本代码渲染态 -->
      <highlightjs v-else language="" :code="txtContent"
        class="font-mono text-[13.5px] leading-[1.65] text-gray-800 dark:text-[#d4d4d4]"></highlightjs>
    </div>

  </div>
</template>

<script setup>
import useClipboard from "vue-clipboard3"
const { toClipboard } = useClipboard()
// 🚨 加载 marked 来解析 md
import { marked } from 'marked';
import { ref, reactive, getCurrentInstance, nextTick, onMounted, watch, computed } from "vue"
import hljs from 'highlight.js'
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
  // 🚨 接收外部文件名
  fileName: {
    type: String,
    default: ""
  }
})

const txtContent = ref("")
const blobResult = ref()
const encode = ref("utf8")
const utf8 = "utf8"
const gbk = "gbk"
const wordWrap = ref(true)

// ============= 新增 Markdown 处理控制区域 =============
// 判定是否为 MD 文件
const isMdFile = computed(() => {
  if (!props.fileName) return false;
  return props.fileName.toLowerCase().endsWith('.md');
});

// 若是md文件，则默认开启 MD 渲染模式
const mdPreviewMode = ref(true);

// 实时转化 Markdown 文本
const parsedMarkdown = computed(() => {
  if (!txtContent.value) return '';
  return marked.parse(txtContent.value);
});

// 监听富文本渲染完成，顺带将里面插入的代码块也用当前 hljs 主题高亮！
watch(parsedMarkdown, () => {
  if (isMdFile.value && mdPreviewMode.value) {
    nextTick(() => {
      document.querySelectorAll('.custom-markdown pre code').forEach((block) => {
        hljs.highlightElement(block);
      });
    });
  }
});
// ====================================================

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

/* ================== 新增：极致优雅的 Apple 风 Markdown 排版层补偿 ================== */
:deep(.custom-markdown p) {
  margin-bottom: 16px;
}

:deep(.custom-markdown h1),
:deep(.custom-markdown h2),
:deep(.custom-markdown h3),
:deep(.custom-markdown h4),
:deep(.custom-markdown h5) {
  font-weight: 600;
  margin-top: 24px;
  margin-bottom: 12px;
  line-height: 1.4;
  color: #1d1d1f;
}

html.dark :deep(.custom-markdown h1),
html.dark :deep(.custom-markdown h2),
html.dark :deep(.custom-markdown h3),
html.dark :deep(.custom-markdown h4),
html.dark :deep(.custom-markdown h5) {
  color: #f5f5f7;
}

:deep(.custom-markdown h1) {
  font-size: 1.8em;
  border-bottom: 1px solid rgba(125, 125, 125, 0.2);
  padding-bottom: 0.3em;
}

:deep(.custom-markdown h2) {
  font-size: 1.5em;
  border-bottom: 1px solid rgba(125, 125, 125, 0.2);
  padding-bottom: 0.3em;
}

:deep(.custom-markdown h3) {
  font-size: 1.25em;
}

:deep(.custom-markdown ul) {
  list-style-type: disc;
  padding-left: 20px;
  margin-bottom: 16px;
}

:deep(.custom-markdown ol) {
  list-style-type: decimal;
  padding-left: 20px;
  margin-bottom: 16px;
}

:deep(.custom-markdown li) {
  margin-bottom: 6px;
}

:deep(.custom-markdown strong) {
  font-weight: 700;
  color: #000;
}

html.dark :deep(.custom-markdown strong) {
  color: #fff;
}

:deep(.custom-markdown blockquote) {
  border-left: 4px solid #007AFF;
  background: rgba(0, 122, 255, 0.05);
  padding: 10px 16px;
  border-radius: 0 8px 8px 0;
  color: #555;
  margin: 16px 0;
}

html.dark :deep(.custom-markdown blockquote) {
  border-left-color: #0a84ff;
  color: #98989d;
  background: rgba(10, 132, 255, 0.1);
}

:deep(.custom-markdown pre) {
  padding: 16px;
  border-radius: 12px;
  overflow-x: auto;
  margin-bottom: 16px;
  border: 1px solid rgba(125, 125, 125, 0.2);
}

:deep(.custom-markdown pre code) {
  background-color: transparent !important;
  padding: 0;
}

:deep(.custom-markdown code:not(pre code)) {
  background-color: rgba(125, 125, 125, 0.12);
  color: #FF2D55;
  padding: 3px 6px;
  border-radius: 6px;
  font-family: monospace;
  font-size: 0.9em;
  margin: 0 2px;
}

html.dark :deep(.custom-markdown code:not(pre code)) {
  color: #FF375F;
}

:deep(.custom-markdown a) {
  color: #007AFF;
  text-decoration: none;
  transition: opacity 0.2s;
}

:deep(.custom-markdown a:hover) {
  text-decoration: underline;
  opacity: 0.8;
}

:deep(.custom-markdown img) {
  max-width: 100%;
  border-radius: 12px;
  margin: 12px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
