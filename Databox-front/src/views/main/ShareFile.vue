<template>
    <div>
        <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="600px"
            :showCancel="showCancel" @close="dialogConfig.show = false" :showCustomTitle="true">
            <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="100px" @submit.prevent>
                <!--分享文件名-->
                <el-form-item label="文件名" class="file-name-item">
                    {{ formData.fileName }}
                </el-form-item>
                <template v-if="showType == 0">
                    <!-- 分享有效期 -->
                    <el-form-item label="有效期" prop="validType">
                        <el-radio-group v-model="formData.validType">
                            <el-radio :label="0">1天</el-radio>
                            <el-radio :label="1">7天</el-radio>
                            <el-radio :label="2">30天</el-radio>
                            <el-radio :label="3">永久有效</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <!-- 提取码 -->
                    <el-form-item label="提取码" prop="codeType">
                        <el-radio-group v-model="formData.codeType" @change="handleCodeTypeChange">
                            <el-radio :label="0">自定义</el-radio>
                            <el-radio :label="1">系统生成</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <!-- 自定义提取码 -->
                    <el-form-item prop="code" v-if="formData.codeType == 0">
                        <el-input clearable placeholder="请输入5位提取码" v-model="formData.code" maxLength="5"
                            class="code-input">
                        </el-input>
                    </el-form-item>
                </template>
                <template v-else>
                    <el-form-item label="分享链接" class="share-result-item">
                        <div class="share-link">{{ shareUrl }}{{ resultInfo.shareId }}</div>
                    </el-form-item>
                    <el-form-item label="提取码" class="share-result-item">
                        <div class="extraction-code">{{ resultInfo.code }}</div>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="copy" class="copy-btn">
                            <i class="el-icon-document-copy"></i> 复制链接
                        </el-button>
                    </el-form-item>
                </template>
            </el-form>
        </Dialog>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import useClipboard from "vue-clipboard3"
const { toClipboard } = useClipboard()

const api = {
    shareFile: '/share/shareFile'
}

// 分享连接
const shareUrl = ref(document.location.origin + '/share/')
// 展示状态(0分享前:显示分享信息表单 1分享后:显示分享链接)
const showType = ref(0)

const formData = ref({});
const formDataRef = ref();
const rules = {
    validType: [{ required: true, message: "请选择有效期" }],
    codeType: [{ required: true, message: "请选择提取码类型" }],
    code: [
        { required: true, message: "请输入提取码" },
        { validator: proxy.Verify.shareCode, message: "请输入提取码" },
        { min: 5, message: "提取码长度不能小于5" }
    ]
};

// 处理提取码类型变更
const handleCodeTypeChange = (value) => {
    if (value === 1) {
        // 系统生成提取码时，清空自定义提取码
        formData.value.code = '';
        // 动态移除code字段的校验规则
        formDataRef.value.clearValidate('code');
    }
}

// 显示取消按钮
const showCancel = ref(true);
const dialogConfig = ref({
    show: false,
    title: "分享文件",
    buttons: [
        {
            type: "primary",
            text: "确定",
            click: (e) => {
                share()
            },
        },
    ],
});

// 分享弹框初始化
const show = (data) => {
    // 弹框状态
    showType.value = 0
    // 是否显示弹框
    dialogConfig.value.show = true
    // 是否显示取消按钮
    showCancel.value = true
    // 分享结果
    resultInfo.value = {}
    nextTick(() => {
        formDataRef.value.resetFields();
        formData.value = Object.assign({}, data);
    });
}

// 分享结果
const resultInfo = ref({})
// 分享操作
const share = async () => {
    if (Object.keys(resultInfo.value).length > 0) {
        dialogConfig.value.show = false
        return
    }
    // 如果是系统生成提取码，则不校验code字段
    if (formData.value.codeType === 1) {
        formDataRef.value.clearValidate('code');
    }
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return
        }
        let params = {}
        Object.assign(params, formData.value);
        let result = await proxy.Request({
            url: api.shareFile,
            params: params
        })
        if (!result) {
            return;
        }
        showType.value = 1
        resultInfo.value = result.data
        dialogConfig.value.buttons[0].text = '关闭'
        showCancel.value = false
        proxy.Message.success("分享成功");
    })
}

// 复制分享信息
const copy = async () => {
    await toClipboard(`链接:${shareUrl.value}${resultInfo.value.shareId} 提取码:${resultInfo.value.code}`);
    proxy.Message.success("复制分享成功");
}
defineExpose({ show });
</script>

<style lang="scss" scoped>
.file-name-item {
    margin-bottom: 20px;
    font-weight: bold;
    color: #303133;
    word-break: break-all;
}

.code-input {
    width: 180px;
}

.share-result-item {
    margin-bottom: 15px;

    .share-link,
    .extraction-code {
        padding: 8px 12px;
        background-color: #f5f7fa;
        border-radius: 4px;
        word-break: break-all;
        font-family: monospace;
        color: #606266;
        border: 1px solid #e4e7ed;
    }

    .extraction-code {
        display: inline-block;
        font-weight: bold;
        color: #409EFF;
        letter-spacing: 1px;
    }
}

.copy-btn {
    margin-top: 10px;
    padding: 10px 20px;

    i {
        margin-right: 5px;
    }
}

:deep(.el-radio) {
    margin-right: 20px;
    margin-bottom: 5px;
}

:deep(.el-form-item__label) {
    font-weight: 500;
}

:deep(.el-dialog__body) {
    padding: 20px 24px;
}
</style>
