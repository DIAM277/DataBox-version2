<template>
    <!-- 修改密码/验证邮箱 主弹窗 -->
    <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="500px"
        :showCancel="true" @close="dialogConfig.show = false" :showCustomTitle="true">

        <!-- 修改密码表单 -->
        <el-form v-if="!isEmailVerify" :model="formData" :rules="rules" ref="formDataRef" label-width="90px"
            @submit.prevent class="px-2 py-2">
            <!-- 输入旧密码 -->
            <el-form-item label="旧密码" prop="oldPassword">
                <el-input type="password" v-model.trim="formData.oldPassword" size="large" placeholder="请输入旧密码"
                    show-password>
                    <template #prefix><span class="iconfont icon-password text-gray-400"></span></template>
                </el-input>
            </el-form-item>
            <!-- 输入新密码 -->
            <el-form-item label="新密码" prop="password">
                <el-input type="password" v-model.trim="formData.password" size="large" placeholder="请输入新密码"
                    show-password>
                    <template #prefix><span class="iconfont icon-password text-gray-400"></span></template>
                </el-input>
            </el-form-item>
            <!-- 确认密码 -->
            <el-form-item label="确认密码" prop="rePassword">
                <el-input type="password" v-model.trim="formData.rePassword" size="large" placeholder="请再次输入新密码"
                    show-password>
                    <template #prefix><span class="iconfont icon-password text-gray-400"></span></template>
                </el-input>
            </el-form-item>
        </el-form>

        <!-- 邮箱验证表单 -->
        <el-form v-else :model="formData4EmailCode" :rules="emailCodeRules" ref="formData4EmailCodeRef"
            label-width="90px" @submit.prevent class="px-2 py-2">
            <el-form-item label="安全邮箱">
                <div
                    class="w-full px-4 py-2.5 bg-gray-100 dark:bg-[#2c2c2e] rounded-xl flex items-center transition-colors">
                    <span class="font-medium text-gray-800 dark:text-gray-200 tracking-wide">{{ userInfo.email }}</span>
                </div>
            </el-form-item>
            <el-form-item label="验证码" prop="emailCode">
                <div class="flex items-center gap-2.5 w-full">
                    <el-input class="flex-1" size="large" clearable placeholder="请输入邮箱验证码"
                        v-model.trim="formData4EmailCode.emailCode" maxLength="6">
                        <template #prefix><span class="iconfont icon-checkcode text-gray-400"></span></template>
                    </el-input>
                    <!-- 重构的极简控制按钮 -->
                    <button type="button" :disabled="sendCodeDisabled" @click="showSendCodeDialog"
                        class="h-[40px] px-5 rounded-xl font-semibold tracking-wide transition-all duration-200 text-[13.5px]"
                        :class="sendCodeDisabled ? 'bg-gray-200 dark:bg-gray-800 text-gray-400 cursor-not-allowed' : 'bg-blue-500 hover:bg-blue-600 text-white shadow-md active:scale-95'">
                        {{ sendCodeButtonText }}
                    </button>
                </div>
            </el-form-item>
            <el-form-item label="新密码" prop="password">
                <el-input type="password" v-model.trim="formData4EmailCode.password" size="large" placeholder="请设置新密码"
                    show-password>
                    <template #prefix><span class="iconfont icon-password text-gray-400"></span></template>
                </el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="rePassword">
                <el-input type="password" v-model.trim="formData4EmailCode.rePassword" size="large"
                    placeholder="请再次输入新密码" show-password>
                    <template #prefix><span class="iconfont icon-password text-gray-400"></span></template>
                </el-input>
            </el-form-item>
        </el-form>

        <!-- 底部链接区域 -->
        <div class="flex justify-end items-center px-4 mt-2 mb-1">
            <div v-if="!isEmailVerify" class="flex items-center gap-1.5">
                <span class="text-[13px] text-gray-500 dark:text-gray-400">忘记旧密码？</span>
                <a href="javascript:void(0)"
                    class="text-[13px] font-semibold text-blue-500 hover:text-blue-600 transition-colors"
                    @click="switchToEmailVerify">通过邮箱重置</a>
            </div>
            <a v-else href="javascript:void(0)"
                class="text-[13px] font-semibold text-blue-500 hover:text-blue-600 transition-colors"
                @click="switchToPasswordChange">返回密码修改</a>
        </div>
    </Dialog>

    <!-- 图形验证码对话框 -->
    <Dialog :show="dialogConfig4SendMailCode.show" :title="dialogConfig4SendMailCode.title"
        :buttons="dialogConfig4SendMailCode.buttons" width="420px" :showCancel="false" :showCustomTitle="true"
        @close="dialogConfig4SendMailCode.show = false">
        <el-form :model="formData4SendMailCode" :rules="emailVerifyRules" ref="formData4SendMailCodeRef"
            label-width="80px" class="px-2 py-4" @submit.prevent>
            <el-form-item label="验证码" prop="checkCode">
                <div class="flex items-center gap-3 w-full">
                    <el-input class="flex-1" size="large" clearable placeholder="请输入图形验证码"
                        v-model.trim="formData4SendMailCode.checkCode" maxLength="5">
                        <template #prefix><span class="iconfont icon-checkcode text-gray-400"></span></template>
                    </el-input>
                    <div class="h-[40px] w-[110px] cursor-pointer shadow-sm hover:opacity-80 transition-opacity border-none rounded-xl overflow-hidden drop-shadow"
                        @click="changeCheckCode">
                        <img class="w-full h-full object-fit" :src="checkCodeUrl4SendMailCode" alt="点我换一张" />
                    </div>
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
    updatePasswordByEmailCode: "/updatePasswordByEmailCode"
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

<style scoped>
/* 表单行间距 */
:deep(.el-form-item) {
    margin-bottom: 20px;
}

/* 优雅字重的灰色 Label */
:deep(.el-form-item__label) {
    @apply text-gray-700 dark:text-gray-300 font-medium;
    padding-right: 12px;
}

/* 核心输入框去边框与扁平化设计 */
:deep(.el-input__wrapper) {
    box-shadow: none !important;
    @apply bg-gray-100 dark:bg-[#2c2c2e] rounded-xl transition-all duration-300 py-2 px-4;
}

/* 聚焦状态苹果光晕效果 */
:deep(.el-input__wrapper.is-focus),
:deep(.el-input__wrapper:focus-within) {
    @apply ring-2 ring-blue-500/50 bg-white dark:bg-[#1c1c1e];
    box-shadow: none !important;
}

/* 输入框文字体感 */
:deep(.el-input__inner) {
    @apply text-gray-800 dark:text-gray-200 font-medium text-[14px];
}

/* 错误提示浮动修复 */
:deep(.el-form-item__error) {
padding-top: 4px;
font-weight: 500;
}
</style>