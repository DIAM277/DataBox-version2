<template>
    <div class="flex flex-col h-full pt-4 pb-1 overflow-hidden">

        <!-- 顶栏：标题与聚合操作区 -->
        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center px-6 mb-5 gap-4 shrink-0">

            <!-- 左：标题与说明 -->
            <div class="flex flex-col shrink-0">
                <h1 class="text-[22px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-tight leading-snug">
                    登录日志</h1>
                <span
                    class="text-[13px] text-[#86868b] dark:text-[#98989d] mt-0.5 font-medium">查看并审计系统用户的登录节点与归属地记录</span>
            </div>

            <!-- 右：极简搜索过滤器 -->
            <div class="flex items-center gap-2.5 shrink-0 select-none flex-wrap">
                <!-- 仅管理员可看的搜用户输入框 -->
                <el-input v-if="isAdmin" clearable placeholder="搜用户ID" v-model.trim="searchForm.userIdFuzzy"
                    @keyup.enter="searchData" class="mac-input w-[130px]" />
                <el-input clearable placeholder="搜登录IP" v-model.trim="searchForm.loginIpFuzzy" @keyup.enter="searchData"
                    class="mac-input w-[130px]" />

                <button type="button" @click="searchData"
                    class="bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-xl px-3 py-1.5 text-[13.5px] font-semibold transition-all shadow-sm flex items-center justify-center gap-1.5 focus:outline-none active:scale-95 ml-1">
                    <span class="iconfont icon-search text-[14px]"></span>查询
                </button>

                <div @click="searchData"
                    class="flex items-center justify-center w-[36px] h-[36px] text-gray-500 hover:text-[#007AFF] bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] shadow-sm rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer group">
                    <span class="iconfont icon-refresh text-[14px]"></span>
                </div>
            </div>

        </div>

        <!-- 主体：底层 Table 容器 -->
        <div class="flex-1 overflow-hidden relative flex flex-col">
            <div class="flex-1 overflow-hidden" v-if="tableData.list && tableData.list.length > 0">
                <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList"
                    :initFetch="false" :options="tableOptions" @sortChange="handleSortChange"
                    :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">

                    <template #userId="{ row }">
                        <span class="text-[14px] font-semibold text-[#007AFF] cursor-pointer hover:underline"
                            :title="row.userId">{{ row.userId }}</span>
                    </template>

                    <template #loginIp="{ row }">
                        <span
                            class="font-mono text-[13.5px] text-[#1d1d1f] dark:text-[#f5f5f7] bg-black/5 dark:bg-white/10 px-2 py-0.5 rounded-md border border-gray-200 dark:border-gray-700 tracking-wider">{{
                                row.loginIp }}</span>
                    </template>

                    <!-- 🔴 修改登录地点的外层标签，使用 inline-flex 自然居中 -->
                    <template #loginLocation="{ row }">
                        <div class="inline-flex items-center justify-center gap-1.5 text-[#86868b] dark:text-[#98989d]">
                            <span class="iconfont icon-import text-[12px]"></span>
                            <span class="truncate text-[13.5px] max-w-[400px]" :title="row.loginLocation">{{
                                row.loginLocation || '未解析归属地' }}</span>
                        </div>
                    </template>

                    <template #loginTime="{ row }">
                        <span class="text-[13.5px] text-[#86868b] dark:text-[#98989d] tracking-wide">{{
                            formatDate(row.loginTime) }}</span>
                    </template>

                </Table>
            </div>

            <!-- 骨架屏占位 -->
            <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="px-6 py-4" />

            <!-- 无数据显示 -->
            <div class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16" v-else>
                <Icon iconName="no_data" :width="140" fit="fill" class="opacity-80 grayscale"></Icon>
                <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">当前没有任何网络登录记录</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import SkeletonLoader from "@/components/Common/SkeletonLoader.vue";
import { ref, reactive, getCurrentInstance, computed, onMounted } from 'vue';
import { useUserStore } from '@/store/userStore';

const { proxy } = getCurrentInstance();
const userStore = useUserStore();
const isAdmin = computed(() => userStore.userInfo?.isAdmin);

const api = {
    loadLoginLog: "/loadLoginLogList",
};

const searchForm = reactive({
    userIdFuzzy: "",
    loginIpFuzzy: "",
});

const tableData = ref({});
const tableOptions = ref({
    extHeight: 50,
    infiniteScroll: true
});

const isLoading = ref(false);

// 排序相关状态
const sortConfig = ref({
    prop: 'loginTime',
    order: 'descending'
});

// 处理排序变化
const handleSortChange = ({ prop, order }) => {
    sortConfig.value = { prop, order };
    searchData();
};

const columns = computed(() => {
    let baseColumns = [
        {
            label: "登录IP",
            prop: "loginIp",
            width: 280,
            align: 'center',
            scopedSlots: 'loginIp',
            sortable: true
        },
        {
            label: "登录地点",
            prop: "loginLocation",
            align: 'center',
            scopedSlots: 'loginLocation',
            sortable: false
        },
        {
            label: "登录时间",
            prop: "loginTime",
            width: 250,
            align: 'center',
            scopedSlots: "loginTime",
            sortable: true
        },
    ];

    // 如果是管理员，在第一列之前插入"用户ID"
    if (isAdmin.value) {
        baseColumns.unshift({
            label: "用户ID",
            prop: "userId",
            width: 200,
            align: 'center',
            scopedSlots: 'userId',
            sortable: true
        });
    }

    return baseColumns;
});

// 初始化表格数据
const initTableData = () => {
    tableData.value = {
        pageNo: 1,
        pageSize: 20,
        list: [],
        totalCount: 0
    }
}
initTableData();

// 查询数据（重置分页）
const searchData = () => {
    loadDataList(false);
};

const loadDataList = async (append = false) => {
    isLoading.value = true;

    if (!tableData.value.pageNo) {
        initTableData();
    }

    if (!append) {
        tableData.value.pageNo = 1;
        tableData.value.list = [];
    }

    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize,
        userIdFuzzy: searchForm.userIdFuzzy,
        loginIpFuzzy: searchForm.loginIpFuzzy,
    };

    // 添加排序参数
    if (sortConfig.value.prop && sortConfig.value.order) {
        params.sortField = sortConfig.value.prop;
        params.sortOrder = sortConfig.value.order === 'ascending' ? 'asc' : 'desc';
    }

    let result = await proxy.Request({
        showLoading: false,
        url: api.loadLoginLog,
        params,
    });

    isLoading.value = false;

    if (!result) {
        return;
    }

    // 如果是追加模式，合并数据
    if (append && tableData.value.list) {
        tableData.value = {
            ...result.data,
            list: [...tableData.value.list, ...result.data.list]
        }
    } else {
        tableData.value = result.data;
    }
};

const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });
};

onMounted(() => {
    loadDataList(false);
});
</script>

<style scoped>
/* 表单组件高度一致性覆写 */
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
    box-shadow: 0 0 0 2px rgba(0, 122, 255, 0.4) inset !important;
}

:deep(.mac-input .el-input__inner) {
    font-weight: 500;
    font-size: 13.5px;
    color: #1d1d1f;
}

html.dark :deep(.mac-input .el-input__inner) {
    color: #f5f5f7;
}
</style>