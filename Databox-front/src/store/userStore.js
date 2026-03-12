import { defineStore } from "pinia";
import { ref } from "vue";
import VueCookies from "vue-cookies";

export const useUserStore = defineStore("user", () => {
  // State
  // 从 cookie 初始化数据，保持登录状态持久化
  const userInfo = ref(VueCookies.get("userInfo") || null);
  const useSpaceInfo = ref({ useSpace: 0, totalSpace: 1 });

  // Actions
  // 登录动作：保存信息并写入 Cookie (过期时间设置为0表示会话级别)
  const login = (userData) => {
    userInfo.value = userData;
    VueCookies.set("userInfo", userData, 0);
  };

  // 登出动作：清除数据并移除 Cookie
  const logout = () => {
    userInfo.value = null;
    useSpaceInfo.value = { useSpace: 0, totalSpace: 1 };
    VueCookies.remove("userInfo");
  };

  // 更新空间使用情况
  const updateSpaceInfo = (spaceData) => {
    useSpaceInfo.value = spaceData;

    // 某些特定场景可能需要把空间数据同步进 userInfo (取决于原业务逻辑)
    if (userInfo.value && spaceData.useSpace !== undefined) {
      userInfo.value.userSpace = spaceData.useSpace;
      userInfo.value.totalSpace = spaceData.totalSpace;
      // 同步回 Cookie 确保页面刷新也能拿到正确的空间
      VueCookies.set("userInfo", userInfo.value, 0);
    }
  };

  // 更新用户头像和名称等资料
  const updateUserInfo = (newData) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...newData };
      VueCookies.set("userInfo", userInfo.value, 0);
    }
  };

  return {
    userInfo,
    useSpaceInfo,
    login,
    logout,
    updateSpaceInfo,
    updateUserInfo,
  };
});
