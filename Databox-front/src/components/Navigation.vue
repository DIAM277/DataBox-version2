<template>
    <!-- 使用 flex-nowrap 防止过度换行，统一为 text-sm 级别灰度字体 -->
    <div
        class="flex items-center flex-nowrap gap-1 text-sm text-gray-600 dark:text-gray-400 select-none overflow-hidden">

        <!-- 返回上一级 -->
        <template v-if="folderList.length > 0">
            <span
                class="flex items-center hover:bg-gray-100 dark:hover:bg-gray-800 px-2 py-1 rounded-md transition-colors cursor-pointer shrink-0"
                @click="backParent">
                <i class="iconfont icon-back text-[12px] mr-1.5"></i>返回上一级
            </span>
            <span class="text-gray-300 dark:text-gray-700 px-1 font-light shrink-0">|</span>
        </template>

        <!-- 根目录 (不可点击状态：此时就是根目录) -->
        <span v-if="folderList.length == 0"
            class="flex items-center font-semibold text-gray-900 dark:text-gray-100 px-2 py-1 shrink-0">
            <i class="iconfont icon-all text-[#007AFF] mr-2 text-[15px]"></i>全部文件
        </span>

        <!-- 根目录 (可点击状态：说明当前在子目录中) -->
        <span v-if="folderList.length > 0"
            class="flex items-center hover:bg-gray-100 dark:hover:bg-gray-800 px-2 py-1 rounded-md transition-colors cursor-pointer shrink-0"
            @click="goToRoot">
            <i class="iconfont icon-all text-[#007AFF] mr-2 text-[15px] opacity-80"></i>全部文件
        </span>

        <!-- ================== 折叠区 ================== -->
        <template v-if="collapsedFolders.length > 0">
            <span class="iconfont icon-right text-gray-300 dark:text-gray-600 text-[10px] mx-0.5 shrink-0"></span>

            <el-dropdown trigger="hover" class="shrink-0">
                <!-- 悬浮触发块：极简圆角点阵 -->
                <span
                    class="flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-800 px-2 py-1 rounded-md transition-colors cursor-pointer tracking-widest focus:outline-none">
                    ...
                </span>

                <!-- 被折叠的历史目录菜单 -->
                <template #dropdown>
                    <el-dropdown-menu
                        class="dark:border-[#38383a] dark:bg-[#1c1c1e] rounded-xl shadow-xl min-w-[120px]">
                        <el-dropdown-item v-for="item in collapsedFolders" :key="item.originalIndex"
                            @click="setCurrentFolder(item.originalIndex)">
                            <span
                                class="text-[13px] text-gray-700 dark:text-gray-300 max-w-[200px] truncate block py-0.5">
                                {{ item.fileName }}
                            </span>
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </template>

        <!-- ================== 可见区 ================== -->
        <template v-for="(item, index) in visibleFolders" :key="item.originalIndex">
            <span class="iconfont icon-right text-gray-300 dark:text-gray-600 text-[10px] mx-0.5 shrink-0"></span>

            <!-- 不是最后一项：灰色可点击的历史路径 -->
            <span v-if="index < visibleFolders.length - 1"
                class="hover:bg-gray-100 dark:hover:bg-gray-800 px-2 py-1 rounded-md transition-colors cursor-pointer max-w-[150px] truncate shrink-0"
                @click="setCurrentFolder(item.originalIndex)">
                {{ item.fileName }}
            </span>

            <!-- 最后一项：加粗、颜色深的不可点击现层级 -->
            <span v-if="index == visibleFolders.length - 1"
                class="font-semibold text-gray-900 dark:text-gray-100 px-2 py-1 max-w-[200px] truncate shrink-0">
                {{ item.fileName }}
            </span>
        </template>

    </div>
</template>

<script setup>
// 新增引入 computed
import { ref, reactive, getCurrentInstance, nextTick, watch, computed } from "vue"
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


// ================== 新增：层级折叠控制逻辑 ==================

// 1. 生成带有原始索引的文件夹列表（防止数组截取后丢失原位关系）
const folderListWithIndex = computed(() => {
    return folderList.value.map((item, index) => ({
        ...item,
        originalIndex: index
    }));
});

// 2. 折叠区：当长度大于3时，截取 0 到 length-2 的元素
const collapsedFolders = computed(() => {
    if (folderListWithIndex.value.length > 8) {
        return folderListWithIndex.value.slice(0, folderListWithIndex.value.length - 2);
    }
    return [];
});

// 3. 可见区：当长度大于3时，截取并显示最后2个元素；否则展示所有
const visibleFolders = computed(() => {
    if (folderListWithIndex.value.length > 8) {
        return folderListWithIndex.value.slice(-4);
    }
    return folderListWithIndex.value;
});

// ==========================================================


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

// 点击导航栏 设置当前目录
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
