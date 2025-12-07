<template>
  <div>
    <!-- 顶部按钮操作栏 -->
    <div class="top" style="margin-top: 20px;">
      <div class="top-op">
        <div class="btn-group">
          <el-button type="success" @click="revertBatch" class="custom-btn" :disabled="selectFileList.length === 0">
            <span class="iconfont icon-revert"></span>
            还原文件
          </el-button>
          <el-button type="danger" class="custom-btn refresh-btn" @click="delFileBatch"
            :disabled="selectFileList.length === 0">
            <span class="iconfont icon-del"></span>
            彻底删除
          </el-button>
          <el-button type="info" class="custom-btn refresh-btn" @click="loadDataList(false)">
            <span class="iconfont icon-refresh"></span>
            刷新
          </el-button>
        </div>
      </div>
    </div>
    <!-- 数据表格 -->
    <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
      <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
        :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
        :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">
        <template #fileName="{ index, row }">
          <div class="file-item" @mouseenter="showOp(row)" @mouseleave="cancelShowOp(row)">
            <template v-if="(row.fileType == 3 || row.fileType == 1) && row.status !== 0">
              <Icon :cover="row.fileCover" :width="32"></Icon>
            </template>
            <template v-else>
              <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
              <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
            </template>
            <span class="file-text" :title="row.fileName">
              <span class="file-name-1">{{ row.fileName }}</span>
            </span>
            <span class="op">
              <template v-if="row.showOp">
                <span class="iconfont icon-link" @click="revert(row)">还原</span>
                <span class="iconfont icon-cancel" @click="delFile(row)">删除</span>
              </template>
            </span>
          </div>
        </template>
        <template #fileSize="{ index, row }">
          <span v-if="row.fileSize">{{ proxy.Utils.size2Str(row.fileSize) }}</span>
          <!-- 目录无大小 显示"-" -->
          <span v-else style="font-size: 20px;">-</span>
        </template>
      </Table>
    </div>
    <!-- 骨架屏 -->
    <SkeletonLoader v-else-if="isLoading" :rowCount="14" />
    <!-- 无数据提示 -->
    <div class="no-data" v-else>
      <div class="no-data-inner">
        <Icon iconName="no_data" :width="150" fit="fill"></Icon>
        <div class="tips">回收站中没有文件</div>
      </div>
    </div>
    <!-- 删除确认对话框 -->
    <Dialog :show="deleteDialogConfig.show" :title="deleteDialogConfig.title" :buttons="deleteDialogConfig.buttons"
      width="500px" :showCancel="true" @close="handleDialogClose" :showCustomTitle="true">
      <div class="delete-confirm-content">
        <div class="icon-container">
          <img src="@/assets/icon-image/warning.png" style="background-color:white">
        </div>
        <div class="message">确定彻底删除所选的文件吗？</div>
        <div class="sub-message">彻底删除后将无法恢复</div>
      </div>
    </Dialog>
    <!-- 还原确认对话框 -->
    <Dialog :show="revertDialogConfig.show" :title="revertDialogConfig.title" :buttons="revertDialogConfig.buttons"
      width="500px" :showCancel="true" @close="handleRevertDialogClose" :showCustomTitle="true">
      <div class="delete-confirm-content">
        <div class="icon-container">
          <img src="@/assets/icon-image/warning.png" style="background-color:white">
        </div>
        <div class="message">确定还原所选的文件吗？</div>
        <div class="sub-message">文件将被还原到原来的位置</div>
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

const columns = [
  {
    label: '文件名',
    prop: 'fileName',
    scopedSlots: 'fileName',
    align: 'left',
    sortable:true
  },
  {
    label: '删除时间',
    prop: 'recoveryTime',
    width: 300,
    align: 'center',
    sortable:true
  },
  {
    label: '大小',
    prop: 'fileSize',
    width: 300,
    align: 'center',
    scopedSlots: 'fileSize',
    sortable:true
  }
]

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
@use "../../assets/main.scss" as *;

.file-list {
  margin-top: 10px;

  .file-item {

    .file-text {
      font-size: 15px;
      color: #333;
      font-weight: 500;
      transition: all 0.3s;
      padding: 0 5px;
      border-radius: 4px;
      cursor: pointer;
      margin-left: 12px;
      flex: 1;
      overflow: hidden;

      span:first-child {
        // 只对文件名文本应用悬停效果
        padding: 2px 5px;
        border-radius: 4px;
        transition: all 0.3s;

        // 实现省略号效果
        display: inline-block; // 使max-width和text-overflow生效
        white-space: nowrap; // 防止文本换行
        overflow: hidden; // 隐藏超出部分
        text-overflow: ellipsis; // 显示省略号
        vertical-align: middle; // 垂直居中对齐
        max-width: 250px; // 设定最大宽度，超过此宽度将显示省略号

        &:hover {
          color: #409eff;
          background-color: rgba(64, 158, 255, 0.1);
        }
      }

      // 移除整个file-text的悬停效果
      &:hover {
        color: inherit;
        background-color: transparent;
      }
    }

    .op {
      position: absolute; // 使用绝对定位
      right: 10px; // 距离右侧20px
      top: 50%; // 垂直居中
      transform: translateY(-50%); // 垂直居中调整
      z-index: 2; // 确保操作栏在最上层

      .iconfont {
        font-size: 12px;
        margin-left: 15px;
        color: #06a7ff;
        cursor: pointer;
      }

      .iconfont::before {
        margin-right: 3px;
      }
    }
  }
}

/* 无数据样式 */
.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 50px;

  .no-data-inner {
    display: flex;
    flex-direction: column;
    align-items: center;

    .tips {
      margin-top: 20px;
      font-size: 16px;
      color: #909399;
    }
  }
}

.delete-confirm-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;

  .icon-container {
    margin-bottom: 20px;

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

/* 添加自定义按钮样式 */
.top-op {
  .btn-group {
    .custom-btn {
      &.el-button--success {
        background-image: linear-gradient(135deg, #52c41a, #237804);
        border: none;

        &:hover {
          background-image: linear-gradient(135deg, #73d13d, #389e0d);
        }

        &:disabled {
          background-image: linear-gradient(135deg, #b7e49d, #bdd3b4);
          opacity: 0.7;
          cursor: not-allowed;
        }
      }
    }
  }
}
</style>