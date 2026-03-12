<template>
  <div class="operation-log">
    <div class="top-panel">
      <div class="search-panel">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户ID">
            <el-input
              v-model="searchForm.userId"
              placeholder="请输入用户ID"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input
              v-model="searchForm.userName"
              placeholder="请输入用户名"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="操作模块">
            <el-select
              v-model="searchForm.module"
              placeholder="请选择模块"
              clearable
              style="width: 150px"
            >
              <el-option label="文件管理" value="文件管理" />
              <el-option label="分享管理" value="分享管理" />
              <el-option label="回收站" value="回收站" />
              <el-option label="用户管理" value="用户管理" />
              <el-option label="系统设置" value="系统设置" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作动作">
            <el-input
              v-model="searchForm.action"
              placeholder="请输入操作动作"
              clearable
              style="width: 150px"
            />
          </el-form-item>
          <el-form-item label="操作状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
            >
              <el-option label="成功" :value="1" />
              <el-option label="失败" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <Icon iconName="search" />
              搜索
            </el-button>
            <el-button @click="handleReset">
              <Icon iconName="refresh" />
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="file-list">
      <Table
        ref="dataTableRef"
        :columns="columns"
        :fetch="loadDataList"
        :dataSource="tableData"
        :options="tableOptions"
      >
        <template #status="{ index, row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
        <template #detail="{ index, row }">
          <el-tooltip
            v-if="row.detail && row.detail.length > 50"
            :content="row.detail"
            placement="top"
          >
            <span class="text-ellipsis">{{ row.detail }}</span>
          </el-tooltip>
          <span v-else>{{ row.detail || '-' }}</span>
        </template>
        <template #resultMsg="{ index, row }">
          <el-tooltip
            v-if="row.resultMsg && row.resultMsg.length > 30"
            :content="row.resultMsg"
            placement="top"
          >
            <span class="text-ellipsis">{{ row.resultMsg }}</span>
          </el-tooltip>
          <span v-else>{{ row.resultMsg || '-' }}</span>
        </template>
      </Table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue';
import Icon from '@/components/Icon.vue';
import Table from '@/components/Table.vue';

const { proxy } = getCurrentInstance();

const api = {
  loadDataList: '/admin/loadOperationLog',
};

const searchForm = reactive({
  userId: '',
  userName: '',
  module: '',
  action: '',
  status: null,
  dateRange: null,
});

const columns = [
//   {
//     label: '日志ID',
//     prop: 'opId',
//     width: 80,
//   },
  {
    label: '用户ID',
    prop: 'userId',
    width: 120,
  },
  {
    label: '用户名',
    prop: 'userName',
    width: 120,
  },
  {
    label: '操作模块',
    prop: 'module',
    width: 120,
  },
  {
    label: '操作动作',
    prop: 'action',
    width: 150,
  },
  {
    label: '操作详情',
    prop: 'detail',
    scopedSlots: 'detail',
  },
  {
    label: '操作状态',
    prop: 'status',
    width: 100,
    scopedSlots: 'status',
  },
  {
    label: '结果信息',
    prop: 'resultMsg',
    width: 150,
    scopedSlots: 'resultMsg',
  },
  {
    label: '操作时间',
    prop: 'createTime',
    width: 160,
  },
];

const tableData = reactive({});
const tableOptions = {
  extHeight: 100,
};

const dataTableRef = ref();

// 加载数据列表
const loadDataList = async () => {
  let params = {
    pageNo: tableData.pageNo,
    pageSize: tableData.pageSize,
  };

  // 添加搜索条件
  if (searchForm.userId) {
    params.userId = searchForm.userId;
  }
  if (searchForm.userName) {
    params.userNameFuzzy = searchForm.userName;
  }
  if (searchForm.module) {
    params.module = searchForm.module;
  }
  if (searchForm.action) {
    params.actionFuzzy = searchForm.action;
  }
  if (searchForm.status !== null && searchForm.status !== '') {
    params.status = searchForm.status;
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.createTimeStart = searchForm.dateRange[0];
    params.createTimeEnd = searchForm.dateRange[1];
  }

  let result = await proxy.Request({
    url: api.loadDataList,
    params,
  });

  if (!result) {
    return;
  }

  tableData.list = result.data.list;
  tableData.pageNo = result.data.pageNo;
  tableData.pageSize = result.data.pageSize;
  tableData.pageTotal = result.data.pageTotal;
  tableData.totalCount = result.data.totalCount;
};

// 搜索
const handleSearch = () => {
  dataTableRef.value.setCurrentPage(1);
  nextTick(() => {
    loadDataList();
  });
};

// 重置
const handleReset = () => {
  searchForm.userId = '';
  searchForm.userName = '';
  searchForm.module = '';
  searchForm.action = '';
  searchForm.status = null;
  searchForm.dateRange = null;
  dataTableRef.value.setCurrentPage(1);
  nextTick(() => {
    loadDataList();
  });
};
</script>

<style lang="scss" scoped>
.operation-log {
  height: 100%;
  display: flex;
  flex-direction: column;

  .top-panel {
    background: #fff;
    padding: 10px 15px;
    border-radius: 5px;
    margin-bottom: 10px;

    .search-form {
      :deep(.el-form-item) {
        margin-bottom: 10px;
      }
    }
  }

  .file-list {
    flex: 1;
    background: #fff;
    border-radius: 5px;
    padding: 10px;

    .text-ellipsis {
      display: inline-block;
      max-width: 100%;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      cursor: pointer;
    }
  }
}
</style>