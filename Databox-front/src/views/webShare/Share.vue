<template>
  <div
    class="h-screen w-screen flex flex-col bg-[#f5f5f7] dark:bg-[#000000] text-[#1d1d1f] dark:text-[#f5f5f7] font-sans overflow-hidden transition-colors duration-500">

    <!-- 独立顶部导航栏 (Header) -->
    <header
      class="h-16 flex-shrink-0 bg-white/70 dark:bg-[#1c1c1e]/70 backdrop-blur-xl border-b border-[#e5e5e9] dark:border-[#38383a] z-20 px-6 flex items-center justify-between transition-all duration-300">

      <!-- 左：Logo -->
      <div class="flex items-center gap-2.5 cursor-pointer group transition-opacity hover:opacity-80"
        @click.stop="jumpToHome">
        <svg class="w-8 h-8 text-[#007AFF] drop-shadow-sm transition-transform group-hover:scale-105"
          viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M17.5 19H6.5C3.46243 19 1 16.5376 1 13.5C1 10.6693 3.14081 8.34327 5.90802 8.03333C6.44299 4.60655 9.42168 2 12 2C15.1118 2 17.7554 4.08182 18.6655 6.94276C21.0963 7.29177 23 9.40059 23 12C23 15.866 19.866 19 17.5 19Z" />
        </svg>
        <span class="font-semibold text-xl text-[#1d1d1f] dark:text-[#f5f5f7] tracking-wider relative top-0.5">
          DataBox <span class="text-sm text-gray-400 dark:text-gray-500 font-normal ml-1">分享</span>
        </span>
      </div>

      <!-- 右：当前访客信息 -->
      <div v-if="currentUserInfo"
        class="flex items-center hover:scale-105 transition-transform duration-300 shadow-sm rounded-full">
        <Avatar :userId="currentUserInfo.userId" :userName="currentUserInfo.userName" :userEmail="currentUserInfo.email"
          :interactive="false" :showActions="false" :width="36"
          class="border flex-shrink-0 border-gray-100 dark:border-gray-700 rounded-full">
        </Avatar>
      </div>

    </header>

    <!-- 主体区域 -->
    <div class="flex-1 flex flex-col overflow-hidden relative">

      <!-- 加载期占位 -->
      <div v-if="Object.keys(shareInfo).length == 0"
        class="absolute inset-0 flex justify-center items-center h-full w-full z-10 bg-[#f5f5f7] dark:bg-[#000000]"
        v-loading="true"></div>

      <template v-else>
        <!-- 核心控制区 (分享者信息与主操作) -->
        <div
          class="px-4 sm:px-6 pt-6 pb-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 shrink-0 transition-all">

          <!-- 分享者信息 -->
          <div class="flex items-center gap-3.5">
            <Avatar :userId="shareInfo.userId" :interactive="false" :width="52"
              class="shadow-sm border border-black/5 dark:border-white/5 rounded-full hover:scale-105 transition-transform" />
            <div class="flex flex-col">
              <div class="flex items-baseline gap-2">
                <span class="text-[17px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-wide">{{
                  shareInfo.userName }}</span>
                <span class="text-[12px] font-medium text-[#86868b] dark:text-gray-500">分享于 {{ shareInfo.shareTime
                }}</span>
              </div>
              <span class="text-[13px] font-medium text-gray-500 dark:text-gray-400 mt-1 line-clamp-1 max-w-[400px]">
                <span
                  class="font-normal mr-1 border-gray-300 dark:border-gray-600 px-1 py-0.5 rounded text-[11px] bg-black/5 dark:bg-white/10 uppercase">文件</span>
                {{ shareInfo.fileName }}
              </span>
            </div>
          </div>

          <!-- 右侧：环境逻辑控制按钮 -->
          <div class="flex items-center gap-2.5 w-full sm:w-auto mt-2 sm:mt-0 select-none">
            <!-- 场景 A：创建者视角 -->
            <button v-if="shareInfo.currentUser" @click="cancelShare"
              class="w-full sm:w-auto px-4 py-2 rounded-xl text-[13px] font-semibold transition-all duration-200 shadow-sm border text-red-600 border-red-200 bg-red-50 hover:bg-red-100 dark:border-red-900/50 dark:bg-red-900/20 dark:hover:bg-red-900/40 flex items-center justify-center gap-1.5 focus:outline-none">
              <span class="iconfont icon-cancel leading-none text-[14px]"></span>取消公开分享
            </button>

            <!-- 场景 B：访客视角 -->
            <button v-else @click="save2MyPan" :disabled="selectFileList.length == 0"
              class="w-full sm:w-auto px-5 py-2.5 rounded-xl text-[13.5px] font-semibold transition-all duration-300 shadow-md flex items-center justify-center gap-1.5 focus:outline-none"
              :class="selectFileList.length === 0 ? 'opacity-50 cursor-not-allowed border-transparent text-white bg-blue-400' : 'text-white bg-[#007AFF] hover:bg-[#0066cc] border-transparent hover:shadow-lg hover:shadow-blue-500/20 active:scale-95'">
              <span class="iconfont icon-import leading-none text-[16px]"></span>保存到我的网盘
            </button>
          </div>
        </div>

        <!-- 苹果风底层卡片 (包含 面包屑导航 与 Table) -->
        <div
          class="flex-1 mx-4 sm:mx-6 mb-4 sm:mb-6 bg-white dark:bg-[#1c1c1e] rounded-2xl shadow-sm border border-gray-200/60 dark:border-gray-800 overflow-hidden relative flex flex-col transition-all">

          <!-- 面包屑内嵌工具栏 -->
          <div
            class="px-4 py-2 border-b border-gray-100 dark:border-gray-800/80 bg-gray-50/50 dark:bg-black/20 flex items-center overflow-x-auto no-scrollbar">
            <Navigation ref="navigationRef" @navChange="navChange" :shareId="shareId"></Navigation>
          </div>

          <!-- 原生表格展示区 -->
          <div class="flex-1 overflow-hidden" v-if="tableData.list && tableData.list.length > 0">
            <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList"
              :initFetch="false" :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
              :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">

              <!-- Core 重构：文件名与右侧悬浮悬浮操作 -->
              <template #fileName="{ index, row }">
                <div class="flex items-center relative group w-full" @mouseenter="showOp(row)"
                  @mouseleave="cancelShowOp(row)">
                  <template v-if="(row.fileType == 3 || row.fileType == 1)">
                    <Icon :cover="row.fileCover" :width="32"></Icon>
                  </template>
                  <template v-else>
                    <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
                    <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
                  </template>

                  <div
                    class="ml-3 flex-1 min-w-0 flex items-center sm:group-hover:pr-[120px] transition-all duration-300"
                    :title="row.fileName">
                    <span
                      class="text-[#1d1d1f] dark:text-[#f5f5f7] font-medium cursor-pointer hover:text-[#007AFF] transition-colors truncate"
                      @click="preview(row)">
                      {{ row.fileName }}
                    </span>
                  </div>

                  <!-- iOS 化：行内右侧绝对定位极简悬浮组 -->
                  <div v-if="row.showOp"
                    class="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-1 sm:gap-1.5 z-10 opacity-0 group-hover:opacity-100 transition-opacity duration-200">

                    <el-tooltip content="下载该文件" placement="top" effect="dark" :show-after="300">
                      <span
                        class="iconfont icon-download w-7 h-7 flex items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-[#007AFF] hover:shadow-sm transition-all cursor-pointer"
                        @click.stop="handleDownloadClick(row)"></span>
                    </el-tooltip>

                    <el-tooltip content="转存至我的网盘" placement="top" effect="dark" :show-after="300"
                      v-if="!shareInfo.currentUser">
                      <span
                        class="iconfont icon-import w-7 h-7 flex items-center justify-center rounded-md text-[14px] text-gray-400 dark:text-gray-500 hover:bg-white dark:hover:bg-gray-700 hover:text-emerald-500 hover:shadow-sm transition-all cursor-pointer"
                        @click.stop="sava2MyPanSingle(index)"></span>
                    </el-tooltip>

                  </div>

                </div>
              </template>

              <!-- 其他简单插槽 -->
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

          <!-- 骨架屏 -->
          <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="p-4" />

          <!-- 无数据显示 -->
          <div v-else class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16">
            <Icon iconName="no_data" :width="130" fit="fill" class="opacity-80"></Icon>
            <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">该目录下暂无文件</div>
          </div>

        </div>
      </template>

      <!-- 弹出层合集容器 (逻辑层控制保持不变) -->
      <FolderSelect ref="folderSelectRef" @folderSelect="save2MyPanDone"></FolderSelect>
      <Preview ref="PreviewRef"></Preview>

      <Dialog :show="cancelDialogConfig.show" :title="cancelDialogConfig.title" :buttons="cancelDialogConfig.buttons"
        width="500px" :showCancel="true" @close="handleDialogClose" :showCustomTitle="true">
        <div class="flex flex-col items-center py-6">
          <div class="w-16 h-16 rounded-full bg-[#fff2f0] dark:bg-red-900/30 flex items-center justify-center mb-5">
            <img src="@/assets/icon-image/warning.png" class="w-10 h-10 rounded-full bg-white object-cover">
          </div>
          <div class="text-[17px] font-medium text-[#1d1d1f] dark:text-[#f5f5f7] mb-2 tracking-wide">确定立即取消对外分享吗？</div>
          <div class="text-[13px] text-[#86868b] dark:text-[#a1a1a6]">取消后，所有拥有此链接的用户将无法再访问该页面</div>
        </div>
      </Dialog>
    </div>
  </div>
</template>

<script setup>
import FolderSelect from "@/components/Business/FolderSelect.vue";
import SkeletonLoader from "@/components/Common/SkeletonLoader.vue";
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

if (route.query.code) {
  sessionStorage.setItem('auto_fill_code_' + shareId, route.query.code);
}

// 获取分享信息
const getShareInfo = async () => {
  let result = await proxy.Request({
    url: api.getShareLoginInfo,
    showLoading: false,
    errorCallback: () => {
      router.push('/shareCheck/' + shareId);
    },
    params: {
      shareId: shareId
    }
  })

  // 遇到空数据断层抛错（即已经被删除），别再原地 return 留在本页转圈，强行重定向至失效态页面
  if (!result) {
    router.push('/shareCheck/' + shareId);
    return;
  }

  if (result.data != null) {
    shareInfo.value = result.data
  } else {
    router.push('/shareCheck/' + shareId)
  }
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
