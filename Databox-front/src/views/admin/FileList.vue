<template>
  <div>
    <div class="top" style="margin-top: 20px;">
      <div class="top-op">
        <div class="btn-group">
          <el-button type="danger" :disabled="selectFileList.length === 0" @click="delFileBatch" class="custom-btn">
            <span class="iconfont icon-del"></span>
            删除
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
            <!-- fileType：1视频 3：图片 -->
            <template v-if="(row.fileType == 3 || row.fileType == 1)">
              <Icon :cover="row.fileCover" :width="32"></Icon>
            </template>
            <template v-else>
              <!-- folderType:0: 文件 1:目录 -->
              <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
              <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
            </template>
            <span class="file-text" :title="row.fileName">
              <span class="file-name-1" @click="preview(row)">{{ row.fileName }}</span>
            </span>
            <span class="op">
              <template v-if="row.showOp">
                <span class="iconfont icon-download" @click.stop="handleDownloadClick(row)">下载</span>
                <span class="iconfont icon-del" @click.stop="delFile(index)">删除</span>
              </template>
            </span>
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
        <!-- 添加用户信息列 -->
        <template #userName="{ index, row }">
          <span>{{ row.userName || '-' }}</span>
        </template>
      </Table>
    </div>
    <!-- 骨架屏 -->
    <SkeletonLoader v-else-if="isLoading" :rowCount="14" />
    <!-- 无数据显示 -->
    <div class="no-data" v-else>
      <div class="no-data-inner">
        <Icon iconName="no_data" :width="150" fit="fill"></Icon>
        <div class="tips">当前列表为空</div>
      </div>
    </div>
    <!-- 删除确认对话框 -->
    <Dialog :show="deleteDialogConfig.show" :title="deleteDialogConfig.title" :buttons="deleteDialogConfig.buttons"
      width="500px" :showCancel="true" @close="handleDialogClose" :showCustomTitle="true">
      <div class="delete-confirm-content">
        <div class="icon-container">
          <img src="@/assets/icon-image/warning.png" style="background-color:white">
        </div>
        <div class="message">确定删除所选的文件吗？</div>
        <div class="sub-message">删除后将无法恢复</div>
      </div>
    </Dialog>
    <!-- 文件预览 -->
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
    sortable:true
  },
  {
    label: '大小',
    prop: 'fileSize',
    scopedSlots: 'fileSize',
    width: 150,
    align: 'center',
    sortable:true
  },
  {
    label: '类型',
    prop: 'fileType',
    scopedSlots: 'fileType',
    width: 150,
    align: 'center',
    sortable:true
  },
  {
    label: '所属用户',
    prop: 'userName',
    scopedSlots: 'userName',
    width: 150,
    align: 'center',
    sortable:true
  },
  {
    label: '上传时间',
    prop: 'createTime',
    width: 200,
    align: 'center',
    sortable:true
  },
  {
    label: '修改时间',
    prop: 'lastUpdateTime',
    width: 200,
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

<style lang="scss" scoped>
@use "../../assets/main.scss" as *;

.delete-confirm-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;

  .icon-container {
    margin-bottom: 15px;

    img {
      width: 60px;
      height: 60px;
    }
  }

  .message {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
    color: #303133;
  }

  .sub-message {
    font-size: 14px;
    color: #909399;
  }
}
</style>