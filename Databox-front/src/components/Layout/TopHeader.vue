<template>
    <!-- 采用三列网格布局，确保左侧彻底靠左，右侧彻底靠右 -->
    <header class="w-full grid grid-cols-3 items-center px-6">

        <!-- 1. 左侧：极简苹果风 Logo (确保靠最左) -->
        <div class="flex items-center justify-start">
            <div class="flex items-center gap-2.5 cursor-pointer group transition-opacity hover:opacity-80"
                @click="router.push('/')">
                <!-- 高级感内联 SVG 云盘图标 (适配强调色) -->
                <svg class="w-8 h-8 text-[#007AFF] drop-shadow-sm transition-transform group-hover:scale-105"
                    viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                    <path
                        d="M17.5 19H6.5C3.46243 19 1 16.5376 1 13.5C1 10.6693 3.14081 8.34327 5.90802 8.03333C6.44299 4.60655 9.42168 2 12 2C15.1118 2 17.7554 4.08182 18.6655 6.94276C21.0963 7.29177 23 9.40059 23 12C23 15.866 19.866 19 17.5 19Z" />
                </svg>
                <span class="font-semibold text-xl text-[#1d1d1f] dark:text-[#f5f5f7] tracking-wider">
                    DataBox
                </span>
            </div>
        </div>

        <!-- 2. 中间：预留给页面标题或未来重加搜索框 (目前留空占据网格中心位置) -->
        <div class="flex items-center justify-center">
            <Search @search="handleSearch" />
        </div>

        <!-- 3. 右侧：功能区 (调色盘 + 传输任务 + 头像) -->
        <div class="flex items-center justify-end space-x-4">
            <div @click="toggleTheme"
                class="w-[36px] h-[36px] flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full cursor-pointer transition-colors group"
                title="切换外观">
                <!-- 太阳图标 (浅色模式时显示) -->
                <svg v-if="!isDark" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"
                    stroke-width="1.8" stroke="currentColor"
                    class="w-5 h-5 text-[#86868b] group-hover:text-[#007AFF] transition-colors">
                    <path stroke-linecap="round" stroke-linejoin="round"
                        d="M12 3v2.25m6.364.386l-1.591 1.591M21 12h-2.25m-.386 6.364l-1.591-1.591M12 21v-2.25m-6.364-.386l1.591-1.591M3 12h2.25m.386-6.364l1.591 1.591M12 18.75a6.75 6.75 0 110-13.5 6.75 6.75 0 010 13.5z" />
                </svg>
                <!-- 月亮图标 (深色模式时显示) -->
                <svg v-else xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.8"
                    stroke="currentColor" class="w-5 h-5 text-[#86868b] group-hover:text-[#007AFF] transition-colors">
                    <path stroke-linecap="round" stroke-linejoin="round"
                        d="M21.752 15.002A9.718 9.718 0 0118 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 003 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 009.002-5.998z" />
                </svg>
            </div>

            <!-- 全局上传任务指示器模块 -->
            <div class="relative z-50 flex items-center">
                <el-popover placement="bottom-end" :width="440" trigger="click" v-model:visible="showUploader"
                    :hide-after="0" :show-arrow="false" popper-class="custom-uploader-popover">
                    <template #reference>
                        <!-- 【修复正圆问题】：强制宽高度和 flex 剧中对齐 -->
                        <div class="w-[36px] h-[36px] flex items-center justify-center hover:bg-black/5 dark:hover:bg-white/10 rounded-full cursor-pointer transition-colors"
                            title="传输列表">
                            <span
                                class="iconfont icon-transfer text-[20px] text-[#86868b] hover:text-[#007AFF] transition-colors"></span>
                        </div>
                    </template>
                    <Uploader ref="uploaderRef" />
                </el-popover>
            </div>

            <!-- 【新增】：系统消息通知铃铛 -->
            <MessageBell />

            <!-- 用户信息与悬浮快捷菜单 -->
            <el-dropdown trigger="hover" class="cursor-pointer" @command="handleCommand">
                <div
                    class="flex items-center space-x-3 border-l pl-4 border-gray-200 dark:border-[#38383a] group focus:outline-none">
                    <div class="rounded-full shadow-sm hover:shadow transition-all duration-300 hover:scale-105">
                        <Avatar :userId="userStore.userInfo?.userId" :avatar="userStore.userInfo?.avatar" :width="36"
                            :interactive="false" class="border border-gray-100 dark:border-gray-700 rounded-full" />
                    </div>
                    <div class="flex flex-col">
                        <span
                            class="text-sm font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] group-hover:text-[#007AFF] transition-colors">
                            {{ userStore.userInfo?.userName || 'User' }}
                        </span>
                    </div>
                </div>

                <!-- 头像下拉菜单 -->
                <template #dropdown>
                    <el-dropdown-menu
                        class="dark:border-[#38383a] dark:bg-[#1c1c1e] min-w-[140px] rounded-xl shadow-lg">
                        <el-dropdown-item command="updateAvatar" class="custom-dropdown-item">
                            <span class="iconfont icon-account mr-2 text-[15px]"></span>修改资料
                        </el-dropdown-item>
                        <el-dropdown-item command="updatePassword" class="custom-dropdown-item">
                            <span class="iconfont icon-password mr-2 text-[15px]"></span>修改密码
                        </el-dropdown-item>
                        <el-dropdown-item divided command="logout"
                            class="text-red-500 hover:!text-red-600 hover:!bg-red-50 dark:hover:!bg-red-900/20 custom-dropdown-item">
                            <span class="iconfont icon-close mr-2 text-[15px]"></span>退出登录
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>

        </div>
    </header>
</template>

<script setup>
import { ref, inject, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/userStore';
import Uploader from '@/views/main/Uploader.vue';
import Search from '@/components/Input/Search.vue';
import MessageBell from '@/components/Business/MessageBell.vue';

const emit = defineEmits(['search']);

const handleSearch = (keyword) => {
    emit('search', keyword);
};

const router = useRouter();
const userStore = useUserStore();
const globalActions = inject('globalActions');

// --- 上传任务模块逻辑 ---
const uploaderRef = ref();
const showUploader = ref(false);

const triggerAddFile = (data) => {
    showUploader.value = true;
    if (uploaderRef.value && uploaderRef.value.addFile) {
        uploaderRef.value.addFile(data.file, data.filePid);
    }
};

// --- 头像下拉菜单指令派发 ---
const handleCommand = (command) => {
    if (globalActions && typeof globalActions[command] === 'function') {
        globalActions[command]();
    }
};

const isDark = ref(false);

const toggleTheme = () => {
    isDark.value = !isDark.value;
    if (isDark.value) {
        document.documentElement.classList.add('dark');
        localStorage.setItem('theme', 'dark');
    } else {
        document.documentElement.classList.remove('dark');
        localStorage.setItem('theme', 'light');
    }
};

// 🔴 修改：增强初始加载并添加全局监听
let observer = null;
onMounted(() => {
    // 初始状态读取
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        isDark.value = true;
        document.documentElement.classList.add('dark');
    } else {
        isDark.value = document.documentElement.classList.contains('dark');
    }

    // 利用 MutationObserver 监听 html 元素的 class 变化
    // 使其能同步响应 SysSettings.vue 中开关的切换
    observer = new MutationObserver(() => {
        isDark.value = document.documentElement.classList.contains('dark');
    });
    observer.observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['class'],
    });
});

onUnmounted(() => {
    if (observer) {
        observer.disconnect();
    }
});

defineExpose({ triggerAddFile });
</script>

<style scoped>
:deep(.custom-dropdown-item) {
    padding: 12px 20px;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.2s ease;
    border-radius: 6px;
    margin: 2px 6px;
    display: flex;
    align-items: center;
}
</style>

<style>
/* 覆盖全局 el-popper 惹出的双层卡片冲突 */
.custom-uploader-popover.el-popper {
    padding: 0 !important;
    background: transparent !important;
    border: none !important;
    box-shadow: none !important;
    backdrop-filter: none !important;
    -webkit-backdrop-filter: none !important;
}
</style>