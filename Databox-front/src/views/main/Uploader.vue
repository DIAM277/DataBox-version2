<template>
  <div class="uploader-panel">
    <div class="uploader-title">
      <span>上传任务</span>
      <span class="tips"> (仅展示本次上传任务)</span>
    </div>
    <div class="file-list">
      <div v-for="(item, index) in fileList" class="file-item">
        <div class="upload-panel">
          <div class="file-name">
            {{ item.fileName }}
          </div>
          <div class="progress">
            <el-progress :percentage="item.uploadProgress"
              v-if="item.status == STATUS.uploading.value || item.status == STATUS.upload_seconds.value || item.status == STATUS.upload_finish.value">
            </el-progress>
          </div>
          <div class="upload-status">
            <span :class="['iconfont', 'icon-' + STATUS[item.status].icon]"
              :style="{ color: STATUS[item.status].color }">
            </span>
            <span class="status" :style="{ color: STATUS[item.status].color }">
              {{ item.status == 'fail' ? item.errorMsg : STATUS[item.status].desc }}
            </span>
            <!-- 上传中 -->
            <span class="upload-info" v-if="item.status == STATUS.uploading.value">
              {{ proxy.Utils.size2Str(item.fileUploadSize) }} / {{ proxy.Utils.size2Str(item.totalSize) }}
            </span>
          </div>
        </div>
        <div class="op">
          <!-- md5 -->
          <el-progress type="circle" :width="50" :percentage="item.md5Progress"
            v-if="item.status == STATUS.init.value || item.status == STATUS.md5_complete.value"
            :status="item.status == STATUS.md5_complete.value ? 'success' : ''">
            <template #default="{ percentage }">
              <div class="progress-content" v-if="item.status != STATUS.md5_complete.value">
                <span class="progress-percentage">{{ percentage }}%</span>
              </div>
              <div class="progress-content" v-else>
                <span class="iconfont icon-right1" style="color: #67c23a; font-size: 20px"></span>
              </div>
            </template>
          </el-progress>
          <div class="op-btn">
            <span v-if="item.status == STATUS.uploading.value" class="action-btn-container">
              <Icon :width="28" class="btn-item action-btn" iconName="upload" v-if="item.pause" title="开始"
                @click="startUpload(item.uid)"></Icon>
              <Icon :width="28" class="btn-item action-btn" iconName="pause" v-else @click="pauseUpload(item.uid)"
                title="暂停">
              </Icon>
            </span>
            <Icon :width="28" class="delete-btn del btn-item" iconName="del"
              v-if="item.status != STATUS.init.value && item.status != STATUS.upload_finish.value && item.status != STATUS.upload_seconds.value && item.status != STATUS.md5_complete.value"
              title="删除" @click="delUpload(item.uid)">
            </Icon>
            <Icon :width="28" class="clean btn-item clean-btn" iconName="clean"
              v-if="item.status == STATUS.upload_finish.value || item.status == STATUS.upload_seconds.value"
              title="清除记录" @click="delUpload(item.uid, index)">
            </Icon>
          </div>
        </div>
      </div>
      <div v-if="fileList.length == 0">
        <NoData msg="暂无上传任务"></NoData>
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
.uploader-panel {
  background-color: #f9fafc;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  border-radius: 12px;
  max-height: 600px;

  .uploader-title {
    background: #ffffff;
    color: rgb(48, 46, 46);
    line-height: 50px;
    padding: 0px 15px;
    font-size: 16px;
    font-weight: 500;
    border-bottom: 1px solid #e0e6ed;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .tips {
      font-size: 14px;
      color: rgba(41, 38, 38, 0.89);
      font-weight: normal;
    }
  }

  .file-list {
    overflow: auto;
    padding: 10px;
    min-height: calc(100vh / 2);
    max-height: calc(600px - 50px);
    scrollbar-width: thin;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: rgba(5, 161, 245, 0.3);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
      background-color: transparent;
    }

    .file-item {
      position: relative;
      display: flex;
      justify-content: center;
      align-items: center;
      padding: 12px 15px;
      background-color: #fff;
      border-radius: 6px;
      margin-bottom: 8px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.205);
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      }
    }

    //可选样式 斑马纹
    // .file-item:nth-child(even) {
    //   background-color: #f8f9fa;
    // }

    .upload-panel {
      flex: 1;
      overflow: hidden;

      .file-name {
        color: #333;
        font-size: 14px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        padding: 0 5px;
      }

      .upload-status {
        display: flex;
        align-items: center;
        margin-top: 5px;

        .iconfont {
          margin-right: 5px;
          font-size: 12px;
        }

        .status {
          font-size: 12px;
          font-weight: 500;
        }

        .upload-info {
          margin-left: 8px;
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .op {
      width: 100px;
      display: flex;
      align-items: center;
      justify-content: flex-end;

      .progress-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
      }

      .progress-percentage {
        font-size: 14px;
        font-weight: bold;
        color: #409eff;
        line-height: 1;
      }

      .op-btn {
        display: flex;
        align-items: center;

        .btn-item {
          cursor: pointer;
          border-radius: 50%;
          color: #606266;
          transition: all 0.2s;
          width: 32px;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin: 0 4px;

          &:hover {
            transform: scale(1.1);
            box-shadow: 0 2px 6px rgba(5, 161, 245, 0.2);
          }

          &:active {
            transform: scale(0.9);
          }
        }

        .action-btn {
          color: #409eff;

          &:hover {
            color: #1989fa;
            background-color: rgba(64, 158, 255, 0.1);
          }
        }

        .delete-btn {
          color: #f56c6c !important;

          &:hover {
            color: #f56c6c;
            background-color: rgba(245, 108, 108, 0.1);
          }
        }

        .clean-btn {
          color: #67c23a;

          &:hover {
            color: #67c23a;
            background-color: rgba(103, 194, 58, 0.1);
          }
        }

        .del,
        .clean {
          margin-left: 8px;
        }
      }
    }
  }
}
</style>