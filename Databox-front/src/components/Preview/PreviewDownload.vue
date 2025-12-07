<template>
    <div class="others">
        <div class="body-content">
            <div class="icon-container">
                <Icon :iconName="fileInfo.fileType == 9 ? 'zip' : 'others'" :width="100"></Icon>
            </div>
            <div class="file-name">{{ fileInfo.fileName }}</div>
            <div class="tips">该类型文件暂不支持在线预览，请下载后查看。</div>
            <div class="download-btn">
                <el-button type="primary" @click="download">
                    <i class="iconfont icon-download"></i>
                    点击下载 {{ proxy.Utils.size2Str(fileInfo.fileSize) }}
                </el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
    createDownloadUrl: {
        type: String
    },
    downloadUrl: {
        type: String
    },
    fileInfo: {
        type: Object
    }
})

// 下载文件
const download = async () => {
    let result = await proxy.Request({
        url: props.createDownloadUrl,
    })
    if (!result) {
        return;
    }
    window.location.href = props.downloadUrl + '/' + result.data
}
</script>

<style lang="scss" scoped>
.others {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    min-height: 300px;
    padding: 20px;

    .body-content {
        text-align: center;
        background-color: #ffffff;
        border-radius: 8px;
        padding: 30px;
        max-width: 500px;
        width: 100%;

        .icon-container {
            margin-bottom: 15px;
        }

        .file-name {
            font-weight: bold;
            font-size: 18px;
            margin-bottom: 10px;
            word-break: break-all;
            color: #303133;
        }

        .tips {
            color: #909399;
            margin: 15px 0;
            font-size: 14px;
            line-height: 1.5;
        }

        .download-btn {
            margin-top: 20px;

            .el-button {
                padding: 12px 20px;

                i {
                    margin-right: 5px;
                }
            }
        }
    }
}
</style>