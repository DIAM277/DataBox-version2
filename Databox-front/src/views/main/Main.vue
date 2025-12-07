<template>
  <div>
    <div class="top" style="margin-top: 20px;">
      <div class="top-op">
        <div class="btn-group">
          <div class="action-btn upload-btn">
            <el-upload :show-file-list="false" :with-credentials="true" :multiple="true" :http-request="addFile"
              :accept="fileAccept">
              <el-button type="primary" class="custom-btn">
                <span class="iconfont icon-upload"></span>
                上传
              </el-button>
            </el-upload>
          </div>
          <el-button type="success" @click="newFolder" class="custom-btn">
            <span class="iconfont icon-folder-add"></span>
            新建
          </el-button>
          <el-button type="danger" :disabled="selectFileList.length === 0" @click="delFileBatch" class="custom-btn">
            <span class="iconfont icon-del"></span>
            删除
          </el-button>
          <el-button type="warning" :disabled="selectFileList.length === 0" @click="moveFileBatch" class="custom-btn">
            <span class="iconfont icon-move"></span>
            移动
          </el-button>
          <el-button type="info" class="custom-btn refresh-btn" @click="loadDataList(false)">
            <span class="iconfont icon-refresh"></span>
            刷新
          </el-button>
        </div>
      </div>
      <!-- 文件导航栏 -->
      <Navigation ref="navigationRef" @navChange="navChange"></Navigation>
    </div>
    <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
      <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
        :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
        :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">
        <!-- 文件名称 -->
        <template #fileName="{ index, row }">
          <div class="file-name" @mouseenter="showOp(row)" @mouseleave="cancelShowOp(row)">
            <!-- fileType：1视频 3：图片 status:0:转码中 1：转码失败 2：转码成功使用中-->
            <template v-if="(row.fileType == 3 || row.fileType == 1) && row.status == 2">
              <Icon :cover="row.fileCover" :width="32"></Icon>
            </template>
            <template v-else>
              <!-- folderType:0: 文件 1:目录 -->
              <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
              <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
            </template>
            <span class="file-text" :title="row.fileName" v-if="!row.showEdit">
              <span class="file-name-1" @click="preview(row)">{{ row.fileName }}</span>
              <span v-if="row.status == 0" class="transfer-status">转码中</span>
              <span v-if="row.status == 1" class="transfer-status transfer-fail">转码失败</span>
            </span>
            <div class="edit-panel" v-if="row.showEdit">
              <el-input v-model.trim="row.fileNameReal" ref="editNameRef" :maxLength="190"
                @keyup.enter.stop="saveNameEdit(index)">
                <template #suffix>{{ row.fileSuffix }}</template>
              </el-input>
              <span :class="['iconfont icon-right1', row.fileNameReal ? '' : 'not-allow']"
                @click.stop="saveNameEdit(index)"></span>
              <span class="iconfont icon-error" @click="cancelNameEdit(index, $event)"></span>
            </div>
            <OpButton :fileData="row" :index="index" :showOp="row.showOp" @share="share" @download="handleDownloadClick"
              @delete="delFile" @rename="editFileName" @move="moveFolder" />
          </div>
        </template>
        <template #fileSize="{ index, row }">
          <span v-if="row.fileSize">{{ proxy.Utils.size2Str(row.fileSize) }}</span>
          <!-- 目录无大小 显示"-" -->
          <span v-else style="font-size: 20px;">-</span>
        </template>
        <!-- 文件类型插槽 -->
        <template #fileType="{ index, row }">
          <span v-if="row.folderType === 1">文件夹</span>
          <span v-else>
            {{ getFileExtension(row.fileName) }}文件
          </span>
        </template>
      </Table>
    </div>
    <!-- 骨架屏 -->
    <SkeletonLoader v-else-if="isLoading" :rowCount="14" />
    <!-- 无数据显示 -->
    <div class="no-data" v-else>
      <div class="no-data-inner">
        <Icon iconName="no_data" :width="150" fit="fill"></Icon>
        <div class="tips">当前列表为空，上传你的第一个文件吧</div>
        <div class="op-list">
          <el-upload :show-file-list="false" :with-credentials="true" :multiple="true" :http-request="addFile"
            :accept="fileAccept">
            <div class="op-item">
              <Icon iconName="file" :width="60"></Icon>
              <div>上传文件</div>
            </div>
          </el-upload>
          <div class="op-item" v-if="category == 'all'" @click="newFolder">
            <Icon iconName="folder" :width="60"></Icon>
            <div>新建目录</div>
          </div>
        </div>
      </div>
    </div>
    <Dialog :show="deleteDialogConfig.show" :title="deleteDialogConfig.title" :buttons="deleteDialogConfig.buttons"
      width="500px" :showCancel="true" @close="handleDialogClose" :showCustomTitle="true">
      <div class="delete-confirm-content">
        <div class="icon-container">
          <img src="@/assets/icon-image/warning.png" style="background-color:white">
        </div>
        <div class="message">确定删除所选的文件吗？</div>
        <div class="sub-message">删除的文件可在10天内通过回收站还原</div>
      </div>
    </Dialog>
    <FolderSelect ref="folderSelectRef" @folderSelect="moveFolderDone"></FolderSelect>
    <!-- 文件预览 -->
    <Preview ref="PreviewRef"></Preview>
    <!-- 文件分享 -->
    <ShareFile ref="shareRef"></ShareFile>
  </div>
</template>

<script setup>
import OpButton from "@/components/Button/OpButton.vue";
import ShareFile from "./ShareFile.vue";
import CategoryInfo from "@/js/CategoryInfo";
import SkeletonLoader from "@/components/SkeletonLoader.vue";
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
    sortable:true
  },
  {
    label: '大小',
    prop: 'fileSize',
    scopedSlots: 'fileSize',
    width: 200,
    align: 'center',
    sortable:true
  },
  {
    label: '类型',
    prop: 'fileType',
    scopedSlots: 'fileType',
    width: 200,
    align: 'center',
    sortable:true
  },
  {
    label: '修改时间',
    prop: 'lastUpdateTime',
    width: 300,
    align: 'center',
    sortable:true
  }
]);

const tableData = ref({})
const tableOptions = ref({
  extHeight: 50,
  selectType: 'checkbox',
  infiniteScroll: true
})
const fileNameFuzzy = ref()
const category = ref()
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

// 添加文件回调
const reload = () => {
  loadDataList()
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
  loadDataList()
}

// 分享相关
const shareRef = ref()
const share = (row) => {
  shareRef.value.show(row)
}

</script>

<style lang="scss" scoped>
@use "../../assets/main.scss" as *;
</style>
