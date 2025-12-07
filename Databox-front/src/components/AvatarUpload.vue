<template>
    <div class="avatar-upload">
        <div class="avatar-show">
            <el-upload name="file" :show-file-list="false" accept=".png,.PNG,.jpg,.JPG,.jpeg,.JPEG,.gif,.GIF,.bmp,.BMP"
                :multiple="false" :http-request="uploadImage" class="avatar-upload-area">
                <template v-if="localFile">
                    <img :src="localFile" />
                </template>
                <template v-else>
                    <img :src="`/api/getAvatar/${modelValue.userId}`" />
                </template>
                <div class="hover-mask">
                    <span class="iconfont icon-picture"></span>
                    <span class="upload-text">点击上传</span>
                </div>
            </el-upload>
        </div>
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

<style lang="scss" scoped>
.avatar-upload {
    display: flex;
    justify-content: center;
    align-items: center;


    .avatar-show {
        background: rgb(245, 245, 245);
        width: 150px;
        height: 150px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;
        position: relative;
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;

        .avatar-upload-area {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            position: relative;

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .hover-mask {
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.5);
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                opacity: 0;
                transition: opacity 0.3s ease;
                color: white;

                .iconfont {
                    font-size: 30px;
                    margin-bottom: 8px;
                }

                .upload-text {
                    font-size: 14px;
                }
            }

            &:hover .hover-mask {
                opacity: 1;
            }
        }

        &:hover {
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        }
    }
}
</style>