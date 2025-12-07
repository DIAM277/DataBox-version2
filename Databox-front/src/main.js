import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import ElementPlus from "element-plus";
import { ElInfiniteScroll } from "element-plus";
import "element-plus/dist/index.css";
import "@/assets/icon/iconfont.css";
import "@/assets/base.scss";
import VueCookies from "vue-cookies";
import hljsVuePlugin from "@highlightjs/vue-plugin";

import Dialog from "@/components/Dialog.vue";
import Avatar from "@/components/Avatar.vue";
import Table from "@/components/Table.vue";
import Icon from "@/components/Icon.vue";
import NoData from "@/components/NoData.vue";
import FolderSelect from "@/components/FolderSelect.vue";
import Navigation from "@/components/Navigation.vue";
import Preview from "@/components/Preview/Preview.vue";
import Window from "@/components/Window.vue";

import Verify from "@/utils/Verify";
import Message from "@/utils/Message";
import Request from "@/utils/Request";
import Utils from "@/utils/Utils";
import confirm from "@/utils/Confirm";

const app = createApp(App);
app.use(ElementPlus);
app.use(router);
app.use(ElInfiniteScroll);
app.use(VueCookies);
app.use(hljsVuePlugin);

app.component("Dialog", Dialog);
app.component("Avatar", Avatar);
app.component("Table", Table);
app.component("Icon", Icon);
app.component("NoData", NoData);
app.component("FolderSelect", FolderSelect);
app.component("Navigation", Navigation);
app.component("Preview", Preview);
app.component("Window", Window);

// 配置全局
app.config.globalProperties.Verify = Verify;
app.config.globalProperties.Message = Message;
app.config.globalProperties.Request = Request;
app.config.globalProperties.VueCookies = VueCookies;
app.config.globalProperties.globalInfo = {
  avatarUrl: "/api/getAvatar/",
  imageUrl: "/api/file/getImage/",
};
app.config.globalProperties.Utils = Utils;
app.config.globalProperties.confirm = confirm;
app.mount("#app");
