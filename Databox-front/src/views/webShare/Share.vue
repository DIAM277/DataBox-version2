<template>
  <div class="share-page">
    <div class="share">
      <div class="header">
        <div class="header-content">
          <AppTitle2 title="DataBox" @click.stop="jumpToHome" />
          <div class="user-info" v-if="currentUserInfo">
            <div class="avatar">
              <Avatar :userId="currentUserInfo.userId" :userName="currentUserInfo.userName"
                :userEmail="currentUserInfo.email" :showActions="false">
              </Avatar>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="share-body">
      <template v-if="Object.keys(shareInfo).length == 0">
        <div class="loading" v-loading="Object.keys(shareInfo).length == 0"></div>
      </template>
      <template v-else>
        <div class="share-panel">
          <div class="share-user-info">
            <div class="avatar">
              <Avatar :userId="shareInfo.userId" :interactive="false" :width="60"></Avatar>
            </div>
            <div class="share-info">
              <div class="user-info">
                <span class="user-name">{{ shareInfo.userName }}</span>
                <span class="share-time">分享于：{{ shareInfo.shareTime }}</span>
              </div>
              <div class="file-name">分享文件：{{ shareInfo.fileName }}</div>
            </div>
            <div class="share-op-btn">
              <el-button v-if="shareInfo.currentUser" type="primary" @click="cancelShare">
                <span class="iconfont icon-cancel"></span>
                取消分享
              </el-button>
              <el-button v-else type="primary" @click="save2MyPan" :disabled="selectFileList.length == 0">
                <span class="iconfont icon-import"></span>
                保存到我的网盘
              </el-button>
            </div>
          </div>
          <!-- 导航组件 -->
          <div class="navigation-container">
            <Navigation ref="navigationRef" @navChange="navChange" :shareId="shareId"></Navigation>
          </div>
          <!-- 文件列表 -->
          <div class="file-container">
            <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
              <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList"
                :initFetch="false" :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
                :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">
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
                        <span class="iconfont icon-import" @click.stop="sava2MyPanSingle(index)"
                          v-if="row.showOp && !shareInfo.currentUser">保存到我的网盘</span>
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
          </div>
        </div>
      </template>
      <!-- 保存目录选择 -->
      <FolderSelect ref="folderSelectRef" @folderSelect="save2MyPanDone"></FolderSelect>
      <Preview ref="PreviewRef"></Preview>
      <!-- 取消分享确认对话框 -->
      <Dialog :show="cancelDialogConfig.show" :title="cancelDialogConfig.title" :buttons="cancelDialogConfig.buttons"
        width="500px" :showCancel="true" @close="handleDialogClose" :showCustomTitle="true">
        <div class="delete-confirm-content">
          <div class="icon-container">
            <img src="@/assets/icon-image/warning.png" style="background-color:white">
          </div>
          <div class="message">确定取消分享吗？</div>
          <div class="sub-message">取消后，分享链接将立即失效</div>
        </div>
      </Dialog>
    </div>
  </div>
</template>

<script setup>
import FolderSelect from "@/components/FolderSelect.vue";
import AppTitle2 from "@/components/AppTitle/AppTitle2.vue";
import SkeletonLoader from "@/components/SkeletonLoader.vue";
import { ref, reactive, getCurrentInstance, nextTick, computed, onMounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from 'vue-router';
const route = useRoute();
const router = useRouter();

const api = {
  getShareLoginInfo: '/showShare/getShareLoginInfo',
  loadFileList: '/showShare/loadFileList',
  createDownloadUrl: '/showShare/createDownloadUrl',
  download: '/api/showShare/download',
  cancelShare: '/share/cancelShare',
  saveShare: '/showShare/saveShare'
}

const currentUserInfo = proxy.VueCookies.get("userInfo")

const shareId = route.params.shareId;
const shareInfo = ref({})
// 获取分享信息
const getShareInfo = async () => {
  let result = await proxy.Request({
    url: api.getShareLoginInfo,
    showLoading: false,
    params: {
      shareId: shareId
    }
  })
  if (!result) {
    return;
  }
  if (result.data == null) {
    router.push(`/shareCheck/${shareId}`)
    return
  }
  shareInfo.value = result.data
  loadDataList()
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

// 表项
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
    label: '修改时间',
    prop: 'lastUpdateTime',
    width: 200,
    align: 'center',
    sortable: true
  }
]);

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
const tableData = ref({})
const tableOptions = ref({
  extHeight: 80,
  selectType: 'checkbox',
  infiniteScroll: true
})

// 当前目录
const currentFolder = ref({ fileId: "0" })
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
    shareId: shareId,
    filePid: currentFolder.value.fileId,
  }
  // 添加排序参数
  // if (sortConfig.value.prop && sortConfig.value.order) {
  //   params.sortField = sortConfig.value.prop;
  //   params.sortOrder = sortConfig.value.order === 'ascending' ? 'asc' : 'desc';
  // }
  // 发送请求
  let result = await proxy.Request({
    showLoading: false,
    url: api.loadFileList,
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

const selectFileList = ref([])
// 导航栏改变
const navChange = (data) => {
  const { curFolder } = data
  currentFolder.value = curFolder
  // 清空选中的文件列表
  selectFileList.value = []
  loadDataList()
  console.log(shareInfo.value)
}
// 多选回调
const rowSelected = (rows) => {
  selectFileList.value = []
  rows.forEach(item => {
    selectFileList.value.push(item.fileId)
  })
}

// 获取文件后缀
const getFileExtension = (fileName) => {
  if (!fileName) return '未知类型';
  const lastDotIndex = fileName.lastIndexOf('.');
  if (lastDotIndex === -1) return '未知类型';
  return fileName.substring(lastDotIndex + 1).toUpperCase();
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
  data.shareId = shareId
  // 文件预览
  PreviewRef.value.showReview(data, 2)
}

// 处理下载按钮点击事件
const handleDownloadClick = async (row) => {
  if (row.folderType == 1) {
    proxy.Message.warning('不支持直接下载文件夹');
    return;
  }
  // 文件下载
  let result = await proxy.Request({
    url: api.createDownloadUrl + '/' + shareId + '/' + row.fileId,
  })
  if (!result) {
    return;
  }
  window.location.href = api.download + '/' + result.data;
};

// 文件夹选择
const folderSelectRef = ref()
const save2MyPanFileIdArray = ref([])
// 检查用户是否已登录
const checkUserLogin = () => {
  if (!proxy.VueCookies.get("userInfo")) {
    proxy.Message.warning('请先登录');
    router.push('/login?redirectUrl=' + route.path)
    return false;
  }
  return true;
}
// 保存到我的网盘(批量)
const save2MyPan = () => {
  if (selectFileList.value.length == 0) {
    proxy.Message.warning('请选择要保存的文件');
    return;
  }
  // 首先检查用户是否已登录
  if (!checkUserLogin()) {
    return;
  }
  save2MyPanFileIdArray.value = selectFileList.value
  folderSelectRef.value.showFolderDialog()
}
// 保存到我的网盘(单文件)
const sava2MyPanSingle = (row) => {
  // 首先检查用户是否已登录
  if (!checkUserLogin()) {
    return;
  }
  save2MyPanFileIdArray.value = [tableData.value.list[row].fileId]
  folderSelectRef.value.showFolderDialog()
}

// 保存到网盘回调
const save2MyPanDone = async (folderId) => {
  let result = await proxy.Request({
    url: api.saveShare,
    params: {
      shareId: shareId,
      shareFileIds: save2MyPanFileIdArray.value.join(','),
      myFolderId: folderId
    }
  })
  if (!result) {
    return;
  }
  loadDataList();
  proxy.Message.success('保存成功');
  folderSelectRef.value.close()
}

// 取消分享
const cancelShare = () => {
  cancelDialogConfig.value.show = true;
}
// 取消分享对话框配置
const cancelDialogConfig = ref({
  show: false,
  title: "取消分享",
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

// 确认取消分享操作
const confirmCancelShare = async () => {
  let result = await proxy.Request({
    url: api.cancelShare,
    params: {
      shareIds: shareId
    }
  })
  if (!result) {
    return;
  }
  cancelDialogConfig.value.show = false;
  proxy.Message.success('取消分享成功');
  // 取消分享后返回主页
  router.push('/');
}

// 监听对话框关闭事件
const handleDialogClose = () => {
  // 关闭对话框
  cancelDialogConfig.value.show = false;
}

const jumpToHome = () => {
  router.push('/')
}

onMounted(() => {
  getShareInfo()
  initTableData()
})
</script>

<style lang="scss" scoped>
@use "../../assets/main.scss" as *;

.share-page {
  background: linear-gradient(135deg, #c3cfe2 0%, #f5f7fa 100%);
  min-height: 100vh;
  user-select: none;
  display: flex;
  flex-direction: column;
}

.header {
  background: #c3cfe2;
  box-shadow: 0 3px 15px 0 rgba(0, 0, 0, 0.25);
  height: 60px;
  padding: 0 24px;
  position: relative;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  transition: all 0.3s ease;

  .header-content {
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: flex-start;
    width: 95%;

    .user-info {
      display: flex;
      align-items: center;
      margin-left: auto;
      margin-right: 10px;

      .avatar {
        cursor: pointer;
        transition: transform 0.2s ease;

        &:hover {
          transform: scale(1.05);
        }
      }
    }
  }
}

.share-body {
  width: 80%;
  max-width: 1500px;
  margin: 0 auto;
  padding-top: 30px;
  flex: 1;

  @media (max-width: 768px) {
    width: 95%;
    padding-top: 30px;
  }

  .loading {
    height: 300px;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .share-panel {
    background-color: rgba(255, 255, 255, 0.116);
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    padding: 24px;
    margin-top: 0px;
    display: flex;
    flex-direction: column;
    transition: transform 0.3s ease, box-shadow 0.3s ease;

    @media (max-width: 768px) {
      padding: 16px;
    }

    .share-user-info {
      display: flex;
      align-items: center;
      margin-bottom: 20px;

      .avatar {
        margin-right: 16px;
        transition: transform 0.2s ease;

        &:hover {
          transform: scale(1.1);
        }
      }

      .share-info {
        flex: 1;

        .user-info {
          display: flex;
          align-items: center;
          flex-wrap: wrap;
          gap: 10px;

          .user-name {
            font-size: 18px;
            font-weight: 600;
            color: #303133;
          }

          .share-time {
            font-size: 14px;
            color: #909399;
            font-style: italic;
          }
        }

        .file-name {
          margin-top: 10px;
          font-size: 14px;
          color: #606266;
          display: flex;
          align-items: center;

          &::before {
            content: '';
            display: inline-block;
            width: 16px;
            height: 16px;
            background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%23606266"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6zm-1 1v5h5v10H6V3h7z"/></svg>');
            background-size: contain;
            margin-right: 6px;
          }
        }
      }
    }

    .share-op-btn {
      display: flex;
      justify-content: flex-end;
      margin-bottom: 20px;

      .el-button {
        padding: 12px 24px;
        border-radius: 8px;
        width: 140px;
        font-weight: 600;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .iconfont {
          margin-right: 3px;
          font-size: 16px;
        }

        &:disabled {
          opacity: 0.7;
          cursor: not-allowed;
        }
      }
    }

    .navigation-container {
      border-top: 1px solid rgba(0, 0, 0, 0.05);
      padding-top: 10px;
    }

    .file-container {
      width: 100%;
    }
  }
}

.file-list {
  background-color: transparent;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;

  .file-name {
    display: flex;
    align-items: center;
    position: relative;
    padding: 8px 0;

    .file-text {
      margin-left: 10px;
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;

      .file-name-1 {
        cursor: pointer;
        transition: color 0.2s ease;

        &:hover {
          color: #409EFF;
          text-decoration: underline;
        }
      }
    }

    .op {
      position: absolute;
      right: 5px;
      display: flex;
      gap: 15px;

      .iconfont {
        cursor: pointer;
        padding: 5px 8px;
        border-radius: 4px;
        font-size: 14px;
        transition: all 0.2s ease;
        color: #606266;

        &:hover {
          background-color: #f0f2f5;
          color: #409EFF;
        }

        &.icon-download:hover {
          color: #409EFF;
        }

        &.icon-import:hover {
          color: #E6A23C;
        }
      }
    }
  }
}

/* 无数据样式 */
.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;

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
</style>