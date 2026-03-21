<template>
  <div class="flex flex-col h-full pt-4 pb-1">

    <!-- 顶栏：标题与局部操作按钮区 -->
    <div class="flex justify-between items-center px-6 mb-5">

      <!-- 左：标题与说明 -->
      <div class="flex flex-col">
        <h1 class="text-[22px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-tight leading-snug">我的分享</h1>
        <span class="text-[13px] text-[#86868b] dark:text-[#98989d] mt-0.5 font-medium">管理与查看你生成的所有对外分享链接</span>
      </div>

      <!-- 右：操作按钮与搜索区 -->
      <div class="flex gap-2.5 items-center shrink-0 select-none flex-wrap">

        <!-- 搜索域 -->
        <el-input clearable placeholder="搜索分享文件" v-model.trim="fileNameFuzzy" @keyup.enter="loadDataList(false)"
          class="mac-input w-[160px]" />

        <!-- 查询按钮 -->
        <button type="button" @click="loadDataList(false)"
          class="bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-xl px-3 py-1.5 text-[13.5px] font-semibold transition-all shadow-sm flex items-center justify-center gap-1.5 focus:outline-none active:scale-95 mr-1">
          <span class="iconfont icon-search text-[14px]"></span>查询
        </button>

        <!-- 【警示红】：批量取消分享 -->
        <div @click="selectFileList.length > 0 ? cancelShareBatch() : null"
          class="flex items-center gap-1.5 px-3 py-2 text-[13px] font-medium border rounded-xl transition-all shadow-sm"
          :class="selectFileList.length === 0 ? 'opacity-40 cursor-not-allowed border-gray-200 dark:border-[#38383a] text-gray-400 bg-gray-50 dark:bg-black/20' : 'cursor-pointer text-red-600 border-red-200 bg-red-50 hover:bg-red-100 dark:border-red-900/50 dark:bg-red-900/20 dark:hover:bg-red-900/40'">
          <span class="iconfont icon-cancel leading-none"></span>取消分享
        </div>

        <!-- 极简刷新按钮 -->
        <div @click="loadDataList(false)"
          class="flex items-center justify-center w-[36px] h-[36px] text-gray-500 hover:text-[#007AFF] bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] shadow-sm rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer group">
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
            <!-- group relative 控制行内悬浮触发体系 -->
            <div class="flex items-center relative group w-full" @mouseenter="showOp(row)"
              @mouseleave="cancelShowOp(row)">

              <template v-if="(row.fileType == 3 || row.fileType == 1) && row.status !== 0">
                <Icon :cover="row.fileCover" :width="32"></Icon>
              </template>
              <template v-else>
                <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
                <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
              </template>

              <div class="ml-3 flex-1 min-w-0 flex items-center sm:group-hover:pr-[120px] transition-all duration-300"
                :title="row.fileName || '该分享文件已被删除'">
                <span class="text-[#1d1d1f] dark:text-[#f5f5f7] font-medium truncate"
                  :class="{ 'text-gray-400 dark:text-gray-500 italic': !row.fileName }">
                  {{ row.fileName || '该分享文件已被删除' }}
                </span>
              </div>

              <!-- 行内悬浮操作区（被折叠于行尾，鼠标悬浮整行时透明度置 1） -->
              <div v-if="row.showOp"
                class="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-1 sm:gap-1.5 z-10 opacity-0 group-hover:opacity-100 transition-opacity duration-200">

                <el-tooltip content="复制链接" placement="top" effect="dark" :show-after="300">
                  <span
                    class="iconfont icon-link w-7 h-7 flex items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-[#007AFF] hover:shadow-sm transition-all cursor-pointer"
                    @click="copy(row)"></span>
                </el-tooltip>

                <el-tooltip content="取消分享" placement="top" effect="dark" :show-after="300">
                  <span
                    class="iconfont icon-cancel w-7 h-7 flex items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-red-500 hover:shadow-sm transition-all cursor-pointer"
                    @click="cancelShare(row)"></span>
                </el-tooltip>

              </div>
            </div>
          </template>

          <!-- 到期时间插槽 -->
          <template #expireTime="{ index, row }">
            <span class="text-gray-500 dark:text-gray-400">{{ row.expireTime ? row.expireTime : '永久有效' }}</span>
          </template>

          <!-- 新增：多维度状态插槽 -->
          <template #status="{ row }">
            <!-- 生效中 -->
            <span v-if="checkShareStatus(row) === 'valid'"
              class="px-2.5 py-1 bg-emerald-50 dark:bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 text-[12px] font-medium rounded-full tracking-wide border border-emerald-100 dark:border-transparent">
              生效中
            </span>
            <!-- 失效 (文件已物理或逻辑删除) -->
            <span v-else-if="checkShareStatus(row) === 'deleted'"
              class="px-2.5 py-1 bg-orange-50 dark:bg-orange-500/10 text-orange-500 dark:text-orange-400 text-[12px] font-medium rounded-full tracking-wide border border-orange-200 dark:border-transparent shadow-sm">
              失效
            </span>
            <!-- 正常超时过期 -->
            <span v-else
              class="px-2.5 py-1 bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400 text-[12px] font-medium rounded-full tracking-wide border border-gray-200 dark:border-transparent">
              已失效
            </span>
          </template>

        </Table>
      </div>

      <!-- 骨架屏 -->
      <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="p-4" />

      <!-- 无数据底盘 -->
      <div v-else class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16">
        <Icon iconName="no_data" :width="130" fit="fill" class="opacity-80"></Icon>
        <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">当前没有分享的文件</div>
      </div>

    </div>

    <!-- 弹窗合集：原装结构保留，样式类名重构为 Tailwind -->
    <Dialog :show="cancelDialogConfig.show" :title="cancelDialogConfig.title" :buttons="cancelDialogConfig.buttons"
      width="500px" :showCancel="true" @close="handleDialogClose" :showCustomTitle="true">
      <div class="flex flex-col items-center py-6">
        <div class="w-16 h-16 rounded-full bg-[#fff2f0] dark:bg-red-900/30 flex items-center justify-center mb-5">
          <img src="@/assets/icon-image/warning.png" class="w-10 h-10 rounded-full bg-white object-cover">
        </div>
        <div class="text-[17px] font-medium text-[#1d1d1f] dark:text-[#f5f5f7] mb-2 tracking-wide">确定取消所选的分享吗？</div>
        <div class="text-sm text-center text-[#606266] dark:text-[#c0c4cc] leading-6">
          取消后，分享链接将立即失效
        </div>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import SkeletonLoader from "@/components/Common/SkeletonLoader.vue";
import useClipboard from "vue-clipboard3"
const { toClipboard } = useClipboard()
import { ref, reactive, getCurrentInstance, nextTick, onMounted, computed } from "vue"
const { proxy } = getCurrentInstance();

const api = {
  loadDataList: '/share/loadShareList',
  cancelShare: '/share/cancelShare'
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

// 判断分享状态：支持文件删除失效、永久有效及按期失效验证
const checkShareStatus = (row) => {
  // 第 1 优先级：源文件被移入回收站(1)、彻底删除(2)或物理不存在
  if (row.fileDelFlag === 1 || row.fileDelFlag === 0 || !row.fileName) {
    return 'deleted';
  }

  // 第 2 优先级：没有设置过期时间或声明为永久有效(3)
  if (row.validType === 3 || !row.expireTime) {
    return 'valid';
  }

  // 第 3 优先级：将后端的 YYYY-MM-DD HH:mm:ss 替换以兼容不同浏览器的时间解析
  const expireDate = new Date(row.expireTime.replace(/-/g, '/'));
  if (expireDate.getTime() > Date.now()) {
    return 'valid';
  }

  // 超时
  return 'expired';
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
    label: '分享时间',
    prop: 'shareTime',
    width: 300,
    align: 'center',
    sortable: true
  },
  {
    label: '失效时间',
    prop: 'expireTime',
    width: 300,
    align: 'center',
    scopedSlots: 'expireTime',
    sortable: true
  },
  {
    label: '状态',
    prop: 'status',
    width: 100,
    align: 'center',
    scopedSlots: 'status',
    sortable: false
  },
  {
    label: '浏览次数',
    prop: 'showCount',
    width: 200,
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
  // 添加排序参数
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
    selectFileList.value.push(item.shareId)
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

// 分享连接
const shareUrl = ref(document.location.origin + '/share/')
// 复制分享信息
const copy = async (data) => {
  await toClipboard(`${shareUrl.value}${data.shareId}?code=${data.code}`);
  proxy.Message.success("复制分享成功");
}

// 取消分享对话框配置
const cancelDialogConfig = ref({
  show: false,
  title: "确定取消分享",
  buttons: [
    {
      text: "确定",
      type: "danger",
      click: () => {
        confirmCancelShare();
      }
    }
  ]
});

// 保存选择状态
const previousSelectedFiles = ref([]);
// 取消单个分享
const cancelShare = (row) => {
  // 保存当前选中的文件
  previousSelectedFiles.value = [...selectFileList.value];
  // 清空之前的选择，设置当前文件为要取消分享的文件
  selectFileList.value = [];
  selectFileList.value.push(row.shareId);

  // 显示确认对话框
  cancelDialogConfig.value.show = true;
}
const cancelShareBatch = () => {
  if (selectFileList.value.length == 0) {
    return;
  }
  // 显示自定义对话框
  cancelDialogConfig.value.show = true;
}
// 确认取消分享操作
const confirmCancelShare = async () => {
  let result = await proxy.Request({
    url: api.cancelShare,
    params: {
      shareIds: selectFileList.value.join(',')
    }
  })
  if (!result) {
    return;
  }
  loadDataList();
  selectFileList.value = [];
  cancelDialogConfig.value.show = false;
  proxy.Message.success('取消分享成功');
}

// 监听对话框关闭事件
const handleDialogClose = () => {
  // 关闭对话框
  cancelDialogConfig.value.show = false;

  // 如果当前只有一个文件被选中，说明可能是从单个文件取消分享操作进入的
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