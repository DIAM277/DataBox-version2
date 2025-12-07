<template>
    <div class="top-navigation">
        <template v-if="folderList.length > 0">
            <span class="back link" @click="backParent">
                <i class="iconfont icon-back"></i>
                返回上一级
            </span>
            <el-divider direction="vertical"></el-divider>
        </template>
        <span v-if="folderList.length == 0" class="all-file">
            <i class="iconfont icon-folder"></i>
            全部文件
        </span>
        <span v-if="folderList.length > 0" class="link all-file-link" @click="goToRoot">
            <i class="iconfont icon-folder"></i>
            全部文件
        </span>
        <template v-for="(item, index) in folderList">
            <span class="iconfont icon-right nav-arrow"></span>
            <span class="link folder-item" v-if="index < folderList.length - 1" @click="setCurrentFolder(index)">
                {{ item.fileName }}
            </span>
            <span v-if="index == folderList.length - 1" class="text current-folder">
                {{ item.fileName }}
            </span>
        </template>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, watch } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from 'vue-router';
const route = useRoute();
const router = useRouter();

const api = {
    getFolderInfo: '/file/getFolderInfo',
    getFolderInfo4Share: '/showShare/getFolderInfo',
    getFolderInfo4Admin: '/admin/getFolderInfo'
}

// 分类
const category = ref()

const props = defineProps({
    watchPath: {
        type: Boolean,
        default: true
    },
    shareId: {
        type: String
    },
    adminShow: {
        type: Boolean,
        default: false
    }
})

// 目录集合
const folderList = ref([])
// 当前目录
const currentFolder = ref({ fileId: '0' })

const init = () => {
    folderList.value = []
    currentFolder.value = { fileId: '0' }
    doCallback()
}

// 对外打开文件夹操作
const openFolder = (data) => {
    const { fileId, fileName } = data
    const folder = {
        fileName: fileName,
        fileId: fileId
    }
    folderList.value.push(folder)
    currentFolder.value = folder
    setPath()
}

//点击导航栏 设置当前目录
const setCurrentFolder = (index) => {
    // 确保索引有效
    if (index >= 0 && index < folderList.value.length) {
        // 获取选中的文件夹
        const selectedFolder = folderList.value[index]
        // 更新当前文件夹
        currentFolder.value = selectedFolder
        // 截断文件夹列表，只保留到选中的文件夹
        folderList.value = folderList.value.slice(0, index + 1)
        // 更新路径
        setPath()
        // 触发回调
        doCallback()
    }
}

// 设置网页路径
const setPath = () => {
    if (!props.watchPath) {
        doCallback()
        return
    }
    let pathArray = []
    folderList.value.forEach(item => {
        pathArray.push(item.fileId)
    })
    router.push({
        path: route.path,
        query: pathArray.length == 0 ? '' : { path: pathArray.join('/') }
    })
}

// 获取当前路径的导航栏目录
const getNavigationFolder = async (path) => {
    let url = api.getFolderInfo
    if (props.shareId) {
        url = api.getFolderInfo4Share
    }
    if (props.adminShow) {
        url = api.getFolderInfo4Admin
    }
    let result = await proxy.Request({
        url: url,
        showLoading: false,
        params: {
            path: path,
            shareId: props.shareId,
        }
    })
    if (!result) {
        return;
    }
    folderList.value = result.data
}

const emit = defineEmits(['navChange'])
const doCallback = () => {
    emit("navChange", {
        categoryId: category.value,
        curFolder: currentFolder.value
    })
}

watch(() => route, (newVal, oldVal) => {
    if (!props.watchPath) {
        return
    }
    if (newVal.path.indexOf("/main") === -1 &&
        newVal.path.indexOf("/settings/fileList") === -1 &&
        newVal.path.indexOf("/share/") === -1) {
        return
    }
    const path = newVal.query.path
    category.value = newVal.params.category
    if (path === undefined) {
        init()
    } else {
        getNavigationFolder(path)
        let pathArray = path.split('/')
        currentFolder.value = { fileId: pathArray[pathArray.length - 1] }
        doCallback()
    }
}, { immediate: true, deep: true });

// 返回根目录
const goToRoot = () => {
    folderList.value = []
    currentFolder.value = { fileId: '0' }
    setPath()
}

// 返回上一级
const backParent = () => {
    if (folderList.value.length > 0) {
        folderList.value.pop()
        if (folderList.value.length > 0) {
            currentFolder.value = folderList.value[folderList.value.length - 1]
        } else {
            currentFolder.value = { fileId: '0' }
        }
        setPath()
    }
}
defineExpose({ openFolder, goToRoot })
</script>

<style lang="scss" scoped>
.top-navigation {
    font-size: 14px;
    display: flex;
    align-items: center;
    line-height: 40px;
    background-color: #f5f7fac0;
    padding: 0 15px;
    border-radius: 6px;
    margin-bottom: 15px;
    margin-right: 20px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    flex-wrap: wrap; // 允许内容换行
    overflow: hidden; // 防止溢出

    .all-file {
        font-weight: 600;
        display: flex;
        align-items: center;

        i {
            margin-right: 5px;
            font-size: 16px;
            color: #409EFF;
        }
    }

    .all-file-link {
        font-weight: 500;
        display: flex;
        align-items: center;

        i {
            margin-right: 5px;
            font-size: 16px;
        }
    }

    .link {
        color: #409EFF;
        cursor: pointer;
        transition: all 0.2s ease;
        padding: 0 5px;
        border-radius: 4px;

        &:hover {
            background-color: #ecf5ff;
            color: #66b1ff;
        }

        &:active {
            color: #3a8ee6;
        }
    }

    .back {
        display: flex;
        align-items: center;

        i {
            margin-right: 4px;
            font-size: 14px;
        }
    }

    .folder-item {
        max-width: 150px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        display: inline-block;
    }

    .nav-arrow {
        color: #909399;
        padding: 0 5px;
        font-size: 12px;
    }

    .current-folder {
        color: #606266;
        font-weight: 500;
        max-width: 200px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        display: inline-block;
    }

    :deep(.el-divider--vertical) {
        margin: 0 10px;
        height: 1em;
    }
}
</style>
