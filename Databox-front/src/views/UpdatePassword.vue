<template>
    <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="550px"
        :showCancel="true" @close="dialogConfig.show = false" :showCustomTitle="true">
        <!-- 修改密码表单 -->
        <el-form v-if="!isEmailVerify" :model="formData" :rules="rules" ref="formDataRef" label-width="90px"
            @submit.prevent>
            <!-- 输入旧密码 -->
            <el-form-item label="旧密码" prop="oldPassword">
                <el-input type="password" v-model.trim="formData.oldPassword" size="large" placeholder="请输入旧密码"
                    show-password>
                    <template #prefix>
                        <span class="iconfont icon-password"></span>
                    </template>
                </el-input>
            </el-form-item>
            <!-- 输入新密码 -->
            <el-form-item label="新密码" prop="password">
                <el-input type="password" v-model.trim="formData.password" size="large" placeholder="请输入新密码"
                    show-password>
                    <template #prefix>
                        <span class="iconfont icon-password"></span>
                    </template>
                </el-input>
            </el-form-item>
            <!-- 确认密码 -->
            <el-form-item label="确认密码" prop="rePassword">
                <el-input type="password" v-model.trim="formData.rePassword" size="large" placeholder="请再次输入新密码"
                    show-password>
                    <template #prefix>
                        <span class="iconfont icon-password"></span>
                    </template>
                </el-input>
            </el-form-item>
        </el-form>

        <!-- 邮箱验证表单 -->
        <el-form v-else :model="formData4EmailCode" :rules="emailCodeRules" ref="formData4EmailCodeRef"
            label-width="90px" @submit.prevent>
            <el-form-item label="邮箱">
                <div class="email-display">
                    <span class="email-text">{{ userInfo.email }}</span>
                </div>
            </el-form-item>
            <el-form-item label="验证码" prop="emailCode">
                <div class="verify-code-panel">
                    <el-input size="large" clearable placeholder="请输入邮箱验证码" v-model.trim="formData4EmailCode.emailCode"
                        maxLength="6">
                        <template #prefix>
                            <span class="iconfont icon-checkcode"></span>
                        </template>
                    </el-input>
                    <el-button type="primary" :disabled="sendCodeDisabled" @click="showSendCodeDialog">
                        {{ sendCodeButtonText }}
                    </el-button>
                </div>
            </el-form-item>
            <el-form-item label="新密码" prop="password">
                <el-input type="password" v-model.trim="formData4EmailCode.password" size="large" placeholder="请输入新密码"
                    show-password>
                    <template #prefix>
                        <span class="iconfont icon-password"></span>
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="rePassword">
                <el-input type="password" v-model.trim="formData4EmailCode.rePassword" size="large"
                    placeholder="请再次输入新密码" show-password>
                    <template #prefix>
                        <span class="iconfont icon-password"></span>
                    </template>
                </el-input>
            </el-form-item>
        </el-form>

        <!-- 底部链接区域 -->
        <div class="form-footer-links">
            <div v-if="!isEmailVerify" class="link-container">
                <span class="normal-text">忘记旧密码？</span>
                <a href="javascript:void(0)" class="highlight-link" @click="switchToEmailVerify">验证邮箱📩</a>
            </div>
            <a v-else href="javascript:void(0)" class="highlight-link" @click="switchToPasswordChange">验证旧密码🔑</a>
        </div>
    </Dialog>

    <!-- 图形验证码对话框 -->
    <Dialog :show="dialogConfig4SendMailCode.show" :title="dialogConfig4SendMailCode.title"
        :buttons="dialogConfig4SendMailCode.buttons" width="500px" :showCancel="false" :showCustomTitle="true"
        @close="dialogConfig4SendMailCode.show = false">
        <el-form :model="formData4SendMailCode" :rules="emailVerifyRules" ref="formData4SendMailCodeRef"
            label-width="80px" @submit.prevent>
            <el-form-item label="验证码" prop="checkCode">
                <div class="check-code-panel">
                    <el-input size="large" clearable placeholder="请输入验证码" v-model.trim="formData4SendMailCode.checkCode"
                        maxLength="5">
                        <template #prefix>
                            <span class="iconfont icon-checkcode"></span>
                        </template>
                    </el-input>
                    <img class="check-code" :src="checkCodeUrl4SendMailCode" @click="changeCheckCode" />
                </div>
            </el-form-item>
        </el-form>
    </Dialog>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from 'vue-router';
const route = useRoute();
const router = useRouter();

const api = {
    updatePassword: 'updatePassword',
    checkCode: "/api/checkCode",
    sendEmailCode: "/sendEmailCode",
    updatePasswordByEmailCode: "/updatePasswordByEmailCode"  // 修改为后端提供的接口
}

// 是否处于邮箱验证模式
const isEmailVerify = ref(false);

// 验证确认密码
const checkRePassword = (rule, value, callback) => {
    if (value !== formData.value.password) {
        callback(new Error(rule.message));
    } else {
        callback();
    }
}

// 验证新密码不能与旧密码相同
const checkNewPassword = (rule, value, callback) => {
    if (value === formData.value.oldPassword) {
        callback(new Error(rule.message));
    } else {
        callback();
    }
}

// 验证邮箱验证码表单中的确认密码
const checkEmailCodeRePassword = (rule, value, callback) => {
    if (value !== formData4EmailCode.value.password) {
        callback(new Error(rule.message));
    } else {
        callback();
    }
}

const formData = ref({});
const formDataRef = ref();
const rules = {
    oldPassword: [
        { required: true, message: "请输入旧密码" },
        { validator: proxy.Verify.password, message: "密码长度为8-16位，必须包含数字、字母" }
    ],
    password: [
        { required: true, message: "请输入新密码" },
        { validator: proxy.Verify.password, message: "密码长度为8-16位，必须包含数字、字母" },
        { validator: checkNewPassword, message: "新密码不能与旧密码相同" }
    ],
    rePassword: [
        { required: true, message: "请再次输入新密码" },
        { validator: checkRePassword, message: "两次输入的密码不一致" }
    ],
};

// 获取用户信息
const userInfo = ref(proxy.VueCookies.get('userInfo') || {});

// 邮箱验证码对话框
const checkCodeUrl4SendMailCode = ref('');
const formData4SendMailCode = ref({});
const formData4SendMailCodeRef = ref();
const dialogConfig4SendMailCode = reactive({
    show: false,
    title: "图形验证码",
    buttons: [
        {
            type: "primary",
            text: "确定",
            click: (e) => {
                sendEmailCode();
            },
        },
    ],
});

// 邮箱验证规则
const emailVerifyRules = {
    checkCode: [
        { required: true, message: "请输入图片验证码" },
    ]
};

// 邮箱验证码输入表单
const formData4EmailCode = ref({});
const formData4EmailCodeRef = ref();

// 发送验证码按钮状态
const countdown = ref(0);
const sendCodeDisabled = computed(() => countdown.value > 0);
const sendCodeButtonText = computed(() => {
    return countdown.value > 0 ? `${countdown.value}秒后重新获取` : '获取验证码';
});

// 对话框配置
const dialogConfig = ref({
    show: false,
    title: "修改密码",
    buttons: [
        {
            type: "primary",
            text: computed(() => "修改密码"),
            click: (e) => {
                if (isEmailVerify.value) {
                    resetPasswordByEmail();
                } else {
                    submitForm();
                }
            },
        },
    ],
});

// 切换到邮箱验证模式
const switchToEmailVerify = () => {
    isEmailVerify.value = true;
    dialogConfig.value.title = "邮箱验证";
    nextTick(() => {
        if (formData4EmailCodeRef.value) {
            formData4EmailCodeRef.value.resetFields();
        }
        formData4EmailCode.value = {};
    });
}

// 切换到密码修改模式
const switchToPasswordChange = () => {
    isEmailVerify.value = false;
    dialogConfig.value.title = "修改密码";
    nextTick(() => {
        if (formDataRef.value) {
            formDataRef.value.resetFields();
        }
        formData.value = {};
    });
}

// 显示发送验证码对话框
const showSendCodeDialog = () => {
    if (sendCodeDisabled.value) return;

    dialogConfig4SendMailCode.show = true;
    nextTick(() => {
        changeCheckCode();
        formData4SendMailCodeRef.value.resetFields();
        formData4SendMailCode.value = {};
    });
}

// 切换验证码
const changeCheckCode = () => {
    checkCodeUrl4SendMailCode.value = api.checkCode + "?type=1&time=" + new Date().getTime();
    if (formData4SendMailCode.value) {
        formData4SendMailCode.value.checkCode = "";
    }
}

// 开始倒计时
const startCountdown = () => {
    countdown.value = 60;
    const timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
            clearInterval(timer);
        }
    }, 1000);
}

// 发送邮箱验证码
const sendEmailCode = () => {
    formData4SendMailCodeRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        const params = Object.assign({}, formData4SendMailCode.value);
        params.email = userInfo.value.email;
        params.type = 1; // 重置密码类型
        let result = await proxy.Request({
            url: api.sendEmailCode,
            params: params,
            errorCallback: () => {
                changeCheckCode();
            },
        });
        if (!result) {
            return;
        }
        proxy.Message.success("验证码发送成功，请前往邮箱查看");
        dialogConfig4SendMailCode.show = false;
        startCountdown(); // 开始倒计时
    });
}

// 通过邮箱验证重置密码
const resetPasswordByEmail = () => {
    formData4EmailCodeRef.value.validate(async (valid) => {
        if (!valid) {
            return;
        }
        const params = {
            email: userInfo.value.email,
            emailCode: formData4EmailCode.value.emailCode,
            password: formData4EmailCode.value.password
        };

        let result = await proxy.Request({
            url: api.updatePasswordByEmailCode,
            params: params
        });

        if (!result) {
            return;
        }

        proxy.Message.success("密码修改成功");
        dialogConfig.value.show = false;
        isEmailVerify.value = false; // 重置为密码修改模式
    });
}

const submitForm = async () => {
    formDataRef.value.validate(async (valid) => {
        if (!valid) {
            return
        }
        let result = await proxy.Request({
            url: api.updatePassword,
            params: {
                oldPassword: formData.value.oldPassword,
                newPassword: formData.value.password
            }
        })
        if (!result) {
            return;
        }
        proxy.Message.success("修改密码成功")
        dialogConfig.value.show = false;
    })
}

const show = () => {
    dialogConfig.value.show = true;
    isEmailVerify.value = false;
    dialogConfig.value.title = "修改密码";
    nextTick(() => {
        if (formDataRef.value) {
            formDataRef.value.resetFields();
        }
        formData.value = {};
        if (formData4EmailCodeRef.value) {
            formData4EmailCodeRef.value.resetFields();
        }
        formData4EmailCode.value = {};
    });
}
defineExpose({
    show
})
</script>

<style lang="scss" scoped>
.verify-code-panel {
    width: 100%;
    display: flex;
    gap: 10px;

    .el-input {
        flex: 1;
    }
}

.check-code-panel {
    width: 100%;
    display: flex;

    .check-code {
        margin-left: 5px;
        cursor: pointer;
    }
}

.email-display {
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;

    .email-text {
        font-weight: 500;
    }
}

.form-footer-links {
    margin-top: 10px;
    padding-right: 10px;
    display: flex;
    justify-content: flex-end;

    .link-container {
        display: flex;
    }

    .normal-text {
        color: #606266;
        font-size: 14px;
    }

    .highlight-link {
        color: #409EFF;
        font-size: 14px;
        text-decoration: none;

        &:hover {
            text-decoration: underline;
        }
    }
}
</style>