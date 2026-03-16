<template>
  <!-- Apple Quick Look 沉浸式外层纸张容器 -->
  <div
    class="w-full h-full bg-white dark:bg-[#1e1e1e] rounded-2xl shadow-2xl overflow-hidden flex flex-col relative transition-colors duration-300">
    <!-- 核心渲染区：增加基础 padding 防止表格贴死圆角边框 -->
    <div ref="excelRef"
      class="flex-1 w-full h-full overflow-auto p-4 md:p-6 text-sm text-[#1d1d1f] dark:text-[#f5f5f7]"></div>
  </div>
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