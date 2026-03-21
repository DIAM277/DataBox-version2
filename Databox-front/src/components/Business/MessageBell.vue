<template>
    <el-popover placement="bottom-end" :width="340" trigger="click" @show="loadMessageList"
        popper-class="notification-popover">
<template #reference>
    <!-- 新增类名：notification-bell -->
    <div class="notification-bell cursor-pointer flex items-center justify-center mt-1 mr-3 outline-none group">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99"
            class="flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.8"
                stroke="currentColor"
                class="w-6 h-6 text-gray-400 dark:text-gray-400 group-hover:text-[#007AFF] transition-colors duration-300">
                <path stroke-linecap="round" stroke-linejoin="round"
                    d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
            </svg>
        </el-badge>
    </div>
</template>

        <!-- 纯干预：手写苹果风弹窗底座 -->
        <div
            class="w-full bg-white/80 dark:bg-[#1c1c1e]/80 backdrop-blur-3xl shadow-[0_20px_60px_rgba(0,0,0,0.15)] rounded-2xl border border-white/40 dark:border-gray-700/50 overflow-hidden flex flex-col font-sans select-none">

            <!-- 面板 Header -->
            <div
                class="flex items-center justify-between px-4 py-3.5 border-b border-gray-200/50 dark:border-gray-700/50 bg-white/40 dark:bg-black/20 shrink-0">
                <span class="text-[15px] font-semibold text-gray-900 dark:text-white tracking-wide">系统通知</span>
                <span v-if="unreadCount > 0" @click="markAsRead('all')"
                    class="text-[12.5px] text-[#007AFF] hover:text-[#0056b3] cursor-pointer font-medium transition-colors">
                    全部已读
                </span>
            </div>

            <!-- 面板 Body 列表区 -->
            <div class="max-h-[350px] overflow-y-auto p-2 custom-scrollbar min-h-[150px] flex flex-col relative">

                <!-- 空数据缺省 -->
                <div v-if="messageList.length === 0"
                    class="flex-1 flex flex-col items-center justify-center opacity-70">
                    <img src="@/assets/icon-image/no_data.png" class="w-20 h-20 grayscale mb-2 opacity-50" />
                    <span class="text-[13px] text-gray-500 dark:text-gray-400 font-medium">暂无新通知</span>
                </div>

                <!-- 消息数据渲染 -->
                <div v-else class="flex flex-col gap-1">
                    <div v-for="msg in messageList" :key="msg.messageId"
                        @click="msg.status === 0 ? markAsRead(msg.messageId) : null"
                        class="relative p-3 rounded-xl transition-all duration-200 flex items-start gap-3 group"
                        :class="msg.status === 0 ? 'bg-blue-50/60 dark:bg-blue-900/10 hover:bg-blue-50 dark:hover:bg-blue-900/30 cursor-pointer' : 'hover:bg-gray-100/80 dark:hover:bg-gray-800/80'">

                        <!-- 未读蓝点提示 -->
                        <div class="mt-1.5 w-2 h-2 rounded-full flex-shrink-0 transition-opacity"
                            :class="msg.status === 0 ? 'bg-[#007AFF] shadow-[0_0_6px_rgba(0,122,255,0.6)]' : 'bg-transparent'">
                        </div>

                        <!-- 文本内容 -->
                        <div class="flex-1 min-w-0">
                            <div class="text-[13.5px] font-semibold text-gray-800 dark:text-gray-200 truncate mb-1">
                                {{ msg.title || '系统消息' }}
                            </div>
                            <div class="text-[12.5px] text-gray-500 dark:text-gray-400 leading-[1.6] line-clamp-2 pr-2">
                                {{ msg.content }}
                            </div>
                            <div class="text-[11px] text-[#86868b] dark:text-gray-500 mt-2 font-medium">
                                {{ msg.createTime }}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </el-popover>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';

const { proxy } = getCurrentInstance();

// 状态定义
const unreadCount = ref(0);
const messageList = ref([]);

const api = {
    getUnreadCount: '/message/getUnreadCount',
    loadDataList: '/message/loadDataList',
    markAsRead: '/message/markAsRead'
};

// 获取未读总数
const fetchUnreadCount = async () => {
    try {
        let result = await proxy.Request({
            url: api.getUnreadCount,
            showLoading: false,
            showError: false
        });
        if (result) {
            unreadCount.value = result.data || 0;
        }
    } catch (e) {
        // 静默处理，避免由于消息通知系统未上线而报错顶掉正常流程
    }
};

// 获取消息面板列表数据
const loadMessageList = async () => {
    let result = await proxy.Request({
        url: api.loadDataList,
        showLoading: false,
        params: {
            pageNo: 1,
            pageSize: 20 // 展现最近20条
        }
    });
    if (result) {
        // 兼容 PageResult 格式或直接 List 数组形式
        messageList.value = result.data.list || result.data || [];
    }
};

// 触发标为已读
const markAsRead = async (messageIds) => {
    let result = await proxy.Request({
        url: api.markAsRead,
        showLoading: false,
        params: {
            messageIds
        }
    });
    if (result) {
        // 操作成功后刷新外源红点和内部列表视图
        fetchUnreadCount();
        loadMessageList();
    }
};

onMounted(() => {
    fetchUnreadCount();
});
</script>


<style>
/* 全局样式：彻底清除 el-popover 自带的边框、背景、阴影和内边距 */
.notification-popover.el-popover {
    padding: 0 !important;
    border: none !important;
    background-color: transparent !important;
    box-shadow: none !important;
    border-radius: 0 !important;
}
</style>

<style scoped>
/* 保留你原来的滚动条样式 */
.custom-scrollbar::-webkit-scrollbar {
    width: 5px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
    transition: background-color 0.3s;
}

.custom-scrollbar:hover::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.15);
}

html.dark .custom-scrollbar:hover::-webkit-scrollbar-thumb {
    background-color: rgba(255, 255, 255, 0.2);
}

/* 鼠标悬浮圆形阴影效果 */
.notification-bell {
  /* 固定宽高，保证正圆形 */
  width: 36px;
  height: 36px;
  /* 正圆形 */
  border-radius: 50%;
  /* 平滑过渡动画 */
  transition: all 0.25s cubic-bezier(0.25, 0.1, 0.25, 1);
  /* 防止偏移 */
  margin: 0;
  margin-right: 12px;
}

/* 鼠标悬浮：圆形背景 + 柔和外阴影 */
.notification-bell:hover {
  background-color: rgba(0, 122, 255, 0.1);
  /* 圆形外发光阴影（核心效果） */
  box-shadow: 0 0 0 8px rgba(0, 122, 255, 0.05);
}

/* 暗黑模式适配 */
html.dark .notification-bell:hover {
  background-color: rgba(0, 122, 255, 0.15);
  box-shadow: 0 0 0 8px rgba(0, 122, 255, 0.08);
}
</style>