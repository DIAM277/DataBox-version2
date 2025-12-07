<template>
    <div>
        <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="800px"
            :showCancel="false" @close="dialogConfig.show = false" :showCustomTitle="true">
            <div>
                <Navigation ref="navigationRef" @navChange="navChange" :watchPath="false"></Navigation>
            </div>
            <div class="folder-list" v-if="folderList.length > 0">
                <div class="folder-item" v-for="item in folderList" @click="selectFolder(item)">
                    <Icon :fileType="0"></Icon>
                    <span class="file-name">{{ item.fileName }}</span>
                </div>
            </div>
            <div class="empty-folder-tip" v-else>
                <i class="iconfont icon-empty"></i>
                <p>当前文件夹为空</p>
                <p class="sub-tip">移动到 <span>{{ currentFolder.fileName || '根目录' }}</span></p>
            </div>
        </Dialog>
    </div>
</template>
<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const api = {
    loadAllFolder: '/file/loadAllFolder'
}

const dialogConfig = ref({
    show: false,
    title: "移动到",
    buttons: [
        {
            type: "primary",
            text: "移动到此",
            click: (e) => {
                folderSelect()
            },
        },
    ],
});

// 父级id
const filePid = ref('0')
const currentFileIds = ref()
const folderList = ref([])
const currentFolder = ref({})

// 获取所有文件夹
const loadAllFolder = async () => {
    let result = await proxy.Request({
        showLoading: false,
        url: api.loadAllFolder,
        params: {
            filePid: filePid.value,
            // 过滤当前文件所在的文件夹
            //currentFileIds: currentFileIds.value
        }
    })
    if (!result) {
        return;
    }
    folderList.value = result.data
}

// 关闭dialog
const close = () => {
    dialogConfig.value.show = false
    // 重置状态
    resetState()
}

// 重置状态
const resetState = () => {
    // 重置当前目录到根目录
    filePid.value = '0'
    currentFolder.value = {}

    // 重置导航组件
    if (navigationRef.value) {
        navigationRef.value.goToRoot()
    }
}

// 展示文件夹选择弹窗
const showFolderDialog = (currentFolder) => {
    dialogConfig.value.show = true
    currentFileIds.value = currentFolder
    // 确保每次打开对话框时都是从根目录开始
    resetState()
    loadAllFolder()
}
defineExpose({
    showFolderDialog,
    close
})

const navigationRef = ref()
// 选择目录进入
const selectFolder = (data) => {
    navigationRef.value.openFolder(data)
}

// 确定选择目标目录
const emit = defineEmits(["folderSelect"])
const folderSelect = () => {
    emit("folderSelect", filePid.value)
}

// 导航变化回调
const navChange = (data) => {
    const { curFolder } = data
    currentFolder.value = curFolder
    filePid.value = curFolder.fileId
    loadAllFolder()
}
</script>

<style lang="scss" scoped>
.navigation-panel {
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 6px;
    margin-bottom: 15px;
    display: flex;
    align-items: center;

    .nav-item {
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #606266;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
            color: #409EFF;
        }

        i {
            margin-right: 6px;
            font-size: 16px;
        }
    }
}

.folder-list {
    max-height: calc(100vh - 250px);
    min-height: 250px;
    overflow-y: auto;
    border-radius: 6px;
    border: 1px solid #ebeef5;

    &::-webkit-scrollbar {
        width: 6px;
    }

    &::-webkit-scrollbar-thumb {
        background-color: #c0c4cc;
        border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
        background-color: #f5f7fa;
    }

    .folder-item {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #ebeef5;
        cursor: pointer;
        transition: all 0.2s ease;

        &:last-child {
            border-bottom: none;
        }

        .file-name {
            display: inline-block;
            margin-left: 12px;
            font-size: 14px;
            color: #303133;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        &:hover {
            background: #f0f7ff;
        }

        &:active {
            background: #e6f1fc;
        }
    }
}

.empty-folder-tip {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 250px;
    color: #909399;

    i {
        font-size: 48px;
        margin-bottom: 16px;
        color: #c0c4cc;
    }

    p {
        margin: 5px 0;
        font-size: 14px;
    }

    .sub-tip {
        font-size: 14px;

        span {
            color: #409EFF;
            font-weight: 500;
            vertical-align: baseline; // 设置基线对齐
        }
    }
}

:deep(.el-dialog__body) {
    padding: 20px 24px;
}

:deep(.el-button--primary) {
    background-color: #409EFF;
    border-color: #409EFF;

    &:hover,
    &:focus {
        background-color: #66b1ff;
        border-color: #66b1ff;
    }

    &:active {
        background-color: #3a8ee6;
        border-color: #3a8ee6;
    }
}
</style>