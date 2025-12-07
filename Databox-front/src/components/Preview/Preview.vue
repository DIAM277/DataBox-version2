<template>
    <!-- 图片预览 -->
    <PreviewImage ref="imageViewRef" :imageList="imageUrls" v-if="fileInfo.fileCategory == 3"></PreviewImage>
    <!-- 其他文件类型预览框 -->
    <Window :show="windowShow" @close="closeWindow" :width="fileInfo.fileCategory == 1 ? 1500 : 900"
        :title="fileInfo.fileName" :align="fileInfo.fileCategory == 1 ? 'center' : 'top'" v-else>
        <!-- 视频预览 -->
        <PreviewVideo ref="videoPlayerRef" :url="url" v-if="fileInfo.fileCategory == 1"></PreviewVideo>
        <!-- PDF文档预览 -->
        <PreviewPdf :url="url" v-else-if="fileInfo.fileType == 4"></PreviewPdf>
        <!-- docx文档预览 -->
        <PreviewDoc :url="url" v-else-if="fileInfo.fileType == 5"></PreviewDoc>
        <!-- excel文档预览 -->
        <PreviewExcel :url="url" v-else-if="fileInfo.fileType == 6"></PreviewExcel>
        <!-- 文本文件预览 -->
        <PreviewText :url="url" v-else-if="fileInfo.fileType == 7 || fileInfo.fileType == 8"></PreviewText>
        <!-- 音乐文件预览 -->
        <PreviewMusic :url="url" v-else-if="fileInfo.fileCategory == 2" :fileName="fileInfo.fileName"></PreviewMusic>
        <!-- 无法在线预览文件 -->
        <PreviewDownload :createDownloadUrl="createDownloadUrl" :downloadUrl="downloadUrl" :fileInfo="fileInfo"
            v-if="fileInfo.fileCategory == 5 && fileInfo.fileType != 8"></PreviewDownload>
    </Window>
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

<style lang="scss" scoped></style>