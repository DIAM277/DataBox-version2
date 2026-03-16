<template>
    <div
        class="h-screen w-screen bg-[#f5f5f7] dark:bg-[#000000] flex items-center justify-center relative overflow-hidden font-sans select-none">

        <!-- 极致柔和的动态背景光晕 -->
        <div
            class="absolute top-[15%] left-[20%] w-[40vw] h-[40vw] bg-blue-400/20 dark:bg-blue-900/20 rounded-full blur-[120px] -z-10 pointer-events-none">
        </div>
        <div
            class="absolute bottom-[10%] right-[15%] w-[35vw] h-[35vw] bg-indigo-400/10 dark:bg-purple-900/20 rounded-full blur-[130px] -z-10 pointer-events-none">
        </div>

        <!-- 核心毛玻璃验证卡片 -->
        <div
            class="w-[400px] sm:w-[440px] p-8 sm:p-10 bg-white/70 dark:bg-[#1c1c1e]/70 backdrop-blur-2xl rounded-[2rem] shadow-[0_20px_60px_rgba(0,0,0,0.08)] dark:shadow-[0_20px_60px_rgba(0,0,0,0.4)] border border-white/60 dark:border-gray-700/30 flex flex-col items-center relative z-10 transition-all duration-500">

            <!-- Apple 风 Logo 区 -->
            <div class="flex items-center gap-2.5 cursor-pointer group mb-7" @click="jumpToHome">
                <svg class="w-9 h-9 text-[#007AFF] drop-shadow-sm transition-transform group-hover:scale-105"
                    viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path
                        d="M17.5 19H6.5C3.46243 19 1 16.5376 1 13.5C1 10.6693 3.14081 8.34327 5.90802 8.03333C6.44299 4.60655 9.42168 2 12 2C15.1118 2 17.7554 4.08182 18.6655 6.94276C21.0963 7.29177 23 9.40059 23 12C23 15.866 19.866 19 17.5 19Z" />
                </svg>
                <span class="font-semibold text-[22px] text-[#1d1d1f] dark:text-[#f5f5f7] tracking-wide">
                    DataBox
                </span>
            </div>

            <!-- 用户与分享文件信息卡槽 -->
            <div
                class="w-full flex flex-col items-center bg-gray-50/80 dark:bg-black/20 px-5 py-6 rounded-3xl mb-7 border border-gray-200/60 dark:border-[#38383a]/60 backdrop-blur-sm">
                <Avatar :userId="shareInfo.userId" :avatar="shareInfo.avatar" :width="100" :interactive="false"
                    class="mb-3 shadow-md border-2 border-white dark:border-[#2c2c2e] rounded-full transition-transform hover:scale-105" />
                <span class="text-[13px] text-[#86868b] dark:text-gray-400 font-medium mb-1.5">
                    <span class="text-gray-700 dark:text-gray-300 font-semibold">{{ shareInfo.userName }}</span> 分享于 {{
                        shareInfo.shareTime?.split(' ')[0] }}
                </span>
                <span
                    class="text-[15px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] text-center px-2 line-clamp-2 leading-snug">
                    {{ shareInfo.fileName }}
                </span>
            </div>

            <!-- 提取码输入与提交区 -->
            <div class="w-full">
                <h2 class="text-center text-[14.5px] text-gray-500 dark:text-gray-400 font-medium mb-4 tracking-wide">
                    请输入提取码以查看分享文件</h2>

                <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent class="m-0">
                    <el-form-item prop="code" class="mb-0 !border-none">
                        <!-- 极简的大号字间距原生级输入框 -->
                        <input v-model.trim="formData.code" @keyup.enter="checkShare" type="text" maxlength="5"
                            placeholder="•••••"
                            class="w-full h-[56px] bg-gray-100/80 dark:bg-[#2c2c2e] border-2 border-transparent focus:border-[#007AFF]/40 text-[#1d1d1f] dark:text-white rounded-2xl text-center text-[22px] tracking-[0.5em] font-bold outline-none transition-all duration-300 focus:bg-white dark:focus:bg-[#1c1c1e] focus:shadow-sm placeholder:text-gray-400/50 placeholder:tracking-normal placeholder:font-normal placeholder:text-lg" />
                    </el-form-item>

                    <button type="button" @click="checkShare"
                        class="w-full h-[52px] bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-xl text-[16px] font-semibold mt-6 transition-all duration-200 active:scale-[0.98] shadow-md shadow-blue-500/20 flex items-center justify-center tracking-wide">
                        提取文件
                    </button>
                </el-form>
            </div>

            <!-- 页脚 -->
            <div
                class="mt-8 text-center text-[12px] text-[#86868b] dark:text-gray-500 font-medium tracking-wide opacity-80">
                DataBox - 安全、高效的文件分享平台
            </div>
        </div>
    </div>
</template>

<script setup>
// 引入 onMounted 处理路由自动提取
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from 'vue-router';

// 移除不再需要的独立 AppTitle2 组件引用
const route = useRoute();
const router = useRouter();

const api = {
    getShareInfo: '/showShare/getShareInfo',
    checkShareCode: '/showShare/checkShareCode'
}

const shareId = route.params.shareId
const shareInfo = ref({})

// 获取分享文件信息
const getShareInfo = async () => {
    let result = await proxy.Request({
        url: api.getShareInfo,
        showLoading: false,
        params: {
            shareId
        }
    })
    if (!result) {
        return;
    }
    shareInfo.value = result.data
}
getShareInfo()

const formData = ref({});
const formDataRef = ref();
const rules = {
    code: [
        { required: true, message: "请输入提取码" },
        { min: 5, max: 5, message: "提取码长度为5位" }
    ]
};

// 检验文件提取码 (原有逻辑完全保留)
const checkShare = () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {}
        Object.assign(params, formData.value)
        let result = await proxy.Request({
            url: api.checkShareCode,
            params: {
                shareId: shareId,
                code: formData.value.code
            }
        })
        if (!result) {
            return;
        }
        proxy.Message.success('文件提取成功')
        router.push(`/share/${shareId}`)
    })
}

const jumpToHome = () => {
    router.push('/')
}

onMounted(() => {
    if (route.query.code) {
        // 将连接中携带的code 自动填入表单并验证
        formData.value.code = route.query.code;
        nextTick(() => {
            checkShare();
        });
    }
});
</script>