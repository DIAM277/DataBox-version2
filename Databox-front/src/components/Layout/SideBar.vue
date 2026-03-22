<template>
  <div class="flex flex-col h-full py-5 bg-transparent select-none">

    <!-- 上半部分：主菜单列表区 (溢出滚动) -->
    <div class="flex-1 overflow-y-auto no-scrollbar pb-4">

      <!-- 遍历渲染主菜单组 -->
      <div v-for="(group, index) in mainMenuGroups" :key="index" class="menu-group">

        <!-- 【优化2】：分组之间的细浅色横线 -->
        <div v-if="index > 0" class="h-[1px] bg-gray-200/80 dark:bg-gray-700/50 mx-4 my-3"></div>

        <!-- 分组细浅色全大写标题 -->
        <div class="text-[11px] text-[#86868b] dark:text-gray-500 font-semibold tracking-wider uppercase mb-1.5 px-4">
          {{ group.title }}
        </div>

        <!-- 组内菜单项 -->
        <ul class="space-y-0.5 px-2">
          <li v-for="menu in group.items" :key="menu.path">
            <div @click="handleNav(menu.path)"
              class="flex items-center px-3 py-2 rounded-xl cursor-pointer transition-all duration-200 group" :class="isActive(menu.path)
                ? 'bg-gray-200/60 dark:bg-gray-700/50 text-[#1d1d1f] dark:text-white font-bold shadow-sm'
                : 'text-[#424245] dark:text-[#a1a1a6] hover:bg-gray-100/80 dark:hover:bg-[#2c2c2e]/80 font-medium'">

              <!-- 【优化：SVG 与 IconFont 并存拦截策略】 -->
              <template v-if="menu.icon === 'icon-star'">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" 
                  class="w-[18px] h-[18px] mr-3 transition-transform duration-300 group-hover:scale-110"
                  :class="isActive(menu.path) ? menu.activeColor || menu.color : menu.color">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
                </svg>
              </template>
              <template v-else>
                <span class="iconfont text-[18px] mr-3 transition-transform duration-300 group-hover:scale-110"
                  :class="[menu.icon, isActive(menu.path) ? menu.activeColor || menu.color : menu.color]">
                </span>
              </template>

              <span class="text-[14px] tracking-wide">{{ menu.name }}</span>
            </div>
          </li>
        </ul>
      </div>

    </div>

    <!-- 下半部分：固定在底部的区域 (设置 + 空间卡片) -->
    <div class="mt-auto px-2 pb-2 pt-3 border-t border-[#e5e5e9] dark:border-[#38383a]/60">

      <!-- 底部设置相关的菜单 (如管理员菜单) -->
      <div v-if="bottomAdminMenus.length > 0" class="mb-2">
        <div class="text-[11px] text-[#86868b] dark:text-gray-500 font-semibold tracking-wider uppercase mb-1.5 px-3">
          管理与设置
        </div>
        <ul class="space-y-0.5">
          <li v-for="menu in bottomAdminMenus" :key="menu.path">
            <div @click="handleNav(menu.path)"
              class="flex items-center px-3 py-2 rounded-xl cursor-pointer transition-all duration-200 group" :class="isActive(menu.path)
                ? 'bg-gray-200/60 dark:bg-gray-700/50 text-[#1d1d1f] dark:text-white font-bold shadow-sm'
                : 'text-[#424245] dark:text-[#a1a1a6] hover:bg-gray-100/80 dark:hover:bg-[#2c2c2e]/80 font-medium'">
              <span class="iconfont text-[18px] mr-3 transition-transform duration-300 group-hover:scale-110"
                :class="[menu.icon, isActive(menu.path) ? 'text-gray-700 dark:text-gray-300' : 'text-gray-400']">
              </span>
              <span class="text-[14px] tracking-wide">{{ menu.name }}</span>
            </div>
          </li>
        </ul>
      </div>

      <!-- 【优化3】：质感空间展示卡片区 -->
      <div
        class="mt-3 bg-white dark:bg-[#1c1c1e] border border-gray-200 dark:border-[#38383a] rounded-2xl p-3.5 shadow-sm hover:shadow-md transition-shadow">
        <div class="flex justify-between items-center mb-2.5">
          <span class="text-[12px] font-semibold text-gray-700 dark:text-gray-300 flex items-center">
            <i class="iconfont icon-cloude text-[#007AFF] mr-1.5 text-[14px]"></i>云端存储
          </span>
          <!-- 刷新按钮 -->
          <span
            class="iconfont icon-refresh text-[14px] text-gray-400 hover:text-[#007AFF] cursor-pointer transition-transform ease-out"
            :class="{ 'animate-spin': isRefreshing }" @click="refreshSpace" title="刷新空间">
          </span>
        </div>

        <!-- 容量文字 -->
        <div
          class="flex justify-between items-center text-[10.5px] text-gray-500 dark:text-gray-400 font-medium tracking-wide mb-1.5">
          <span>已用 {{ useSpaceStr }} / {{ totalSpaceStr }}</span>
          <span>{{ Math.round(spacePercentage) }}%</span>
        </div>

        <!-- 进度条轨道 -->
        <div class="w-full h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden shadow-inner">
          <div class="h-full rounded-full transition-all duration-700 ease-out"
            :class="spacePercentage > 90 ? 'bg-red-500' : 'bg-blue-500 dark:bg-[#0a84ff]'"
            :style="{ width: `${spacePercentage}%` }">
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { computed, ref, getCurrentInstance } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/store/userStore';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const { proxy } = getCurrentInstance();

// --- 独立空间刷新逻辑 ---
const isRefreshing = ref(false);
const refreshSpace = async () => {
  if (isRefreshing.value) return;
  isRefreshing.value = true;
  let result = await proxy.Request({
    url: '/getUseSpace',
    showLoading: false
  });
  if (result) {
    userStore.updateSpaceInfo(result.data); // 将最新数据推入 Store
  }
  setTimeout(() => { isRefreshing.value = false; }, 600);
};

// --- 容量文字转化与百分比计算 ---
// 重点修复：将原来的 userStore.spaceInfo 修改为在 store 中真实存在的 useSpaceInfo
const useSpaceStr = computed(() => proxy.Utils.size2Str(userStore.useSpaceInfo?.useSpace || 0));
const totalSpaceStr = computed(() => proxy.Utils.size2Str(userStore.useSpaceInfo?.totalSpace || 0));

const spacePercentage = computed(() => {
  const use = userStore.useSpaceInfo?.useSpace || 0;
  const total = userStore.useSpaceInfo?.totalSpace || 0;

  // 防御除以 0 以及 total 还没加载的情况
  if (!total || total === 0) return 0;

  const percentage = (use / total) * 100;
  return percentage > 100 ? 100 : percentage;
});

// 构建结构化的主菜单组 (加入了独立分配的 Color，贴近 iCloud 风格的鲜丽插画感)
const mainMenuGroups = [
  {
    title: '核心',
    items: [
      { name: "全部文件", path: "/main/all", icon: "icon-all", color: "text-[#007AFF]" },
    ]
  },
  {
    title: '分类',
    items: [
      { name: "视频", path: "/main/video", icon: "icon-video", color: "text-[#AF52DE]" }, // 苹果紫
      { name: "音频", path: "/main/music", icon: "icon-music", color: "text-[#FF9500]" }, // 苹果橙
      { name: "图片", path: "/main/image", icon: "icon-image", color: "text-[#34C759]" }, // 苹果绿
      { name: "文档", path: "/main/doc", icon: "icon-doc", color: "text-[#00C7BE]" },     // 苹果青
      { name: "其他", path: "/main/others", icon: "icon-more", color: "text-[#8E8E93]" }, // 苹果灰
    ]
  },
  {
    title: '我的',
    items: [
      { name: "我的收藏", path: "/favorites", icon: "icon-star", color: "text-[#FFCC00]" }, // 明黄色
      { name: "我的分享", path: "/myshare", icon: "icon-share", color: "text-[#5856D6]" }, // 靛蓝色
      { name: "回收站", path: "/recycle", icon: "icon-del", color: "text-[#FF3B30]" },    // 警示红
    ]
  }
];

const bottomAdminMenus = computed(() => {
  // 从 Store 中获取当前用户信息里的管理员标记
  const isAdmin = userStore.userInfo?.isAdmin;

  // 统一定义完整的管理设置类菜单，并打上对应需要的权限标签
  const allMenus = [
    { name: "系统设置", path: "/settings/sysSetting", icon: "icon-settings", requireAdmin: false },
    { name: "用户管理", path: "/settings/userList", icon: "icon-account", requireAdmin: true },
    { name: "用户文件", path: "/settings/fileList", icon: "icon-doc", requireAdmin: true },
    { name: "登录日志", path: "/settings/loginLog", icon: "icon-import", requireAdmin: false }, // 普通用户可看自己的
    { name: "操作日志", path: "/settings/operationLog", icon: "icon-edit", requireAdmin: false },
    { name: "举报处理", path: "/settings/shareReport", icon: "icon-cancel" }
  ];

  // 进行权限映射与过滤过滤
  return allMenus.filter(menu => {
    // 如果该菜单项需要 admin 权限，则返回当前用户的 admin 状态
    if (menu.requireAdmin) {
      return isAdmin;
    }
    return true;
  });
});

const handleNav = (path) => {
  router.push(path);
};

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/');
};
</script>

<style scoped>
.no-scrollbar::-webkit-scrollbar {
  display: none;
}

.no-scrollbar {
  -ms-overflow-style: none;
  /* IE and Edge */
  scrollbar-width: none;
  /* Firefox */
}
</style>