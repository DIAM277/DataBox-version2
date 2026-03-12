<template>
  <div
    class="relative flex items-center justify-center min-h-screen bg-center bg-cover transition-all duration-700 font-sans"
    :style="{ backgroundImage: `url(${currentBg})` }">
    <!-- 装饰性全屏背景遮罩 (提升玻璃质感和暗色模式兼容) -->
    <div
      class="absolute inset-0 bg-white/10 dark:bg-black/40 backdrop-blur-[2px] pointer-events-none transition-colors duration-500">
    </div>

    <div class="absolute top-6 left-6 z-20">
      <ChangeBgButton @change-background="switchBackground" />
    </div>

    <!-- 登录注册毛玻璃主卡片 -->
    <div class="relative z-10 w-full max-w-[440px] px-8 py-10 mx-4 sm:mx-auto 
             bg-white/75 dark:bg-[#1c1c1e]/80 backdrop-blur-2xl 
             border border-white/40 dark:border-white/10 
             rounded-[2rem] shadow-[0_20px_40px_rgba(0,0,0,0.1)] dark:shadow-[0_20px_40px_rgba(0,0,0,0.5)]
             transition-all duration-500 ease-[cubic-bezier(0.25,0.8,0.25,1)]"
      :style="{ marginTop: opType === 1 ? '0' : '-8vh' }">

      <!-- 顶部 Logo 模块 -->
      <div class="flex justify-center mb-8 transform scale-110">
        <AppTitle1></AppTitle1>
      </div>

      <el-form class="w-full" :model="formData" :rules="rules" ref="formDataRef" @submit.prevent @keyup.enter="doSubmit"
        size="large">
        <!-- 动态标题 -->
        <h2 class="text-2xl font-semibold text-center tracking-widest text-[#1d1d1f] dark:text-[#f5f5f7] mb-8">
          {{ opType === 0 ? '创建账号' : (opType === 1 ? '登 录' : '重置密码') }}
        </h2>

        <!-- 通用：邮箱输入 -->
        <el-form-item prop="email">
          <el-input v-model.trim="formData.email" placeholder="请输入邮箱" maxLength="150" clearable>
            <template #prefix><span class="iconfont icon-account text-gray-400"></span></template>
          </el-input>
        </el-form-item>

        <!-- 注册专有：用户名 -->
        <el-form-item prop="userName" v-if="opType === 0">
          <el-input v-model.trim="formData.userName" placeholder="请输入用户名" maxLength="20" clearable>
            <template #prefix><span class="iconfont icon-account text-gray-400"></span></template>
          </el-input>
        </el-form-item>

        <!-- 通用：登录密码 -->
        <el-form-item prop="password" v-if="opType === 1">
          <el-input v-model.trim="formData.password" placeholder="请输入密码" type="password" maxLength="150" clearable
            show-password>
            <template #prefix><span class="iconfont icon-password text-gray-400"></span></template>
          </el-input>
        </el-form-item>

        <!-- 注册及修改密码 -->
        <el-form-item prop="registerPassword" v-if="opType === 0 || opType === 2">
          <el-input v-model.trim="formData.registerPassword" placeholder="请设置密码" type="password" show-password>
            <template #prefix><span class="iconfont icon-password text-gray-400"></span></template>
          </el-input>
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item prop="reRegisterPassword" v-if="opType === 0 || opType === 2">
          <el-input v-model.trim="formData.reRegisterPassword" placeholder="请再次输入密码" type="password" show-password>
            <template #prefix><span class="iconfont icon-password text-gray-400"></span></template>
          </el-input>
        </el-form-item>

        <!-- 注册/改密：邮箱验证码域 -->
        <el-form-item prop="emailCode" v-if="opType === 0 || opType === 2">
          <div class="flex w-full items-center gap-3">
            <el-input v-model.trim="formData.emailCode" placeholder="邮箱验证码" maxLength="6" class="flex-1" clearable>
              <template #prefix><span class="iconfont icon-checkcode text-gray-400"></span></template>
            </el-input>
            <el-button class="apple-btn" type="primary" @click="getEmailCode">
              获取验证码
            </el-button>
          </div>
        </el-form-item>

        <!-- 通用：图形验证码域 -->
        <el-form-item prop="checkCode">
          <div class="flex w-full items-center gap-3">
            <el-input v-model.trim="formData.checkCode" placeholder="图形验证码" maxLength="5" class="flex-1" clearable>
              <template #prefix><span class="iconfont icon-checkcode text-gray-400"></span></template>
            </el-input>
            <div
              class="h-[40px] w-[110px] border border-gray-200 dark:border-gray-700 bg-white/50 cursor-pointer shadow-sm hover:opacity-80 transition-opacity"
              @click="changeCheckCode(0)">
              <img class="w-full h-full" :src="checkCodeUrl" alt="验证码" />
            </div>
          </div>
        </el-form-item>

        <!-- 登录专有：记住我与忘记密码 -->
        <div v-if="opType === 1"
          class="flex justify-between items-center mb-6 px-1 text-sm text-[#86868b] dark:text-gray-400">
          <el-checkbox v-model="formData.rememberMe" class="custom-checkbox">记住我的登录状态</el-checkbox>
          <a href="javascript:void(0)" class="text-[#007AFF] hover:text-[#0056b3] transition-colors"
            @click="showPanel(2)">密码恢复</a>
        </div>

        <!-- 返回登录节点 (改密用) -->
        <div v-if="opType === 2" class="flex justify-end mb-6 text-sm">
          <a href="javascript:void(0)"
            class="text-[#007AFF] hover:text-[#0056b3] transition-colors flex items-center gap-1" @click="showPanel(1)">
            <span class="iconfont icon-right"></span> 记起密码？返回登录
          </a>
        </div>

        <!-- 主操作按钮 -->
        <div class="mt-8 flex justify-center w-full">
          <CustomButton :text="opType === 0 ? '注 册 账 号' : (opType === 1 ? '登 录 空 间' : '确 认 重 置')" :width="360"
            @click="doSubmit" />
        </div>

        <!-- 底部模式切换辅助文字 -->
        <div class="mt-6 text-center text-sm text-[#86868b] dark:text-gray-400">
          <p v-if="opType === 1">
            尚未拥有账号？
            <a href="javascript:void(0)" class="text-[#007AFF] font-medium hover:underline transition-all"
              @click="showPanel(0)">马上创建</a>
          </p>
          <p v-if="opType === 0">
            已有？
            <a href="javascript:void(0)" class="text-[#007AFF] font-medium hover:underline transition-all"
              @click="showPanel(1)">直接登录</a>
          </p>
        </div>
      </el-form>
    </div>

    <!-- 邮箱验证码发送内嵌对话框 (保持原有逻辑框架，做轻量 Tailwind 润色) -->
    <Dialog :show="dialogConfig4SendMailCode.show" :title="dialogConfig4SendMailCode.title"
      :buttons="dialogConfig4SendMailCode.buttons" width="450px" :showCancel="false" :showCustomTitle="true"
      @close="dialogConfig4SendMailCode.show = false">
      <el-form :model="formData4SendMailCode" :rules="rules" ref="formData4SendMailCodeRef" label-width="80px"
        @submit.prevent size="large">
        <el-form-item label="投递邮箱">
          <div
            class="w-full px-4 py-2 bg-gray-50 dark:bg-white/5 rounded-xl border border-gray-100 dark:border-none text-gray-700 dark:text-gray-200">
            {{ formData.email }}
          </div>
        </el-form-item>
        <el-form-item label="安全验证" prop="checkCode">
          <div class="flex items-center gap-3 w-full">
            <el-input v-model.trim="formData4SendMailCode.checkCode" placeholder="图形验证码" maxLength="5" clearable
              class="flex-1">
              <template #prefix><span class="iconfont icon-checkcode text-gray-400"></span></template>
            </el-input>
            <div class="h-[40px] w-[110px] cursor-pointer shadow-sm" @click="changeCheckCode(1)">
              <img class="w-full h-full" :src="checkCodeUrl4SendMailCode" alt="点我换一张" />
            </div>
          </div>
        </el-form-item>
      </el-form>
    </Dialog>

    <RandomQuote class="absolute bottom-6 mx-auto z-20 opacity-80" />
  </div>
</template>

<script setup>
import RandomQuote from "@/components/RandomQuote.vue";
import AppTitle1 from "@/components/AppTitle/AppTitle1.vue";
import CustomButton from "@/components/Button/LoginButton.vue";
import ChangeBgButton from "@/components/Button/ChangeBgButton.vue";
import { ref, reactive, getCurrentInstance, nextTick, onMounted, computed } from "vue"
import { useRouter, useRoute } from "vue-router";
import md5 from "js-md5";

const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

// 背景图片相关
const bgIndex = ref(1); // 默认使用第一张背景图
const totalBgCount = 11; // 总共有11张背景图
const currentBg = computed(() => {
  return new URL(`../assets/background/login_bg${bgIndex.value}.jpg`, import.meta.url).href;
});

// 切换背景图片
const switchBackground = () => {
  bgIndex.value = bgIndex.value % totalBgCount + 1; // 循环切换背景
};

// 根据操作类型计算面板的顶部边距
const panelMarginTop = computed(() => {
  // 登录状态使用较大的上边距，注册和修改密码使用较小的上边距
  return opType.value === 1 ? 'calc((100vh - 500px) / 2)' : 'calc((100vh - 700px) / 2)';
});

// 接口地址
const api = {
  checkCode: "/api/checkCode",
  sendEmailCode: "/sendEmailCode",
  register: "/register",
  resetPwd: "/resetPwd",
  login: "/login",
}

// 操作类型 0:注册 1:登录 2:重置密码
const opType = ref(1);
const showPanel = (type) => {
  opType.value = type;
  resetForm();
}

// 校验前后两次密码是否一致
const checkRePassword = (rule, value, callback) => {
  if (value !== formData.value.registerPassword) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
}
const formData = ref({});
const formDataRef = ref();
const rules = {
  email: [
    { required: true, message: "请输入正确的邮箱" },
    { validator: proxy.Verify.email, message: "请输入正确的邮箱" }
  ],
  password: [
    { required: true, message: "请输入密码" },
  ],
  emailCode: [
    { required: true, message: "请输入邮箱验证码" },
  ],
  userName: [
    { required: true, message: "请输入用户名" },
  ],
  registerPassword: [
    { required: true, message: "请输入密码" },
    { validator: proxy.Verify.password, message: "密码长度为8-16位，必须包含数字、字母" }
  ],
  reRegisterPassword: [
    { required: true, message: "请再次输入密码" },
    { validator: checkRePassword, message: "两次输入的密码不一致" }
  ],
  checkCode: [
    { required: true, message: "请输入图片验证码" },
  ]
};

const checkCodeUrl = ref('');
const checkCodeUrl4SendMailCode = ref('');
// 切换验证码
const changeCheckCode = (type) => {
  if (type === 0) {
    checkCodeUrl.value = api.checkCode + "?type=" + type + "&time=" + new Date().getTime();
    if (formData.value) {
      formData.value.checkCode = "";
    }
  } else {
    checkCodeUrl4SendMailCode.value = api.checkCode + "?type=" + type + "&time=" + new Date().getTime();
    if (formData4SendMailCode.value) {
      formData4SendMailCode.value.checkCode = "";
    }
  }
}

// 发送邮箱验证码
const formData4SendMailCode = ref({})
const formData4SendMailCodeRef = ref();
const dialogConfig4SendMailCode = reactive({
  show: false,
  title: "发送邮箱验证码",
  buttons: [
    {
      type: "primary",
      text: "发送验证码",
      click: (e) => {
        sendEmailCode()
      },
    },
  ],
});

// 主界面获取邮箱验证码
const getEmailCode = () => {
  formDataRef.value.validateField("email", (valid) => {
    if (!valid) {
      return
    }
    // 先显示对话框，再加载验证码，避免用户等待
    dialogConfig4SendMailCode.show = true;
    // 使用 nextTick 确保对话框已渲染后再加载验证码
    nextTick(() => {
      changeCheckCode(1);
      formData4SendMailCodeRef.value.resetFields();
      formData4SendMailCode.value = { email: formData.value.email };
    });
  })
}

// 发送邮箱验证码
const sendEmailCode = () => {
  formData4SendMailCodeRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    const params = Object.assign({}, formData4SendMailCode.value)
    params.type = opType.value == 0 ? 0 : 1;
    let result = await proxy.Request({
      url: api.sendEmailCode,
      params: params,
      errorCallback: () => {
        changeCheckCode(1);
      },
    })
    if (!result) {
      return
    }
    proxy.Message.success("验证码发送成功，请前往邮箱查看")
    dialogConfig4SendMailCode.show = false;
  })
}

// 重置表单
const resetForm = () => {
  changeCheckCode(0);
  formDataRef.value.resetFields();
  formData.value = {};
  // 登录
  if (opType.value == 1) {
    const cookieLoginInfo = proxy.VueCookies.get('loginInfo');
    if (cookieLoginInfo) {
      formData.value = cookieLoginInfo;
    }
  }
}
// 登录、注册、重置密码表单提交
const doSubmit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}
    Object.assign(params, formData.value);
    // 注册操作/找回密码
    if (opType.value == 0 || opType.value == 2) {
      // 参数设置密码 移除确认密码字段
      params.password = params.registerPassword
      delete params.registerPassword
      delete params.reRegisterPassword
    }
    // 登录操作
    if (opType.value == 1) {
      // 获取存储的登录信息和密码
      let cookieLoginInfo = proxy.VueCookies.get("loginInfo")
      let cookiePassword = cookieLoginInfo == null ? null : cookieLoginInfo.password
      if (params.password !== cookiePassword) {
        // 若输入密码与存储密码不匹配 对密码进行MD5加密
        params.password = md5(params.password)
      }
    }
    let url = null;
    // 根据操作类型确定请求url
    if (opType.value == 0) {
      url = api.register
    } else if (opType.value == 1) {
      url = api.login
    } else if (opType.value == 2) {
      url = api.resetPwd
    }
    // 发起请求
    let result = await proxy.Request({
      url: url,
      params: params,
      errorCallback: () => {
        changeCheckCode(0);
      }
    })
    if (!result) {
      return;
    }
    // 注册返回
    if (opType.value == 0) {
      proxy.Message.success("注册成功，请登录")
      showPanel(1)
    } else if (opType.value == 1) {
      if (params.rememberMe) {
        const loginInfo = {
          email: params.email,
          password: params.password,
          rememberMe: params.rememberMe,
        }
        proxy.VueCookies.set("loginInfo", loginInfo, "7d")
      } else {
        proxy.VueCookies.remove("loginInfo")
      }
      proxy.Message.success("登录成功")
      // 存储cookie
      proxy.VueCookies.set("userInfo", result.data, 0)
      // 重定向到原始页面
      const redirectUrl = route.query.redirectUrl || '/';
      router.push(redirectUrl);
    } else if (opType == 2) {
      proxy.Message.success("修改密码成功，请登录")
      showPanel(1)
    }
  });
}

onMounted(() => {
  changeCheckCode(0);
  showPanel(1)
  // 随机初始化一个背景
  bgIndex.value = Math.floor(Math.random() * totalBgCount) + 1;
})
</script>

<style lang="scss" scoped>
/* 
  利用 Element Plus 变量深度覆盖：实现苹果风格输入框
  使输入框融入毛玻璃背景，圆润、无违和感
*/
:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.45) !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.02) !important;
  border-radius: 12px !important;
  padding: 0 16px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-input__wrapper:hover) {
  background-color: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 0 0 2px rgba(0, 122, 255, 0.3) !important;
  /* Apple Blue glow */
}

/* 暗黑模式下输入框适配 */
html.dark :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.08) !important;
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.05) !important;
}

html.dark :deep(.el-input__wrapper.is-focus),
html.dark :deep(.el-input__wrapper:hover) {
  background-color: rgba(255, 255, 255, 0.15) !important;
  box-shadow: 0 0 0 2px rgba(10, 132, 255, 0.5) !important;
  /* Dark Apple Blue glow */
}

:deep(.el-input__inner) {
  color: #1d1d1f !important;
  font-weight: 500;
  letter-spacing: 0.5px;
}

html.dark :deep(.el-input__inner) {
  color: #f5f5f7 !important;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

/* 自定义右侧 Apple 风格按钮 */
.apple-btn {
  height: 40px !important;
  border-radius: 12px !important;
  background-color: #007AFF !important;
  border: none !important;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
  transition: all 0.3s ease;

  &:hover {
    background-color: #0066d6 !important;
    transform: translateY(-1px);
    box-shadow: 0 6px 16px rgba(0, 122, 255, 0.4);
  }

  &:active {
    transform: translateY(1px);
  }
}

/* 复选框暗黑/白天优化 */
:deep(.custom-checkbox .el-checkbox__label) {
  color: inherit !important;
}

html.dark :deep(.el-checkbox__inner) {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
}
</style>
