<template>
    <div>
        <div class="top" style="margin-top: 20px;">
            <div class="top-op">
                <div class="search-form">
                    <el-form :model="searchForm" ref="formDataRef" @submit.prevent inline class="form-inline">
                        <div class="form-item-group">
                            <span class="label">用户ID</span>
                            <el-input clearable placeholder="请输入用户ID" v-model.trim="searchForm.userIdFuzzy"
                                @keyup.enter="searchData" class="input-field" style="width: 200px;" />
                            <span class="label" style="margin-left: 15px;">登录IP</span>
                            <el-input clearable placeholder="请输入登录IP" v-model.trim="searchForm.loginIpFuzzy"
                                @keyup.enter="searchData" class="input-field" style="width: 200px;" />
                            <el-button type="primary" class="custom-btn search-btn" @click="searchData">
                                <span class="iconfont icon-search"></span>
                                查询
                            </el-button>
                            <el-button type="info" class="custom-btn refresh-btn" @click="searchData">
                                <span class="iconfont icon-refresh"></span>
                                刷新
                            </el-button>
                        </div>
                    </el-form>
                </div>
            </div>
        </div>

        <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
            <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList"
                :initFetch="false" :options="tableOptions" @sortChange="handleSortChange"
                :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">
                <!-- 用户ID -->
                <template #userId="{ row }">
                    <span class="user-id-text" :title="row.userId">
                        {{ row.userId }}
                    </span>
                </template>
                <!-- 登录IP -->
                <template #loginIp="{ row }">
                    <span class="ip-text">{{ row.loginIp }}</span>
                </template>
                <!-- 登录地点 -->
                <template #loginLocation="{ row }">
                    <span class="location-text" :title="row.loginLocation">
                        {{ row.loginLocation || '未知' }}
                    </span>
                </template>
                <!-- 登录时间 -->
                <template #loginTime="{ row }">
                    <span class="time-text">{{ formatDate(row.loginTime) }}</span>
                </template>
            </Table>
        </div>

        <!-- 骨架屏 -->
        <SkeletonLoader v-else-if="isLoading" :rowCount="14" />

        <!-- 无数据显示 -->
        <div class="no-data" v-else>
            <div class="no-data-inner">
                <Icon iconName="no_data" :width="150" fit="fill"></Icon>
                <div class="tips">当前没有登录日志数据</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import SkeletonLoader from "@/components/SkeletonLoader.vue";
import { ref, reactive, getCurrentInstance, computed, onMounted } from 'vue';

const { proxy } = getCurrentInstance();

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

const columns = computed(() => [
    {
        label: "用户ID",
        prop: "userId",
        width: 200,
        align: 'center',
        scopedSlots: 'userId',
        sortable: true
    },
    {
        label: "登录IP",
        prop: "loginIp",
        width: 180,
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
        width: 200,
        align: 'center',
        scopedSlots: "loginTime",
        sortable: true
    },
]);

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

<style lang="scss" scoped>
@use "../../assets/main.scss" as *;

.top-op {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
}

.search-form {
    flex: 1;

    :deep(.el-form) {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
    }
}

.form-inline {
    display: flex;
    align-items: center;
}

.form-item-group {
    display: flex;
    align-items: center;
    height: 40px;
}

.label {
    margin-right: 10px;
    font-size: 14px;
    color: #606266;
}

.input-field {
    margin-right: 10px;
}

.search-btn,
.refresh-btn {
    margin-right: 10px;
    height: 32px;
}

.user-id-text {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #409EFF;
    font-weight: 500;
}

.ip-text {
    color: #303133;
    font-family: 'Courier New', monospace;
}

.location-text {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #606266;
}

.time-text {
    color: #909399;
    font-size: 13px;
}

.no-data {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 500px;

    .no-data-inner {
        text-align: center;

        .tips {
            margin-top: 20px;
            font-size: 16px;
            color: #909399;
        }
    }
}
</style>