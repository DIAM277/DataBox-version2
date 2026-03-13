<template>
    <div class="apple-table-container w-full h-full flex flex-col">
        <el-table ref="dataTable" class="flex-1" height="100%" :data="dataSource.list || []" :stripe="options.stripe"
            :border="options.border" header-row-class-name="table-header-row" @row-click="handleRowClick"
            @sort-change="handleSortChange" @selection-change="handleSelectionChange" v-infinite-scroll="loadMore"
            :infinite-scroll-disabled="infiniteScrollDisabled" :infinite-scroll-distance="20"
            :default-sort="defaultSort">

            <!-- 选择列 -->
            <el-table-column type="selection" width="50" v-if="options.selectType && options.selectType == 'checkbox'"
                align="center"></el-table-column>

            <!-- 序号 -->
            <el-table-column type="index" width="60" v-if="options.showIndex" align="center"
                label="序号"></el-table-column>

            <!-- 数据列 -->
            <template v-for="(column, index) in columns">
                <template v-if="column.scopedSlots">
                    <el-table-column :key="index" :prop="column.prop" :label="column.label" :width="column.width"
                        :align="column.align || 'left'" :sortable="column.sortable">
                        <template #default="scope">
                            <slot :name="column.scopedSlots" :index="scope.$index" :row="scope.row"></slot>
                        </template>
                    </el-table-column>
                </template>
                <template v-else>
                    <el-table-column :key="index" :prop="column.prop" :label="column.label" :width="column.width"
                        :align="column.align || 'left'" :sortable="column.sortable">
                    </el-table-column>
                </template>
            </template>

            <!-- 加载更多提示 -->
            <template #append>
                <div v-if="loading" class="loading-more">
                    <el-icon class="is-loading">
                        <Loading />
                    </el-icon>
                    <span>加载中...</span>
                </div>
                <div v-else-if="noMore && dataSource.list && dataSource.list.length > 0" class="no-more-data">
                    <span>到底了...</span>
                </div>
            </template>
        </el-table>

        <!-- 分页 - 当不使用无限滚动时显示 -->
        <div class="pagination-container" v-if="showPagination && !options.infiniteScroll">
            <el-pagination v-if="dataSource.totalCount" background :total="dataSource.totalCount"
                :page-sizes="[10, 20, 30, 40]" :page-size="dataSource.pageSize" :current-page-sync="dataSource.pageNo"
                :layout="layout" @size-change="handelPageSizeChange" @current-change="handelPageNoChange">
            </el-pagination>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue"
import { Loading } from '@element-plus/icons-vue'
const { proxy } = getCurrentInstance();

const emit = defineEmits(['rowClick', 'rowSelected', 'sortChange']);
const props = defineProps({
    dataSource: Object,
    showPagination: {
        type: Boolean,
        default: true
    },
    showPageSize: {
        type: Boolean,
        default: true
    },
    options: {
        type: Object,
        default: {
            extHeight: 0,
            showIndex: false,
            infiniteScroll: false // 是否启用无限滚动
        }
    },
    columns: Array,
    fetch: Function,
    initFetch: {
        type: Boolean,
        default: true
    },
    defaultSort: {
        type: Object,
        default: () => ({})
    }
})

const layout = computed(() => {
    return `total, ${props.showPageSize ? 'sizes, ' : ''
        }, prev, pager, next, jumper`
})

// 无限滚动相关状态
const loading = ref(false)
const noMore = ref(false)
const infiniteScrollDisabled = computed(() => {
    return loading.value || noMore.value || !props.options.infiniteScroll
})

// 初始化
const init = () => {
    // 确保dataSource有初始值
    if (!props.dataSource.pageNo) {
        props.dataSource.pageNo = 1
    }
    if (!props.dataSource.pageSize) {
        props.dataSource.pageSize = 10
    }

    // 只在initFetch为true且fetch函数存在时才调用fetch
    if (props.initFetch && props.fetch) {
        props.fetch()
    }
}
init()

// 加载更多数据
const loadMore = async () => {
    // 如果正在加载、已经没有更多数据或未启用无限滚动，则不执行
    if (loading.value || noMore.value || !props.options.infiniteScroll) return
    // 确保dataSource已初始化
    if (!props.dataSource.pageNo || !props.dataSource.pageSize || !props.dataSource.totalCount) {
        return
    }
    // 如果已经加载了所有数据
    if (props.dataSource.pageNo * props.dataSource.pageSize >= props.dataSource.totalCount) {
        noMore.value = true
        return
    }
    loading.value = true
    // 增加页码
    props.dataSource.pageNo += 1
    try {
        await props.fetch(true) // 传入true表示追加模式
    } finally {
        loading.value = false
    }
}

// 清除选中
const dataTable = ref()
const clearSelection = () => {
    dataTable.value.clearSelection()
}

// 设置行选中
const setCurrentRow = (rowKey, rowValue) => {
    let row = props.dataSource.list.find((item) => {
        return item[rowKey] == rowValue
    })
    dataTable.value.setCurrentRow(row)
}

// 添加排序事件处理
const handleSortChange = (sortParams) => {
    emit('sortChange', sortParams)
}
defineExpose({
    clearSelection,
    setCurrentRow,
})

// 行点击
const handleRowClick = (row) => {
    const isFileNameOrOp = event.target.closest('.file-name-1') || event.target.closest('.op');
    if (!isFileNameOrOp && !row.showEdit) {
        dataTable.value.toggleRowSelection(row);
    }
    emit('rowClick', row)
}

// 多选
const handleSelectionChange = (row) => {
    emit('rowSelected', row)
}

// 切换每页大小
const handelPageSizeChange = (size) => {
    props.dataSource.pageSize = size
    props.dataSource.pageNo = 1
    noMore.value = false
    props.fetch()
}

// 切换页码
const handelPageNoChange = (pageNo) => {
    props.dataSource.pageNo = pageNo
    props.fetch()
}
</script>

<style lang="scss" scoped>
/* 整个表格容器 */
.apple-table-container {
    @apply relative flex flex-col h-full bg-transparent overflow-hidden;
}

/* 核心变量覆盖与透视化处理 */
:deep(.el-table) {
    /* 强制透明底层 */
    --el-table-bg-color: transparent !important;
    --el-table-tr-bg-color: transparent !important;
    --el-table-header-bg-color: transparent !important;

    /* 彻底消除生硬外边缘与底部多余的双线 */
    --el-table-border-color: transparent !important;
    --el-table-border: none !important;
    background-color: transparent !important;
    border: none !important;

    /* 悬浮体验：亮色模式下使用略带苹果蓝的隐忍灰色，清晰但不刺眼 */
    --el-table-row-hover-bg-color: rgba(0, 122, 255, 0.05) !important;

    /* 禁用原生表格自带的极其顽固的伪元素下划线 */
    &::before,
    &::after {
        display: none !important;
    }

    .el-table__inner-wrapper {

        &::before,
        &::after {
            display: none !important;
        }
    }

    .el-table__border-left-patch {
        display: none !important;
    }

    /* 表头极简处理 */
    th.el-table__cell {
        @apply text-[12px] font-semibold tracking-wider text-[#86868b] dark:text-gray-400 py-2.5 border-b border-[#e5e5e9]/70 dark:border-[#38383a]/70 !important;
        background-color: transparent !important;
    }

    /* 数据行内分割线 */
    td.el-table__cell {
        @apply py-[11px] text-[13.5px] border-b border-gray-100 dark:border-[#38383a]/60 transition-colors duration-200 !important;
        background-color: transparent !important;
    }

    /* 表格内容的最后一行无底边框，与外部容器底部完美融合，消除空隙悬空感 */
    .el-table__body tr:last-child td.el-table__cell {
        border-bottom: none !important;
    }

    /* 空数据背景透传 */
    .el-table__empty-block {
        background-color: transparent !important;
        border-top: none !important;
    }
}

/* 适配暗黑模式的悬浮调优：大幅降低白色的强光刺激度 */
html.dark :deep(.el-table) {
    --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.04) !important;
}

/* 隐藏原生繁杂的包裹级滚动条，改为无痕毛玻璃细条 */
:deep(.el-table__body-wrapper) {
    &::-webkit-scrollbar {
        width: 6px;
        height: 6px;
    }

    &::-webkit-scrollbar-thumb {
        @apply bg-black/10 dark:bg-white/10 rounded-full;
    }

    &::-webkit-scrollbar-track {
        background-color: transparent;
    }
}

/* 无限滚动加载底盘美化 */
.loading-more,
.no-more-data {
    @apply flex items-center justify-center py-2.5 pb-3.5 text-[12px] text-[#86868b] font-medium tracking-wide bg-transparent;

    .el-icon {
        @apply mr-1.5;
    }
}

/* 分页器 */
.pagination-container {
    @apply flex justify-end items-center pt-3 pb-2 w-full bg-transparent border-t border-transparent;

    :deep(.el-pagination) {
        @apply bg-transparent;

        button {
            @apply bg-transparent text-gray-500 dark:text-gray-400 hover:text-[#007AFF] transition-colors border-none;

            &:disabled {
                @apply opacity-30 cursor-not-allowed hover:text-gray-500 dark:hover:text-gray-400;
            }
        }

        .el-pager li {
            @apply bg-transparent text-gray-500 dark:text-gray-400 text-[13px] rounded-lg transition-colors border-none font-medium mx-0.5;

            &:hover {
                @apply bg-black/5 dark:bg-white/10 text-[#007AFF];
            }

            &.is-active {
                @apply bg-[#007AFF]/10 text-[#007AFF] font-bold;
            }
        }

        .el-pagination__total,
        .el-pagination__jump {
            @apply text-[#86868b] dark:text-gray-400 text-[12px];
        }
    }
}
</style>