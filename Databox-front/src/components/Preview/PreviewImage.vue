<template>
    <div class="image-viewer"></div>
    <el-image-viewer :url-list="imageList" :initial-index="previewImageIndex" v-if="previewImageIndex !== null"
        @close="closeImgViewer" hide-on-click-modal :show-progress="true" :infinite="true">
    </el-image-viewer>
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

<style lang="scss" scoped>
.image-viewer {
    .el-image-viewer__mask {
        opacity: 0.8;
    }
}
</style>
