<template>
    <!-- Apple Quick Look 沉浸式下载提示屏 -->
    <div class="w-full h-full flex flex-col items-center justify-center bg-transparent select-none font-sans">
        
        <!-- 悬浮大图标 -->
        <div class="w-32 h-32 opacity-90 drop-shadow-2xl mb-6 flex items-center justify-center transition-transform hover:scale-105 duration-500">
            <Icon :iconName="fileInfo.fileType == 9 ? 'zip' : 'others'" :width="128"></Icon>
        </div>
        
        <!-- 核心文件名称 -->
        <h2 class="text-2xl md:text-3xl font-bold text-white tracking-wide text-center px-4 line-clamp-2 max-w-3xl">
            {{ fileInfo.fileName || '未知文件' }}
        </h2>
        
        <!-- 辅助提示副标题 -->
        <p class="text-gray-400 mt-3.5 text-sm md:text-[15px] font-medium">
            该文件类型暂不支持在线预览，请下载后查看
        </p>

        <!-- 饱满呼吸感下载大按钮 -->
        <button @click="download" type="button"
            class="mt-10 px-10 py-3.5 bg-[#007AFF] hover:bg-[#0066cc] text-white font-semibold rounded-full shadow-[0_8px_20px_rgba(0,122,255,0.4)] transition-all ease-out duration-200 active:scale-95 flex items-center justify-center gap-2.5 cursor-pointer focus:outline-none">
            <span class="iconfont icon-download text-[18px]"></span>
            <span class="tracking-wide">下载文件 ({{ proxy.Utils.size2Str(fileInfo.fileSize) }})</span>
        </button>
        
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
    createDownloadUrl: {
        type: String
    },
    downloadUrl: {
        type: String
    },
    fileInfo: {
        type: Object
    }
})

// 下载文件
const download = async () => {
    let result = await proxy.Request({
        url: props.createDownloadUrl,
    })
    if (!result) {
        return;
    }
    window.location.href = props.downloadUrl + '/' + result.data
}
</script>

