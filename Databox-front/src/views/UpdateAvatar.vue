<template>
    <div>
        <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="420px"
            :showCancel="true" @close="dialogConfig.show = false" :showCustomTitle="true">

            <!-- 纯粹的 Apple 极简居中排版风格 -->
            <el-form :model="formData" ref="formDataRef" @submit.prevent
                class="flex flex-col items-center justify-center py-6 px-8">

                <!-- 大头像展示与交互区 -->
                <div
                    class="relative flex justify-center items-center w-32 h-32 md:w-36 md:h-36 rounded-full border-4 border-white dark:border-[#2c2c2e] shadow-xl hover:shadow-2xl transition-all duration-300 mb-8 overflow-hidden bg-gray-50 dark:bg-black/20 group">
                    <AvatarUpload v-model="formData.avatar"
                        class="w-full h-full scale-105 transition-transform group-hover:scale-110" />
                </div>

                <!-- 极简高亮输入框：去除原生 Label，强调输入本身 -->
                <div class="w-full mb-2">
                    <el-form-item class="mb-0 custom-form-wrapper">
                        <el-input v-model="formData.newUserName" placeholder="你的新用户名" size="large" clearable>
                            <template #prefix>
                                <span class="iconfont icon-account text-gray-400 text-[18px]"></span>
                            </template>
                        </el-input>
                    </el-form-item>
                </div>

                <div class="text-[12px] text-gray-400 font-medium tracking-wide mt-3 text-center">
                    在此设定你在系统内展示的新名片
                </div>

            </el-form>
        </Dialog>
    </div>
</template>

<script setup>
import AvatarUpload from "@/components/Business/AvatarUpload.vue";
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
import { useRoute, useRouter } from 'vue-router';
// ★ 1. 引入并使用刚刚梳理好的 userStore
import { useUserStore } from '@/store/userStore';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const api = {
    updateUserAvatar: 'updateUserAvatar'
}

const formData = ref({});
const formDataRef = ref();

const show = (data) => {
    formData.value = Object.assign({}, data);
    formData.value.avatar = { userId: data.userId }
    formData.value.newUserName = data.userName; // 初始化为当前用户名
    dialogConfig.value.show = true;
}
defineExpose({ show });

const dialogConfig = ref({
    show: false,
    title: "修改资料",
    buttons: [
        {
            type: "primary",
            text: "确定",
            click: (e) => {
                submitForm();
            },
        },
    ],
});

const emit = defineEmits(['updateAvatar'])

// 提交表单
const submitForm = async () => {
    // 检查用户名是否为空
    if (!formData.value.newUserName || formData.value.newUserName.trim() === '') {
        proxy.Message.warning('用户名不能为空')
        return;
    }

    // 如果没有选择新头像且用户名没有变化，则不需要提交
    if (!(formData.value.avatar instanceof File) && formData.value.newUserName === formData.value.userName) {
        proxy.Message.warning('请选择新头像或修改用户名')
        return;
    }

    let params = {
        newUserName: formData.value.newUserName
    };

    // 如果有新头像，添加到参数中
    if (formData.value.avatar instanceof File) {
        params.avatar = formData.value.avatar;
    }

    let result = await proxy.Request({
        url: api.updateUserAvatar,
        params: params
    })

    if (!result) {
        return;
    }

    proxy.Message.success("修改成功");
    dialogConfig.value.show = false;

    // ★ 2. 彻底接管状态更新：加入时间戳突破浏览器图片强缓存，并交付给全局 Store 触发所有组件顺滑同步
    const updates = {};
    if (formData.value.newUserName !== formData.value.userName) {
        updates.userName = formData.value.newUserName;
    }
    if (formData.value.avatar instanceof File) {
        updates.avatar = `${proxy.globalInfo.avatarUrl}${formData.value.userId}?t=${new Date().getTime()}`;
    }

    // 执行状态下发与自动 Cookie 固化
    if (Object.keys(updates).length > 0) {
        userStore.updateUserInfo(updates);
    }

    // 通知父框架 (可选冗余保留)
    emit('updateAvatar', {
        avatar: result.data,
        userName: formData.value.newUserName
    });
};
</script>

<style scoped>
/* 隐藏 el-form-item 可能自带的多余占位 */
:deep(.custom-form-wrapper .el-form-item__content) {
    margin-left: 0 !important;
    display: flex;
    justify-content: center;
}

/* 核心输入框去边框与扁平化设计 */
:deep(.el-input__wrapper) {
    box-shadow: none !important;
    @apply bg-gray-100 dark:bg-[#2c2c2e] rounded-xl transition-all duration-300 py-3 px-5;
}

/* 聚焦状态苹果光晕效果 */
:deep(.el-input__wrapper.is-focus),
:deep(.el-input__wrapper:focus-within) {
    @apply ring-2 ring-blue-500/50 bg-white dark:bg-[#1c1c1e];
    box-shadow: none !important;
}

/* 输入框文字体感：放大并居中靠拢 */
:deep(.el-input__inner) {
    @apply text-gray-800 dark:text-gray-200 font-bold text-[15px];
    text-align: center;
    /* 让文字居中显示，加强名片录入感 */
}

:deep(.el-input__inner::placeholder) {
    font-weight: 500;
}
</style>