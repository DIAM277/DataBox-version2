<template>
    <div class="flex flex-col h-full pt-4 pb-1">

        <!-- 顶栏：标题与局部操作区 -->
        <div class="flex justify-between items-center px-6 mb-5">

            <!-- 左：标题与说明 -->
            <div class="flex flex-col">
                <h1 class="text-[22px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-tight leading-snug">🌟
                    我的收藏
                </h1>
                <span class="text-[13px] text-[#86868b] dark:text-[#98989d] mt-0.5 font-medium">快速访问和管理你标记的重要文件</span>
            </div>

            <!-- 右：操作按钮 -->
            <div class="flex gap-3 items-center shrink-0 select-none">

                <!-- 新增：搜索域 -->
                <el-input clearable placeholder="搜索收藏文件" v-model.trim="fileNameFuzzy" @keyup.enter="loadDataList(false)"
                    class="mac-input w-[180px]" />

                <!-- 新增：查询按钮 -->
                <button type="button" @click="loadDataList(false)"
                    class="bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-xl px-3 py-1.5 text-[13.5px] font-semibold transition-all shadow-sm flex items-center justify-center gap-1.5 focus:outline-none active:scale-95 mr-1">
                    <span class="iconfont icon-search text-[14px]"></span>查询
                </button>

                <!-- 【警示橘】：批量取消收藏 -->
                <div @click="selectFileList.length > 0 ? cancelBatch() : null"
                    class="flex items-center gap-1.5 px-3 py-2 text-[13px] font-medium border rounded-xl transition-all shadow-sm"
                    :class="selectFileList.length === 0 ? 'opacity-40 cursor-not-allowed border-gray-200 dark:border-[#38383a] text-gray-400 bg-gray-50 dark:bg-black/20' : 'cursor-pointer text-orange-600 border-orange-200 bg-orange-50 hover:bg-orange-100 dark:border-orange-900/50 dark:bg-orange-900/20 dark:hover:bg-orange-900/40'">
                    <span class="text-[14px] leading-none mb-[2px]">☆</span>取消收藏
                </div>

                <!-- 极简刷新按钮 -->
                <div @click="loadDataList(false)"
                    class="flex items-center justify-center w-[34px] h-[34px] text-gray-500 hover:text-[#007AFF] bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] shadow-sm rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer">
                    <span class="iconfont icon-refresh text-[14px]"></span>
                </div>

            </div>
        </div>

        <!-- 主体：底层 Table 容器 -->
        <div class="flex-1 overflow-hidden relative flex flex-col">

            <!-- 有数据：原生表格 -->
            <div class="flex-1 overflow-hidden" v-if="tableData.list && tableData.list.length > 0">
                <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList"
                    :initFetch="false" :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
                    :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">

                    <!-- 文件名称插槽 (带行内隐形式悬浮操作) -->
                    <template #fileName="{ index, row }">
                        <div class="flex items-center relative group w-full" @mouseenter="showOp(row)"
                            @mouseleave="cancelShowOp(row)">

                            <template v-if="(row.fileType == 3 || row.fileType == 1) && row.status == 2">
                                <Icon :cover="row.fileCover" :width="32"></Icon>
                            </template>
                            <template v-else>
                                <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
                                <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
                            </template>

                            <div class="ml-3 flex-1 min-w-0 flex items-center sm:group-hover:pr-[120px] transition-all duration-300"
                                :title="row.fileName">
                                <span
                                    class="text-[#1d1d1f] dark:text-[#f5f5f7] font-medium cursor-pointer hover:text-[#007AFF] transition-colors truncate"
                                    @click.stop="preview(row)">
                                    {{ row.fileName }}
                                </span>
                                <!-- 转码状态标识 -->
                                <span v-if="row.status == 0"
                                    class="text-xs text-blue-500 ml-2 bg-blue-50 dark:bg-blue-900/20 px-1.5 py-0.5 rounded flex-shrink-0 whitespace-nowrap">转码中</span>
                                <span v-if="row.status == 1"
                                    class="text-xs text-red-500 ml-2 bg-red-50 dark:bg-red-900/20 px-1.5 py-0.5 rounded flex-shrink-0 whitespace-nowrap">转码失败</span>
                            </div>

                            <!-- 行内悬浮操作区 -->
                            <div v-if="row.showOp"
                                class="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-1.5 z-10 opacity-0 group-hover:opacity-100 transition-opacity duration-200">

                                <el-tooltip content="取消收藏" placement="top" effect="dark" :show-after="300">
                                    <span
                                        class="w-7 h-7 flex items-center justify-center rounded-md transition-all cursor-pointer"
                                        :class="row.isToggling
                                            ? 'opacity-40 cursor-not-allowed text-gray-400'
                                            : 'hover:bg-white dark:hover:bg-gray-700 hover:shadow-sm text-yellow-500 dark:text-yellow-400 hover:!text-red-500'"
                                        @click.stop="cancelFavoriteSingle(row)">

                                        <!-- 转圈 Loading (操作中) -->
                                        <svg v-if="row.isToggling" class="animate-spin w-[18px] h-[18px]"
                                            xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor"
                                                stroke-width="3"></circle>
                                            <path class="opacity-75" fill="currentColor"
                                                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z">
                                            </path>
                                        </svg>

                                        <!-- 实心星星 SVG (空闲态) -->
                                        <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"
                                            fill="currentColor"
                                            class="w-[18px] h-[18px] transition-transform active:scale-90">
                                            <path fill-rule="evenodd"
                                                d="M10.788 3.21c.448-1.077 1.976-1.077 2.424 0l2.082 5.006 5.404.434c1.164.093 1.636 1.545.749 2.305l-4.117 3.527 1.257 5.273c.271 1.136-.964 2.033-1.96 1.425L12 18.354 7.373 21.18c-.996.608-2.231-.29-1.96-1.425l1.257-5.273-4.117-3.527c-.887-.76-.415-2.212.749-2.305l5.404-.434 2.082-5.005Z"
                                                clip-rule="evenodd" />
                                        </svg>
                                    </span>
                                </el-tooltip>
                            </div>
                        </div>
                    </template>

                    <template #fileSize="{ row }">
                        <span class="font-medium text-[#86868b] dark:text-[#a1a1a6] text-[13px]" v-if="row.fileSize">{{
                            proxy.Utils.size2Str(row.fileSize) }}</span>
                        <span v-else class="text-gray-300 dark:text-[#424245] text-lg">-</span>
                    </template>

                    <!-- 新增：渲染文件类型栏目 -->
                    <template #fileType="{ row }">
                        <span v-if="row.folderType === 1"
                            class="text-[#86868b] dark:text-[#a1a1a6] text-[13px]">文件夹</span>
                        <span v-else class="text-[#86868b] dark:text-[#a1a1a6] text-[13px]">{{
                            getFileExtension(row.fileName) }}文件</span>
                    </template>

                </Table>
            </div>

            <!-- 加载中 -->
            <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="px-6 py-4" />

            <!-- 无数据缺省图 -->
            <div v-else class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16">
                <img src="@/assets/icon-image/no_data.png"
                    class="w-36 h-36 opacity-70 mb-5 grayscale drop-shadow-sm transition-all hover:scale-105 hover:grayscale-0 hover:opacity-90 duration-500" />
                <div class="text-[16px] text-gray-800 dark:text-gray-200 font-semibold tracking-wide">空空如也</div>
                <div class="text-gray-500 dark:text-gray-400 mt-2 text-sm">暂无收藏的文件，快去网盘主页收藏吧~</div>
            </div>

        </div>

        <Preview ref="PreviewRef"></Preview>

        <!-- 取消收藏确认框 -->
        <Dialog :show="cancelDialogConfig.show" :title="cancelDialogConfig.title" :buttons="cancelDialogConfig.buttons"
            width="450px" :showCancel="true" @close="cancelDialogConfig.show = false" :showCustomTitle="true">
            <div class="flex flex-col items-center py-6">
                <div
                    class="w-16 h-16 rounded-full bg-[#fff2f0] dark:bg-orange-900/30 flex items-center justify-center mb-5">
                    <span class="text-[28px] text-orange-500 leading-none pb-[3px]">☆</span>
                </div>
                <div class="text-[17px] font-medium text-[#1d1d1f] dark:text-[#f5f5f7] mb-2 tracking-wide">确定取消所选文件的收藏吗？
                </div>
                <div class="text-sm text-[#606266] dark:text-[#c0c4cc]">
                    该操作不会删除原文件，仅将其从收藏列表移除
                </div>
            </div>
        </Dialog>
    </div>
</template>

<script setup>
import SkeletonLoader from "@/components/Common/SkeletonLoader.vue";
import { ref, getCurrentInstance, onMounted } from "vue"

const { proxy } = getCurrentInstance();

const api = {
    loadDataList: '/file/loadDataList',
    cancelFavorite: '/file/favorite'
}

const columns = [
    {
        label: '文件名',
        prop: 'fileName',
        scopedSlots: 'fileName',
        align: 'left',
        sortable: true
    },
    {
        label: '大小',
        prop: 'fileSize',
        scopedSlots: 'fileSize',
        width: 150,
        align: 'center',
        sortable: true
    },
    {
        label: '类型',
        prop: 'fileType',
        scopedSlots: 'fileType',
        width: 150,
        align: 'center',
        sortable: true
    },
    {
        label: '收藏时间',
        prop: 'lastUpdateTime',
        width: 250,
        align: 'center',
        sortable: true
    }
]

const fileNameFuzzy = ref("");
const tableData = ref({})
const tableOptions = ref({
    extHeight: 50,
    selectType: 'checkbox',
    infiniteScroll: true
})

const isLoading = ref(false);

const sortConfig = ref({
    prop: 'lastUpdateTime',
    order: 'descending'
});

const handleSortChange = ({ prop, order }) => {
    sortConfig.value = { prop, order };
    loadDataList();
};

// 新增：提取文件后缀的高级显示名称
const getFileExtension = (fileName) => {
    if (!fileName) return '未知类型';
    const lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex === -1) return '未知类型';
    return fileName.substring(lastDotIndex + 1).toUpperCase();
};

const initTableData = () => {
    tableData.value = {
        pageNo: 1,
        pageSize: 20,
        list: [],
        totalCount: 0
    }
}
initTableData()

// 加载列表 （核心：搭载 queryFavorite: true 联合查询）
const loadDataList = async (append = false) => {
    isLoading.value = true;
    if (!tableData.value.pageNo) {
        initTableData()
    }
    if (!append) {
        tableData.value.pageNo = 1
        tableData.value.list = []
    }
    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize,
        fileNameFuzzy: fileNameFuzzy.value,
        queryFavorite: true, // 查询该用户所有的收藏
        category: 'all'      // 绕过 Pid 检查机制，在所有文件域中搜索收藏
    }

    if (sortConfig.value.prop && sortConfig.value.order) {
        params.sortField = sortConfig.value.prop;
        params.sortOrder = sortConfig.value.order === 'ascending' ? 'asc' : 'desc';
    }

    let result = await proxy.Request({
        showLoading: false,
        url: api.loadDataList,
        params: params
    })
    isLoading.value = false;

    if (!result) return;

    if (append && tableData.value.list) {
        tableData.value = {
            ...result.data,
            list: [...tableData.value.list, ...result.data.list]
        }
    } else {
        tableData.value = result.data
    }
}

const selectFileList = ref([])

const rowSelected = (rows) => {
    selectFileList.value = []
    rows.forEach(item => {
        selectFileList.value.push(item.fileId)
    })
}

const showOp = (row) => {
    tableData.value.list.forEach(item => {
        item.showOp = false
    })
    row.showOp = true
}

const cancelShowOp = (row) => {
    row.showOp = false
}

// 预览控制
const PreviewRef = ref()
const preview = (data) => {
    if (data.folderType == 1) {
        proxy.Message.warning('不能直接在收藏列表内穿透打开文件夹哦');
        return
    }
    if (data.status != 2) {
        proxy.Message.warning('文件正在转码中，请稍后再试');
        return
    }
    PreviewRef.value.showReview(data, 0)
}

// 单个取消 (核心修复：加入级联行锁与真实 ID 寻找防错剔除)
const cancelFavoriteSingle = async (row) => {
    // 防爆点：防止用户手抖导致 Toggle 请求连发又变成「加入收藏」
    if (row.isToggling) return;
    row.isToggling = true;

    try {
        let res = await proxy.Request({
            url: api.cancelFavorite,
            params: { fileId: row.fileId },
            showLoading: false // 静默取消全屏 Loading，由行内 Loading 接管
        });

        if (res) {
            proxy.Message.success("已移除收藏");

            // 抛弃危险不可靠的索引依赖，每次通过真实唯一 ID 校验真实下表
            const realIndex = tableData.value.list.findIndex(item => item.fileId === row.fileId);
            if (realIndex !== -1) {
                // 丝滑剔除视图中的数据行，避免重绘闪烁
                tableData.value.list.splice(realIndex, 1);
                tableData.value.totalCount -= 1;
            }

            // 智能翻页判断：如果当前页的数据被清空
            if (tableData.value.list.length === 0) {
                loadDataList(false);
            }
        }
    } finally {
        // 释放行级防抖锁
        row.isToggling = false;
    }
}

// 批量取消弹窗支持
const cancelDialogConfig = ref({
    show: false,
    title: "取消收藏",
    buttons: [
        {
            text: "确定取消",
            type: "warning",
            click: () => confirmCancelBatch()
        }
    ]
});

const cancelBatch = () => {
    if (selectFileList.value.length === 0) return;
    cancelDialogConfig.value.show = true;
}

// 批量取消执行
const confirmCancelBatch = async () => {
    try {
        // 利用 Promise.all 支持并行向后端发出针对每一个 fileId 的移除请求。
        await Promise.all(selectFileList.value.map(fileId =>
            proxy.Request({ url: api.cancelFavorite, params: { fileId: fileId }, showError: false })
        ));
        proxy.Message.success("取消收藏成功");
        cancelDialogConfig.value.show = false;
    } catch (e) {
        // 降级吞错处理
    } finally {
        selectFileList.value = [];
        loadDataList(false);
    }
}

onMounted(() => {
    loadDataList()
})
</script>

<style lang="scss" scoped>
:deep(.mac-input .el-input__wrapper) {
    box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08) inset !important;
    background-color: white !important;
    border-radius: 10px;
    height: 38px;
    padding: 0 12px;
    transition: all 0.25s ease;
}

html.dark :deep(.mac-input .el-input__wrapper) {
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
    background-color: #1c1c1e !important;
}

:deep(.mac-input .el-input__wrapper.is-focus),
:deep(.mac-input .el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px rgba(0, 122, 255, 0.4) inset !important;
}
</style>