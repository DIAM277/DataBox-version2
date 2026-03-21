<template>
  <div class="flex flex-col h-full pt-4 pb-1">

    <!-- 顶栏：标题与局部操作按钮区 (与 Share 和 Recycle 完全拉齐对标) -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center px-6 mb-5 gap-4">

      <!-- 左：标题与说明 -->
      <div class="flex flex-col">
        <h1 class="text-[22px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-tight leading-snug">用户管理</h1>
        <span class="text-[13px] text-[#86868b] dark:text-[#98989d] mt-0.5 font-medium">查看并管理系统用户的状态与存储配额</span>
      </div>

      <!-- 右：操作与筛选区 -->
      <div class="flex items-center gap-2.5 shrink-0 select-none flex-wrap">

        <!-- 🔴 新增：搜索用户名输入框 -->
        <el-input clearable placeholder="搜索用户名" v-model.trim="formData.userNameFuzzy" @keyup.enter="loadDataList(false)"
          class="mac-input w-[150px]" />

        <el-select clearable placeholder="请选择状态" v-model="formData.status" class="mac-select w-[130px]">
          <el-option :value="1" label="正常模式"></el-option>
          <el-option :value="0" label="已被禁用"></el-option>
        </el-select>

        <button type="button" @click="loadDataList(false)"
          class="bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-xl px-3 py-1.5 text-[13.5px] font-semibold transition-all shadow-sm flex items-center justify-center gap-1.5 focus:outline-none active:scale-95">
          <span class="iconfont icon-search text-[14px]"></span>查询
        </button>

        <!-- 极简刷新按钮 -->
        <div @click="loadDataList(false)"
          class="flex items-center justify-center w-[36px] h-[36px] text-gray-500 hover:text-[#007AFF] bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] shadow-sm rounded-xl hover:bg-gray-50 dark:hover:bg-[#2c2c2e] transition-all cursor-pointer group">
          <span class="iconfont icon-refresh text-[14px]"></span>
        </div>
      </div>

    </div>

    <!-- 主体：底层 Table 容器 (彻底去除了生硬的卡片包裹并融于原生结构) -->
    <div class="flex-1 overflow-hidden relative flex flex-col">

      <div class="flex-1 overflow-hidden" v-if="tableData.list && tableData.list.length > 0">
        <Table ref="dataTableRef" :columns="columns" :dataSource="tableData" :fetch="loadDataList" :initFetch="false"
          :options="tableOptions" @rowSelected="rowSelected" @sortChange="handleSortChange"
          :default-sort="{ prop: sortConfig.prop, order: sortConfig.order }">

          <!-- 头像 -->
          <template #avatar="{ row }">
            <div class="flex justify-center items-center py-1">
              <div
                class="w-10 h-10 rounded-full border border-gray-200/60 dark:border-gray-700 shadow-sm overflow-hidden flex-shrink-0 transition-transform hover:scale-105 bg-gray-50 dark:bg-black/20">
                <Avatar :userId="row.userId" :interactive="false" :width="40"></Avatar>
              </div>
            </div>
          </template>

          <!-- 用户名 -->
          <template #userName="{ row }">
            <span class="text-[14px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-wide"
              :title="row.userName">
              {{ row.userName }}
            </span>
          </template>

          <!-- 空间使用 -->
          <template #space="{ row }">
            <div class="flex flex-col gap-1.5 w-full pt-1 px-4">
              <div class="flex justify-between items-center text-[12px] font-medium text-[#86868b] dark:text-gray-400">
                <span>{{ proxy.Utils.size2Str(row.userSpace) }}</span>
                <span>{{ proxy.Utils.size2Str(row.totalSpace) }}</span>
              </div>
              <el-progress :percentage="calculateSpacePercentage(row)" :stroke-width="6" :show-text="false"
                color="#007AFF" class="mac-progress"></el-progress>
            </div>
          </template>

          <!-- 状态 -->
          <template #status="{ row }">
            <span
              :class="row.status === 1 ? 'bg-emerald-50 text-emerald-600 border-emerald-200 dark:bg-emerald-900/20 dark:border-emerald-900/40 dark:text-emerald-400' : 'bg-red-50 text-red-600 border-red-200 dark:bg-red-900/20 dark:border-red-900/40 dark:text-red-400'"
              class="px-2.5 py-1 text-[12px] font-bold border rounded-lg tracking-wider transition-colors">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </span>
          </template>

          <!-- 操作 -->
          <template #op="{ row }">
            <div class="flex items-center justify-center gap-1.5 w-full">
              <!-- 修改空间 -->
              <el-tooltip content="修改空间配额" placement="top" effect="dark" :show-after="300">
                <div
                  class="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-[#007AFF] hover:shadow-sm cursor-pointer transition-all active:scale-90"
                  @click="showUpdateSpaceDialog(row)">
                  <span class="iconfont icon-settings text-[16px]"></span>
                </div>
              </el-tooltip>

              <!-- 切换状态 -->
              <el-tooltip :content="row.status === 1 ? '禁用该用户' : '启用该用户'" placement="top" effect="dark"
                :show-after="300">
                <div
                  class="w-8 h-8 flex items-center justify-center rounded-lg text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:shadow-sm cursor-pointer transition-all active:scale-90"
                  :class="row.status === 1 ? 'hover:text-red-500' : 'hover:text-emerald-500'"
                  @click="updateUserStatus(row)">
                  <span class="iconfont text-[16px]" :class="row.status === 1 ? 'icon-cancel' : 'icon-ok'"></span>
                </div>
              </el-tooltip>
            </div>
          </template>

        </Table>
      </div>

      <!-- 骨架屏 -->
      <SkeletonLoader v-else-if="isLoading" :rowCount="14" class="px-6 py-4" />

      <!-- 无数据显示 -->
      <div class="flex-1 flex flex-col justify-center items-center h-full inset-0 pb-16" v-else>
        <Icon iconName="no_data" :width="140" fit="fill" class="opacity-80 grayscale"></Icon>
        <div class="mt-5 text-[#86868b] text-[14px] tracking-wide font-medium">当前没有任何用户数据</div>
      </div>

    </div>

    <!-- 修改空间对话框 -->
    <Dialog :show="spaceDialogConfig.show" :title="spaceDialogConfig.title" :buttons="spaceDialogConfig.buttons"
      width="450px" :showCancel="true" @close="spaceDialogConfig.show = false" :showCustomTitle="true">
      <div class="px-5 py-2">
        <el-form :model="spaceForm" label-position="top" @submit.prevent>
          <el-form-item class="mb-4">
            <template #label><span
                class="text-[13px] font-semibold text-gray-500 dark:text-gray-400">当前被占用空间</span></template>
            <div class="font-mono text-[16px] text-gray-800 dark:text-gray-200 mt-1 pl-1">
              {{ proxy.Utils.size2Str(currentUser.userSpace || 0) }} / {{ proxy.Utils.size2Str(currentUser.totalSpace ||
                0)
              }}
            </div>
          </el-form-item>

          <el-form-item>
            <template #label><span class="text-[13px] font-semibold text-gray-500 dark:text-gray-400">设定新总配额
                (GB)</span></template>
            <div class="flex items-center gap-3 w-full mt-1">
              <el-input-number v-model="spaceForm.spaceSize" :min="1" :max="1024" :step="1"
                class="mac-input-number w-[160px]"></el-input-number>
            </div>
            <div class="text-[12px] text-gray-400 mt-2 hover:text-[#007AFF] transition-colors">
              修改后的总配额必须大于当前该用户已储内容的所占总空间
            </div>
          </el-form-item>
        </el-form>
      </div>
    </Dialog>

    <!-- 状态修改确认对话框 -->
    <Dialog :show="statusDialogConfig.show" :title="statusDialogConfig.title" :buttons="statusDialogConfig.buttons"
      width="480px" :showCancel="true" @close="statusDialogConfig.show = false" :showCustomTitle="true">
      <div class="flex flex-col items-center py-6">
        <div
          class="w-16 h-16 rounded-full bg-[#fff2f0] dark:bg-red-900/30 flex items-center justify-center mb-5 shadow-sm">
          <img src="@/assets/icon-image/warning.png" class="w-9 h-9 object-cover opacity-90">
        </div>
        <div class="text-[17px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] mb-2 tracking-wide text-center">
          {{ statusDialogConfig.message }}
        </div>
        <div class="text-[13px] text-[#86868b] dark:text-[#a1a1a6] text-center w-[80%] leading-relaxed">
          {{ statusDialogConfig.subMessage }}
        </div>
      </div>
    </Dialog>

  </div>
</template>

<script setup>
import SkeletonLoader from "@/components/Common/SkeletonLoader.vue";
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
    status: formData.value.status,
    userNameFuzzy: formData.value.userNameFuzzy
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

<style scoped>
:deep(.mac-input .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08) inset !important;
  background-color: white !important;
  border-radius: 12px;
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

/* Apple macOS Select 框样式覆写 */
:deep(.mac-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08) inset !important;
  background-color: white !important;
  border-radius: 12px;
  height: 38px;
  padding: 0 12px;
  transition: all 0.25s ease;
}

html.dark :deep(.mac-select .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
  background-color: #1c1c1e !important;
}

:deep(.mac-select .el-input__wrapper.is-focus),
:deep(.mac-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 2px rgba(0, 122, 255, 0.4) inset !important;
}

:deep(.mac-select .el-input__inner) {
  font-weight: 500;
  color: #1d1d1f;
}

html.dark :deep(.mac-select .el-input__inner) {
  color: #f5f5f7;
}

/* Apple macOS 进度条覆写 */
:deep(.mac-progress .el-progress-bar__outer) {
  background-color: rgba(0, 0, 0, 0.06) !important;
  border-radius: 99px;
}

html.dark :deep(.mac-progress .el-progress-bar__outer) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

/* 步进数字输入器重写 */
:deep(.mac-input-number.el-input-number) {
  line-height: 36px;
}

:deep(.mac-input-number .el-input__wrapper) {
  height: 38px;
  border-radius: 10px;
  background-color: rgba(0, 0, 0, 0.03) !important;
  box-shadow: none !important;
}

html.dark :deep(.mac-input-number .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

:deep(.mac-input-number .el-input-number__decrease),
:deep(.mac-input-number .el-input-number__increase) {
  background-color: transparent !important;
  border: none !important;
  color: #86868b;
  width: 36px;
}

:deep(.mac-input-number .el-input-number__decrease:hover),
:deep(.mac-input-number .el-input-number__increase:hover) {
  color: #007AFF;
}

:deep(.mac-input-number .el-input__inner) {
  font-family: monospace;
  font-size: 16px;
  font-weight: bold;
  color: #1d1d1f;
}

html.dark :deep(.mac-input-number .el-input__inner) {
  color: #f5f5f7;
}

/* 清理不需要的 margin */
:deep(.el-form-item) {
  margin-bottom: 12px;
}
</style>