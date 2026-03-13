<template>
    <div class="relative group flex items-center">
        <!-- 左侧：搜索放大镜图标 (随聚焦状态泛蓝) -->
        <svg class="absolute left-3 w-4 h-4 text-gray-400 group-focus-within:text-[#007AFF] transition-colors duration-300 pointer-events-none"
            xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>

        <input ref="inputRef" type="text" v-model="keyword" @keyup.enter="handleSearch" placeholder="搜索文件..."
            class="w-64 py-1.5 pl-9 pr-9 text-sm text-gray-700 dark:text-gray-200 bg-gray-100 dark:bg-[#2c2c2e] border border-transparent rounded-full outline-none placeholder-gray-400/80 transition-all duration-300 ease-in-out focus:w-80 focus:bg-white dark:focus:bg-[#1c1c1e] focus:shadow-md focus:border-blue-500/30 focus:ring-2 focus:ring-blue-500/10" />

        <button v-if="keyword" @mousedown.prevent="clearSearch"
            class="absolute right-8 w-4 h-4 flex items-center justify-center text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 cursor-pointer transition-colors duration-200">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-3.5 h-3.5">
                <path fill-rule="evenodd"
                    d="M5.47 5.47a.75.75 0 011.06 0L12 10.94l5.47-5.47a.75.75 0 111.06 1.06L13.06 12l5.47 5.47a.75.75 0 11-1.06 1.06L12 13.06l-5.47 5.47a.75.75 0 01-1.06-1.06L10.94 12 5.47 6.53a.75.75 0 010-1.06z"
                    clip-rule="evenodd" />
            </svg>
        </button>
    </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';

const keyword = ref('');
const inputRef = ref(null);
const emit = defineEmits(['search']);

const route = useRoute();

// 监听路由变化，自动清空搜索框
watch(() => route.path, () => {
    keyword.value = '';
});

// 搜索
const handleSearch = () => {
    emit('search', keyword.value);
};

const clearSearch = () => {
    keyword.value = '';
    emit('search', ''); // 清空后自动触发搜索
    // 显式锁定焦点，避免收缩
    if (inputRef.value) {
        inputRef.value.focus();
    }
};
</script>