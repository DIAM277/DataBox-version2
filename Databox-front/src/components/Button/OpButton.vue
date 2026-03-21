<template>
    <div v-if="showOp && fileData.fileId && fileData.status == 2 && !fileData.showEdit"
        class="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-1 sm:gap-1.5 z-10">

        <!-- 新增：收藏/取消收藏按钮 (抛弃文字字符，改用纯净 SVG) -->
        <el-tooltip content="收藏/取消" placement="top" effect="dark" :show-after="300">
            <span
                class="w-7 h-7 flex items-center justify-center rounded-md text-[17px] text-orange-400 hover:bg-white dark:hover:bg-gray-700 hover:text-orange-500 hover:shadow-sm transition-all cursor-pointer hidden md:flex"
                @click.stop="handleFavorite">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2"
                    stroke="currentColor" class="w-[18px] h-[18px]">
                    <path stroke-linecap="round" stroke-linejoin="round"
                        d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
                </svg>
            </span>
        </el-tooltip>

        <!-- ================= 桌面端：纯图标柔和悬浮按钮与 Tooltip ================= -->
        <el-tooltip content="分享" placement="top" effect="dark" :show-after="300">
            <span
                class="iconfont icon-share1 w-7 h-7 items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-[#007AFF] hover:shadow-sm transition-all cursor-pointer hidden md:flex"
                @click="handleShare"></span>
        </el-tooltip>

        <el-tooltip content="下载" placement="top" effect="dark" :show-after="300">
            <span
                class="iconfont icon-download w-7 h-7 items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-[#007AFF] hover:shadow-sm transition-all cursor-pointer hidden md:flex"
                @click.stop="handleDownload"></span>
        </el-tooltip>

        <el-tooltip content="删除" placement="top" effect="dark" :show-after="300">
            <span
                class="iconfont icon-del w-7 h-7 items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-red-500 hover:shadow-sm transition-all cursor-pointer hidden md:flex"
                @click.stop="handleDelete"></span>
        </el-tooltip>

        <el-tooltip content="重命名" placement="top" effect="dark" :show-after="300">
            <span
                class="iconfont icon-edit w-7 h-7 items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-[#007AFF] hover:shadow-sm transition-all cursor-pointer hidden md:flex"
                @click.stop="handleRename"></span>
        </el-tooltip>

        <el-tooltip content="移动" placement="top" effect="dark" :show-after="300">
            <span
                class="iconfont icon-move w-7 h-7 items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-[#007AFF] hover:shadow-sm transition-all cursor-pointer hidden md:flex"
                @click.stop="handleMove"></span>
        </el-tooltip>

        <!-- ================= 移动端/小屏：折叠为点阵下拉式菜单 ================= -->
        <!-- <el-dropdown trigger="click" class="flex md:hidden relative">
            <span
                class="w-7 h-7 flex items-center justify-center rounded-md text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:shadow-sm cursor-pointer transition-all outline-none">
                <span class="font-bold tracking-widest leading-none mb-[2px]">...</span>
            </span>
            <template #dropdown>
                <el-dropdown-menu class="dark:border-[#38383a] dark:bg-[#1c1c1e] rounded-xl shadow-xl min-w-[120px]">
                    <el-dropdown-item @click="handleShare">
                        <span class="iconfont icon-share1 mr-2 text-[14px]"></span>分享
                    </el-dropdown-item> -->

                    <!-- 下拉菜单中的收藏 (去除 iconfont 依赖，改为 SVG) -->
                    <!-- <el-dropdown-item @click.stop="handleFavorite">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2"
                            stroke="currentColor" class="w-[15px] h-[15px] mr-2 text-orange-500">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
                        </svg>
                        收藏/取消
                    </el-dropdown-item>

                    <el-dropdown-item @click.stop="handleDownload">
                        <span class="iconfont icon-download mr-2 text-[14px]"></span>下载
                    </el-dropdown-item>
                    <el-dropdown-item @click.stop="handleDelete" class="group">
                        <span class="iconfont icon-del mr-2 text-[14px] text-red-500 group-hover:text-red-500"></span>
                        <span class="text-red-500">删除</span>
                    </el-dropdown-item>
                    <el-dropdown-item @click.stop="handleRename">
                        <span class="iconfont icon-edit mr-2 text-[14px]"></span>重命名
                    </el-dropdown-item>
                    <el-dropdown-item @click.stop="handleMove">
                        <span class="iconfont icon-move mr-2 text-[14px]"></span>移动
                    </el-dropdown-item>
                </el-dropdown-menu>
            </template>
        </el-dropdown> -->

    </div>
</template>

<script setup>

const props = defineProps({
    fileData: {
        type: Object,
        required: true
    },
    index: {
        type: Number,
        required: true
    },
    showOp: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits([
    'share',
    'download',
    'delete',
    'rename',
    'move',
    'favorite'
]);

const handleFavorite = () => {
    emit('favorite', props.fileData);
};

const handleShare = () => {
    emit('share', props.fileData);
};

const handleDownload = (event) => {
    emit('download', props.fileData);
};

const handleDelete = (event) => {
    emit('delete', props.index);
};

const handleRename = (event) => {
    emit('rename', props.index);
};

const handleMove = (event) => {
    emit('move', props.fileData);
};
</script>