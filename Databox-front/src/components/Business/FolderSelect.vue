<template>
    <div>
        <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="600px"
            :showCancel="false" @close="dialogConfig.show = false" :showCustomTitle="true">

            <!-- 顶部导航区 (面包屑底板) -->
            <div
                class="bg-gray-50 dark:bg-[#1c1c1e] px-4 py-3 rounded-xl mb-4 flex items-center gap-2 text-sm font-medium text-gray-700 dark:text-gray-300">
                <Navigation ref="navigationRef" @navChange="navChange" :watchPath="false"></Navigation>
            </div>

            <!-- 目录列表展示区 -->
            <div class="max-h-[45vh] min-h-[250px] overflow-y-auto space-y-1 pr-2" v-if="folderList.length > 0">
                <div class="flex items-center gap-3 px-3 py-2.5 rounded-lg cursor-pointer transition-all text-gray-800 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 group"
                    v-for="item in folderList" :key="item.fileId" @click="selectFolder(item)">

                    <!-- 借用已经封装好的通用目录 Icon，悬浮时有微小放大反馈 -->
                    <div class="transition-transform group-hover:scale-105">
                        <Icon :fileType="0" :width="30"></Icon>
                    </div>

                    <!-- 文件夹名称 -->
                    <span
                        class="flex-1 overflow-hidden text-ellipsis whitespace-nowrap text-[14.5px] font-medium tracking-wide">
                        {{ item.fileName }}
                    </span>

                    <!-- 引导右侧右侧箭头，仅鼠标悬浮时显现 -->
                    <span
                        class="iconfont icon-right text-gray-300 dark:text-gray-600 opacity-0 group-hover:opacity-100 transition-opacity text-[12px]"></span>
                </div>
            </div>

            <!-- 没有数据时的缺省提示 -->
            <div class="flex flex-col items-center justify-center min-h-[250px] py-10 text-gray-400 dark:text-gray-500 select-none"
                v-else>
                <i class="iconfont icon-empty text-[50px] mb-3 opacity-40 dark:opacity-20"></i>
                <p class="text-[14.5px] tracking-wide">当前文件夹为空</p>
                <p class="text-[13px] mt-2 opacity-80">
                    将移动到 <span class="text-[#007AFF] font-semibold mx-1">{{ currentFolder.fileName || '根目录' }}</span>
                </p>
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
