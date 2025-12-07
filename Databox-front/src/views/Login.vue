<template>
  <div class="login-body" :style="{ background: `url(${currentBg}) center/cover no-repeat` }">
      <ChangeBgButton @change-background="switchBackground" />
    <div class="login-panel" :style="{ marginTop: panelMarginTop }">
      <!-- <div class="app-title">DataBox</div> -->
      <AppTitle1></AppTitle1>
      <el-form class="login-register" :model="formData" :rules="rules" ref="formDataRef" @submit.prevent
        @keyup.enter="doSubmit">
        <!-- 根据操作类型显示不同的标题 -->
        <div class="login-title">{{ opType == 0 ? '注 册' : (opType == 1 ? '登 录' : '修改密码') }}</div>
        <!-- 邮箱输入 -->
        <el-form-item prop="email">
          <el-input size="large" clearable placeholder="请输入邮箱" v-model.trim="formData.email" maxLength="150">
            <template #prefix>
              <span class="iconfont icon-account"></span>
            </template>
          </el-input>
        </el-form-item>
        <!-- 密码输入 -->
        <el-form-item prop="password" v-if="opType == 1">
          <el-input size="large" placeholder="请输入密码" v-model.trim="formData.password" maxLength="150" type="password"
            clearable>
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <!-- 注册 -->
        <div v-if="opType == 0 || opType == 2">
          <el-form-item prop="emailCode">
            <div class="send-email-panel">
              <el-input clearable placeholder="请输入邮箱验证码" v-model.trim="formData.emailCode" size="large" maxLength="6">
                <template #prefix>
                  <span class="iconfont icon-checkcode"></span>
                </template>
              </el-input>
              <el-button class="send-mail-btn" type="primary" size="large" @click="getEmailCode">获取验证码</el-button>
            </div>
          </el-form-item>
          <!-- 用户名 -->
          <el-form-item prop="userName" v-if="opType == 0">
            <el-input clearable placeholder="请输入用户名" v-model.trim="formData.userName" size="large" maxLength="20">
              <template #prefix>
                <span class="iconfont icon-account"></span>
              </template>
            </el-input>
          </el-form-item>
          <!--注册密码/修改密码-->
          <el-form-item prop="registerPassword">
            <el-input show-password placeholder="请输入密码" v-model.trim="formData.registerPassword" type="password"
              size="large">
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
          <!-- 再次输入密码 -->
          <el-form-item prop="reRegisterPassword">
            <el-input show-password placeholder="请再次输入密码" v-model.trim="formData.reRegisterPassword" type="password"
              size="large">
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
        </div>
        <!-- 验证码 -->
        <el-form-item prop="checkCode">
          <div class="check-code-panel">
            <el-input size="large" clearable placeholder="请输入验证码" v-model.trim="formData.checkCode" maxLength="5">
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img class="check-code" :src="checkCodeUrl" @click="changeCheckCode(0)" />
          </div>
        </el-form-item>
        <!-- 登录 -->
        <el-form-item v-if="opType == 1">
          <div class="rememberme-panel">
            <el-checkbox v-model="formData.rememberMe">记住我</el-checkbox>
            <a href="javascript:void(0)" class="a-link" @click="showPanel(2)">忘记密码❓</a>
          </div>
        </el-form-item>
        <!-- 找回密码 -->
        <el-form-item v-if="opType == 2">
          <div class="rememberme-panel">
            <div></div> <!-- 空div占位 -->
            <a href="javascript:void(0)" class="a-link" @click="showPanel(1)">前往登录✅</a>
          </div>
        </el-form-item>
        <!-- 登录按钮 -->
        <el-form-item>
          <!-- <el-button type="primary" class="op-btn" size="large" @click="submit">
            <span v-if="opType == 0">注 册</span>
            <span v-if="opType == 1">登 录</span>
            <span v-if="opType == 2">修 改 密 码</span>
          </el-button> -->
          <CustomButton :text="opType == 0 ? '注 册' : (opType == 1 ? '登 录' : '修 改 密 码')" :width="380"
            @click="doSubmit" />
        </el-form-item>
        <!-- 账号注册/登录链接 -->
        <div v-if="opType == 1" class="account-link-container">
          <span>没有账号？<a href="javascript:void(0)" class="a-link" @click="showPanel(0)">前往注册</a></span>
        </div>
        <div v-if="opType == 0" class="account-link-container">
          <span>已有账号？<a href="javascript:void(0)" class="a-link" @click="showPanel(1)">前往登录✅</a></span>
        </div>
      </el-form>
    </div>
    <Dialog :show="dialogConfig4SendMailCode.show" :title="dialogConfig4SendMailCode.title"
      :buttons="dialogConfig4SendMailCode.buttons" width="500px" :showCancel="false" :showCustomTitle="true"
      @close="dialogConfig4SendMailCode.show = false">
      <el-form :model="formData4SendMailCode" :rules="rules" ref="formData4SendMailCodeRef" label-width="80px"
        @submit.prevent>
        <el-form-item label="邮箱">
          <div class="email-display">
            <span class="email-text">{{ formData.email }}</span>
          </div>
        </el-form-item>
        <el-form-item label="验证码" prop="checkCode">
          <div class="check-code-panel">
            <el-input size="large" clearable placeholder="请输入验证码" v-model.trim="formData4SendMailCode.checkCode"
              maxLength="5">
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img class="check-code" :src="checkCodeUrl4SendMailCode" @click="changeCheckCode(1)" />
          </div>
        </el-form-item>
      </el-form>
    </Dialog>
    <RandomQuote />
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
.login-body {
  height: calc(100vh);
  display: flex;
  position: relative;
  user-select: none;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif; // 现代字体栈

  .bg-button-container {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 10;
  }

  .login-panel {
    width: 430px;
    margin: 0 auto;
    //margin-top: calc((100vh - 500px) / 2);
    transition: margin-top 0.3s ease;

    /* 添加DataBox标题样式 */
    .app-title {
      text-align: center;
      font-size: 36px;
      font-weight: bold;
      color: #fff;
      margin-bottom: 20px;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
      letter-spacing: 2px; // 增加字间距
    }

    .login-register {
      padding: 30px;
      background: rgba(255, 255, 255, 0.96);
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
      border-radius: 10px;

      .login-title {
        text-align: center;
        font-size: 22px; // 增大字体
        font-weight: 600;
        margin-bottom: 25px; // 增加下边距
        color: #409EFF; // 使用Element Plus主题色
        position: relative;

        // 添加下划线装饰
        &::after {
          content: '';
          position: absolute;
          bottom: -9px;
          left: 50%;
          transform: translateX(-50%);
          width: 50px;
          height: 2px;
          background-color: #409EFF;
        }
      }

      .send-email-panel {
        display: flex;
        width: 100%;
        justify-content: space-between;
        align-items: center;

        .send-mail-btn {
          margin-left: 5px;
          /* 增加左侧间距 */
        }
      }

      .rememberme-panel {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .login-btn-container {
        display: flex;
        align-items: center;
        width: 100%;

        .op-btn {
          flex: 1;

          span {
            letter-spacing: 2px;
            font-weight: 500;
            font-size: 16px;
          }
        }
      }

      .register-link-container {
        width: 100%;
        display: flex;
        justify-content: flex-end;
        margin-top: 10px;
      }

      .no-account {
        width: 100%;
        display: flex;
        justify-content: space-between;
      }

      .op-btn {
        width: 100%;

        span {
          letter-spacing: 2px;
          font-weight: 500;
          font-size: 16px;
        }
      }
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

  .login-btn-qq {
    margin-top: 20px;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      cursor: pointer;
      margin-left: 10px;
      width: 20px;
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
  }

  :deep(.el-button) {
    letter-spacing: 1px;
  }

  /* 美化链接样式 */
  .a-link {
    color: #409EFF;
    text-decoration: none;
    transition: color 0.3s;

    &:hover {
      color: #66b1ff;
      text-decoration: underline;
    }
  }

  .email-display {
    display: flex;
    align-items: center;
    padding: 8px 12px;
    background-color: #f5f7fa;
    border-radius: 4px;
    border: 1px solid #e4e7ed;

    .iconfont {
      color: #909399;
      margin-right: 8px;
      font-size: 16px;
    }

    .email-text {
      color: #606266;
      font-weight: 500;
      word-break: break-all;
    }
  }

  .account-link-container {
    width: 100%;
    display: flex;
    justify-content: center;
    margin-top: 15px;

    span,
    a {
      color: #606266;
      font-size: 15px;
      line-height: 1.5;
      font-family: inherit;
      vertical-align: baseline;
    }

    .a-link {
      color: #409EFF;
      font-weight: normal;
      text-decoration: none;
      transition: color 0.3s;

      &:hover {
        color: #66b1ff;
        text-decoration: underline;
      }
    }
  }
}
</style>
