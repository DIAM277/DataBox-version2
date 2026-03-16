<template>
    <!-- 全屏居中透明包裹层 -->
    <div class="w-full h-full flex items-center justify-center p-4 md:p-10 relative bg-transparent">
        <el-image-viewer class="mac-image-viewer" :url-list="imageList" :initial-index="previewImageIndex"
            v-if="previewImageIndex !== null" @close="closeImgViewer" hide-on-click-modal :show-progress="true"
            :infinite="true">
        </el-image-viewer>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const previewImageIndex = ref(null)

const props = defineProps({
    imageList: {
        type: Array,
    }
})

const show = (index) => {
    previewImageIndex.value = index
}

defineExpose({
    show
})

const closeImgViewer = () => {
    previewImageIndex.value = null
}
</script>

<style scoped>
:deep(.el-image-viewer__mask) {
    background: rgba(0, 0, 0, 0.9) !important;
    backdrop-filter: blur(12px) !important;
    -webkit-backdrop-filter: blur(12px) !important;
    opacity: 1 !important;
}

:deep(.el-image-viewer__img) {
    max-width: 100% !important;
    max-height: 100% !important;
    object-fit: contain !important;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.6) !important;
    transition: transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
    cursor: grab !important;
}

:deep(.el-image-viewer__img:active) {
    cursor: grabbing !important;
}

:deep(.el-image-viewer__btn) {
    background-color: rgba(255, 255, 255, 0.1) !important;
    backdrop-filter: blur(8px) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    color: white !important;
    transition: all 0.25s ease !important;
}

:deep(.el-image-viewer__btn:hover) {
    background-color: rgba(255, 255, 255, 0.2) !important;
}

:deep(.el-image-viewer__close:hover) {
    transform: scale(1.1) !important;
}

:deep(.el-image-viewer__prev:hover) {
    transform: translateY(-50%) scale(1.1) !important;
}

:deep(.el-image-viewer__next:hover) {
    transform: translateY(-50%) scale(1.1) !important;
}

:deep(.el-image-viewer__actions:hover) {
    transform: translateX(-50%) scale(1.02) !important;
}

:deep(.el-image-viewer__close) {
    top: 24px !important;
    right: 24px !important;
    width: 40px !important;
    height: 40px !important;
    border-radius: 50% !important;
}
</style>
