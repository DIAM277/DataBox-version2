<template>
    <div
        class="p-6 md:p-8 flex flex-col h-full overflow-y-auto bg-[#f5f5f7] dark:bg-[#000000] font-sans transition-colors duration-500">

        <!-- ==================== 管理员视角：系统设置 ==================== -->
        <template v-if="isAdmin">
            <!-- macOS 风醒目大标题 -->
            <div class="mb-8">
                <h1 class="text-3xl font-bold text-gray-900 dark:text-[#f5f5f7] tracking-tight">系统设置</h1>
                <p class="text-[13px] font-medium text-gray-500 dark:text-gray-400 mt-2">统筹管理你的云盘基础空间与邮件下发策略</p>
            </div>

            <div class="max-w-4xl">
                <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>

                    <!-- Section 1: 基础与用户限制 -->
                    <div
                        class="text-[12px] text-[#86868b] dark:text-gray-500 font-semibold tracking-wider uppercase mb-2 px-2 shadow-none select-none">
                        基础与存储设置
                    </div>
                    <div
                        class="bg-white dark:bg-[#1c1c1e] rounded-2xl shadow-sm border border-gray-200/60 dark:border-gray-800 overflow-hidden mb-8 transition-colors">

                        <div class="flex items-center justify-between p-5 pb-3">
                            <div class="flex flex-col">
                                <span class="text-[15px] text-gray-800 dark:text-gray-200 font-medium">新用户初始空间</span>
                                <span
                                    class="text-[12.5px] text-gray-400 dark:text-gray-500 mt-0.5">新注册用户默认分配的基础配额</span>
                            </div>

                            <div class="flex-shrink-0 w-[160px]">
                                <el-form-item prop="userInitUseSpace" class="mb-0 custom-form-item">
                                    <el-input v-model.trim="formData.userInitUseSpace" placeholder="空间数值"
                                        class="mac-input w-full">
                                        <template #append>
                                            <span
                                                class="text-gray-500 dark:text-gray-400 font-medium bg-transparent border-none">MB</span>
                                        </template>
                                    </el-input>
                                </el-form-item>
                            </div>
                        </div>
                    </div>

                    <!-- Section 2: 邮件服务设置 -->
                    <div
                        class="text-[12px] text-[#86868b] dark:text-gray-500 font-semibold tracking-wider uppercase mb-2 px-2 shadow-none select-none mt-4">
                        验证邮件配置
                    </div>
                    <div
                        class="bg-white dark:bg-[#1c1c1e] rounded-2xl shadow-sm border border-gray-200/60 dark:border-gray-800 overflow-hidden mb-6 transition-colors">

                        <!-- 注册邮件标题 -->
                        <div
                            class="flex flex-col sm:flex-row sm:items-center justify-between p-5 border-b border-gray-100 dark:border-gray-800/80">
                            <div class="flex flex-col mb-3 sm:mb-0 w-1/3">
                                <span class="text-[15px] text-gray-800 dark:text-gray-200 font-medium">注册邮件标题</span>
                                <span
                                    class="text-[12.5px] text-gray-400 dark:text-gray-500 mt-0.5">用户触发验证码时收到的邮件大纲</span>
                            </div>
                            <div class="flex-1 max-w-[500px]">
                                <el-form-item prop="registerEmailTitle" class="mb-0 custom-form-item">
                                    <el-input type="textarea" :rows="2" v-model.trim="formData.registerEmailTitle"
                                        class="mac-textarea" placeholder="例：DataBox 账号激活验证码" :maxlength="150"
                                        show-word-limit />
                                </el-form-item>
                            </div>
                        </div>

                        <!-- 注册邮件内容 -->
                        <div class="flex flex-col sm:flex-row sm:items-start justify-between p-5">
                            <div class="flex flex-col mb-3 sm:mb-0 w-1/3 pt-1">
                                <span class="text-[15px] text-gray-800 dark:text-gray-200 font-medium">注册邮件正文</span>
                                <span class="text-[12.5px] text-gray-400 dark:text-gray-500 mt-0.5 w-[85%]">
                                    需使用 <span
                                        class="font-mono text-[#007AFF] bg-blue-50 dark:bg-blue-900/40 px-1 rounded">%s</span>
                                    占位符。
                                </span>
                            </div>
                            <div class="flex-1 max-w-[500px]">
                                <el-form-item prop="registerEmailContent" class="mb-0 custom-form-item">
                                    <el-input type="textarea" :rows="4" v-model.trim="formData.registerEmailContent"
                                        class="mac-textarea" placeholder="例：您的验证码为: %s ，15分钟内有效。" :maxlength="150"
                                        show-word-limit />
                                </el-form-item>
                            </div>
                        </div>

                    </div>

                    <!-- 底部交互动作区 -->
                    <div class="flex justify-end items-center gap-3 mt-8 pb-4 select-none">
                        <button type="button" @click="getSysSettings"
                            class="px-5 py-2.5 rounded-xl text-[14px] font-semibold text-gray-600 bg-gray-200/50 hover:bg-gray-200 dark:text-gray-300 dark:bg-[#2c2c2e] dark:hover:bg-[#38383a] transition-all active:scale-95 flex items-center justify-center gap-1.5 focus:outline-none">
                            <span class="iconfont icon-refresh text-[12px]"></span>重新加载
                        </button>

                        <button type="button" @click="saveSettings"
                            class="bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-xl px-7 py-2.5 text-[14.5px] font-bold transition-all active:scale-[0.98] shadow-md shadow-blue-500/20 flex items-center justify-center gap-1.5 focus:outline-none">
                            <span class="iconfont icon-save text-[15px]"></span>保存更改
                        </button>
                    </div>

                </el-form>
            </div>
        </template>

        <!-- ==================== 普通用户视角：个人偏好设置 ==================== -->
        <template v-else>
            <!-- macOS 风醒目大标题 -->
            <div class="mb-8">
                <h1 class="text-3xl font-bold text-gray-900 dark:text-[#f5f5f7] tracking-tight">个人偏好设置</h1>
                <p class="text-[13px] font-medium text-gray-500 dark:text-gray-400 mt-2">定制你的专属云盘外观与系统体验</p>
            </div>

            <div class="max-w-4xl">
                <!-- Section 1: 外观与交互 -->
                <div
                    class="text-[12px] text-[#86868b] dark:text-gray-500 font-semibold tracking-wider uppercase mb-2 px-2 shadow-none select-none">
                    界面与外观
                </div>
                <div
                    class="bg-white dark:bg-[#1c1c1e] rounded-2xl shadow-sm border border-gray-200/60 dark:border-gray-800 overflow-hidden mb-6 transition-colors">

                    <div class="flex items-center justify-between p-5">
                        <div class="flex flex-col">
                            <span class="text-[15px] text-gray-800 dark:text-gray-200 font-medium">深色模式</span>
                            <span
                                class="text-[12.5px] text-gray-400 dark:text-gray-500 mt-0.5">在暗光环境下提供更舒适的视觉体验，同时节省屏幕功耗</span>
                        </div>
                        <div class="flex-shrink-0">
                            <!-- 使用原生 Apple 绿配色开关 -->
                            <el-switch v-model="isDark" @change="handleThemeChange"
                                style="--el-switch-on-color: #34C759;"></el-switch>
                        </div>
                    </div>

                </div>
            </div>
        </template>

    </div>
</template>

<script setup>
// 🔴 引入 onUnmounted
import { ref, reactive, getCurrentInstance, onMounted, onUnmounted, computed } from "vue"
import { useUserStore } from '@/store/userStore';

const { proxy } = getCurrentInstance();
const userStore = useUserStore();

// 兼容获取管理员权限（以防后台定义属性名为 admin 或 isAdmin）
const isAdmin = computed(() => userStore.userInfo?.admin || userStore.userInfo?.isAdmin);

const api = {
    getSysSettings: '/admin/getSysSetting',
    saveSettings: '/admin/saveSysSetting'
}

// ========================
// 管理员业务逻辑
// ========================
const formData = ref({});
const formDataRef = ref();
const rules = {
    registerEmailTitle: [
        { required: true, message: "请输入邮箱验证码邮件标题" }
    ],
    registerEmailContent: [
        { required: true, message: "请输入邮箱验证码邮件内容" }
    ],
    userInitUseSpace: [
        { required: true, message: "请输入新用户初始空间大小" },
        { validator: proxy.Verify.number, message: "请输入数字" }
    ]
};

const getSysSettings = async () => {
    let result = await proxy.Request({
        url: api.getSysSettings
    })
    if (!result) {
        return;
    }
    formData.value = result.data;
}

const saveSettings = async () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {}
        Object.assign(params, formData.value);
        let result = await proxy.Request({
            url: api.saveSettings,
            params: params
        })
        if (!result) {
            return;
        }
        proxy.Message.success('保存成功');
        getSysSettings();
    })
}


// ========================
// 普通用户偏好设置逻辑
// ========================
const isDark = ref(false);

const handleThemeChange = (val) => {
    if (val) {
        document.documentElement.classList.add('dark');
        localStorage.setItem('theme', 'dark');
    } else {
        document.documentElement.classList.remove('dark');
        localStorage.setItem('theme', 'light');
    }
}

// 🔴 新增：用于防脱节的属性监听器
let observer = null;

// ========================
// 生命周期初始化
// ========================
onMounted(() => {
    // 1. 初始化普通用户的深色模式读取状态
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        isDark.value = true;
    } else {
        // 双重保险：兜底检查根节点是否已经被顶部栏点亮了 dark
        isDark.value = document.documentElement.classList.contains('dark');
    }

    // 🔴 新增：利用 MutationObserver 监听 html class 变化，和 TopHeader 保持同步
    observer = new MutationObserver(() => {
        isDark.value = document.documentElement.classList.contains('dark');
    });
    observer.observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['class'],
    });

    // 2. 只有当前身份是管理员时，才发起向后端拉取配置的请求，防止普通用户触发 403 / 越权报错
    if (isAdmin.value) {
        getSysSettings();
    }
});

// 🔴 新增：卸载组件时清理监听，防止内存泄漏
onUnmounted(() => {
    if (observer) {
        observer.disconnect();
    }
});

</script>

<style lang="scss" scoped>
/* 重置 el-form-item 自身的行高溢出 */
:deep(.custom-form-item .el-form-item__content) {
    line-height: normal;
}

/* 错误提示文字漂浮定位 */
:deep(.el-form-item__error) {
    padding-top: 5px;
    position: relative;
    font-size: 12px;
    color: #ff3b30;
    font-weight: 500;
}

/* ----- Apple Mac 数值单行输入框 ----- */
:deep(.mac-input .el-input__wrapper) {
    box-shadow: none !important;
    background-color: rgba(233, 227, 227, 0.04) !important;
    border-radius: 10px;
    padding: 0 12px;
    transition: all 0.25s ease;
}

html.dark :deep(.mac-input .el-input__wrapper) {
    background-color: rgb(255, 255, 255) !important;
}

:deep(.mac-input .el-input__wrapper.is-focus),
:deep(.mac-input .el-input__wrapper:hover) {
    background-color: #fff !important;
    box-shadow: 0 0 0 2px rgba(0, 122, 255, 0.3) !important;
}

html.dark :deep(.mac-input .el-input__wrapper.is-focus),
html.dark :deep(.mac-input .el-input__wrapper:hover) {
    background-color: #1c1c1e !important;
    box-shadow: 0 0 0 2px rgba(10, 132, 255, 0.5) !important;
}

:deep(.mac-input .el-input__inner) {
    height: 38px;
    text-align: right;
    color: #1d1d1f;
    font-weight: 600;
    font-family: ui-rounded, 'SF Pro Rounded', -apple-system, sans-serif;
}

html.dark :deep(.mac-input .el-input__inner) {
    color: rgb(97, 97, 100)
}

/* 附加单位字样插槽 */
:deep(.mac-input .el-input-group__append) {
    background-color: transparent !important;
    box-shadow: none !important;
    border: none !important;
    padding: 0 0 0 6px;
    color: #86868b;
}

/* ----- Apple iOS / Mac 大文本框 ----- */
:deep(.mac-textarea .el-textarea__inner) {
    box-shadow: none !important;
    background-color: rgba(0, 0, 0, 0.02) !important;
    border: 1px solid rgba(0, 0, 0, 0.05) !important;
    border-radius: 12px;
    padding: 12px 14px;
    color: #1d1d1f;
    font-size: 14.5px;
    font-weight: 500;
    line-height: 1.6;
    transition: all 0.25s ease;
    resize: none;
}

html.dark :deep(.mac-textarea .el-textarea__inner) {
    background-color: rgba(255, 255, 255, 0.03) !important;
    border-color: rgba(255, 255, 255, 0.05) !important;
    color: #f5f5f7;
}

:deep(.mac-textarea .el-textarea__inner:focus),
:deep(.mac-textarea .el-textarea__inner:hover) {
    background-color: #fff !important;
    border-color: transparent !important;
    box-shadow: 0 0 0 2px rgba(0, 122, 255, 0.3) !important;
}

html.dark :deep(.mac-textarea .el-textarea__inner:focus),
html.dark :deep(.mac-textarea .el-textarea__inner:hover) {
    background-color: #1c1c1e !important;
    border-color: transparent !important;
    box-shadow: 0 0 0 2px rgba(10, 132, 255, 0.5) !important;
}

/* 字数统计沉浸式浮底 */
:deep(.mac-textarea .el-input__count) {
    background: transparent;
    bottom: 8px;
    right: 12px;
    font-size: 11px;
    color: #86868b;
    font-weight: 500;
}
</style>