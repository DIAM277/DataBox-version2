<template>
    <div class="w-full flex flex-col select-none">
        <div class="flex flex-col">
            <!-- 骨架列表行渲染 -->
            <div v-for="i in rowCount" :key="i"
                class="flex items-center px-4 py-3.5 border-b border-[#e5e5e9]/50 dark:border-[#38383a]/40 last:border-none">

                <!-- 第一列：文件图标骨架框 (圆角方块) -->
                <div class="w-[30px] h-[30px] rounded-lg bg-gray-200/80 dark:bg-gray-800 animate-pulse flex-shrink-0">
                </div>

                <!-- 第一列：文件名占位条 -->
                <div
                    class="ml-4 h-[14px] bg-gray-200/80 dark:bg-gray-800 rounded-full animate-pulse flex-1 max-w-[25%] sm:max-w-[35%]">
                </div>

                <!-- 弹性垫片推开剩余列 -->
                <div class="flex-1"></div>

                <!-- 后续列：文件大小、类型、时间占位条 (自适应隐藏) -->
                <div class="hidden md:flex items-center space-x-12 lg:space-x-28 mr-8">
                    <!-- 大小占位 -->
                    <div class="h-3 w-16 bg-gray-100 dark:bg-[#2c2c2e] rounded-full animate-pulse"></div>
                    <!-- 类型占位 -->
                    <div class="h-3 w-16 bg-gray-100 dark:bg-[#2c2c2e] rounded-full animate-pulse hidden lg:block">
                    </div>
                    <!-- 时间占位 -->
                    <div class="h-3 w-32 bg-gray-100 dark:bg-[#2c2c2e] rounded-full animate-pulse"></div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({
    // 骨架行数
    rowCount: {
        type: Number,
        default: 10
    }
})
</script>