<template>
  <div class="flex flex-col h-full pt-4 pb-1">

    <!-- 顶栏：导航区与全局操作按钮 -->
    <div class="flex justify-between items-center w-full px-2 mb-4">

      <!-- 左：面包屑导航 -->
      <div class="flex-1 overflow-hidden mr-4">
        <Navigation ref="navigationRef" @navChange="navChange"></Navigation>
      </div>

      <!-- 右：微拟物纯组件按键组 -->
      <div class="flex items-center gap-2.5 shrink-0 select-none">

        <!-- 【苹果核心亮色】：上传按钮 -->
        <div class="action-btn upload-btn">
          <el-upload :show-file-list="false" :with-credentials="true" :multiple="true" :http-request="addFile"
            :accept="fileAccept">
            <div
              class="flex items-center justify-center gap-1.5 px-3 py-2 bg-[#007AFF] text-white rounded-xl shadow-[0_2px_10px_rgba(0,122,255,0.3)] hover:-translate-y-0.5 hover:shadow-[0_4px_15px_rgba(0,122,255,0.4)] transition-all font-medium text-[13px] cursor-pointer">
              <span class="iconfont icon-upload"></span> 上传
            </div>
          </el-upload>
        </div>

        <!-- 【原生灰】：新建文件夹 -->
        <div v-if="category == 'all'" @click="newFolder"
          class="flex items-center justify-center gap-1.5 px-3 py-2 text-[13px] font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer group shadow-sm">
          <span
            class="iconfont icon-folder-add text-gray-400 group-hover:text-[#007AFF] transition-colors leading-none"></span>新建
        </div>

        <!-- 【警示红】：批量删除 (附带空状态灰度) -->
        <div @click="selectFileList.length > 0 ? delFileBatch() : null"
          class="flex items-center gap-1.5 px-3 py-2 text-[13px] font-medium border rounded-xl transition-all shadow-sm"
          :class="selectFileList.length === 0 ? 'opacity-40 cursor-not-allowed border-gray-200 dark:border-[#38383a] text-gray-400 bg-gray-50 dark:bg-black/20' : 'cursor-pointer text-red-600 border-red-200 bg-red-50 hover:bg-red-100 dark:border-red-900/50 dark:bg-red-900/20 dark:hover:bg-red-900/40'">
          <span class="iconfont icon-del leading-none"></span>删除
        </div>

        <!-- 【中庸灰】：批量移动 (附带空状态灰度) -->
        <div @click="selectFileList.length > 0 ? moveFileBatch() : null"
          class="flex items-center gap-1.5 px-3 py-2 text-[13px] font-medium border rounded-xl transition-all shadow-sm"
          :class="selectFileList.length === 0 ? 'opacity-40 cursor-not-allowed border-gray-200 dark:border-[#38383a] text-gray-400 bg-gray-50 dark:bg-black/20' : 'cursor-pointer text-gray-700 dark:text-gray-300 border-gray-200 dark:border-[#38383a] bg-white dark:bg-[#1c1c1e] hover:bg-gray-50 dark:hover:bg-[#2c2c2e]'">
          <span class="iconfont icon-move leading-none"></span>移动
        </div>

        <!-- 柔和垂直分割线 -->
        <div class="w-[1px] h-4 bg-gray-200 dark:bg-gray-700 mx-1"></div>

        <!-- 高级悬浮筛选组件 -->
        <FileFilter @filter-change="handleFilterChange" />

        <!-- 极简刷新按钮 -->
        <div @click="loadDataList(false)"
          class="flex items-center justify-center w-[34px] h-[34px] text-gray-500 hover:text-[#007AFF] bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] shadow-sm rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer">
          <span class="iconfont icon-refresh text-[14px]"></span>
        </div>
      </div>
    </div>

    <!-- 主体：底层 Table 容器 (去除内层生硬嵌套的卡片边框与背景，完美融于外层全局结构) -->
    <div class="flex-1 overflow-hidden relative flex flex-col">

      <!-- 有数据：原生表格 -->
      <div class="flex-1 overflow-hidden file-list" v-if="tableData.list && tableData.list.length > 0">
        <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
          :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
          :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">

          <template #fileName="{ index, row }">
            <div class="file-name flex items-center pr-10 relative group w-full" @mouseenter="showOp(row)"
              @mouseleave="cancelShowOp(row)">
              <template v-if="(row.fileType == 3 || row.fileType == 1) && row.status == 2">
                <Icon :cover="row.fileCover" :width="32"></Icon>
              </template>
              <template v-else>
                <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
                <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
              </template>

              <span class="file-text ml-3 flex-1 overflow-hidden" :title="row.fileName" v-if="!row.showEdit">
                <span
                  class="file-name-1 text-[#1d1d1f] dark:text-[#f5f5f7] font-medium cursor-pointer hover:text-[#007AFF]"
                  @click="preview(row)">{{ row.fileName }}</span>
                <span v-if="row.status == 0"
                  class="text-xs text-blue-500 ml-2 bg-blue-50 dark:bg-blue-900/20 px-1.5 py-0.5 rounded">转码中</span>
                <span v-if="row.status == 1"
                  class="text-xs text-red-500 ml-2 bg-red-50 dark:bg-red-900/20 px-1.5 py-0.5 rounded">转码失败</span>
              </span>

              <div class="edit-panel flex-1 flex items-center ml-2" v-if="row.showEdit">
                <el-input v-model.trim="row.fileNameReal" ref="editNameRef" :maxLength="190"
                  @keyup.enter.stop="saveNameEdit(index)">
                  <template #suffix>{{ row.fileSuffix }}</template>
                </el-input>
                <span
                  :class="['iconfont icon-right1 ml-2 p-1 rounded-md text-white cursor-pointer', row.fileNameReal ? 'bg-[#007AFF] hover:bg-[#0056b3]' : 'bg-gray-300 cursor-not-allowed']"
                  @click.stop="saveNameEdit(index)"></span>
                <span class="iconfont icon-error ml-2 text-gray-400 hover:text-red-500 cursor-pointer"
                  @click="cancelNameEdit(index, $event)"></span>
              </div>

              <OpButton :fileData="row" :index="index" :showOp="row.showOp" @share="share"
                @download="handleDownloadClick" @delete="delFile" @rename="editFileName" @move="moveFolder"
                class="opacity-0 group-hover:opacity-100 transition-opacity" />
            </div>
          </template>

          <template #fileSize="{ index, row }">
            <span class="font-medium text-[#86868b] dark:text-[#a1a1a6] text-[13px]" v-if="row.fileSize">{{
              proxy.Utils.size2Str(row.fileSize) }}</span>
            <span v-else class="text-gray-300 dark:text-[#424245] text-lg">-</span>
          </template>

          <template #fileType="{ index, row }">
            <span v-if="row.folderType === 1" class="text-[#86868b] dark:text-[#a1a1a6] text-[13px]">文件夹</span>
            <span v-else class="text-[#86868b] dark:text-[#a1a1a6] text-[13px]">{{ getFileExtension(row.fileName)
            }}文件</span>
          </template>
        </Table>
      </div>

      <!-- 加载中包裹 -->
      <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="p-4" />

      <!-- 无数据底盘：区分搜索无果、筛选无果与目录为空 -->
      <div v-else
        class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16 transition-all duration-300"
        @dragover.prevent="(!fileNameFuzzy && !isAdvancedFiltered) ? isDragOver = true : null"
        @dragleave.prevent="(!fileNameFuzzy && !isAdvancedFiltered) ? isDragOver = false : null"
        @drop.prevent="(!fileNameFuzzy && !isAdvancedFiltered) ? handleDrop($event) : null"
        :class="isDragOver && (!fileNameFuzzy && !isAdvancedFiltered) ? 'bg-blue-50/50 dark:bg-blue-900/20 border-2 border-dashed border-[#007AFF] rounded-2xl m-2' : ''">

        <!-- 状态 1：搜索无结果空背景 -->
        <template v-if="fileNameFuzzy">
          <Icon iconName="no_data" :width="130" fit="fill" class="opacity-60 grayscale transition-all"></Icon>
          <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">
            未能找到与 "<span class="text-[#007AFF] font-semibold mx-1">{{ fileNameFuzzy }}</span>" 匹配的文件
          </div>
        </template>

        <!-- 状态 2：高级筛选无结果空背景 -->
        <template v-else-if="isAdvancedFiltered">
          <Icon iconName="no_data" :width="130" fit="fill" class="opacity-60 grayscale transition-all"></Icon>
          <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">
            未找到符合条件的文件
          </div>
        </template>

        <!-- 状态 3：常规空目录 (含拖拽响应与上传按钮) -->
        <template v-else>
          <!-- 当文件悬浮时改变图标表现层 -->
          <Icon iconName="no_data" :width="130" fit="fill" class="transition-all duration-300"
            :class="isDragOver ? 'opacity-100 scale-110' : 'opacity-80'"></Icon>

          <!-- 动态引导文案 -->
          <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium transition-colors"
            :class="isDragOver ? 'text-[#007AFF] font-bold' : ''">
            {{ isDragOver ? '松开鼠标极速上传文件' : '当前列表为空，点击上传或拖拽上传文件' }}
          </div>

          <div class="mt-6 flex flex-wrap gap-5">
            <el-upload :show-file-list="false" :with-credentials="true" :multiple="true" :http-request="addFile"
              :accept="fileAccept">
              <div
                class="w-[110px] h-[110px] rounded-2xl bg-gray-50/80 dark:bg-[#2c2c2e]/60 border border-[#e5e5e9] dark:border-[#38383a] flex flex-col items-center justify-center cursor-pointer hover:border-[#007AFF] hover:shadow-sm transition-all group">
                <Icon iconName="file" :width="45" class="transition-transform group-hover:scale-105"></Icon>
                <span
                  class="text-[13px] font-semibold mt-3 text-gray-600 dark:text-gray-300 group-hover:text-[#007AFF]">上传文件</span>
              </div>
            </el-upload>

            <div v-if="category == 'all'" @click="newFolder"
              class="w-[110px] h-[110px] rounded-2xl bg-gray-50/80 dark:bg-[#2c2c2e]/60 border border-[#e5e5e9] dark:border-[#38383a] flex flex-col items-center justify-center cursor-pointer hover:border-[#007AFF] hover:shadow-sm transition-all group">
              <Icon iconName="folder" :width="48" class="transition-transform group-hover:scale-105"></Icon>
              <span
                class="text-[13px] font-semibold mt-3 text-gray-600 dark:text-gray-300 group-hover:text-[#007AFF]">新建目录</span>
            </div>
          </div>
        </template>
      </div>

    </div>

    <!-- 弹窗合集 (保持不变) -->
    <div>
      <Dialog :show="deleteDialogConfig.show" :title="deleteDialogConfig.title" :buttons="deleteDialogConfig.buttons"
        width="500px" :showCancel="true" @close="handleDialogClose" :showCustomTitle="true">
        <div class="flex flex-col items-center py-6">
          <div class="w-16 h-16 rounded-full bg-[#fff2f0] dark:bg-red-900/30 flex items-center justify-center mb-5">
            <img src="@/assets/icon-image/warning.png" class="w-10 h-10 rounded-full bg-white object-cover">
          </div>
          <div class="text-[17px] font-medium text-[#1d1d1f] dark:text-[#f5f5f7] mb-2 tracking-wide">确定删除所选的文件吗？</div>
          <div class="text-[13px] text-[#86868b] dark:text-[#a1a1a6]">删除的文件可在10天内通过回收站还原</div>
        </div>
      </Dialog>
      <FolderSelect ref="folderSelectRef" @folderSelect="moveFolderDone"></FolderSelect>
      <!-- 文件预览 -->
      <Preview ref="PreviewRef"></Preview>
      <!-- 文件分享 -->
      <ShareFile ref="shareRef"></ShareFile>
    </div>
  </div>
</template>

<script setup>
import OpButton from "@/components/Button/OpButton.vue";
import ShareFile from "./ShareFile.vue";
import FileFilter from "@/components/Business/FileFilter.vue"; // 🔴 导入高级筛选组件
import CategoryInfo from "@/js/CategoryInfo";
import SkeletonLoader from "@/components/Common/SkeletonLoader.vue";
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue"
const { proxy } = getCurrentInstance();

const fileAccept = computed(() => {
  const categoryItem = CategoryInfo[category.value]
  return categoryItem ? categoryItem.accept : "*"
});
const api = {
  loadDataList: '/file/loadDataList',
  rename: '/file/rename',
  newFolder: '/file/newFolder',
  delFile: '/file/delFile',
  changeFileFolder: '/file/changeFileFolder',
  createDownloadUrl: '/file/createDownloadUrl',
  download: '/api/file/download'
}

const columns = computed(() => [
  {
    label: fileNameColumnLabel.value,
    prop: 'fileName',
    scopedSlots: 'fileName',
    align: 'left',
    sortable: true
  },
  {
    label: '大小',
    prop: 'fileSize',
    scopedSlots: 'fileSize',
    width: 200,
    align: 'center',
    sortable: true
  },
  {
    label: '类型',
    prop: 'fileType',
    scopedSlots: 'fileType',
    width: 200,
    align: 'center',
    sortable: true
  },
  {
    label: '修改时间',
    prop: 'lastUpdateTime',
    width: 300,
    align: 'center',
    sortable: true
  }
]);

const tableData = ref({})
const tableOptions = ref({
  tableHeight: '100%',
  extHeight: 50,
  selectType: 'checkbox',
  infiniteScroll: true
})
const fileNameFuzzy = ref()
const category = ref()

const currentFilters = ref({})
const handleFilterChange = (payload) => {
  currentFilters.value = payload
  loadDataList(false) // 筛选条件变更后，重置分页并刷新列表
}

const isAdvancedFiltered = computed(() => {
  const f = currentFilters.value;
  if (!f) return false;
  return !!(f.fileCategory || f.createTimeStart || f.createTimeEnd || f.fileSizeMin || f.fileSizeMax);
});

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
    fileNameFuzzy: fileNameFuzzy.value,
    filePid: currentFolder.value.fileId,
    category: category.value
  }

  // 🔴 增补：将高级筛选组件抛出的参数合并到后端请求对象中
  Object.assign(params, currentFilters.value);

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

// 获取文件后缀
const getFileExtension = (fileName) => {
  if (!fileName) return '未知类型';
  const lastDotIndex = fileName.lastIndexOf('.');
  if (lastDotIndex === -1) return '未知类型';
  return fileName.substring(lastDotIndex + 1).toUpperCase();
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

// 计算属性：动态文件名列标题
const fileNameColumnLabel = computed(() => {
  if (selectFileList.value.length === 0) {
    return '文件名';
  } else {
    // 计算文件和文件夹的数量
    const selectedRows = tableData.value.list ? tableData.value.list.filter(item =>
      selectFileList.value.includes(item.fileId)) : [];

    const folderCount = selectedRows.filter(item => item.folderType === 1).length;
    const fileCount = selectedRows.length - folderCount;

    let label = `已选中${selectedRows.length}个`;

    if (fileCount > 0 && folderCount > 0) {
      label += `(${fileCount}文件/${folderCount}文件夹)`;
    } else if (fileCount > 0) {
      label += '文件';
    } else if (folderCount > 0) {
      label += '文件夹';
    }
    return label;
  }
});

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
  selectFileList.value.push(row.fileId);

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
    url: api.createDownloadUrl + '/' + row.fileId,
  })
  if (!result) {
    return;
  }
  window.location.href = api.download + '/' + result.data;
};

// 编辑行
const editing = ref(false)
const editNameRef = ref()
// 新建文件夹
const newFolder = () => {
  if (editing.value) {
    return
  }
  tableData.value.list.forEach((element) => {
    element.showEdit = false
  })
  editing.value = true
  tableData.value.list.unshift({
    showEdit: true,
    fileType: 0,
    fileId: "",
    filePid: currentFolder.value.fileId,
  })
  nextTick(() => {
    editNameRef.value.focus()
  })
}

// 取消行编辑操作
const cancelNameEdit = (index, event) => {
  if (event) {
    event.stopPropagation();
  }
  const fileData = tableData.value.list[index]
  // 有fileId 表示为重命名操作
  if (fileData.fileId) {
    fileData.showEdit = false
  } else {
    // 否则为新建目录操作
    tableData.value.list.splice(index, 1)
  }
  editing.value = false
}

// 确认行编辑操作
const saveNameEdit = async (index) => {
  const { fileId, filePid, fileNameReal, originalFileNameReal } = tableData.value.list[index]
  if (fileNameReal == '' || fileNameReal.indexOf("/") != -1) {
    proxy.Message.warning('文件名不能为空且不能包含"/"');
    return
  }
  // 如果是重命名操作且文件名没有变化，直接退出编辑模式
  if (fileId != "" && originalFileNameReal && fileNameReal === originalFileNameReal) {
    tableData.value.list[index].showEdit = false
    editing.value = false
    return
  }
  let url = api.rename
  // fileId为空，表示新建文件夹
  if (fileId == "") {
    url = api.newFolder
  }
  let result = await proxy.Request({
    url: url,
    params: {
      fileId: fileId,
      filePid: filePid,
      fileName: fileNameReal
    }
  })
  if (!result) {
    return;
  }
  //提示操作信息
  if (fileId == "") {
    proxy.Message.success("创建文件夹成功")
  } else {
    proxy.Message.success("重命名成功")
  }
  tableData.value.list[index] = result.data
  editing.value = false
}

// 重命名行
const editFileName = (index) => {
  if (tableData.value.list[0].fileId == '') {
    tableData.value.list.splice(0, 1)
    index = index - 1
  }
  tableData.value.list.forEach(element => {
    element.showEdit = false
  })
  let currentData = tableData.value.list[index]
  currentData.showEdit = true
  // 编辑文件名字
  if (currentData.folderType == 0) {
    // 获取文件名
    currentData.fileNameReal = currentData.fileName.substring(0, currentData.fileName.lastIndexOf('.'))
    //获取后缀
    currentData.fileSuffix = currentData.fileName.substring(currentData.fileName.lastIndexOf('.') + 1)
    // 保存原始文件名
    currentData.originalFileNameReal = currentData.fileNameReal
  } else {
    currentData.fileNameReal = currentData.fileName
    currentData.fileSuffix = ''
    currentData.originalFileNameReal = currentData.fileNameReal
  }
  editing.value = true
  nextTick(() => {
    editNameRef.value.focus()
  })
}

const emit = defineEmits(['addFile'])
// 上传文件
const addFile = (fileData) => {
  emit('addFile', { file: fileData.file, filePid: currentFolder.value.fileId })
}

// ---------------- 新增：拖拽上传响应逻辑 ----------------
const isDragOver = ref(false);

const handleDrop = (e) => {
  isDragOver.value = false;
  // 获取浏览器拖入产生的文件对象
  const files = e.dataTransfer.files;
  if (!files || files.length === 0) return;

  // 遍历多个文件并利用现有的 addFile 口子分发上传
  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    // 修复：原生文件对象没有 ElUpload 提供的uid，必须手动生成一个唯一 id 供 Uploader 追踪进度
    if (!file.uid) {
      file.uid = new Date().getTime() + i + Math.random().toString(36).substr(2, 5);
    }
    addFile({ file: file });
  }
};
// ---------------------------------------------------

// 添加文件回调
const reload = () => {
  showLoading.value = false;
  loadDataList();
}

// 搜索文件方法，供父组件调用
const searchFiles = (keyword) => {
  fileNameFuzzy.value = keyword;
  loadDataList(false);
}

defineExpose({
  reload,
  searchFiles
})

// 移动文件选择目标位置
const folderSelectRef = ref()
// 当前目录
const currentFolder = ref({ fileId: "0" })
const currentMoveFile = ref({})
const moveFolder = (data) => {
  currentMoveFile.value = data
  folderSelectRef.value.showFolderDialog(currentFolder.value.fileId)
}
// 批量移动文件
const moveFileBatch = () => {
  currentMoveFile.value = {}
  folderSelectRef.value.showFolderDialog(currentFolder.value.fileId)
}
const moveFolderDone = async (folderId) => {
  if (currentFolder.value.fileId == folderId) {
    proxy.Message.warning("文件已在该目录下")
    return
  }
  let fileIdsArray = []
  if (currentMoveFile.value.fileId) {
    fileIdsArray.push(currentMoveFile.value.fileId)
  } else {
    fileIdsArray = fileIdsArray.concat(selectFileList.value)
  }
  let result = await proxy.Request({
    url: api.changeFileFolder,
    params: {
      fileIds: fileIdsArray.join(","),
      filePid: folderId
    }
  })
  if (!result) {
    return;
  }
  proxy.Message.success("文件移动成功")
  selectFileList.value = [];
  folderSelectRef.value.close()
  loadDataList()
}

const navigationRef = ref()
const PreviewRef = ref()
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
  // 文件
  if (data.status != 2) {
    proxy.Message.warning('文件正在转码中，请稍后再试');
    return
  }
  PreviewRef.value.showReview(data, 0)
}

// 导航栏改变
const navChange = (data) => {
  const { categoryId, curFolder } = data
  currentFolder.value = curFolder
  category.value = categoryId

  // 清空选中的文件列表
  selectFileList.value = []
  fileNameFuzzy.value = ''
  loadDataList()
}

// 分享相关
const shareRef = ref()
const share = (row) => {
  shareRef.value.show(row)
}

</script>

<style lang="scss" scoped>
/* 整个表格容器 */
.apple-table-container {
  @apply relative overflow-hidden bg-transparent;
}

/* 核心变量覆盖与透视化处理 */
:deep(.el-table) {
  /* 强制透明底层 */
  --el-table-bg-color: transparent !important;
  --el-table-tr-bg-color: transparent !important;
  --el-table-header-bg-color: transparent !important;

  /* 消除生硬边缘 */
  --el-table-border-color: rgba(134, 134, 139, 0.15) !important;
  background-color: transparent !important;
  border: none !important;

  /* 默认悬浮体验：极简浅灰色覆盖 */
  --el-table-row-hover-bg-color: rgba(0, 0, 0, 0.04) !important;

  &::before,
  &::after {
    display: none !important;
  }

  .el-table__inner-wrapper {
    &::before {
      display: none !important;
    }
  }

  /* 表头极简处理 */
  th.el-table__cell {
    @apply text-[12px] font-semibold tracking-wider text-[#86868b] dark:text-gray-400 py-2.5 border-b border-[#e5e5e9]/70 dark:border-[#38383a]/70 !important;
    background-color: transparent !important;
  }

  /* 数据行内分割线与丝滑颜色过渡 */
  td.el-table__cell {
    @apply py-[11px] text-[13.5px] border-b border-gray-100 dark:border-[#38383a]/60 !important;
    background-color: transparent !important;
    /* 增加 CSS 过渡，让悬浮和选中色块更加平滑柔和 */
    transition: background-color 0.25s ease-in-out, border-color 0.25s ease-in-out !important;
  }

  /* 最后一行无底边框，与外部容器融合 */
  tr:last-child td.el-table__cell {
    border-bottom: none !important;
  }

  /* 空数据背景透传 */
  .el-table__empty-block {
    background-color: transparent !important;
  }

  /* ========================================================= */
  /* 苹果级悬浮 (Hover) 和选中 (Active/Selected) 视觉重构 */
  /* ========================================================= */
  .el-table__body {

    /* 普通情况的悬浮色 (覆盖自带 hover) */
    tr:hover>td.el-table__cell,
    tr.hover-row>td.el-table__cell {
      background-color: var(--el-table-row-hover-bg-color) !important;
    }

    /* 选中态：利用 CSS 截获 Element Plus 多选框状态或单选行，渲染纯正苹果蓝微透明背板 */
    tr.current-row>td.el-table__cell,
    tr:has(.el-checkbox__input.is-checked)>td.el-table__cell {
      background-color: rgba(0, 122, 255, 0.08) !important;
      /* 选中时淡化下划线，让整行色块更通透聚合 */
      border-bottom-color: rgba(0, 122, 255, 0.1) !important;
    }

    /* 选中态进一步触发的悬浮效果 (加深颜色反馈) */
    tr.current-row:hover>td.el-table__cell,
    tr:has(.el-checkbox__input.is-checked):hover>td.el-table__cell {
      background-color: rgba(0, 122, 255, 0.12) !important;
    }
  }
}

/* 适配暗黑模式的高亮与边界线反调 */
html.dark :deep(.el-table) {
  --el-table-row-hover-bg-color: rgba(255, 255, 255, 0.12) !important;
  --el-table-border-color: rgba(255, 255, 255, 0.12) !important;

  .el-table__body {

    /* 暗黑模式下：选中的苹果蓝需要相对明亮一点以保障可视度 */
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
  @apply flex items-center justify-center py-4 text-[12px] text-[#86868b] font-medium tracking-wide bg-transparent;

  .el-icon {
    @apply mr-1.5;
  }
}

/* 传统分页器隐藏背板，转化为文本质感 */
.pagination-container {
  @apply flex justify-end items-center pt-3 pb-2 w-full bg-transparent;

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
      @apply text-[#86868b] dark:text-gray-500 text-[13px];
    }
  }
}
</style>
