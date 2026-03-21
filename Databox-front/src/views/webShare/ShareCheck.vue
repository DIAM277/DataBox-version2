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

        <!-- 1. 核心毛玻璃验证卡片 (有效分享呈现) -->
        <div v-if="shareValid"
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
                        <input v-model.trim="formData.code" @keyup.enter="checkShare" type="text" maxlength="5"
                            placeholder="•••••"
                            class="extraction-input w-full h-[56px] border-2 border-transparent focus:border-[#007AFF]/40 rounded-2xl text-center text-[22px] tracking-[0.5em] font-bold outline-none transition-all duration-300 focus:shadow-sm placeholder:tracking-normal placeholder:font-normal placeholder:text-lg" />
                    </el-form-item>

                    <button type="button" @click="checkShare" :disabled="submitLoading"
                        class="w-full h-[52px] bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-xl text-[16px] font-semibold mt-6 transition-all duration-200 active:scale-[0.98] shadow-md shadow-blue-500/20 flex items-center justify-center tracking-wide disabled:opacity-70 disabled:cursor-not-allowed">
                        <span v-if="submitLoading"
                            class="w-5 h-5 border-2 border-white/80 border-t-transparent rounded-full animate-spin mr-2"></span>
                        {{ submitLoading ? '验证中...' : '提取文件' }}
                    </button>
                </el-form>
            </div>

            <!-- 页脚 -->
            <div
                class="mt-8 text-center text-[12px] text-[#86868b] dark:text-gray-500 font-medium tracking-wide opacity-80">
                DataBox - 安全、高效的文件分享平台
            </div>
        </div>

        <!-- 2. 失效拦截占位图 (无效、过期、删除分享展现) -->
        <div v-else
            class="flex flex-col items-center justify-center relative z-10 p-6 text-center animate-[fadeIn_0.5s_ease-out]">
            <img src="@/assets/icon-image/no_data.png" class="w-36 h-36 opacity-60 mb-8 drop-shadow-md" />
            <h2 class="text-2xl md:text-3xl font-bold text-gray-800 dark:text-gray-200 tracking-wide mb-3">分享链接已失效</h2>
            <p class="text-gray-500 dark:text-gray-400 text-sm md:text-base mb-10 max-w-sm">
                该文件可能已被原作者取消分享、被系统安全删除，或者已超过有效期限。</p>
            <div class="bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-full px-10 py-3 font-semibold transition-all active:scale-95 cursor-pointer shadow-lg shadow-blue-500/30"
                @click="jumpToHome">
                返回网盘首页
            </div>
        </div>

    </div>
</template>

<script setup>
import { ref, getCurrentInstance, onMounted } from "vue"
import { useRoute, useRouter } from 'vue-router';

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const api = {
    getShareInfo: '/showShare/getShareInfo',
    checkShareCode: '/showShare/checkShareCode'
}

const shareId = route.params.shareId
const shareInfo = ref({})

// --- [修复点1]：新增底层防御相关状态 --- 
const shareValid = ref(true) // 标识分享是否依然存活，默认允许先尝试拉取
const submitLoading = ref(false) // 验证框转圈控制

// 重构: 将初始化数据和解析提取码逻辑合并至完整的闭环函数中
const initShareData = async () => {
    try {
        // 【第一道防线】 侦测源文件与分享条目的物理健康状态
        let result = await proxy.Request({
            url: api.getShareInfo,
            showLoading: false,
            // 关闭报错弹出的通知遮罩，转由我们优雅拦截处理
            errorCallback: () => {
                shareValid.value = false;
            },
            params: {
                shareId
            }
        });

        // 斩断拦截: 后台拦截/文件删除 会导致返回 result 为空
        if (!result) {
            shareValid.value = false;
            return;
        }

        // 文件与分享链接均健康，绑定基本信息展示卡片
        shareValid.value = true;
        shareInfo.value = result.data;

        // 【第二道防线】 只在有效的前提下展开验证接管（解决：文件没得提取系统还强制自动补全和打错误验证的 Bug）
        checkAutoFillCode();

    } catch (error) {
        shareValid.value = false;
    }
}

// 自动填充机制安全判定封装
const checkAutoFillCode = () => {
    const autoCode = route.query.code || sessionStorage.getItem('auto_fill_code_' + shareId);

    if (autoCode) {
        // 给足 UI 挂载和 Element Plus 的缓冲周期
        setTimeout(() => {
            formData.value.code = autoCode;
            // 阅后即焚，不为日后的同ID其他行为埋雷
            sessionStorage.removeItem('auto_fill_code_' + shareId);

            // 自动发车执行校验
            checkShare();
        }, 100);
    }
}

const formData = ref({ code: '' });
const formDataRef = ref();
const rules = {
    code: [
        { required: true, message: "请输入提取码" },
        { min: 5, max: 5, message: "提取码长度不匹配" }
    ]
};

// 提取验证操作核心重制
const checkShare = () => {
    // 阻击拦截: 请求未完成前绝对不让玩家疯狂点击
    if (submitLoading.value) return;

    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }

        // 开启锁圈
        submitLoading.value = true;

        try {
            let result = await proxy.Request({
                url: api.checkShareCode,
                params: {
                    shareId: shareId,
                    code: formData.value.code
                }
            })

            if (!result) {
                // 如果提取码输入发生错误 (系统抛错或者 403) , result 将会返回 false / nothing。我们直接拦截但不跳失效界面。
                return;
            }

            // 下发令牌放行通关
            proxy.Message.success('文件提取成功');
            router.push(`/share/${shareId}`);
        } finally {
            // [修复点2]：无论成功、失效或接口崩坏，重置按钮状态回归正常！告别无尽旋转！
            submitLoading.value = false;
        }
    })
}

const jumpToHome = () => {
    router.push('/')
}

onMounted(() => {
    initShareData();
});
</script>

<style scoped>
/* 强制保障原生提取码输入框在暗黑模式下的文字可视度与背景高反差对比，防止被 ElementPlus 断层样式污染 */
.extraction-input {
    color: #1d1d1f !important;
    background-color: rgba(243, 244, 246, 0.8) !important;
    /* 接近灰100 */
}

.extraction-input:focus {
    background-color: #ffffff !important;
}

.extraction-input::placeholder {
    color: rgba(0, 0, 0, 0.4) !important;
}

/* 暗色模式强制接管 */
html.dark .extraction-input {
    color: #ffffff !important;
    background-color: #2c2c2e !important;
}

html.dark .extraction-input:focus {
    background-color: #1c1c1e !important;
}

html.dark .extraction-input::placeholder {
    color: rgba(255, 255, 255, 0.3) !important;
}
</style>