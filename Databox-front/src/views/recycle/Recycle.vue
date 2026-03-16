<template>
  <div class="flex flex-col h-full pt-4 pb-1">

    <!-- 顶栏：标题与局部操作按钮区 -->
    <div class="flex justify-between items-center px-6 mb-5">

      <!-- 左：标题与说明 -->
      <div class="flex flex-col">
        <h1 class="text-[22px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-tight leading-snug">回收站</h1>
        <span class="text-[13px] text-[#86868b] dark:text-[#98989d] mt-0.5 font-medium">文件保留30天后将被自动彻底清除，请及时处理</span>
      </div>

      <!-- 右：操作按钮 -->
      <div class="flex gap-3 items-center shrink-0 select-none">

        <!-- 新增：搜索域 -->
        <el-input clearable placeholder="搜索删除文件" v-model.trim="fileNameFuzzy" @keyup.enter="loadDataList(false)"
          class="mac-input w-[160px]" />

        <!-- 新增：查询按钮 -->
        <button type="button" @click="loadDataList(false)"
          class="bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-xl px-4 py-2.5 text-[13.5px] font-semibold transition-all shadow-sm flex items-center justify-center gap-1.5 focus:outline-none active:scale-95 mr-1">
          <span class="iconfont icon-search text-[14px]"></span>查询
        </button>

        <!-- 【新生绿】：批量还原 -->
        <div @click="selectFileList.length > 0 ? revertBatch() : null"
          class="flex items-center gap-1.5 px-3 py-2 text-[13px] font-medium border rounded-xl transition-all shadow-sm"
          :class="selectFileList.length === 0 ? 'opacity-40 cursor-not-allowed border-gray-200 dark:border-[#38383a] text-gray-400 bg-gray-50 dark:bg-black/20' : 'cursor-pointer text-emerald-600 border-emerald-200 bg-emerald-50 hover:bg-emerald-100 dark:border-emerald-900/50 dark:bg-emerald-900/20 dark:hover:bg-emerald-900/40'">
          <span class="iconfont icon-revert leading-none"></span>还原文件
        </div>

        <!-- 【警示红】：彻底删除 -->
        <div @click="selectFileList.length > 0 ? delFileBatch() : null"
          class="flex items-center gap-1.5 px-3 py-2 text-[13px] font-medium border rounded-xl transition-all shadow-sm"
          :class="selectFileList.length === 0 ? 'opacity-40 cursor-not-allowed border-gray-200 dark:border-[#38383a] text-gray-400 bg-gray-50 dark:bg-black/20' : 'cursor-pointer text-red-600 border-red-200 bg-red-50 hover:bg-red-100 dark:border-red-900/50 dark:bg-red-900/20 dark:hover:bg-red-900/40'">
          <span class="iconfont icon-del leading-none"></span>彻底删除
        </div>

        <!-- 极简刷新按钮 -->
        <div @click="loadDataList(false)"
          class="flex items-center justify-center w-[34px] h-[34px] text-gray-500 hover:text-[#007AFF] bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] shadow-sm rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer">
          <span class="iconfont icon-refresh text-[14px]"></span>
        </div>

      </div>
    </div>

    <!-- 主体：底层 Table 容器 (去除内层生硬嵌套的卡片边框与背景) -->
    <div class="flex-1 overflow-hidden relative flex flex-col">

      <!-- 有数据：原生表格 -->
      <div class="flex-1 overflow-hidden" v-if="tableData.list && tableData.list.length > 0">
        <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
          :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
          :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">

          <!-- 文件名称插槽 -->
          <template #fileName="{ index, row }">
            <div class="flex items-center pr-10 relative group w-full" @mouseenter="showOp(row)"
              @mouseleave="cancelShowOp(row)">

              <template v-if="(row.fileType == 3 || row.fileType == 1) && row.status !== 0">
                <Icon :cover="row.fileCover" :width="32"></Icon>
              </template>
              <template v-else>
                <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
                <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
              </template>

              <span class="ml-3 flex-1 overflow-hidden text-ellipsis whitespace-nowrap" :title="row.fileName">
                <span class="text-[#1d1d1f] dark:text-[#f5f5f7] font-medium">{{ row.fileName }}</span>
              </span>

              <!-- 行内悬浮操作区 -->
              <div v-if="row.showOp"
                class="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-1 sm:gap-1.5 z-10 opacity-0 group-hover:opacity-100 transition-opacity duration-200">

                <el-tooltip content="还原" placement="top" effect="dark" :show-after="300">
                  <span
                    class="iconfont icon-revert w-7 h-7 flex items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-[#007AFF] hover:shadow-sm transition-all cursor-pointer"
                    @click="revert(row)"></span>
                </el-tooltip>

                <el-tooltip content="彻底删除" placement="top" effect="dark" :show-after="300">
                  <span
                    class="iconfont icon-del w-7 h-7 flex items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-red-500 hover:shadow-sm transition-all cursor-pointer"
                    @click="delFile(row)"></span>
                </el-tooltip>

              </div>
            </div>
          </template>

          <!-- 🔴 新增：剩余保留时间状态插槽 -->
          <template #timeLeft="{ row }">
            <template v-if="calculateTimeLeft(row.recoveryTime) > 7">
              <span
                class="px-2.5 py-1 bg-blue-50 dark:bg-blue-500/10 text-blue-600 dark:text-blue-400 text-[12px] font-medium rounded-full tracking-wide border border-blue-100 dark:border-transparent">
                剩 {{ calculateTimeLeft(row.recoveryTime) }} 天
              </span>
            </template>
            <template v-else>
              <span v-if="calculateTimeLeft(row.recoveryTime) <= 0"
                class="px-2.5 py-1 bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400 text-[12px] font-medium rounded-full tracking-wide border border-gray-200 dark:border-transparent">
                即将删除
              </span>
              <span v-else
                class="px-2.5 py-1 bg-red-50 dark:bg-red-500/10 text-red-500 dark:text-red-400 text-[12px] font-medium rounded-full tracking-wide border border-red-100 dark:border-transparent">
                仅剩 {{ calculateTimeLeft(row.recoveryTime) }} 天
              </span>
            </template>
          </template>

          <template #fileSize="{ index, row }">
            <span class="font-medium text-[#86868b] dark:text-[#a1a1a6] text-[13px]" v-if="row.fileSize">{{
              proxy.Utils.size2Str(row.fileSize) }}</span>
            <span v-else class="text-gray-300 dark:text-[#424245] text-lg">-</span>
          </template>

        </Table>
      </div>

      <!-- 骨架屏 -->
      <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="p-4" />

      <!-- 无数据底盘 -->
      <div v-else class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16">
        <Icon iconName="no_data" :width="130" fit="fill" class="opacity-80"></Icon>
        <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">回收站中没有文件</div>
      </div>

    </div>

    <!-- 彻底删除确认对话框 -->
    <Dialog :show="deleteDialogConfig.show" :title="deleteDialogConfig.title" :buttons="deleteDialogConfig.buttons"
      width="500px" :showCancel="true" @close="handleDialogClose" :showCustomTitle="true">
      <div class="flex flex-col items-center py-6">
        <div class="w-16 h-16 rounded-full bg-[#fff2f0] dark:bg-red-900/30 flex items-center justify-center mb-5">
          <img src="@/assets/icon-image/warning.png" class="w-10 h-10 rounded-full bg-white object-cover">
        </div>
        <div class="text-[17px] font-medium text-[#1d1d1f] dark:text-[#f5f5f7] mb-2 tracking-wide">确定彻底删除所选的文件吗？</div>
        <div class="text-[13px] text-[#86868b] dark:text-[#a1a1a6]">彻底删除后将彻底无法恢复，请谨慎操作</div>
      </div>
    </Dialog>

    <!-- 还原确认对话框 -->
    <Dialog :show="revertDialogConfig.show" :title="revertDialogConfig.title" :buttons="revertDialogConfig.buttons"
      width="500px" :showCancel="true" @close="handleRevertDialogClose" :showCustomTitle="true">
      <div class="flex flex-col items-center py-6">
        <div class="w-16 h-16 rounded-full bg-blue-50 dark:bg-blue-900/30 flex items-center justify-center mb-5">
          <span class="iconfont icon-revert text-[30px] text-[#007AFF]"></span>
        </div>
        <div class="text-[17px] font-medium text-[#1d1d1f] dark:text-[#f5f5f7] mb-2 tracking-wide">确定还原所选的文件吗？</div>
        <div class="text-[13px] text-[#86868b] dark:text-[#a1a1a6]">文件将被还原到原来的位置</div>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import SkeletonLoader from "@/components/SkeletonLoader.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from "vue"
const { proxy } = getCurrentInstance();

const api = {
  loadDataList: '/recycle/loadRecycleList',
  delFile: '/recycle/delFile',
  recoverFile: '/recycle/recoverFile'
}

// 排序相关状态
const sortConfig = ref({
  prop: '', // 排序的属性
  order: '' // 排序的顺序：ascending或descending
});
// 处理排序变化
const handleSortChange = ({ prop, order }) => {
  sortConfig.value = { prop, order };
  // 重新加载数据
  loadDataList();
};

// 系统回收文件的保留期限(天)
const RETENTION_DAYS = 30;

// 计算剩余保留时间
const calculateTimeLeft = (recoveryTime) => {
  if (!recoveryTime) return 0;
  // 兼容不同设备上的日期解析，将 '-' 替换为 '/'
  const deleteDate = new Date(recoveryTime.replace(/-/g, '/')).getTime();
  const expireDate = deleteDate + RETENTION_DAYS * 24 * 60 * 60 * 1000;
  const daysLeft = Math.ceil((expireDate - Date.now()) / (1000 * 60 * 60 * 24));
  return daysLeft <= 0 ? 0 : daysLeft;
};

const columns = [
  {
    label: '文件名',
    prop: 'fileName',
    scopedSlots: 'fileName',
    align: 'left',
    sortable: true
  },
  {
    label: '删除时间',
    prop: 'recoveryTime',
    width: 250,
    align: 'center',
    sortable: true
  },
    {
    label: '状态',
    prop: 'timeLeft',
    width: 120,
    align: 'center',
    scopedSlots: 'timeLeft',
    sortable: false
  },
  {
    label: '大小',
    prop: 'fileSize',
    width: 300,
    align: 'center',
    scopedSlots: 'fileSize',
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
// 添加加载状态变量
const isLoading = ref(false);
// 初始化表格数据，确保pageNo和pageSize有初始值
const initTableData = () => {
  tableData.value = {
    pageNo: 1,
    pageSize: 20,
    list: [],
    totalCount: 0
  }
}
initTableData()

// 获取文件列表数据
const loadDataList = async (append = false) => {
  // 设置为正在加载状态
  isLoading.value = true;
  // 如果tableData没有初始化，先初始化
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
  }
  // // 添加排序参数
  if (sortConfig.value.prop && sortConfig.value.order) {
    params.sortField = sortConfig.value.prop;
    params.sortOrder = sortConfig.value.order === 'ascending' ? 'asc' : 'desc';
  }
  // 只有当category不为"all"时才删除filePid
  if (params.category !== "all") {
    delete params.filePid
  }
  // 发送axios请求
  let result = await proxy.Request({
    showLoading: false,
    url: api.loadDataList,
    params: params
  })
  isLoading.value = false
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
    tableData.value = result.data
  }
}

// 多选
const selectFileList = ref([])
// 多选回调
const rowSelected = (rows) => {
  selectFileList.value = []
  rows.forEach(item => {
    selectFileList.value.push(item.fileId)
  })
}

// 操作栏目
const showOp = (row) => {
  tableData.value.list.forEach(item => {
    item.showOp = false
  })
  row.showOp = true
}

// 取消操作栏目
const cancelShowOp = (row) => {
  row.showOp = false
}

// 删除确认对话框配置
const deleteDialogConfig = ref({
  show: false,
  title: "彻底删除文件",
  buttons: [
    {
      text: "确定",
      type: "danger",
      click: () => {
        confirmDelete();
      }
    }
  ]
});

// 还原确认对话框配置
const revertDialogConfig = ref({
  show: false,
  title: "还原文件",
  buttons: [
    {
      text: "确定",
      type: "primary",
      click: () => {
        confirmRevert();
      }
    }
  ]
});

// 保存选择状态
const previousSelectedFiles = ref([]);

// 删除单个文件
const delFile = (row) => {
  // 保存当前选中的文件列表
  previousSelectedFiles.value = [...selectFileList.value];
  // 清空之前的选择，设置当前文件为要删除的文件
  selectFileList.value = [];
  selectFileList.value.push(row.fileId);

  // 显示确认对话框
  deleteDialogConfig.value.show = true;
}

// 批量删除文件
const delFileBatch = () => {
  if (selectFileList.value.length == 0) {
    return;
  }
  // 显示确认对话框
  deleteDialogConfig.value.show = true;
}

// 确认删除操作
const emit = defineEmits(['reload'])
const confirmDelete = async () => {
  let result = await proxy.Request({
    url: api.delFile,
    params: {
      fileIds: selectFileList.value.join(',')
    }
  })
  if (!result) {
    return;
  }
  loadDataList();
  selectFileList.value = [];
  deleteDialogConfig.value.show = false;
  proxy.Message.success('删除成功');
  emit('reload')
}

// 监听删除对话框关闭事件
const handleDialogClose = () => {
  // 关闭对话框
  deleteDialogConfig.value.show = false;

  // 如果当前只有一个文件被选中，说明可能是从单个文件删除操作进入的
  if (selectFileList.value.length === 1) {
    // 恢复之前保存的选择状态
    selectFileList.value = [...previousSelectedFiles.value];
    previousSelectedFiles.value = []; // 清空临时数据
  }
}

// 还原单个文件
const revert = (row) => {
  // 保存当前选中的文件列表
  previousSelectedFiles.value = [...selectFileList.value];
  // 清空之前的选择，设置当前文件为要还原的文件
  selectFileList.value = [];
  selectFileList.value.push(row.fileId);

  // 显示确认对话框
  revertDialogConfig.value.show = true;
}

// 批量还原文件
const revertBatch = () => {
  if (selectFileList.value.length == 0) {
    return;
  }
  // 显示确认对话框
  revertDialogConfig.value.show = true;
}

// 确认还原操作
const confirmRevert = async () => {
  let result = await proxy.Request({
    url: api.recoverFile,
    params: {
      fileIds: selectFileList.value.join(',')
    }
  })
  if (!result) {
    return;
  }
  loadDataList();
  selectFileList.value = [];
  revertDialogConfig.value.show = false;
  proxy.Message.success('还原成功');
}

// 监听还原对话框关闭事件
const handleRevertDialogClose = () => {
  // 关闭对话框
  revertDialogConfig.value.show = false;

  // 如果当前只有一个文件被选中，说明可能是从单个文件还原操作进入的
  if (selectFileList.value.length === 1) {
    // 恢复之前保存的选择状态
    selectFileList.value = [...previousSelectedFiles.value];
    previousSelectedFiles.value = []; // 清空临时数据
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

:deep(.el-table) {

  /* 1. 给所有单元格注入颜色变化的平滑过渡 (解决生硬突变问题) */
  td.el-table__cell {
    transition: background-color 0.2s ease-in-out, border-color 0.2s ease-in-out !important;
  }

  .el-table__body {

    /* 2. 普通未选中状态的丝滑悬浮效果 (强制穿透 td 渲染) */
    tr:hover>td.el-table__cell,
    tr.hover-row>td.el-table__cell {
      background-color: rgba(0, 0, 0, 0.04) !important;
    }

    /* 3. 选中态：纯正苹果蓝微透明背板 */
    tr.current-row>td.el-table__cell,
    tr:has(.el-checkbox__input.is-checked)>td.el-table__cell {
      background-color: rgba(0, 122, 255, 0.08) !important;
      border-bottom-color: rgba(0, 122, 255, 0.1) !important;
    }

    /* 4. 选中态进一步触发的悬浮效果 (加深颜色反馈) */
    tr.current-row:hover>td.el-table__cell,
    tr:has(.el-checkbox__input.is-checked):hover>td.el-table__cell {
      background-color: rgba(0, 122, 255, 0.12) !important;
    }
  }
}

/* 适配暗黑模式的高亮与边界线反调 */
html.dark :deep(.el-table) {
  .el-table__body {

    /* 暗黑模式下的普通悬浮 (微白光) */
    tr:hover>td.el-table__cell,
    tr.hover-row>td.el-table__cell {
      background-color: rgba(255, 255, 255, 0.04) !important;
    }

    /* 暗黑模式下苹果蓝需要相对明亮一点以保障可视度 */
    tr.current-row>td.el-table__cell,
    tr:has(.el-checkbox__input.is-checked)>td.el-table__cell {
      background-color: rgba(10, 132, 255, 0.15) !important;
      border-bottom-color: transparent !important;
    }

    /* 暗色下选中后悬浮的微强化 */
    tr.current-row:hover>td.el-table__cell,
    tr:has(.el-checkbox__input.is-checked):hover>td.el-table__cell {
      background-color: rgba(10, 132, 255, 0.22) !important;
    }
  }
}
</style>