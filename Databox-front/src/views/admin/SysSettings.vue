<template>
    <div>
        <div class="settings-container">
            <div class="settings-card">
                <div class="card-header">
                    <span class="card-title">系统设置</span>
                </div>
                <div class="card-body">
                    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="140px" @submit.prevent>
                        <!--注册邮箱验证码标题-->
                        <el-form-item label="注册邮箱标题" prop="registerEmailTitle">
                            <el-input clearable placeholder="请输入邮箱验证码邮件标题" type="textarea" :rows="3" :maxlength="150"
                                show-word-limit v-model.trim="formData.registerEmailTitle"></el-input>
                        </el-form-item>
                        <!-- 注册邮箱内容 -->
                        <el-form-item label="注册邮箱内容" prop="registerEmailContent">
                            <el-input clearable placeholder="请输入邮箱验证码邮件内容" type="textarea" :rows="3" :maxlength="150"
                                show-word-limit v-model.trim="formData.registerEmailContent"></el-input>
                        </el-form-item>
                        <!-- 新用户初始空间大小 -->
                        <el-form-item label="新用户初始空间大小" prop="userInitUseSpace">
                            <el-input clearable placeholder="请输入新用户初始空间大小" v-model.trim="formData.userInitUseSpace">
                                <template #suffix>MB</template>
                            </el-input>
                            <div class="form-item-tip">新注册用户将获得的初始存储空间大小</div>
                        </el-form-item>
                        <!-- 按钮区域 -->
                        <el-form-item>
                            <div class="btn-group">
                                <el-button type="primary" @click="saveSettings" class="custom-btn">
                                    <span class="iconfont icon-save"></span>
                                    保存设置
                                </el-button>
                                <el-button type="info" class="custom-btn refresh-btn" @click="getSysSettings">
                                    <span class="iconfont icon-refresh"></span>
                                    刷新
                                </el-button>
                            </div>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import request from "@/utils/Request";
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();

const api = {
    getSysSettings: '/admin/getSysSetting',
    saveSettings: '/admin/saveSysSetting'
}

const formData = ref({});
const formDataRef = ref();
const rules = {
    registerEmailTitle: [
        { required: true, message: "请输入邮箱验证码邮件标题" }
    ],
    registerEmailContent: [
        { required: true, message: "请输入邮箱验证码邮件内容" }
    ],
    userInitUseSpace: [
        { required: true, message: "请输入新用户初始空间大小" },
        { validator: proxy.Verify.number, message: "请输入数字" }
    ]
};

const getSysSettings = async () => {
    let result = await proxy.Request({
        url: api.getSysSettings
    })
    if (!result) {
        return;
    }
    formData.value = result.data;
}
getSysSettings();

const saveSettings = async () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {}
        Object.assign(params, formData.value);
        let result = await proxy.Request({
            url: api.saveSettings,
            params: params
        })
        if (!result) {
            return;
        }
        proxy.Message.success('保存成功');
        getSysSettings();
    })
}

</script>

<style lang="scss" scoped>
.settings-container {
    margin-top: 100px;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center; /* 添加垂直居中 */
}

.settings-card {
    width: 800px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.card-header {
    padding: 15px 20px;
    border-bottom: 1px solid #ebeef5;
    background-color: #f5f7fa;
    position: relative;

    &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 20px;
        width: 50px;
        height: 3px;
        background-color: #409EFF;
        border-radius: 3px;
    }
}

.card-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
}

.card-body {
    padding: 20px;
}

.form-item-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
}

.btn-group {
    display: flex;
    gap: 10px;
}

.custom-btn {
    display: flex;
    align-items: center;

    .iconfont {
        margin-right: 5px;
    }
}

:deep(.el-form-item__label) {
    font-weight: 500;
}

:deep(.el-textarea__inner) {
    border-radius: 4px;
    border: 1px solid #dcdfe6;

    &:focus {
        border-color: #409EFF;
    }
}

:deep(.el-input__inner) {
    border-radius: 4px;
    height: 40px;
}
</style>