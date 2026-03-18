<template>
    <el-popover trigger="click" width="340" placement="bottom-end" :hide-after="0"
        popper-class="!p-4 !rounded-2xl !border-gray-200 dark:!border-[#38383a] !shadow-xl dark:!bg-[#1c1c1e]">

        <!-- 触发按钮：置于 Tooltip 内的纯图标悬浮热区 -->
        <template #reference>
            <div class="inline-block">
                <el-tooltip content="高级筛选" placement="top" :show-after="300" effect="dark">
                    <div class="flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-[#007AFF] hover:bg-black/5 dark:hover:bg-white/10 transition-colors cursor-pointer"
                        :class="isActive ? 'text-[#007AFF] bg-blue-50 dark:bg-blue-900/30' : ''">
                        <!-- 调节滑块图标 -->
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.8"
                            stroke="currentColor" class="w-[18px] h-[18px]">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                d="M10.5 6h9.75M10.5 6a1.5 1.5 0 1 1-3 0m3 0a1.5 1.5 0 1 0-3 0M3.75 6H7.5m3 12h9.75m-9.75 0a1.5 1.5 0 0 1-3 0m3 0a1.5 1.5 0 0 0-3 0m-3.75 0H7.5m9-6h3.75m-3.75 0a1.5 1.5 0 0 1-3 0m3 0a1.5 1.5 0 0 0-3 0m-9.75 0h9.75" />
                        </svg>
                    </div>
                </el-tooltip>
            </div>
        </template>

        <!-- Popover 内部展开的高级面板内容 -->
        <div class="flex flex-col select-none">

            <div class="flex items-center justify-between mb-4 px-1">
                <span class="text-[15px] font-semibold tracking-wide text-[#1d1d1f] dark:text-[#f5f5f7]">高级筛选</span>
            </div>

            <!-- 1. 文件类型 -->
            <div class="mb-5">
                <div class="text-[12px] font-medium text-gray-400 dark:text-gray-500 mb-2 px-1 tracking-wider">文件类型
                </div>
                <div class="flex flex-wrap gap-2.5">
                    <div v-for="item in categories" :key="item.value" @click="updateFilter('category', item.value)"
                        class="px-3.5 py-1.5 rounded-lg text-[13px] cursor-pointer transition-all duration-200"
                        :class="filterState.category === item.value
                            ? 'bg-blue-50 text-[#007AFF] font-bold dark:bg-[#0a84ff]/20 dark:text-[#0a84ff]'
                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-[#2c2c2e] dark:text-gray-300 dark:hover:bg-[#38383a] font-medium'">
                        {{ item.label }}
                    </div>
                </div>
            </div>

            <!-- 2. 上传时间 -->
            <div class="mb-5">
                <div class="text-[12px] font-medium text-gray-400 dark:text-gray-500 mb-2 px-1 tracking-wider">上传时间
                </div>
                <div class="flex flex-wrap gap-2.5">
                    <div v-for="item in times" :key="item.value" @click="updateFilter('time', item.value)"
                        class="px-3.5 py-1.5 rounded-lg text-[13px] cursor-pointer transition-all duration-200"
                        :class="filterState.time === item.value
                            ? 'bg-blue-50 text-[#007AFF] font-bold dark:bg-[#0a84ff]/20 dark:text-[#0a84ff]'
                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-[#2c2c2e] dark:text-gray-300 dark:hover:bg-[#38383a] font-medium'">
                        {{ item.label }}
                    </div>
                </div>
            </div>

            <!-- 3. 文件大小 -->
            <div class="mb-3">
                <div class="text-[12px] font-medium text-gray-400 dark:text-gray-500 mb-2 px-1 tracking-wider">文件大小
                </div>
                <div class="flex flex-wrap gap-2.5">
                    <div v-for="item in sizes" :key="item.value" @click="updateFilter('size', item.value)"
                        class="px-3.5 py-1.5 rounded-lg text-[13px] cursor-pointer transition-all duration-200"
                        :class="filterState.size === item.value
                            ? 'bg-blue-50 text-[#007AFF] font-bold dark:bg-[#0a84ff]/20 dark:text-[#0a84ff]'
                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-[#2c2c2e] dark:text-gray-300 dark:hover:bg-[#38383a] font-medium'">
                        {{ item.label }}
                    </div>
                </div>
            </div>

            <!-- 底部重置操作区 -->
            <div class="mt-4 pt-3 flex justify-end border-t border-gray-100 dark:border-gray-800">
                <span @click="resetFilter"
                    class="text-[13px] font-medium text-gray-500 hover:text-gray-800 dark:hover:text-gray-200 cursor-pointer transition-colors px-2 py-1 relative right-0"
                    :class="{ 'opacity-40 cursor-not-allowed pointer-events-none': !isActive }">
                    重置条件
                </span>
            </div>

        </div>
    </el-popover>
</template>

<script setup>
import { ref, computed } from 'vue';

const emit = defineEmits(['filter-change']);

// 选项数据
const categories = [
    { label: '全部', value: '' },
    { label: '视频', value: 1 },
    { label: '音频', value: 2 },
    { label: '图片', value: 3 },
    { label: '文档', value: 4 },
    { label: '其他', value: 5 }
];

const times = [
    { label: '全部时间', value: '' },
    { label: '今天', value: 'today' },
    { label: '近7天', value: '7days' },
    { label: '近30天', value: '30days' }
];

const sizes = [
    { label: '全部大小', value: '' },
    { label: '<10MB', value: 'lt10' },
    { label: '10MB-100MB', value: '10to100' },
    { label: '>100MB', value: 'gt100' }
];

// 面板响应式状态
const filterState = ref({
    category: '',
    time: '',
    size: ''
});

// 计算是否处于“发力过滤”状态（供高亮外部漏斗图标及底部重置按钮验证使用）
const isActive = computed(() => {
    return filterState.value.category !== '' || filterState.value.time !== '' || filterState.value.size !== '';
});

// 格式化时间辅助函数 (YYYY-MM-DD)
const getFormattedDate = (date) => {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
};

// 触发条件变更
const updateFilter = (key, value) => {
    if (filterState.value[key] === value) return; // 避免重复触发
    filterState.value[key] = value;
    applyFilter();
};

// 重置条件
const resetFilter = () => {
    filterState.value = { category: '', time: '', size: '' };
    applyFilter();
};

// 映射状态至实际查询载荷并抛出
const applyFilter = () => {
    const payload = {
        fileCategory: filterState.value.category || null,
        createTimeStart: null,
        createTimeEnd: null,
        fileSizeMin: null,
        fileSizeMax: null
    };

    // --- 处理日期倒推 ---
    if (filterState.value.time) {
        const today = new Date();
        payload.createTimeEnd = getFormattedDate(today);

        if (filterState.value.time === 'today') {
            payload.createTimeStart = getFormattedDate(today);
        } else if (filterState.value.time === '7days') {
            const start = new Date(today);
            start.setDate(today.getDate() - 7);
            payload.createTimeStart = getFormattedDate(start);
        } else if (filterState.value.time === '30days') {
            const start = new Date(today);
            start.setDate(today.getDate() - 30);
            payload.createTimeStart = getFormattedDate(start);
        }
    }

    // --- 处理大小单位转换 ---
    const MB = 1024 * 1024;
    if (filterState.value.size === 'lt10') {
        payload.fileSizeMax = 10 * MB;
    } else if (filterState.value.size === '10to100') {
        payload.fileSizeMin = 10 * MB;
        payload.fileSizeMax = 100 * MB;
    } else if (filterState.value.size === 'gt100') {
        payload.fileSizeMin = 100 * MB;
    }
    // 送出结构体到父组件
    emit('filter-change', payload);
};
</script>