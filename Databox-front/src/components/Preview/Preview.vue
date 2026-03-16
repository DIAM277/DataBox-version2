<template>
    <!-- 图片预览 (它自身已包含完美的 el-image-viewer 遮罩，保持独立控制) -->
    <PreviewImage ref="imageViewRef" :imageList="imageUrls" v-if="fileInfo.fileCategory == 3"></PreviewImage>

    <!-- macOS Quick Look 级别沉浸式全屏预览外壳 (针对所有非图片的类型文件) -->
    <Transition name="fade">
        <div v-if="windowShow && fileInfo.fileCategory != 3"
            class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/90 backdrop-blur-md transition-all duration-300">

            <!-- 极简流线型：顶部文件名指示器 -->
            <div
                class="absolute top-6 left-1/2 -translate-x-1/2 px-6 py-2 rounded-full bg-white/5 backdrop-blur-xl border border-white/10 z-50 shadow-2xl pointer-events-none select-none">
                <span class="text-white/90 text-[14.5px] font-medium tracking-wide">{{ fileInfo.fileName }}</span>
            </div>

            <!-- 右上角：全局控制栏 -->
            <div class="absolute top-6 right-6 flex items-center gap-4 z-50">
                <!-- 醒目的高反差阻尼感关闭按钮 -->
                <div @click="closeWindow"
                    class="w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center cursor-pointer backdrop-blur-md transition-all ease-out duration-200 hover:scale-110 shadow-lg border border-white/10">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2"
                        stroke="currentColor" class="w-5 h-5">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                </div>
            </div>

            <!-- 核心预览内容容器 (交给组件自己决定背板与表现) -->
            <div class="w-full h-full max-w-[90vw] max-h-[90vh] flex items-center justify-center p-4 md:p-8 relative">

                <!-- 视频预览 (需要释放全部宽度) -->
                <PreviewVideo ref="videoPlayerRef" :url="url" v-if="fileInfo.fileCategory == 1" class="w-full">
                </PreviewVideo>

                <!-- PDF文档预览 -->
                <PreviewPdf :url="url" v-else-if="fileInfo.fileType == 4"
                    class="w-full h-full overflow-auto rounded-xl shadow-2xl"></PreviewPdf>

                <!-- docx文档预览 -->
                <PreviewDoc :url="url" v-else-if="fileInfo.fileType == 5"
                    class="w-full h-full overflow-auto rounded-xl shadow-2xl max-w-6xl"></PreviewDoc>

                <!-- excel文档预览 -->
                <PreviewExcel :url="url" v-else-if="fileInfo.fileType == 6"
                    class="w-full h-full overflow-hidden rounded-xl shadow-2xl max-w-7xl"></PreviewExcel>

                <!-- 文本文件与代码预览 -->
                <PreviewText :url="url" v-else-if="fileInfo.fileType == 7 || fileInfo.fileType == 8"
                    class="w-full h-full overflow-hidden rounded-xl shadow-2xl max-w-6xl"></PreviewText>

                <!-- 音乐文件预览 (组件本身是自带背板效果的控制盘) -->
                <PreviewMusic :url="url" v-else-if="fileInfo.fileCategory == 2" :fileName="fileInfo.fileName">
                </PreviewMusic>

                <!-- 无法在线预览的其他文件 (引导下载面板) -->
                <PreviewDownload :createDownloadUrl="createDownloadUrl" :downloadUrl="downloadUrl" :fileInfo="fileInfo"
                    v-if="fileInfo.fileCategory == 5 && fileInfo.fileType != 8" class="rounded-3xl overflow-hidden">
                </PreviewDownload>

            </div>
        </div>
    </Transition>
</template>

<script setup>
import PreviewDownload from "./PreviewDownload.vue";
import PreviewMusic from "./PreviewMusic.vue";
import PreviewPdf from "./PreviewPdf.vue";
import PreviewExcel from "./PreviewExcel.vue";
import PreviewVideo from "./PreviewVideo.vue";
import PreviewImage from "./PreviewImage.vue";
import PreviewDoc from "./PreviewDoc.vue";
import PreviewText from "./PreviewText.vue";
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const FILE_URL_MAP = {
    0: {
        fileUrl: '/file/getFile',
        videoUrl: '/file/ts/getVideoInfo',
        createDownloadUrl: '/file/createDownloadUrl',
        downloadUrl: '/api/file/download',
        getFolderImage: '/file/getFolderImage'
    },
    1: {
        fileUrl: '/admin/getFile',
        videoUrl: '/admin/ts/getVideoInfo',
        createDownloadUrl: '/admin/createDownloadUrl',
        downloadUrl: '/api/admin/download',
        getFolderImage: '/admin/getFolderImage'
    },
    2: {
        fileUrl: '/showShare/getFile',
        videoUrl: '/showShare/ts/getVideoInfo',
        createDownloadUrl: '/showShare/createDownloadUrl',
        downloadUrl: '/api/showShare/download',
        getFolderImage: '/showShare/getFolderImage'
    }
}

// 预览框
const windowShow = ref()
const videoPlayerRef = ref(null)

// 关闭窗口时销毁视频播放器
const closeWindow = () => {
    // 如果是视频，先销毁播放器
    if (fileInfo.value.fileCategory == 1 && videoPlayerRef.value) {
        videoPlayerRef.value.destroyPlayer();
    }
    windowShow.value = false
}

// 图片URL列表
const imageUrls = ref([])
// 当前图片在列表中的索引
const currentImageIndex = ref(0)

const fileInfo = ref({})
const imageViewRef = ref()
const url = ref(null)
// 创建下载链接
const createDownloadUrl = ref(null)
// 下载链接
const downloadUrl = ref(null)

// 文件预览
const showReview = async (data, showPart) => {
    fileInfo.value = data
    if (data.fileCategory == 3) {
        // 获取当前目录下的所有图片
        try {
            const url = FILE_URL_MAP[showPart || 0].getFolderImage
            const result = await proxy.Request({
                url,
                params: {
                    filePid: data.filePid
                }
            })

            if (result && result.data) {
                // 处理图片列表
                imageUrls.value = result.data.map(item =>
                    proxy.globalInfo.imageUrl + item.fileCover.replaceAll("_.", ".")
                )

                // 找到当前图片在列表中的索引
                const currentUrl = proxy.globalInfo.imageUrl + data.fileCover.replaceAll("_.", ".")
                currentImageIndex.value = imageUrls.value.findIndex(url => url === currentUrl)
                if (currentImageIndex.value === -1) {
                    currentImageIndex.value = 0
                }

                // 显示图片预览
                nextTick(() => {
                    imageViewRef.value.show(currentImageIndex.value)
                })
            } else {
                // 如果接口失败，则只显示当前图片
                imageUrls.value = [proxy.globalInfo.imageUrl + data.fileCover.replaceAll("_.", ".")]
                nextTick(() => {
                    imageViewRef.value.show(0)
                })
            }
        } catch (e) {
            console.error("获取文件夹图片失败", e)
            // 出错时只显示当前图片
            imageUrls.value = [proxy.globalInfo.imageUrl + data.fileCover.replaceAll("_.", ".")]
            nextTick(() => {
                imageViewRef.value.show(0)
            })
        }
    } else {
        // 非图片文件预览
        windowShow.value = true
        let _url = FILE_URL_MAP[showPart].fileUrl
        let _createDownloadUrl = FILE_URL_MAP[showPart].createDownloadUrl
        let _downloadUrl = FILE_URL_MAP[showPart].downloadUrl

        if (data.fileCategory == 1) {
            _url = FILE_URL_MAP[showPart].videoUrl
        }
        if (showPart == 0) {
            _url = _url + '/' + data.fileId
            _createDownloadUrl = _createDownloadUrl + '/' + data.fileId
        } else if (showPart == 1) {
            _url = _url + '/' + data.userId + '/' + data.fileId
            _createDownloadUrl = _createDownloadUrl + '/' + data.userId + '/' + data.fileId
        } else if (showPart == 2) {
            _url = _url + '/' + data.shareId + '/' + data.fileId
            _createDownloadUrl = _createDownloadUrl + '/' + data.shareId + '/' + data.fileId
        }
        url.value = _url
        createDownloadUrl.value = _createDownloadUrl
        downloadUrl.value = _downloadUrl
    }
}


defineExpose({
    showReview
})
</script>

<style scoped>
/* 原生 macOS 级别的平滑淡入淡出过场动画 */
.fade-enter-active,
.fade-leave-active {
    transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: scale(0.98);
    /* 加入超级轻微的缩放回弹，带来物理阻尼感 */
}
</style>