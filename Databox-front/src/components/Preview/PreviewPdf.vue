<template>
    <!-- Apple Quick Look 沉浸式外层纸张容器 -->
    <div
        class="w-full h-full bg-white dark:bg-[#1e1e1e] rounded-2xl shadow-2xl overflow-hidden flex flex-col relative transition-colors duration-300">
        <!-- 核心渲染区：强制居中对齐，并在下侧留出呼吸空间 -->
        <div class="flex-1 w-full h-full overflow-auto flex justify-center py-6 px-2">
            <vue-pdf-embed ref="pdfRef" :source="state.url" width="850" :page="state.pageNum"
                class="shadow-md"></vue-pdf-embed>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue"
const { proxy } = getCurrentInstance();
import VuePdfEmbed from "vue-pdf-embed";

const props = defineProps({
    url: {
        type: String,
    },
})

const state = ref({
    url: "",
    pageNum: 0,
    numPage: 0
})

// 初始化pdf
const initPdf = () => {
    state.value.url = '/api' + props.url
}

onMounted(() => {
    initPdf()
})
</script>
