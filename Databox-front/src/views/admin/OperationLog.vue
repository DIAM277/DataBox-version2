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
        <el-input v-if="isAdmin" v-model.trim="searchForm.userId" placeholder="按用户ID检索" clearable
          @keyup.enter="searchData" class="mac-input w-[140px]" />

        <el-select v-model="searchForm.module" placeholder="全部模块" clearable @change="searchData"
          class="mac-input w-[110px]">
          <el-option label="文件管理" value="文件管理" />
          <el-option label="分享管理" value="分享管理" />
          <el-option label="回收站" value="回收站" />
          <el-option label="用户管理" value="用户管理" />
          <el-option label="系统设置" value="系统设置" />
        </el-select>

        <el-input v-model.trim="searchForm.action" placeholder="操作动作" clearable @keyup.enter="searchData"
          class="mac-input w-[110px]" />

        <el-select v-model="searchForm.status" placeholder="状态" clearable @change="searchData"
          class="mac-input w-[100px]">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>

        <div class="w-[230px]">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日"
            end-placeholder="结束日" value-format="YYYY-MM-DD" @change="searchData" class="mac-daterange" />
        </div>

        <!-- 操作按钮组 -->
        <div class="flex items-center gap-1.5 ml-1">
          <button type="button" @click="searchData"
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
        <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
          :options="tableOptions">

          <!-- 用户ID (样式全面对标 LoginLog) -->
          <template #userId="{ row }">
            <span class="text-[14px] font-semibold text-[#007AFF] cursor-pointer hover:underline select-all">
              {{ row.userId }}
            </span>
          </template>

          <!-- 模块专属彩色标签 -->
          <template #module="{ row }">
            <span :class="getModuleColor(row.module)"
              class="px-2.5 py-1 text-[12px] font-semibold rounded-lg tracking-wider border">
              {{ row.module || '其它类型' }}
            </span>
          </template>

          <!-- 动作描述渲染 -->
          <template #action="{ row }">
            <span class="text-[#1d1d1f] dark:text-[#f5f5f7] font-medium text-[13.5px] tracking-wide">{{ row.action
              }}</span>
          </template>

          <!-- 细节详情极简悬浮气泡结构 (强制中心约束) -->
          <template #detail="{ row }">
            <div v-if="row.formattedDetail && row.formattedDetail !== '-'"
              class="flex items-center justify-center w-full">
              <el-tooltip placement="top" effect="dark" :show-after="200">
                <template #content>
                  <div class="max-w-[400px] leading-relaxed text-[13px] tracking-wide">
                    {{ row.formattedDetail }}
                  </div>
                </template>
                <div
                  class="inline-flex items-center justify-center gap-1.5 cursor-help text-[#86868b] hover:text-[#007AFF] transition-all bg-gray-50 hover:bg-blue-50 dark:bg-[#2c2c2e] dark:hover:bg-blue-900/30 px-3 py-1 rounded-md border border-gray-200 dark:border-[#38383a]">
                  <span class="font-serif italic font-bold">i</span>
                  <span class="text-[12.5px] font-medium">详情</span>
                </div>
              </el-tooltip>
            </div>
            <span v-else class="text-[#86868b] dark:text-[#98989d] text-[13px] block text-center w-full">-</span>
          </template>

          <!-- 状态原生对齐 -->
          <template #status="{ row }">
            <span
              :class="row.status === 1 ? 'bg-emerald-50 text-emerald-600 border-emerald-200 dark:bg-emerald-900/20 dark:border-emerald-900/40 dark:text-emerald-400' : 'bg-red-50 text-red-600 border-red-200 dark:bg-red-900/20 dark:border-red-900/40 dark:text-red-400'"
              class="px-2.5 py-1 text-[12px] font-bold border rounded-lg tracking-wider">
              {{ row.status === 1 ? '成功' : '失败' }}
            </span>
          </template>

          <!-- 退色时间戳 -->
          <template #createTime="{ row }">
            <span class="text-[#86868b] dark:text-[#98989d] text-[13px] tracking-wide">{{ row.createTime }}</span>
          </template>

        </Table>
      </div>

      <!-- 骨架屏占位 -->
      <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="px-6 py-4" />

      <!-- 无数据缺省页 -->
      <div class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16" v-else>
        <Icon iconName="no_data" :width="140" fit="fill" class="opacity-80 grayscale"></Icon>
        <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">当前没有任何操作记录日志</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, computed, onMounted } from 'vue';
import Icon from '@/components/Common/Icon.vue';
import Table from '@/components/Common/Table.vue';
import SkeletonLoader from "@/components/Common/SkeletonLoader.vue";
import { useUserStore } from '@/store/userStore';

const { proxy } = getCurrentInstance();
const userStore = useUserStore();
const isAdmin = computed(() => userStore.userInfo?.isAdmin);

const api = {
  loadDataList: '/admin/loadOperationLog',
};

const searchForm = reactive({
  userId: '',
  module: '',
  action: '',
  status: null,
  dateRange: null,
});

// 计算不同模块所属的 Color 风格
const getModuleColor = (module) => {
  const colorMap = {
    '文件管理': 'bg-blue-50 text-blue-600 border-blue-200 dark:bg-blue-900/20 dark:border-blue-900/40 dark:text-blue-400',
    '分享管理': 'bg-purple-50 text-purple-600 border-purple-200 dark:bg-purple-900/20 dark:border-purple-900/40 dark:text-purple-400',
    '回收站': 'bg-orange-50 text-orange-600 border-orange-200 dark:bg-orange-900/20 dark:border-orange-900/40 dark:text-orange-400',
    '用户管理': 'bg-teal-50 text-teal-600 border-teal-200 dark:bg-teal-900/20 dark:border-teal-900/40 dark:text-teal-400',
    '系统设置': 'bg-gray-100 text-gray-600 border-gray-200 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400'
  };
  return colorMap[module] || 'bg-gray-50 text-gray-500 border-gray-200 dark:bg-gray-800 dark:border-gray-700 dark:text-gray-400';
};

const columns = computed(() => {
  let baseColumns = [
    {
      label: '操作模块',
      prop: 'module',
      width: 200,
      align: 'center',
      scopedSlots: 'module'
    },
    {
      label: '操作动作',
      prop: 'action',
      width: 200,
      align: 'center',
      scopedSlots: 'action'
    },
    {
      label: '操作详情',
      prop: 'formattedDetail',
      align: 'center',
      scopedSlots: 'detail',
    },
    {
      label: '状态',
      prop: 'status',
      width: 200,
      align: 'center',
      scopedSlots: 'status',
    },
    {
      label: '操作时间',
      prop: 'createTime',
      width: 200,
      align: 'center',
      scopedSlots: 'createTime',
    },
  ];

  // 管理员权限动态压入用户ID
  if (isAdmin.value) {
    baseColumns.unshift(
      {
        label: '用户ID',
        prop: 'userId',
        width: 200,
        align: 'center',
        scopedSlots: 'userId'
      }
    );
  }

  return baseColumns;
});

const tableData = ref({});
const isLoading = ref(false);
const tableOptions = ref({
  extHeight: 50,
  infiniteScroll: true, // 启用完美的向下滚动加载
});

// 初始化表格分页数据状态
const initTableData = () => {
  tableData.value = {
    pageNo: 1,
    pageSize: 20,
    list: [],
    totalCount: 0
  }
}
initTableData();

// 解析 JSON 格式的操作详情字段
const formatDetailJson = (detailStr) => {
  if (!detailStr) return '-';
  try {
    const obj = JSON.parse(detailStr);
    if (typeof obj !== 'object' || obj === null) {
      return detailStr;
    }

    let resultArr = [];
    if (obj.fileNames && Array.isArray(obj.fileNames) && obj.fileNames.length > 0) {
      resultArr.push(`文件名称：${obj.fileNames.join('、')}`);
    } else if (obj.targetIds) {
      resultArr.push(`目标标识：${obj.targetIds}`);
    }

    if (resultArr.length === 0 && obj.fileIds && Array.isArray(obj.fileIds) && obj.fileIds.length > 0) {
      resultArr.push(`操作文件ID：${obj.fileIds.join('、')}`);
    }
    return resultArr.length > 0 ? resultArr.join(' | ') : detailStr;
  } catch (e) {
    return detailStr;
  }
};

// 触发检索 (重置到第一页)
const searchData = () => {
  loadDataList(false);
};

// 加载数据列表 (对接 Infinite Scroll 与搜索逻辑)
const loadDataList = async (append = false) => {
  isLoading.value = true;

  if (!tableData.value.pageNo) {
    initTableData();
  }

  // 区分初始加载还是下拉追载
  if (!append) {
    tableData.value.pageNo = 1;
    tableData.value.list = [];
  }

  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
  };

  // 组装查询约束
  if (searchForm.userId) {
     params.userIdFuzzy = searchForm.userId;
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
    params.createTimeStart = searchForm.dateRange[0] + " 00:00:00";
    params.createTimeEnd = searchForm.dateRange[1] + " 23:59:59";
  }

  let result = await proxy.Request({
    showLoading: false,
    url: api.loadDataList,
    params,
  });

  isLoading.value = false;

  if (!result) return;

  // 格式化解析 JSON 字段
  const formattedList = result.data.list.map(item => {
    item.formattedDetail = formatDetailJson(item.detail);
    return item;
  });
  result.data.list = formattedList;

  // 下拉数据合并拼接
  if (append && tableData.value.list) {
    tableData.value = {
      ...result.data,
      list: [...tableData.value.list, ...result.data.list]
    }
  } else {
    tableData.value = result.data;
  }
};

const handleReset = () => {
  searchForm.userId = '';
  searchForm.module = '';
  searchForm.action = '';
  searchForm.status = null;
  searchForm.dateRange = null;
  searchData();
};

onMounted(() => {
  loadDataList();
});
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