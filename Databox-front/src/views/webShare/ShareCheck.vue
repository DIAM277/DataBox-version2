<template>
    <div class="share">
        <div class="body-content">
            <div class="logo">
                <AppTitle2 title="DataBox" @click.stop="jumpToHome" />
            </div>
            <div class="code-panel">
                <div class="file-info">
                    <div class="avatar">
                        <Avatar :userId="shareInfo.userId" :avatar="shareInfo.avatar" :width="50" :interactive="false">
                        </Avatar>
                    </div>
                    <div class="share-info">
                        <div class="user-info">
                            <span class="user-name">{{ shareInfo.userName }}</span>
                            <span class="share-time">分享于：{{ shareInfo.shareTime }}</span>
                        </div>
                        <div class="file-name">
                            <i class="iconfont icon-file"></i> 分享文件：{{ shareInfo.fileName }}
                        </div>
                    </div>
                </div>
                <div class="code-body">
                    <div class="tips">请输入提取码：</div>
                    <div class="input-area">
                        <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
                            <el-form-item prop="code">
                                <div class="input-group">
                                    <el-input clearable v-model.trim="formData.code" @keyup.enter="checkShare"
                                        class="input" placeholder="请输入提取码">
                                        <template #prefix>
                                            <i class="iconfont icon-lock"></i>
                                        </template>
                                    </el-input>
                                    <el-button type="primary" @click="checkShare" class="submit-btn">提取文件</el-button>
                                </div>
                            </el-form-item>
                        </el-form>
                    </div>
                </div>
            </div>
            <div class="footer">
                <p>DataBox - 安全、高效的文件分享平台</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from 'vue-router';
const route = useRoute();
const router = useRouter();
import AppTitle2 from "@/components/AppTitle/AppTitle2.vue";

const api = {
    getShareInfo: '/showShare/getShareInfo',
    checkShareCode: '/showShare/checkShareCode'
}

const shareId = route.params.shareId
const shareInfo = ref({})

// 获取分享文件信息
const getShareInfo = async () => {
    let result = await proxy.Request({
        url: api.getShareInfo,
        showLoading: false,
        params: {
            shareId
        }
    })
    if (!result) {
        return;
    }
    shareInfo.value = result.data
}
getShareInfo()

const formData = ref({});
const formDataRef = ref();
const rules = {
    code: [
        { required: true, message: "请输入提取码" },
        { min: 5, max: 5, message: "提取码长度为5位" }
    ]
};

// 检验文件提取码
const checkShare = () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        let params = {}
        Object.assign(params, formData.value)
        let result = await proxy.Request({
            url: api.checkShareCode,
            params: {
                shareId: shareId,
                code: formData.value.code
            }
        })
        if (!result) {
            return;
        }
        proxy.Message.success('文件提取成功')
        router.push(`/share/${shareId}`)
    })
}

const jumpToHome = () => {
    router.push('/')
}
</script>

<style lang="scss" scoped>
.share {
    user-select: none;
    height: 100vh;
    // 背景图
    // background: url("../../assets/background/login_bg7.jpg");
    // 背景色
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
    background-repeat: no-repeat;
    background-size: cover;
    display: flex;
    justify-content: center;
    align-items: center;

    .body-content {
        width: 550px;
        display: flex;
        flex-direction: column;
        align-items: center;
        transform: translateY(-5%);

        .logo {
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 30px;

            :deep(.app-title) {
                transform: scale(1.2);
            }
        }

        .code-panel {
            width: 100%;
            background: #fff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;

            &:hover {
                box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
                transform: translateY(-5px);
            }

            .file-info {
                padding: 20px;
                background: linear-gradient(90deg, #409eff, #64b5f6);
                color: #fff;
                display: flex;
                align-items: center;

                .avatar {
                    margin-right: 15px;

                    :deep(.el-avatar) {
                        border: 3px solid rgba(255, 255, 255, 0.7);
                        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
                    }
                }

                .share-info {
                    flex: 1;

                    .user-info {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;

                        .user-name {
                            font-size: 16px;
                            font-weight: 600;
                        }

                        .share-time {
                            font-size: 14px;
                            opacity: 0.8;
                        }
                    }

                    .file-name {
                        margin-top: 12px;
                        font-size: 14px;
                        display: flex;
                        align-items: center;

                        .iconfont {
                            margin-right: 5px;
                            font-size: 16px;
                        }
                    }
                }
            }

            .code-body {
                padding: 30px 25px 40px;

                .tips {
                    font-weight: 600;
                    font-size: 16px;
                    color: #333;
                    margin-bottom: 15px;
                }

                .input-area {
                    width: 100%;

                    .input-group {
                        display: flex;
                        align-items: center;
                        width: 100%;

                        .input {
                            flex: 1;

                            :deep(.el-input__inner) {
                                height: 35px;
                                border-radius: 4px;
                            }
                        }

                        .submit-btn {
                            margin-left: 10px;
                            height: 35px;
                            width: 120px;
                            border-radius: 4px;
                            padding: 0 15px;
                            font-size: 15px;
                            font-weight: 500;
                            transition: all 0.3s;

                            &:hover {
                                transform: translateY(-2px);
                                box-shadow: 0 5px 15px rgba(64, 158, 255, 0.3);
                            }
                        }
                    }
                }
            }
        }

        .footer {
            margin-top: 25px;
            text-align: center;
            color: #505256b3;
            font-size: 14px;
            opacity: 0.7;
        }
    }
}
</style>
