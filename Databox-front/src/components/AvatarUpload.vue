<template>
    <!-- 完美的 100% 撑满容器，形状与阴影已由父组件 UpdateAvatar.vue 的 rounded-full 接管 -->
    <div class="w-full h-full relative cursor-pointer bg-gray-100 dark:bg-[#1c1c1e] text-gray-400 group">
        <el-upload name="file" :show-file-list="false" accept=".png,.PNG,.jpg,.JPG,.jpeg,.JPEG,.gif,.GIF,.bmp,.BMP"
            :multiple="false" :http-request="uploadImage"
            class="mac-avatar-uploader w-full h-full absolute inset-0 z-0">

            <!-- 头像图片本身层：自适应父组件正圆与撑满 -->
            <img v-if="localFile" :src="localFile"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" />
            <img v-else :src="`/api/getAvatar/${modelValue?.userId}`"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" />

            <!-- Apple 沉浸式高斯模糊悬浮遮罩 (Hover 玻璃态反馈) -->
            <div
                class="absolute inset-0 bg-black/40 dark:bg-black/60 backdrop-blur-[3px] flex flex-col items-center justify-center opacity-0 hover:opacity-100 transition-all duration-300 ease-out z-10">
                <span
                    class="iconfont icon-picture text-white text-[28px] drop-shadow-md mb-1.5 transition-transform duration-300 hover:scale-110"></span>
                <span class="text-white text-[12.5px] font-semibold tracking-wider drop-shadow-md">点击更换</span>
            </div>

        </el-upload>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, watch } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from 'vue-router';
const route = useRoute();
const router = useRouter();

const timestamp = ref('');

const props = defineProps({
    modelValue: {
        type: Object,
        default: null
    }
})

const localFile = ref(null)
const emit = defineEmits()
const uploadImage = async (file) => {
    file = file.file
    let img = new FileReader()
    img.readAsDataURL(file)
    img.onload = ({ target }) => {
        localFile.value = target.result
    }
    emit('update:modelValue', file)
}

watch(() => props.modelValue, (newVal) => {
    if (!(newVal instanceof File)) {
        localFile.value = null;
    }
})
</script>

<style scoped>
/* 
  利用深层穿透彻底接管 Element Plus 上传组件的生硬外壳 
  强制让上传热区百分百贴合外部圆形，不留任何可点击死角
*/
:deep(.mac-avatar-uploader .el-upload) {
    width: 100% !important;
    height: 100% !important;
    display: block !important;
    position: absolute;
    inset: 0;
}
</style>