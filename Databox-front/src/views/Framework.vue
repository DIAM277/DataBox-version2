<template>
  <div
    class="flex flex-col h-screen w-full bg-[#f5f5f7] dark:bg-[#000000] text-[#1d1d1f] dark:text-[#f5f5f7] font-sans overflow-hidden transition-colors duration-500">

    <!-- 顶部操作栏 (现在它是全局第一个元素，拥有 100% 宽度) -->
    <TopHeader ref="topHeaderRef" @search="handleSearch"
      class="h-16 flex-shrink-0 bg-white/70 dark:bg-[#1c1c1e]/70 backdrop-blur-xl border-b border-[#e5e5e9] dark:border-[#38383a] z-20 px-6 flex items-center justify-between transition-all duration-300 w-full" />

    <!-- 下半部主体：包含侧边栏 + 内容区 -->
    <div class="flex-1 flex overflow-hidden min-h-0 relative">

      <!-- 左侧导航栏: 【增加亮色模式的微阴影】 -->
      <SideBar
        class="w-56 flex-shrink-0 border-r border-[#e5e5e9] dark:border-[#38383a] bg-gray-50/60 dark:bg-[#1c1c1e]/60 backdrop-blur-xl z-20 shadow-[2px_0_15px_-3px_rgba(0,0,0,0.06)] dark:shadow-none transition-all duration-300" />

      <!-- 主数据内容区 -->
      <main class="flex-1 overflow-hidden p-4 lg:p-6 relative">
        <div
          class="h-full w-full bg-white dark:bg-[#151515] rounded-2xl shadow-sm border border-[#e5e5e9] dark:border-[#38383a] overflow-auto transition-all duration-300">
          <router-view v-slot="{ Component }">
            <component :is="Component" @addFile="addFile" ref="routerViewRef" @reload="reloadSpaceInfo" />
          </router-view>
        </div>
      </main>

    </div>

    <!-- 全局操作弹窗 -->
    <UpdateAvatar ref="uploadAvatarRef" @UpdateAvatar="reloadUserAvatar" />
    <UpdatePassword ref="updatePasswordRef" />
    <Logout ref="logoutRef" />
  </div>
</template>

<script setup>
import { ref, getCurrentInstance, nextTick, provide, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
// 引入刚刚建立的 Pinia Store
import { useUserStore } from '@/store/userStore';

// 后续您需要新建的两个布局子组件
import SideBar from '@/components/Layout/SideBar.vue';
import TopHeader from '@/components/Layout/TopHeader.vue';

import Logout from '@/components/Logout.vue';
import UpdatePassword from './UpdatePassword.vue';
import UpdateAvatar from './UpdateAvatar.vue';

const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const topHeaderRef = ref();
const routerViewRef = ref();
const uploadAvatarRef = ref();
const updatePasswordRef = ref();
const logoutRef = ref();

const api = {
  getUserSpace: '/getUseSpace'
};

// 1. 初始化或刷新空间状态，直接通过 Pinia 派发给所有组件
const reloadSpaceInfo = async () => {
  let result = await proxy.Request({
    url: api.getUserSpace,
    showLoading: false,
  });
  if (result) {
    userStore.updateSpaceInfo(result.data); // ★ 调用 Pinia 更新状态
  }
};

// 2. 提供全局动作给 TopHeader (如点击头像退出、修改密码等机制解耦)
provide('globalActions', {
  updateAvatar: () => uploadAvatarRef.value?.show(userStore.userInfo),
  updatePassword: () => updatePasswordRef.value?.show(),
  logout: () => logoutRef.value?.show()
});

// 重新加载用户头像 (此时也应该同步给 store)
const reloadUserAvatar = () => {
  // 强制触发由 cookie 读取更新 Pinia 状态
  userStore.userInfo = proxy.VueCookies.get('userInfo');
  // 如果 TopHeader 里依赖 timestamp 刷新头像，可由 store 传递
};

// 3. 处理由路由表冒泡上来的上传事件（交给 TopHeader 内部的 Uploader 去处理）
const addFile = (data) => {
  if (topHeaderRef.value) {
    // 调用 TopHeader 中暴露出的方法触发上传
    topHeaderRef.value.triggerAddFile(data);
  }
};

// 4. 重构后的搜索机制：触发后利用 routerViewRef 调用子组件搜索逻辑
const handleSearch = (keyword) => {
  if (!route.path.startsWith('/main')) {
    router.push('/main/all');
  }
  nextTick(() => {
    if (routerViewRef.value && routerViewRef.value.searchFiles) {
      routerViewRef.value.searchFiles(keyword);
    }
  });
};

onMounted(() => {
  reloadSpaceInfo();
});
</script>

<style lang="scss" scoped>
/* 所有原生杂乱样式均已被 Tailwind 替代，这里保持清爽 */
</style>