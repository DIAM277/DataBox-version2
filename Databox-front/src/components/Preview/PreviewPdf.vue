<template>
    <div class="pdf">
        <vue-pdf-embed ref="pdfRef" :source="state.url" class="vue-pdf-embed" width="850"
            :page="state.pageNum"></vue-pdf-embed>
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

<style lang="scss" scoped>
.pdf {
    width: 100%;
}
</style>
