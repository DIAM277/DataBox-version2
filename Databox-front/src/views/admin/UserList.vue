<template>
  <div>
    <div class="top" style="margin-top: 20px;">
      <div class="top-op">
        <div class="search-form">
          <el-form :model="formData" ref="formDataRef" @submit.prevent inline class="form-inline">
            <div class="form-item-group">
              <span class="label">状态</span>
              <el-select clearable placeholder="请选择状态" v-model="formData.status" class="status-select">
                <el-option :value="1" label="正常"></el-option>
                <el-option :value="0" label="禁用"></el-option>
              </el-select>
              <el-button type="primary" class="custom-btn search-btn" @click="loadDataList(false)">
                <span class="iconfont icon-search"></span>
                查询
              </el-button>
              <el-button type="info" class="custom-btn refresh-btn" @click="loadDataList(false)">
                <span class="iconfont icon-refresh"></span>
                刷新
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
    <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
      <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
        :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
        :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">
        <!-- 头像 -->
        <template #avatar="{ row }">
          <div class="avatar">
            <Avatar :userId="row.userId" :interactive="false"></Avatar>
          </div>
        </template>
        <!-- 用户名 -->
        <template #userName="{ row }">
          <span class="file-text" :title="row.userName">
            <span class="file-name-1">{{ row.userName }}</span>
          </span>
        </template>
        <!-- 空间使用 -->
        <template #space="{ row }">
          <div>
            <div>{{ proxy.Utils.size2Str(row.userSpace) }} / {{ proxy.Utils.size2Str(row.totalSpace) }}</div>
            <el-progress :percentage="calculateSpacePercentage(row)" :stroke-width="10" :show-text="false"
              :striped="true"></el-progress>
          </div>
        </template>
        <!-- 状态 -->
        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
        <!-- 操作 -->
        <template #op="{ row }">
          <div class="op-buttons">
            <el-button type="primary" size="small" @click="showUpdateSpaceDialog(row)">修改空间</el-button>
            <el-button :type="row.status === 1 ? 'danger' : 'success'" size="small" @click="updateUserStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </div>
        </template>
      </Table>
    </div>
    <!-- 骨架屏 -->
    <SkeletonLoader v-else-if="isLoading" :rowCount="14" />
    <!-- 无数据显示 -->
    <div class="no-data" v-else>
      <div class="no-data-inner">
        <Icon iconName="no_data" :width="150" fit="fill"></Icon>
        <div class="tips">当前没有用户数据</div>
      </div>
    </div>
    <!-- 修改空间对话框 -->
    <Dialog :show="spaceDialogConfig.show" :title="spaceDialogConfig.title" :buttons="spaceDialogConfig.buttons"
      width="500px" :showCancel="true" @close="spaceDialogConfig.show = false" :showCustomTitle="true">
      <div class="space-dialog-content">
        <el-form :model="spaceForm" label-width="100px">
          <el-form-item label="当前空间">
            {{ proxy.Utils.size2Str(currentUser.userSpace || 0) }} / {{ proxy.Utils.size2Str(currentUser.totalSpace ||
              0)
            }}
          </el-form-item>
          <el-form-item label="新空间大小">
            <el-input-number v-model="spaceForm.spaceSize" :min="1" :max="1024" :step="1"></el-input-number>
            <span style="margin-left: 10px;">GB</span>
          </el-form-item>
        </el-form>
      </div>
    </Dialog>
    <!-- 状态修改确认对话框 -->
    <Dialog :show="statusDialogConfig.show" :title="statusDialogConfig.title" :buttons="statusDialogConfig.buttons"
      width="500px" :showCancel="true" @close="statusDialogConfig.show = false" :showCustomTitle="true">
      <div class="delete-confirm-content">
        <div class="icon-container">
          <img src="@/assets/icon-image/warning.png" style="background-color:white">
        </div>
        <div class="message">{{ statusDialogConfig.message }}</div>
        <div class="sub-message">{{ statusDialogConfig.subMessage }}</div>
      </div>
    </Dialog>
  </div>
</template>

<script setup>
import SkeletonLoader from "@/components/SkeletonLoader.vue";
import { ref, reactive, getCurrentInstance, nextTick, computed, onMounted } from "vue"
const { proxy } = getCurrentInstance();

const api = {
  loadDataList: '/admin/loadUserList',
  updateUserStatus: '/admin/updateUserStatus',
  updateUserSpace: '/admin/updateUserSpace'
}

const columns = computed(() => [
  {
    label: '头像',
    prop: 'avatar',
    scopedSlots: 'avatar',
    align: 'center',
    width: 100,
    sortable: false
  },
  {
    label: '用户名',
    prop: 'userName',
    width: 150,
    align: 'center',
    scopedSlots: 'userName',
    sortable: true
  },
  {
    label: '邮箱',
    prop: 'email',
    width: 200,
    align: 'center',
    sortable: true
  },
  {
    label: '空间使用',
    prop: 'space',
    align: 'center',
    scopedSlots: 'space',
    sortable: false
  },
  {
    label: '创建时间',
    prop: "createTime",
    width: 200,
    align: 'center',
    sortable: true
  },
  {
    label: '最后登录时间',
    prop: "lastLoginTime",
    width: 200,
    align: 'center',
    sortable: true
  },
  {
    label: '状态',
    prop: 'status',
    scopedSlots: 'status',
    width: 100,
    align: 'center',
    sortable: true
  },
  {
    label: '操作',
    prop: 'op',
    scopedSlots: 'op',
    width: 180,
    align: 'center',
    sortable: false
  }
])

const formData = ref({})
const tableData = ref({})
const tableOptions = ref({
  extHeight: 50,
  infiniteScroll: true
})
// 添加加载状态变量
const isLoading = ref(false);
// 排序相关状态
const sortConfig = ref({
  prop: '', // 默认按创建时间排序
  order: '' // 默认降序
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

// 获取用户列表数据
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
    status: formData.value.status
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

// 多选
const selectFileList = ref([])
// 多选回调
const rowSelected = (rows) => {
  selectFileList.value = []
  rows.forEach(item => {
    selectFileList.value.push(item.userId)
  })
}

// 计算空间使用百分比
const calculateSpacePercentage = (row) => {
  if (!row.totalSpace || row.totalSpace === 0) return 0;
  return Math.min(Math.round((row.userSpace / row.totalSpace) * 100), 100);
}

// 当前操作的用户
const currentUser = ref({});

// 空间修改对话框配置
const spaceForm = ref({
  spaceSize: 0
});

const spaceDialogConfig = ref({
  show: false,
  title: "修改用户空间",
  buttons: [
    {
      text: "确认修改",
      type: "primary",
      click: () => {
        updateUserSpace();
      }
    }
  ]
});

// 显示修改空间对话框
const showUpdateSpaceDialog = (row) => {
  currentUser.value = row;
  // 计算当前已使用空间（GB）
  const usedSpaceGB = Math.ceil(row.userSpace / (1024 * 1024 * 1024));
  spaceForm.value.spaceSize = Math.floor(row.totalSpace / (1024 * 1024 * 1024)); // 转换为GB
  // 设置最小值为已使用空间
  spaceForm.value.minSpace = usedSpaceGB;
  spaceDialogConfig.value.show = true;
}

// 更新用户空间
const updateUserSpace = async () => {
  // 获取当前已使用空间（GB）
  const usedSpaceGB = Math.ceil(currentUser.value.userSpace / (1024 * 1024 * 1024));
  if (!spaceForm.value.spaceSize || spaceForm.value.spaceSize <= 0) {
    proxy.Message.warning('请输入有效的空间大小');
    return;
  }
  // 前端验证：新空间不能小于已使用空间
  if (spaceForm.value.spaceSize < usedSpaceGB) {
    proxy.Message.warning(`新空间大小不能小于当前已使用空间(${usedSpaceGB}GB)`);
    return;
  }
  // 直接设置新的总空间大小（单位为GB）
  const newSpace = spaceForm.value.spaceSize
  let result = await proxy.Request({
    url: api.updateUserSpace,
    params: {
      userId: currentUser.value.userId,
      totalSpace: newSpace
    }
  });
  if (!result) {
    return;
  }
  spaceDialogConfig.value.show = false;
  proxy.Message.success('用户空间修改成功');
  loadDataList(false);
}

// 状态修改对话框配置
const statusDialogConfig = ref({
  show: false,
  title: "确认操作",
  message: "",
  subMessage: "",
  buttons: [
    {
      text: "确认",
      type: "primary",
      click: () => {
        confirmUpdateStatus();
      }
    }
  ]
});

// 更新用户状态
const updateUserStatus = (row) => {
  currentUser.value = row;

  if (row.status === 1) {
    statusDialogConfig.value.message = `确定要禁用用户 "${row.userName}" 吗？`;
    statusDialogConfig.value.subMessage = "禁用后该用户将无法登录系统";
  } else {
    statusDialogConfig.value.message = `确定要启用用户 "${row.userName}" 吗？`;
    statusDialogConfig.value.subMessage = "启用后该用户可以正常登录系统";
  }

  statusDialogConfig.value.show = true;
}

// 确认更新用户状态
const confirmUpdateStatus = async () => {
  const newStatus = currentUser.value.status === 1 ? 0 : 1;

  let result = await proxy.Request({
    url: api.updateUserStatus,
    params: {
      userId: currentUser.value.userId,
      status: newStatus
    }
  });

  if (!result) {
    return;
  }

  statusDialogConfig.value.show = false;
  proxy.Message.success(`用户${newStatus === 1 ? '启用' : '禁用'}成功`);
  loadDataList(false);
}

onMounted(() => {
  loadDataList();
})
</script>

<style lang="scss" scoped>
@use "../../assets/main.scss" as *;

.top-op {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.search-form {
  flex: 1;

  :deep(.el-form) {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
  }
}

.btn-group {
  margin-left: 10px;
}

.top-panel {
  margin-top: 10px;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 25px;
  overflow: hidden;
  margin: 0 auto;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.op-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.space-dialog-content {
  padding: 20px;
}

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

.form-inline {
  display: flex;
  align-items: center;
}

.form-item-group {
  display: flex;
  align-items: center;
  height: 40px;
}

.label {
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}

.status-select {
  width: 120px;
  margin-right: 10px;
}

.search-btn,
.refresh-btn {
  margin-right: 10px;
  height: 32px;
}

.file-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  .file-name-1 {
    font-weight: 600;
    color: #409EFF;
    transition: all 0.3s ease;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 0;
      height: 1px;
      background-color: #409EFF;
      transition: width 0.3s ease;
    }

    &:hover::after {
      width: 100%;
    }
  }
}
</style>