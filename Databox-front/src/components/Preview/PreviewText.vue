<template>
  <div class="code">
    <div class="top-op">
      <div class="left-controls">
        <div class="encode-select">
          <!-- 编码选择下拉框 -->
          <el-select v-model="encode" placeholder="请选择编码" @change="changeEncode">
            <el-option :value="utf8" label="UTF-8" />
            <el-option :value="gbk" label="GBK" />
          </el-select>
          <div class="tips">乱码了？切换编码试试😊</div>
        </div>
        <!-- 主题选择器 -->
        <div class="theme-select">
          <el-select v-model="currentTheme" placeholder="选择主题" @change="changeTheme">
            <el-option v-for="theme in themes" :key="theme.value" :label="theme.label" :value="theme.value" />
          </el-select>
        </div>
        <!-- 换行模式切换 -->
        <div class="wrap-mode">
          <el-switch v-model="wordWrap" active-text="自动换行" inactive-text="原始格式" @change="toggleWordWrap" />
        </div>
      </div>
      <div class="copy-btn">
        <el-button @click="copy" type="primary">复制</el-button>
      </div>
    </div>
    <highlightjs language="" :code="txtContent" class="code-block" :class="{ 'word-wrap': wordWrap }"></highlightjs>
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
const wordWrap = ref(false) // 默认不自动换行

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

<style lang="scss" scoped>
.code {
  width: 100%;
  height: 100%;
  background-color: #fff;
  border-radius: 8px;
  overflow: auto;
  padding: 15px;

  .top-op {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .left-controls {
      display: flex;
      align-items: center;
      gap: 15px;

      .encode-select {
        display: flex;
        align-items: center;

        .tips {
          margin-left: 10px;
          font-size: 12px;
          color: #909399;
        }
      }

      .theme-select {
        display: flex;
        min-width: 120px;
      }

      .wrap-mode {
        margin-left: 10px;
      }
    }
  }

  .code-block {
    width: 100%;
    overflow-x: auto; // 添加水平滚动
  }

  :deep(.hljs) {
    padding: 15px;
    border-radius: 8px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 14px;
    line-height: 1.5;
    overflow-x: auto; // 确保代码块可以水平滚动
    white-space: pre; // 保持原始格式，不换行
    background-color: #f5f7fa;
  }

  // 自动换行样式
  .code-block.word-wrap {
    :deep(.hljs) {
      white-space: pre-wrap; // 自动换行
      word-wrap: break-word; // 确保长单词也能换行
    }
  }
}
</style>