<template>
  <!-- Apple 的半透明毛玻璃风面板 -->
  <div
    class="w-full flex flex-col bg-white/80 dark:bg-[#1c1c1e]/90 backdrop-blur-3xl rounded-xl border border-[#e5e5e9] dark:border-[#38383a]/80 overflow-hidden shadow-2xl select-none">

    <!-- 头部：平滑标题和极简信息 -->
    <div
      class="flex items-center justify-between px-5 py-3.5 border-b border-[#e5e5e9]/70 dark:border-[#38383a]/70 bg-white/40 dark:bg-black/20">
      <div class="flex flex-col">
        <span class="text-[14px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-wider">传输列表</span>
        <span class="text-[11px] text-[#86868b] dark:text-gray-400 mt-0.5">仅展示本次任务</span>
      </div>
      <div class="flex items-center gap-2">
        <!-- 预留给可能的全局操作图标 -->
      </div>
    </div>

    <!-- 列表主体 (定制流线型滚动条) -->
    <div class="overflow-y-auto p-2 min-h-[220px] max-h-[480px] apple-scrollbar space-y-1">

      <div v-for="(item, index) in fileList" :key="item.uid"
        class="group flex items-center p-3 hover:bg-black/5 dark:hover:bg-white/10 rounded-xl transition-all duration-300">

        <!-- 左侧状态/类型图标 (提取原有的 STATUS 状态字典注入) -->
        <div
          class="flex-shrink-0 w-[42px] h-[42px] flex items-center justify-center rounded-xl bg-gray-100 dark:bg-gray-800/80 mr-3.5 border border-black/5 dark:border-white/5 shadow-sm">
          <span :class="['iconfont text-[22px]', 'icon-' + STATUS[item.status].icon]"
            :style="{ color: STATUS[item.status].color }"></span>
        </div>

        <!-- 中间：信息与进度条 -->
        <div class="flex-1 min-w-0 mr-3 flex flex-col justify-center">
          <div class="flex justify-between items-end mb-1.5">
            <!-- 文件名 -->
            <span class="text-[13px] font-medium text-[#1d1d1f] dark:text-gray-300 truncate tracking-wide pr-2">
              {{ item.fileName }}
            </span>
            <!-- 状态描述文字 (变色) -->
            <span class="text-[11px] whitespace-nowrap font-medium" :style="{ color: STATUS[item.status].color }">
              {{ item.status == 'fail' ? item.errorMsg : STATUS[item.status].desc }}
            </span>
          </div>

          <!-- 极简流线型 h-1.5 进度条轨道 -->
          <div class="w-full h-1.5 bg-gray-200/80 dark:bg-gray-700/80 rounded-full overflow-hidden shadow-inner">
            <!-- MD5 解析进度 -->
            <div v-if="item.status === STATUS.init.value || item.status === STATUS.md5_complete.value"
              class="h-full rounded-full transition-all duration-500 ease-out"
              :class="item.status === STATUS.md5_complete.value ? 'bg-[#34C759]' : 'bg-[#007AFF] dark:bg-[#0a84ff]'"
              :style="{ width: item.md5Progress + '%' }">
            </div>
            <!-- 实际文件上传进度 -->
            <div v-else class="h-full rounded-full transition-all duration-500 ease-out"
              :class="item.status === STATUS.upload_finish.value || item.status === STATUS.upload_seconds.value ? 'bg-[#34C759]' : (item.status === 'fail' ? 'bg-[#FF3B30]' : 'bg-[#007AFF] dark:bg-[#0a84ff]')"
              :style="{ width: item.uploadProgress + '%' }">
            </div>
          </div>

          <!-- 底部容量提示层 -->
          <div class="mt-1.5 text-[10px] text-[#86868b] dark:text-gray-500 flex justify-between font-medium">
            <span v-if="item.status == STATUS.uploading.value">{{ proxy.Utils.size2Str(item.fileUploadSize) }} / {{
              proxy.Utils.size2Str(item.totalSize) }}</span>
            <span v-else>{{ proxy.Utils.size2Str(item.totalSize) }}</span>

            <!-- 动态数字百分比 -->
            <span v-if="item.status === STATUS.init.value || item.status === STATUS.uploading.value"
              class="tabular-nums font-semibold"
              :class="{ 'text-[#007AFF] dark:text-[#0a84ff]': item.status === STATUS.uploading.value }">
              {{ item.status === STATUS.init.value ? item.md5Progress : item.uploadProgress }}%
            </span>
          </div>
        </div>

        <!-- 右侧：操作按钮组 (移除 opacity-0 和 group-hover:opacity-100，让其永远显示) -->
        <div class="flex-shrink-0 flex items-center pl-1 space-x-1">
          <!-- 开始按钮 -->
          <button v-if="item.status == STATUS.uploading.value && item.pause" @click="startUpload(item.uid)"
            class="w-8 h-8 flex items-center justify-center text-[#007AFF] hover:bg-[#007AFF]/10 rounded-full transition-colors"
            title="继续上传">
            <span class="iconfont icon-upload text-[15px]"></span>
          </button>

          <!-- 暂停按钮 -->
          <button v-if="item.status == STATUS.uploading.value && !item.pause" @click="pauseUpload(item.uid)"
            class="w-8 h-8 flex items-center justify-center text-[#86868b] hover:bg-black/5 dark:hover:bg-white/10 rounded-full transition-colors"
            title="暂停">
            <span class="iconfont icon-pause text-[15px]"></span>
          </button>

          <!-- 删除按钮 (等候、进行中、失败状态可删) -->
          <button
            v-if="item.status != STATUS.init.value && item.status != STATUS.upload_finish.value && item.status != STATUS.upload_seconds.value && item.status != STATUS.md5_complete.value"
            @click="delUpload(item.uid, index)"
            class="w-8 h-8 flex items-center justify-center text-[#FF3B30] hover:bg-[#FF3B30]/10 rounded-full transition-colors"
            title="取消并删除">
            <span class="iconfont icon-del text-[15px]"></span>
          </button>

          <!-- 清除按钮 (完成状态可清)：替换为库里存在的 icon-del，并改为醒目的红色背景 -->
          <button v-if="item.status == STATUS.upload_finish.value || item.status == STATUS.upload_seconds.value"
            @click="delUpload(item.uid, index)"
            class="w-7 h-7 flex items-center justify-center bg-[#FF3B30] text-white hover:bg-[#ff2015] shadow-sm rounded-full transition-all duration-200 hover:scale-105"
            title="清除记录">
            <span class="iconfont icon-del text-[13px]"></span>
          </button>
        </div>

      </div>

      <!-- 缺省状态视图 (无数据时) -->
      <div v-if="fileList.length === 0"
        class="flex flex-col items-center justify-center h-[200px] text-[#86868b] dark:text-gray-500">
        <span class="iconfont icon-transfer text-[40px] mb-3 opacity-40"></span>
        <span class="text-[13px] font-medium tracking-wide">暂无传输任务</span>
      </div>

    </div>
  </div>
</template>

<script setup>
import SparkMD5 from "spark-md5";
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const api = {
  upload: {
    url: "/file/uploadFile",
  }
}

const STATUS = {
  emptyfile: {
    value: 'emptyfile',
    desc: '文件为空',
    color: "#f75000",
    icon: 'close'
  },
  fail: {
    value: "fail",
    desc: "上传失败",
    color: "#F75000",
    icon: "close",
  },
  init: {
    value: "init",
    desc: "解析中",
    color: "#e6a23c",
    icon: "clock",
  },
  uploading: {
    value: "uploading",
    desc: "上传中",
    color: "#409eff",
    icon: "upload",
  },
  upload_finish: {
    value: "upload_finish",
    desc: "上传完成",
    color: "#67c23a",
    icon: "ok",
  },
  upload_seconds: {
    value: "upload_seconds",
    desc: "上传秒传",
    color: "#67c23a",
    icon: "ok",
  },
  md5_complete: {
    value: "md5_complete",
    desc: "解析完成",
    color: "#67c23a",
    icon: "ok"
  }
}
// 分片大小
const chunkSize = 1024 * 1024 * 5
const fileList = ref([])
const delList = ref([])
// 上传文件
const addFile = async (file, filePid) => {
  const fileItem = {
    // 文件对象
    file: file,
    // 文件uid
    uid: file.uid,
    // 文件md5进度
    md5Progress: 0,
    // 文件md5值
    md5: null,
    // 文件名
    fileName: file.name,
    // 文件状态
    status: STATUS.init.value,
    // 文件已上传大小
    fileUploadSize: 0,
    // 文件总大小
    totalSize: file.size,
    // 文件上传进度
    uploadProgress: 0,
    // 暂停
    pause: false,
    // 当前分片
    chunkIndex: 0,
    // 当前文件父级Id
    filePid: filePid,
    // 错误信息
    errorMsg: null
  }
  fileList.value.unshift(fileItem)
  // 上传文件大小为空 不进行上传
  if (fileItem.totalSize == 0) {
    fileItem.status = STATUS.emptyfile.value
    return
  }
  let md5FileUid = await computeMd5(fileItem)
  if (md5FileUid == null) {
    return
  }
  uploadFile(md5FileUid)
}
defineExpose({ addFile })

// 计算md5 以便上传文件时进行分片处理
const computeMd5 = (fileItem) => {
  // 上传的目标文件
  let file = fileItem.file
  // 获取文件分片方法
  let blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
  // 计算分片总片数
  let chunks = Math.ceil(file.size / chunkSize)
  // 当前分片索引
  let currentChunk = 0
  // 初始化SparkMD5
  let spark = new SparkMD5.ArrayBuffer()
  let fileReader = new FileReader()

  // 加载下一个分片函数[根据当前分片索引读取文件的下一个分片，将其转换为ArrayBuffer]
  let loadNext = () => {
    let start = currentChunk * chunkSize
    let end = start + chunkSize >= file.size ? file.size : start + chunkSize
    fileReader.readAsArrayBuffer(blobSlice.call(file, start, end))
  }
  // 开始加载第一个分片
  loadNext()

  return new Promise((resolve, reject) => {
    // 根据文件uid获取文件对象
    let resultFile = getFileByUid(file.uid)
    // 文件读取成功回调
    fileReader.onload = (e) => {
      spark.append(e.target.result)
      currentChunk++
      // 循环读取分片直至最后一个分片
      if (currentChunk < chunks) {
        //console.log(`${file.name}的第${currentChunk}块上传成功，开始第${currentChunk + 1}块上传`)
        let percent = Math.floor(currentChunk / chunks * 100)
        resultFile.md5Progress = percent
        loadNext()
      } else {
        // 所有分片读取完毕 计算最终MD5值
        let md5 = spark.end()
        // 销毁spark对象 释放内存
        spark.destroy()
        resultFile.md5Progress = 100
        // 先切换到完成状态
        resultFile.status = STATUS.md5_complete.value
        resultFile.md5 = md5
        // 延迟切换上传状态
        setTimeout(() => {
          resultFile.status = STATUS.uploading.value
          resolve(fileItem.uid)
        }, 1500)
      }
    }
    fileReader.onerror = () => {
      resultFile.md5Progress = -1
      resultFile.status = STATUS.fail.value
      resolve(fileItem.uid)
    }
  }).catch((error) => {
    return null
  })
}

// 从上传列表获取文件
const getFileByUid = (uid) => {
  let file = fileList.value.find((item) => {
    return item.file.uid === uid
  })
  return file
}

const emit = defineEmits(["uploadCallback"])
// 上传文件
const uploadFile = async (uid, chunkIndex) => {
  // 初始化分片索引
  chunkIndex = chunkIndex ? chunkIndex + 1 : 0
  // 文件分片上传
  let currentFile = getFileByUid(uid)
  const file = currentFile.file
  const fileSize = currentFile.totalSize
  // 计算分片总片数
  const chunks = Math.ceil(fileSize / chunkSize)
  // 遍历每个分片进行上传
  for (let i = chunkIndex; i < chunks; i++) {
    // 检查是否取消上传
    let delIndex = delList.value.indexOf(uid)
    if (delIndex != -1) {
      delList.value.splice(delIndex, 1)
      break
    }
    currentFile = getFileByUid(uid)
    // 检查是否暂停上传
    if (currentFile.pause) {
      break
    }
    // 计算分片起始和结束位置
    let start = i * chunkSize
    let end = start + chunkSize >= fileSize ? fileSize : start + chunkSize
    // 获取当前分片文件
    let chunkFile = file.slice(start, end)
    // 发起文件上传请求
    let updateResult = await proxy.Request({
      url: api.upload.url,
      showLoading: false,
      dataType: 'file',
      params: {
        file: chunkFile,
        fileName: file.name,
        fileMd5: currentFile.md5,
        chunkIndex: i,
        chunks: chunks,
        fileId: currentFile.fileId,
        filePid: currentFile.filePid,
      },
      showError: false,
      // 处理上传失败
      errorCallback: (errorMsg) => {
        currentFile.status = STATUS.fail.value
        currentFile.errorMsg = errorMsg
      },
      // 更新上传进度
      uploadProgressCallback: (event) => {
        let loaded = event.loaded
        if (loaded > fileSize) {
          loaded = fileSize
        }
        currentFile.fileUploadSize = i * chunkSize + loaded
        currentFile.uploadProgress = Math.floor(currentFile.fileUploadSize / fileSize * 100)
      }
    })
    if (updateResult == null) {
      break
    }
    // 更新文件ID和状态
    currentFile.fileId = updateResult.data.fileId
    currentFile.status = STATUS[updateResult.data.status].value
    currentFile.chunkIndex = i
    // 检查是否上传完成
    if (updateResult.data.status == STATUS.upload_seconds.value || updateResult.data.status == STATUS.upload_finish.value) {
      currentFile.uploadProgress = 100
      console.log('上传完成')
      emit("uploadCallback")
      break
    }
  }
}
// 删除上传记录
const delUpload = (uid, index) => {
  // 从文件列表中移除该文件
  let fileIndex = fileList.value.findIndex(item => item.uid === uid)
  if (fileIndex !== -1) {
    fileList.value.splice(fileIndex, 1)
  }
}

// 暂停上传
const pauseUpload = (uid) => {
  let currentFile = getFileByUid(uid)
  if (currentFile) {
    currentFile.pause = true
  }
}

// 开始上传
const startUpload = (uid) => {
  let currentFile = getFileByUid(uid)
  if (currentFile) {
    currentFile.pause = false
    uploadFile(uid, currentFile.chunkIndex)
  }
}
</script>

<style lang="scss" scoped>
/* 定制苹果原生风格极细滚动条，替代之前的杂乱样式 */
.apple-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.apple-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(134, 134, 139, 0.3);
  border-radius: 4px;
}

.apple-scrollbar::-webkit-scrollbar-track {
  background-color: transparent;
}

.apple-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: rgba(134, 134, 139, 0.3) transparent;
}
</style>