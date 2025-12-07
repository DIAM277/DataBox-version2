<template>
  <div ref="excelRef" class="excel-content"></div>
</template>

<script setup>
import * as XLSX from 'xlsx';
import { ref, getCurrentInstance, onMounted } from "vue";
const { proxy } = getCurrentInstance();

const props = defineProps({
  url: {
    type: String
  }
});

const excelRef = ref();

const initExcel = async () => {
  try {
    // 获取Excel文件
    let result = await proxy.Request({
      url: props.url,
      responseType: 'arraybuffer'
    });

    if (!result) {
      return;
    }

    // 解析Excel文件
    const workbook = XLSX.read(result, { type: 'array' });
    const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
    // 将工作表转换为HTML
    const htmlTable = XLSX.utils.sheet_to_html(firstSheet, { id: 'excel-table' });
    // 将HTML插入到DOM中
    excelRef.value.innerHTML = htmlTable;
    // 设置表格样式
    styleTable();
  } catch (error) {
    console.error('Excel预览失败:', error);
  }
};

// 为表格添加样式
const styleTable = () => {
  const table = excelRef.value.querySelector('#excel-table');
  if (table) {
    table.style.width = '100%';
    table.style.borderCollapse = 'collapse';

    // 表格单元格样式
    const cells = table.querySelectorAll('td, th');
    cells.forEach(cell => {
      cell.style.border = '1px solid #e0e0e0';
      cell.style.padding = '8px';
      cell.style.textAlign = 'left';
    });

    // 表头样式
    const headers = table.querySelectorAll('th');
    headers.forEach(header => {
      header.style.backgroundColor = '#f5f5f5';
      header.style.fontWeight = '600';
    });

    // 奇数行背景色
    const rows = table.querySelectorAll('tr:nth-child(odd)');
    rows.forEach(row => {
      row.style.backgroundColor = '#fafafa';
    });
  }
};

onMounted(() => {
  initExcel();
});
</script>

<style lang="scss" scoped>
.excel-content {
  width: 100%;
  height: 100%;
  overflow: auto;
  padding: 15px;
  background-color: #fff;

  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c0c4cc;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-track {
    background: #f5f7fa;
  }
}
</style>