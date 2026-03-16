<template>
  <div class="flex flex-col h-full pt-4 pb-1 overflow-hidden">

    <!-- 顶栏：标题与聚合长检索区 -->
    <div class="flex flex-col lg:flex-row justify-between items-start lg:items-center px-6 mb-5 gap-4 shrink-0">

      <div class="flex flex-col shrink-0">
        <h1 class="text-[22px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-tight leading-snug">操作日志</h1>
        <span class="text-[13px] text-[#86868b] dark:text-[#98989d] mt-0.5 font-medium">全方位追踪系统各模块的用户操作行为序列</span>
      </div>

      <!-- 右侧：重构为纯浮动折行网格，抛弃 el-form 捆绑 -->
      <div class="flex flex-wrap items-center gap-2.5 justify-start lg:justify-end select-none w-full lg:w-auto">
        <el-input v-if="isAdmin" v-model.trim="searchForm.userId" placeholder="用户ID" clearable
          class="mac-input w-[110px]" />
        <el-input v-if="isAdmin" v-model.trim="searchForm.userName" placeholder="用户名" clearable
          class="mac-input w-[110px]" />

        <el-select v-model="searchForm.module" placeholder="全部模块" clearable class="mac-input w-[110px]">
          <el-option label="文件管理" value="文件管理" />
          <el-option label="分享管理" value="分享管理" />
          <el-option label="回收站" value="回收站" />
          <el-option label="用户管理" value="用户管理" />
          <el-option label="系统设置" value="系统设置" />
        </el-select>

        <el-input v-model.trim="searchForm.action" placeholder="操作动作" clearable class="mac-input w-[110px]" />
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="mac-input w-[100px]">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>

        <div class="w-[230px]">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日"
            end-placeholder="结束日" value-format="YYYY-MM-DD" class="mac-daterange" />
        </div>

        <!-- 操作按钮组 -->
        <div class="flex items-center gap-1.5 ml-1">
          <button type="button" @click="handleSearch"
            class="bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-xl px-4 py-2 text-[13.5px] font-semibold transition-all shadow-sm flex items-center gap-1 focus:outline-none active:scale-95 h-[36px]">
            <span class="iconfont icon-search flex items-center justify-center py-1"></span>搜索
          </button>
          <div @click="handleReset" title="重置搜索条件"
            class="flex items-center justify-center w-[36px] h-[36px] text-gray-500 hover:text-[#FF3B30] bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] shadow-sm rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer group">
            <span class="iconfont icon-refresh text-[14px]"></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 表格主体 -->
    <div class="flex-1 overflow-hidden relative flex flex-col">
      <div class="flex-1 overflow-hidden" v-if="tableData.list && tableData.list.length > 0">
        <Table ref="dataTableRef" :columns="columns" :fetch="loadDataList" :dataSource="tableData"
          :options="tableOptions">

          <!-- 状态胶囊对齐原生 -->
          <template #status="{ row }">
            <span
              :class="row.status === 1 ? 'bg-emerald-50 text-emerald-600 border-emerald-200 dark:bg-emerald-900/20 dark:border-emerald-900/40 dark:text-emerald-400' : 'bg-red-50 text-red-600 border-red-200 dark:bg-red-900/20 dark:border-red-900/40 dark:text-red-400'"
              class="px-2.5 py-1 text-[12px] font-bold border rounded-lg tracking-wider">
              {{ row.status === 1 ? '成功' : '失败' }}
            </span>
          </template>

          <!-- 细节摘要处理并增加对齐黑色气泡 -->
          <template #detail="{ row }">
            <el-tooltip v-if="row.detail && row.detail.length > 35" :content="row.detail" placement="top" effect="dark"
              :show-after="200">
              <span
                class="inline-block max-w-full overflow-hidden text-ellipsis whitespace-nowrap text-[#86868b] dark:text-[#98989d] text-[13px] cursor-help">
                {{ row.detail }}
              </span>
            </el-tooltip>
            <span v-else class="text-[#86868b] dark:text-[#98989d] text-[13px]">{{ row.detail || '-' }}</span>
          </template>

          <template #resultMsg="{ row }">
            <el-tooltip v-if="row.resultMsg && row.resultMsg.length > 25" :content="row.resultMsg" placement="top"
              effect="dark" :show-after="200">
              <span
                class="inline-block max-w-full overflow-hidden text-ellipsis whitespace-nowrap font-medium text-[13px] cursor-help"
                :class="row.status === 1 ? 'text-[#1d1d1f] dark:text-[#f5f5f7]' : 'text-red-500'">
                {{ row.resultMsg }}
              </span>
            </el-tooltip>
            <span v-else class="font-medium text-[13px]"
              :class="row.status === 1 ? 'text-[#1d1d1f] dark:text-[#f5f5f7]' : 'text-red-500'">{{ row.resultMsg || '-'
              }}</span>
          </template>
        </Table>
      </div>

      <div class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16" v-else>
        <Icon iconName="no_data" :width="140" fit="fill" class="opacity-80 grayscale"></Icon>
        <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">当前没有任何操作记录日志</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue';
import Icon from '@/components/Icon.vue';
import Table from '@/components/Table.vue';
import { useUserStore } from '@/store/userStore';

const { proxy } = getCurrentInstance();
const userStore = useUserStore();
const isAdmin = computed(() => userStore.userInfo?.isAdmin);

const api = {
  loadDataList: '/admin/loadOperationLog',
};

const searchForm = reactive({
  userId: '',
  userName: '',
  module: '',
  action: '',
  status: null,
  dateRange: null,
});

const columns = computed(() => {
  let baseColumns = [
    {
      label: '操作模块',
      prop: 'module',
      width: 120,
    },
    {
      label: '操作动作',
      prop: 'action',
      width: 150,
    },
    {
      label: '操作详情',
      prop: 'detail',
      scopedSlots: 'detail',
    },
    {
      label: '操作状态',
      prop: 'status',
      width: 100,
      scopedSlots: 'status',
    },
    {
      label: '结果信息',
      prop: 'resultMsg',
      width: 150,
      scopedSlots: 'resultMsg',
    },
    {
      label: '操作时间',
      prop: 'createTime',
      width: 160,
    },
  ];

  // 仅在管理员权限时压入用户信息
  if (isAdmin.value) {
    baseColumns.unshift(
      {
        label: '用户ID',
        prop: 'userId',
        width: 120,
      },
      {
        label: '用户名',
        prop: 'userName',
        width: 120,
      }
    );
  }

  return baseColumns;
});

const tableData = reactive({});
const tableOptions = {
  extHeight: 100,
};

const dataTableRef = ref();

// 加载数据列表
const loadDataList = async () => {
  let params = {
    pageNo: tableData.pageNo,
    pageSize: tableData.pageSize,
  };

  // 添加搜索条件
  if (searchForm.userId) {
    params.userId = searchForm.userId;
  }
  if (searchForm.userName) {
    params.userNameFuzzy = searchForm.userName;
  }
  if (searchForm.module) {
    params.module = searchForm.module;
  }
  if (searchForm.action) {
    params.actionFuzzy = searchForm.action;
  }
  if (searchForm.status !== null && searchForm.status !== '') {
    params.status = searchForm.status;
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.createTimeStart = searchForm.dateRange[0];
    params.createTimeEnd = searchForm.dateRange[1];
  }

  let result = await proxy.Request({
    url: api.loadDataList,
    params,
  });

  if (!result) {
    return;
  }

  tableData.list = result.data.list;
  tableData.pageNo = result.data.pageNo;
  tableData.pageSize = result.data.pageSize;
  tableData.pageTotal = result.data.pageTotal;
  tableData.totalCount = result.data.totalCount;
};

// 搜索
const handleSearch = () => {
  dataTableRef.value.setCurrentPage(1);
  nextTick(() => {
    loadDataList();
  });
};

// 重置
const handleReset = () => {
  searchForm.userId = '';
  searchForm.userName = '';
  searchForm.module = '';
  searchForm.action = '';
  searchForm.status = null;
  searchForm.dateRange = null;
  dataTableRef.value.setCurrentPage(1);
  nextTick(() => {
    loadDataList();
  });
};
</script>

<style scoped>
/* 核心输入框基础打底 */
:deep(.mac-input .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08) inset !important;
  background-color: white !important;
  border-radius: 10px;
  height: 36px;
  padding: 0 10px;
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

/* DatePicker 日期范围组件深度重写 */
:deep(.mac-daterange.el-date-editor) {
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08) inset !important;
  background-color: white !important;
  border-radius: 10px;
  height: 36px;
  padding: 0 12px;
  transition: all 0.25s ease;
  width: 100% !important;
  border: none;
}

html.dark :deep(.mac-daterange.el-date-editor) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
  background-color: #1c1c1e !important;
}

:deep(.mac-daterange.el-date-editor.is-active),
:deep(.mac-daterange.el-date-editor:hover) {
  box-shadow: 0 0 0 2px rgba(0, 122, 255, 0.4) inset !important;
}

:deep(.mac-daterange .el-range-input) {
  background: transparent !important;
  color: #1d1d1f !important;
  font-weight: 500;
  font-size: 13px;
}

html.dark :deep(.mac-daterange .el-range-input) {
  color: #f5f5f7 !important;
}

:deep(.mac-daterange .el-range-separator) {
  color: #86868b !important;
  position: relative;
  top: -1px;
}
</style>