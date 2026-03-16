<template>
  <!-- Apple Quick Look 沉浸式外层纸张容器 -->
  <div
    class="w-full h-full bg-white dark:bg-[#1e1e1e] rounded-2xl shadow-2xl overflow-hidden flex flex-col relative transition-colors duration-300">
    <!-- 核心渲染区：内部自动撑满并接管安全滚动 -->
    <div ref="docRef" class="flex-1 w-full h-full overflow-auto"></div>
  </div>
</template>

<script setup>
import * as docx from "docx-preview";
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
  url: {
    type: String
  }
});

const docRef = ref();
const initDoc = async () => {
  let result = await proxy.Request({
    url: props.url,
    responseType: 'blob'
  })
  if (!result) {
    return;
  }
  docx.renderAsync(result, docRef.value)
};

onMounted(() => {
  initDoc();
});
</script>

