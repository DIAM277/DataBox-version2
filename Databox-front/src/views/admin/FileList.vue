<template>
  <div class="flex flex-col h-full pt-4 pb-1 overflow-hidden">

    <!-- 顶栏：标题与局部操作区 -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center px-6 mb-4 gap-4 shrink-0">

      <!-- 左：标题与说明 -->
      <div class="flex flex-col">
        <h1 class="text-[22px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-tight leading-snug">用户文件</h1>
        <span class="text-[13px] text-[#86868b] dark:text-[#98989d] mt-0.5 font-medium">查看和管理云盘中所有的用户存储资源</span>
      </div>

      <!-- 右：操作按钮 -->
      <div class="flex items-center gap-2.5 shrink-0 select-none">

        <!-- 【警告红】：批量删除 -->
        <div @click="selectFileList.length > 0 ? delFileBatch() : null"
          class="flex items-center gap-1.5 px-3 py-2 text-[13px] font-medium border rounded-xl transition-all shadow-sm"
          :class="selectFileList.length === 0 ? 'opacity-40 cursor-not-allowed border-gray-200 dark:border-[#38383a] text-gray-400 bg-gray-50 dark:bg-black/20' : 'cursor-pointer text-red-600 border-red-200 bg-red-50 hover:bg-red-100 dark:border-red-900/50 dark:bg-red-900/20 dark:hover:bg-red-900/40'">
          <span class="iconfont icon-del leading-none"></span>删除
        </div>

        <!-- 极简刷新按钮 -->
        <div @click="loadDataList(false)"
          class="flex items-center justify-center w-[36px] h-[36px] text-gray-500 hover:text-[#007AFF] bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] shadow-sm rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer group">
          <span class="iconfont icon-refresh text-[14px]"></span>
        </div>

      </div>
    </div>

    <!-- 顶部独立文件路径面包屑 (Mac Finder 风格) -->
    <div class="px-6 mb-3 shrink-0">
      <Navigation ref="navigationRef" @navChange="navChange"></Navigation>
    </div>

    <!-- 主体：底层 Table 容器 -->
    <div class="flex-1 overflow-hidden relative flex flex-col">

      <!-- 数据渲染态 -->
      <div class="flex-1 overflow-hidden" v-if="tableData.list && tableData.list.length > 0">
        <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
          :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
          :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">

          <!-- 重构插槽 1：精美化文件名称与隐形式右侧悬浮悬浮操作 -->
          <template #fileName="{ index, row }">
            <div class="flex items-center pr-10 relative group w-full" @mouseenter="showOp(row)"
              @mouseleave="cancelShowOp(row)">

              <!-- 图标判断逻辑保持与全局高度一致 -->
              <template v-if="(row.fileType == 3 || row.fileType == 1)">
                <Icon :cover="row.fileCover" :width="32"></Icon>
              </template>
              <template v-else>
                <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
                <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
              </template>

              <!-- 文件名文本 -->
              <span
                class="ml-3 flex-1 overflow-hidden text-ellipsis whitespace-nowrap cursor-pointer hover:text-[#007AFF] transition-colors"
                :title="row.fileName" @click="preview(row)">
                <span class="text-[#1d1d1f] dark:text-[#f5f5f7] font-medium">{{ row.fileName }}</span>
              </span>

              <!-- iOS 化：行内极简悬浮组 -->
              <div v-if="row.showOp"
                class="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-1 sm:gap-1.5 z-10 opacity-0 group-hover:opacity-100 transition-opacity duration-200">

                <el-tooltip content="下载此文件" placement="top" effect="dark" :show-after="300" v-if="row.folderType !== 1">
                  <span
                    class="iconfont icon-download w-7 h-7 flex items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-[#007AFF] hover:shadow-sm transition-all cursor-pointer"
                    @click.stop="handleDownloadClick(row)"></span>
                </el-tooltip>

                <el-tooltip content="强制删除" placement="top" effect="dark" :show-after="300">
                  <span
                    class="iconfont icon-del w-7 h-7 flex items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-red-500 hover:shadow-sm transition-all cursor-pointer"
                    @click.stop="delFile(index)"></span>
                </el-tooltip>

              </div>
            </div>
          </template>

          <!-- 重构插槽 2：弱化大小展示 -->
          <template #fileSize="{ index, row }">
            <span class="font-medium text-[#86868b] dark:text-[#a1a1a6] text-[13px]" v-if="row.fileSize">{{
              proxy.Utils.size2Str(row.fileSize) }}</span>
            <span v-else class="text-gray-300 dark:text-[#424245] text-lg">-</span>
          </template>

          <!-- 重构插槽 3：弱化类型展示 -->
          <template #fileType="{ index, row }">
            <span v-if="row.folderType === 1" class="text-[#86868b] dark:text-[#a1a1a6] text-[13px]">文件夹</span>
            <span v-else class="text-[#86868b] dark:text-[#a1a1a6] text-[13px]">{{ getFileExtension(row.fileName)
            }}文件</span>
          </template>

          <!-- 重构插槽 4：所属用户高亮化 -->
          <template #userName="{ index, row }">
            <span class="text-[13px] text-[#1d1d1f] dark:text-[#f5f5f7] font-semibold">{{ row.userName || '-' }}</span>
          </template>

        </Table>
      </div>

      <!-- 骨架屏占位 -->
      <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="px-6 py-4" />

      <!-- 无数据显示 -->
      <div class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16" v-else>
        <Icon iconName="no_data" :width="140" fit="fill" class="opacity-80 grayscale"></Icon>
        <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">当前未发现任何用户文件</div>
      </div>

    </div>

    <!-- 警告级物理弹窗重置 -->
    <Dialog :show="deleteDialogConfig.show" :title="deleteDialogConfig.title" :buttons="deleteDialogConfig.buttons"
      width="480px" :showCancel="true" @close="handleDialogClose" :showCustomTitle="true">
      <div class="flex flex-col items-center py-6">
        <div
          class="w-16 h-16 rounded-full bg-[#fff2f0] dark:bg-red-900/30 flex items-center justify-center mb-5 border-4 border-white dark:border-[#1c1c1e] shadow-sm">
          <img src="@/assets/icon-image/warning.png" class="w-9 h-9 object-cover opacity-90">
        </div>
        <div class="text-[17px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] mb-2 tracking-wide text-center">
          确定删除所选的文件吗？
        </div>
        <div class="text-[13px] text-[#86868b] dark:text-[#a1a1a6] text-center w-[80%] leading-relaxed">
          彻底删除后将无法恢复，相关用户也将丢失该文件索引</div>
      </div>
    </Dialog>

    <!-- 文件预览核心保留 -->
    <Preview ref="PreviewRef"></Preview>

  </div>
</template>

<script setup>
import SkeletonLoader from "@/components/SkeletonLoader.vue";
import { ref, reactive, getCurrentInstance, nextTick, computed, onMounted } from "vue"
const { proxy } = getCurrentInstance();

const api = {
  loadDataList: '/admin/loadFileList',
  delFile: '/admin/delFile',
  createDownloadUrl: '/admin/createDownloadUrl',
  download: '/api/admin/download'
}

const columns = computed(() => [
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
    label: '所属用户',
    prop: 'userName',
    scopedSlots: 'userName',
    width: 150,
    align: 'center',
    sortable: true
  },
  {
    label: '上传时间',
    prop: 'createTime',
    width: 200,
    align: 'center',
    sortable: true
  },
  {
    label: '修改时间',
    prop: 'lastUpdateTime',
    width: 200,
    align: 'center',
    sortable: true
  }
]);

const tableData = ref({})
const tableOptions = ref({
  extHeight: 50,
  selectType: 'checkbox',
  infiniteScroll: true
})
// 添加加载状态变量
const isLoading = ref(false);
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
    filePid: currentFolder.value.fileId
  }
  // 添加排序参数
  if (sortConfig.value.prop && sortConfig.value.order) {
    params.sortField = sortConfig.value.prop;
    params.sortOrder = sortConfig.value.order === 'ascending' ? 'asc' : 'desc';
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

// 获取文件后缀
const getFileExtension = (fileName) => {
  if (!fileName) return '未知类型';
  const lastDotIndex = fileName.lastIndexOf('.');
  if (lastDotIndex === -1) return '未知类型';
  return fileName.substring(lastDotIndex + 1).toUpperCase();
}

// 当前目录
const currentFolder = ref({ fileId: "0" })
// 导航栏改变
const navChange = (data) => {
  const { curFolder } = data
  currentFolder.value = curFolder
  // 清空选中的文件列表
  selectFileList.value = []
  loadDataList()
}

// 多选
const selectFileList = ref([])
// 多选回调
const rowSelected = (rows) => {
  selectFileList.value = []
  rows.forEach(item => {
    selectFileList.value.push(item.userId + "_" + item.fileId)
  })
}

// 删除对话框配置
const deleteDialogConfig = ref({
  show: false,
  title: "确定删除",
  buttons: [
    {
      text: "删除",
      type: "danger",
      click: () => {
        confirmDelete();
      }
    }
  ]
});

// 批量删除
const delFileBatch = () => {
  if (selectFileList.value.length === 0) {
    return
  }
  // 显示自定义对话框
  deleteDialogConfig.value.show = true;
}

// 确认删除操作
const confirmDelete = async () => {
  let result = await proxy.Request({
    url: api.delFile,
    params: {
      fileIdAndUserIds: selectFileList.value.join(',')
    }
  })
  if (!result) {
    return;
  }
  loadDataList();
  selectFileList.value = [];
  deleteDialogConfig.value.show = false;
  proxy.Message.success('删除成功');
}

// 保存选择状态
const previousSelectedFiles = ref([]);
// 删除单个文件
const delFile = (index) => {
  // 获取当前行的数据
  const row = tableData.value.list[index];
  if (!row || !row.fileId) {
    proxy.Message.warning('无法获取文件信息');
    return;
  }
  // 保存当前选中的文件列表
  previousSelectedFiles.value = [...selectFileList.value];
  // 清空之前的选择，设置当前文件为要删除的文件
  selectFileList.value = [];
  selectFileList.value.push(row.userId + "_" + row.fileId);

  // 显示确认对话框
  deleteDialogConfig.value.show = true;
}

// 监听对话框关闭事件
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

// 展示操作栏
const showOp = (row) => {
  tableData.value.list.forEach(element => {
    element.showOp = false
  })
  row.showOp = true
}

// 取消操作栏
const cancelShowOp = (row) => {
  row.showOp = false
}

// 处理下载按钮点击事件
const handleDownloadClick = async (row) => {
  if (row.folderType == 1) {
    proxy.Message.warning('不支持直接下载文件夹');
    return;
  }
  // 文件下载
  let result = await proxy.Request({
    url: api.createDownloadUrl + '/' + row.userId + '/' + row.fileId,
  })
  if (!result) {
    return;
  }
  window.location.href = api.download + '/' + result.data;
};

const PreviewRef = ref()
const navigationRef = ref()
// 点击文件名事件
const preview = (data) => {
  // 目录
  if (data.folderType == 1) {
    // 清空选中的文件列表
    selectFileList.value = []
    // 清除选中状态
    navigationRef.value.openFolder(data)
    return
  }
  if (data.status != 2) {
    proxy.Message.warning('文件正在转码中，请稍后再试');
    return
  }
  // 文件预览
  PreviewRef.value.showReview(data, 0)
}

onMounted(() => {
  loadDataList();
})
</script>
