<template>
    <div>
        <el-table ref="dataTable" :data="dataSource.list || []" :height="tableHeight" :stripe="options.stripe"
            :border="options.border" header-row-class-name="table-header-row" @row-click="handleRowClick"
            @sort-change="handleSortChange" @selection-change="handleSelectionChange" v-infinite-scroll="loadMore"
            :infinite-scroll-disabled="infiniteScrollDisabled" :infinite-scroll-distance="20"
            :default-sort="defaultSort">
            <!-- 选择列 -->
            <el-table-column type="selection" width="50" v-if="options.selectType && options.selectType == 'checkbox'"
                align="center">
            </el-table-column>
            <!-- 序号 -->
            <el-table-column type="index" width="60" v-if="options.showIndex" align="center" label="序号">
            </el-table-column>
            <!-- 数据列 -->
            <template v-for="(column, index) in columns">
                <template v-if="column.scopedSlots">
                    <el-table-column :key="index" :prop="column.prop" :label="column.label" :width="column.width"
                        :align="column.align || 'left'" :sortable="column.sortable">
                        <template #default="scope">
                            <slot :name="column.scopedSlots" :index="scope.$index" :row="scope.row">
                            </slot>
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
                    <span>没有更多数据了...</span>
                </div>
            </template>
        </el-table>
        <!-- 分页 - 当不使用无限滚动时显示 -->
        <div class="pagination" v-if="showPagination && !options.infiniteScroll">
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

const topHeight = 60 + 20 + 30 + 46

const tableHeight = ref(
    props.options.tableHeight ? props.options.tableHeight : window.innerHeight - topHeight - props.options.extHeight
)

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
.pagination {
    padding-top: 15px;
    padding-bottom: 15px;
    display: flex;
    justify-content: flex-end;
}

.el-pagination {
    justify-content: flex-end;
}

.loading-more,
.no-more-data {
    text-align: center;
    padding: 10px 0;
    color: #12121359;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;

    .el-icon {
        margin-right: 5px;
    }
}

:deep(.el-table__cell) {
    padding: 4px 0px;
}

// 添加圆角样式
:deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;

    // 表格外边框圆角
    &::before {
        display: none;
    }

    .el-table__inner-wrapper {
        border-radius: 8px;
        overflow: hidden;
    }

    // 表头圆角
    .el-table__header-wrapper {
        th:first-child .cell {
            border-top-left-radius: 8px;
        }

        th:last-child .cell {
            border-top-right-radius: 8px;
        }
    }

    // 表格底部圆角
    .el-table__body-wrapper {
        border-bottom-left-radius: 8px;
        border-bottom-right-radius: 8px;
        overflow: hidden;
    }

    // 最后一行单元格圆角
    .el-table__body tr:last-child td:first-child {
        border-bottom-left-radius: 8px;
    }

    .el-table__body tr:last-child td:last-child {
        border-bottom-right-radius: 8px;
    }


    :deep(.el-table__empty-block) {
        min-height: 60px;
        height: auto !important;

        .el-table__empty-text {
            line-height: 60px;
        }
    }

    // 当没有数据时调整表格高度
    :deep(.el-table__body-wrapper) {
        &::-webkit-scrollbar {
            width: 8px;
            height: 8px;
        }

        &::-webkit-scrollbar-thumb {
            background-color: #dcdfe6;
            border-radius: 4px;
        }

        &::-webkit-scrollbar-track {
            background-color: #f5f7fa;
        }

        // 当没有数据时隐藏滚动条
        .el-scrollbar__bar.is-vertical {
            display: none !important;
        }
    }
}
</style>