<template>
    <div class="flex flex-col h-full pt-4 pb-1">

        <!-- 顶栏：标题与局部操作按钮区 -->
        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center px-6 mb-5 gap-4">

            <!-- 左：标题与说明 -->
            <div class="flex flex-col">
                <h1 class="text-[22px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-tight leading-snug">
                    举报处理</h1>
                <span
                    class="text-[13px] text-[#86868b] dark:text-[#98989d] mt-0.5 font-medium">审核处理外部访客对违规分享的举报反馈</span>
            </div>

            <!-- 右：操作刷新区 -->
            <div class="flex items-center gap-2.5 shrink-0 select-none flex-wrap">
                <!-- 极简刷新按钮 -->
                <div @click="loadDataList(false)"
                    class="flex items-center justify-center w-[36px] h-[36px] text-gray-500 hover:text-[#007AFF] bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] shadow-sm rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer group">
                    <span class="iconfont icon-refresh text-[14px]"></span>
                </div>
            </div>

        </div>

        <!-- 主体：底层 Table 容器 -->
        <div class="flex-1 overflow-hidden relative flex flex-col">

            <!-- 有数据：原生表格 -->
            <div class="flex-1 overflow-hidden" v-if="tableData.list && tableData.list.length > 0">
                <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList"
                    :initFetch="false" :options="tableOptions">

                    <!-- 新增 1：极其优雅的复合双源文件展现体 -->
                    <template #fileInfo="{ row }">
                        <div class="flex items-center gap-2.5 w-full pr-2">
                            <!-- [ 图标位 ] 物理失效警告图 -->
                            <template v-if="!row.fileName">
                                <span class="iconfont icon-warning text-red-500 text-[26px]"></span>
                            </template>
                            <!-- [ 图标位 ] 正常复用文件图标渲染逻辑 -->
                            <template v-else>
                                <template v-if="(row.fileType === 3 || row.fileType === 1)">
                                    <Icon :cover="row.fileCover" :width="32"></Icon>
                                </template>
                                <template v-else>
                                    <Icon v-if="row.folderType === 0" :fileType="row.fileType"></Icon>
                                    <Icon v-if="row.folderType === 1" :fileType="0"></Icon>
                                </template>
                            </template>

                            <!-- [ 名字位 ] 兼容长度溢出策略 -->
                            <div class="flex-1 min-w-0 truncate text-[13.5px] font-medium leading-tight"
                                :title="row.fileName || '文件/分享已失效'"
                                :class="!row.fileName ? 'text-red-500/80 italic' : 'text-gray-700 dark:text-gray-300'">
                                {{ row.fileName || '源文件/分享已失效' }}
                            </div>
                        </div>
                    </template>

                    <!-- 新增 2：分享提取码展示 -->
                    <template #shareCode="{ row }">
                        <el-tag v-if="row.shareCode" size="small" type="info"
                            class="font-mono bg-gray-50 border-gray-200 text-gray-500 dark:bg-gray-800 dark:border-gray-700">
                            {{ row.shareCode }}
                        </el-tag>
                        <span v-else
                            class="text-gray-300 dark:text-gray-600 text-sm font-medium tracking-widest">-</span>
                    </template>

                    <!-- 举报人/IP -->
                    <template #reporter="{ row }">
                        <span v-if="!row.reportUserId || row.reportUserId === 'guest'"
                            class="text-gray-500 dark:text-gray-400 text-[13px] font-medium">
                            游客 (IP: <span class="font-mono text-gray-400">{{ row.reportIp || '未知' }}</span>)
                        </span>
                        <span v-else class="text-[#007AFF] font-semibold tracking-wide cursor-pointer hover:underline">
                            {{ row.reportUserId }}
                        </span>
                    </template>

                    <!-- 举报原因断行支持 -->
                    <template #reason="{ row }">
                        <div class="truncate lg:whitespace-normal line-clamp-2 text-gray-700 dark:text-gray-300 text-[13px]"
                            :title="row.reason">
                            {{ row.reason }}
                        </div>
                    </template>

                    <!-- 状态渲染 -->
                    <template #status="{ row }">
                        <!-- 待处理：醒目红预警 -->
                        <span v-if="row.status === 0"
                            class="px-2.5 py-1 bg-red-50 text-red-600 border border-red-200 dark:bg-red-900/20 dark:border-red-900/40 dark:text-red-400 text-[12px] font-bold rounded-full tracking-wider transition-colors inline-block">
                            <span class="w-1.5 h-1.5 rounded-full bg-red-500 inline-block mr-1"></span>待处理
                        </span>
                        <!-- 已封禁：幽灵灰禁用态 -->
                        <span v-else-if="row.status === 1"
                            class="px-2.5 py-1 bg-gray-100 text-gray-500 border border-gray-200 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400 text-[12px] font-bold rounded-full tracking-wider inline-block">
                            已封禁
                        </span>
                        <!-- 正常：翠绿色放行 -->
                        <span v-else-if="row.status === 2"
                            class="px-2.5 py-1 bg-emerald-50 text-emerald-600 border border-emerald-200 dark:bg-emerald-900/20 dark:border-emerald-900/40 dark:text-emerald-400 text-[12px] font-bold rounded-full tracking-wider inline-block">
                            正常
                        </span>
                    </template>

                    <!-- 操作栏 -->
                    <template #op="{ row }">
                        <div class="flex items-center justify-center gap-3 w-full">

                            <!-- 强制通用的直通审查特权挂载区 -->
                            <el-tooltip content="前往审查" placement="top" effect="dark" :show-after="300">
                                <span
                                    class="text-sm text-[#007AFF] hover:bg-blue-50 dark:hover:bg-blue-900/20 px-2 py-1 rounded cursor-pointer font-semibold transition-all active:scale-95 flex items-center gap-1"
                                    @click="reviewShare(row)">
                                    <span class="iconfont icon-explore text-[13px]"></span>审查
                                </span>
                            </el-tooltip>

                            <template v-if="row.status === 0">
                                <!-- 封禁 -->
                                <el-tooltip content="判定违规并封禁分享" placement="top" effect="dark" :show-after="300">
                                    <span
                                        class="text-sm text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 px-2 py-1 rounded cursor-pointer font-semibold transition-all active:scale-95"
                                        @click="showProcessDialog(row, 1)">封禁</span>
                                </el-tooltip>

                                <!-- 放行 -->
                                <el-tooltip content="判定无违规并放行" placement="top" effect="dark" :show-after="300">
                                    <span
                                        class="text-sm text-emerald-500 hover:bg-emerald-50 dark:hover:bg-emerald-900/20 px-2 py-1 rounded cursor-pointer font-semibold transition-all active:scale-95"
                                        @click="showProcessDialog(row, 2)">正常</span>
                                </el-tooltip>
                            </template>

                            <!-- 已结束反馈 -->
                            <div v-else class="text-gray-300 dark:text-gray-600 text-sm font-medium tracking-widest">-
                            </div>
                        </div>
                    </template>

                </Table>
            </div>

            <!-- 加载中 -->
            <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="px-6 py-4" />

            <!-- 无数据显示 -->
            <div class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16" v-else>
                <img src="@/assets/icon-image/no_data.png"
                    class="w-36 h-36 opacity-70 mb-5 grayscale drop-shadow-sm transition-all hover:scale-105 hover:grayscale-0 hover:opacity-90 duration-500" />
                <div class="text-[16px] text-gray-800 dark:text-gray-200 font-semibold tracking-wide">举报清空</div>
                <div class="mt-2 text-[#86868b] text-[13px] tracking-wide font-medium">当前没有任何待处理的违规反馈</div>
            </div>

        </div>

        <!-- 处理二次确认专属弹窗 -->
        <Dialog :show="processDialogConfig.show" :title="processDialogConfig.title"
            :buttons="processDialogConfig.buttons" width="480px" :showCancel="true"
            @close="processDialogConfig.show = false" :showCustomTitle="true">
            <div class="flex flex-col items-center py-6">
                <div class="w-16 h-16 rounded-full flex items-center justify-center mb-5 shadow-sm"
                    :class="processDialogConfig.processStatus === 1 ? 'bg-[#fff2f0] dark:bg-red-900/30 text-red-500' : 'bg-emerald-50 dark:bg-emerald-900/30 text-emerald-500'">
                    <span v-if="processDialogConfig.processStatus === 1"
                        class="iconfont icon-cancel leading-none text-[34px]"></span>
                    <span v-else class="iconfont icon-ok leading-none text-[36px]"></span>
                </div>
                <div
                    class="text-[17px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] mb-2 tracking-wide text-center">
                    {{ processDialogConfig.message }}
                </div>
                <div class="text-[13px] text-[#86868b] dark:text-[#a1a1a6] text-center w-[80%] leading-relaxed">
                    操作后不可逆逆转，请确保已经核实过该分享文件的内容
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
    loadDataList: '/admin/shareReport/loadDataList',
    processReport: '/admin/shareReport/process'
};

const columns = [
    // 隐藏了冗杂且无体验意义的 ID 字段
    // { label: '举报ID', prop: 'reportId', width: 80, align: 'center' },
    // { label: '分享ID', prop: 'shareId', width: 120, align: 'center' },

    // 全新优雅的高信息密度组合列
    { label: '举报文件', prop: 'fileInfo', scopedSlots: 'fileInfo', width: 280, align: 'center' },
    { label: '提取码', prop: 'shareCode', scopedSlots: 'shareCode', width: 100, align: 'center' },
    { label: '举报原因', prop: 'reason', scopedSlots: 'reason', align: 'center' },
    { label: '举报人/来源', prop: 'reporter', scopedSlots: 'reporter', width: 180, align: 'center' },
    { label: '举报时间', prop: 'createTime', width: 180, align: 'center' },
    { label: '处理状态', prop: 'status', scopedSlots: 'status', width: 120, align: 'center' },
    { label: '处理操作', prop: 'op', scopedSlots: 'op', width: 200, align: 'center' }
];

const tableData = ref({});
const tableOptions = ref({
    extHeight: 50,
    infiniteScroll: true
});
const isLoading = ref(false);

const initTableData = () => {
    tableData.value = {
        pageNo: 1,
        pageSize: 20,
        list: [],
        totalCount: 0
    };
};
initTableData();

// 获取拉取核心数据
const loadDataList = async (append = false) => {
    isLoading.value = true;
    if (!tableData.value.pageNo) {
        initTableData();
    }
    if (!append) {
        tableData.value.pageNo = 1;
        tableData.value.list = [];
    }

    let result = await proxy.Request({
        showLoading: false,
        url: api.loadDataList,
        params: {
            pageNo: tableData.value.pageNo,
            pageSize: tableData.value.pageSize,
        }
    });

    isLoading.value = false;
    if (!result) return;

    if (append && tableData.value.list) {
        tableData.value = {
            ...result.data,
            list: [...tableData.value.list, ...result.data.list]
        };
    } else {
        tableData.value = result.data;
    }
};

// 状态处理流程控制
const currentProcessRow = ref(null);
const processDialogConfig = ref({
    show: false,
    title: "处理审核",
    message: "",
    processStatus: null,
    buttons: [
        {
            text: "确定",
            type: "danger",
            click: () => {
                submitProcess();
            }
        }
    ]
});

// 展现审核防御面板
const showProcessDialog = (row, status) => {
    currentProcessRow.value = row;
    processDialogConfig.value.processStatus = status;

    if (status === 1) {
        processDialogConfig.value.message = "确认判定违规并强制封禁删除该分享吗？";
        processDialogConfig.value.buttons[0].type = "danger";
    } else {
        processDialogConfig.value.message = "确认该分享无违规予以放行过审吗？";
        processDialogConfig.value.buttons[0].type = "primary";
    }

    processDialogConfig.value.show = true;
};

// 提交决断
const submitProcess = async () => {
    let res = await proxy.Request({
        url: api.processReport,
        params: {
            reportId: currentProcessRow.value.reportId,
            status: processDialogConfig.value.processStatus
        }
    });

    if (res) {
        proxy.Message.success("判定结果处理成功");
        processDialogConfig.value.show = false;
        // 光速刷新对应行的前端属性，彻底消灭二次请求延迟闪烁
        currentProcessRow.value.status = processDialogConfig.value.processStatus;
    }
};

// 交互一键审查直通车
const reviewShare = (row) => {
    // 拦截物理失效的文件
    if (!row.shareCode || !row.fileName) {
        proxy.Message.warning("该分享链接可能已被原作者删除，无法预览");
        return;
    }

    // 将提取码通过 URL 直接抛给检验界面 实现防查自动进盘
    window.open(`/shareCheck/${row.shareId}?code=${row.shareCode}`, '_blank');
};

onMounted(() => {
    loadDataList();
});
</script>