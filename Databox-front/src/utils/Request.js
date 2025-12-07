import axios from "axios";
import { ElLoading } from "element-plus";
import Message from "../utils/Message";
import router from "@/router";

const contentTypeForm = "application/x-www-form-urlencoded;charset=UTF-8";
const contentTypeJson = "application/json";
const responseTypeJson = "json";

let loading = null;
const instance = axios.create({
  baseURL: "/api",
  timeout: 10 * 1000,
});

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    //加载遮罩 防止重复提交
    if (config.showLoading) {
      loading = ElLoading.service({
        lock: true,
        text: "加载中.......",
        background: "rgba(0,0,0,0.7)",
      });
    }
    return config;
  },
  (error) => {
    //发生错误时关闭加载动画
    if (config.showLoading && loading) {
      loading.close();
    }
    Message.error("请求发送失败");
    return Promise.reject("请求发送失败");
  }
);

// 请求后拦截器
instance.interceptors.response.use(
  (response) => {
    //结构配置项
    const {
      showLoading,
      errorCallback,
      showError = true,
      responseType,
    } = response.config;
    //关闭加载动画
    if (showLoading && loading) {
      loading.close();
    }
    const responseData = response.data;
    //处理二进制数据响应
    if (responseType == "arraybuffer" || responseType == "blob") {
      return responseData;
    }

    //正常请求
    if (responseData.code == 200) {
      return responseData;
    } else if (responseData.code == 901) {
      //登陆超时
      router.push(
        "/login?redirectUrl=" + encodeURI(router.currentRoute.value.path)
      );
      return Promise.reject({ showError: false, msg: "登陆超时" });
    } else {
      //其他错误
      if (errorCallback) {
        errorCallback(responseData.info);
      }
      return Promise.reject({ showError: showError, msg: responseData.info });
    }
  },
  (error) => {
    //网络错误处理
    if (error.config.showLoading && loading) {
      loading.close();
    }
    return Promise.reject({ showError: true, msg: "网络异常" });
  }
);

const request = (config) => {
  const {
    url,
    params,
    dataType,
    showLoading = true,
    responseType = responseTypeJson,
  } = config;
  let contentType = contentTypeForm;
  let formData = new FormData();
  for (let key in params) {
    formData.append(key, params[key] == undefined ? "" : params[key]);
  }
  if (dataType != null && dataType == "json") {
    contentType = contentTypeJson;
  }
  let headers = {
    "Content-Type": contentType,
    "X-Requested-With": "XMLHttpRequest",
  };
  return instance.post(url, formData, {
      onUploadProgress: (event) => {
        if (config.uploadProgressCallback) {
          config.uploadProgressCallback(event);
        }
      },
      responseType: responseType,
      headers: headers,
      showLoading: showLoading,
      errorCallback: config.errorCallback,
      showError: config.showError
    })
    .catch((error) => {
      //统一错误处理
      console.log(error);
      if (error.showError) {
        Message.error(error.msg);
      }
      return null;
    });
};

export default request;
