<template>
    <div class="relative font-sans select-none">
        <!-- 魔法悬浮按钮 (常驻右下角) -->
        <div v-show="!showPanel" @click="fetchSummary"
            class="fixed bottom-10 right-10 z-[100] px-5 py-3 rounded-full bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white font-semibold flex items-center gap-2 shadow-[0_0_20px_rgba(168,85,247,0.4)] hover:shadow-[0_0_30px_rgba(168,85,247,0.6)] hover:scale-105 active:scale-95 transition-all duration-300 cursor-pointer">
            <span class="text-[17px] leading-none">✨</span>
            <span class="tracking-wide text-[14px]">智能摘要</span>
        </div>

        <!-- 毛玻璃抽屉面板 -->
        <div class="fixed right-0 top-0 bottom-0 w-[85vw] md:w-[420px] bg-white/70 dark:bg-black/60 backdrop-blur-3xl shadow-[-10px_0_30px_rgba(0,0,0,0.1)] border-l border-white/20 dark:border-gray-700/50 z-[110] flex flex-col transition-transform duration-500 ease-[cubic-bezier(0.25,0.8,0.25,1)]"
            :class="showPanel ? 'translate-x-0' : 'translate-x-full'">

            <!-- 面板 Header -->
            <div
                class="flex items-center justify-between px-6 py-5 border-b border-gray-200/50 dark:border-gray-700/50 shrink-0">
                <div class="flex items-center gap-2">
                    <span
                        class="text-[18px] leading-none bg-clip-text text-transparent bg-gradient-to-r from-blue-500 to-purple-500">✨</span>
                    <span class="text-lg font-bold text-gray-900 dark:text-white tracking-wide">AI 文档摘要</span>

                    <!-- 🚨 新增：极简苹果风额度跳变胶囊 -->
                    <span v-if="remainQuota || remainQuota === 0"
                        class="ml-3 px-2.5 py-0.5 bg-blue-50/80 dark:bg-blue-900/30 text-blue-500 dark:text-blue-400 text-[11px] font-medium rounded-full border border-blue-100 dark:border-blue-800 shadow-sm transition-all duration-300">
                        剩余 {{ remainQuota }} 次
                    </span>
                </div>
                <!-- 缩小关闭按钮 -->
                <div @click="showPanel = false"
                    class="w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400 flex items-center justify-center cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-700 hover:text-gray-800 dark:hover:text-white transition-all shadow-sm active:scale-90">
                    <span class="iconfont icon-close2 text-[14px]"></span>
                </div>
            </div>

            <!-- 面板 Body -->
            <div class="flex-1 overflow-y-auto p-6 scrollbar-hide relative">

                <!-- 1. 骨架：Apple 风呼吸加载态 UI -->
                <div v-if="aiLoading" class="flex flex-col gap-4 animate-pulse pt-2">
                    <div class="flex items-center gap-3 mb-2">
                        <!-- 发光的加载圆钮 -->
                        <div
                            class="w-4 h-4 rounded-full border-2 border-purple-500 border-t-transparent animate-spin shadow-lg">
                        </div>
                        <span
                            class="text-[14.5px] font-semibold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-purple-500">
                            AI 正在深度阅读文档...
                        </span>
                    </div>
                    <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full w-full mt-2"></div>
                    <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full w-11/12"></div>
                    <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full w-4/5"></div>
                    <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full w-full"></div>
                    <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full w-5/6"></div>
                    <div class="h-3 bg-gray-200 dark:bg-gray-700 rounded-full w-3/4 mt-4"></div>
                </div>

                <!-- 2. 异常捕获区：优雅地留住面板并汇报情况 -->
                <div v-else-if="aiErrorMsg" class="flex flex-col items-center justify-center h-full pb-10">
                    <div
                        class="w-16 h-16 rounded-full bg-red-50 dark:bg-red-900/20 flex flex-col items-center justify-center mb-4 border border-red-100 dark:border-red-900/30">
                        <!-- 🔴 修复：使用绝对不丢失的内联 SVG 警告图标 -->
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.8"
                            stroke="currentColor" class="w-8 h-8 text-red-500 opacity-90">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                    </div>
                    <div
                        class="text-[14.5px] text-gray-700 dark:text-gray-300 font-medium text-center px-4 leading-[1.8] tracking-wide whitespace-pre-line">
                        {{ aiErrorMsg }}
                    </div>
                    <button @click="fetchSummary" type="button"
                        class="mt-8 px-6 py-2.5 bg-[#007AFF] hover:bg-[#0066cc] text-white rounded-full text-[13.5px] font-semibold transition-all active:scale-95 shadow-sm">
                        重新生成
                    </button>
                </div>

                <!-- 3. 结果展现区 (使用 v-html 进行 Markdown 强渲染) -->
                <div v-else-if="aiResult"
                    class="markdown-body text-[14.5px] leading-[1.75] text-gray-800 dark:text-gray-300 selection:bg-purple-200 dark:selection:bg-purple-900 tracking-wide select-text"
                    v-html="parsedMarkdown">
                </div>

            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, getCurrentInstance } from 'vue';
import { marked } from 'marked';

const { proxy } = getCurrentInstance();

const props = defineProps({
    fileId: {
        type: String,
        required: true
    }
});

const showPanel = ref(false);
const aiLoading = ref(false);
const aiResult = ref('');
const aiErrorMsg = ref('');
// 每日额度配置
const remainQuota = ref(0);

const parsedMarkdown = computed(() => {
    if (!aiResult.value) return '';
    return marked.parse(aiResult.value);
});

// 同步获取最新AI额度
const fetchQuota = async () => {
    let result = await proxy.Request({
        url: '/ai/getAiQuota',
        showLoading: false
    });
    if (result !== undefined && result !== null) {
        // 由于不同页面的封装返回特征可能不同，直接利用安全取值
        remainQuota.value = result.data !== undefined ? result.data : result;
    }
};

// 核心对接请求方法
const fetchSummary = async () => {
    // 首次点击或被隐藏后再次触发生成前，执行安全额度对齐
    if (!showPanel.value) {
        showPanel.value = true;
        fetchQuota();
    }

    if (aiResult.value || aiLoading.value) {
        return;
    }

    aiLoading.value = true;
    aiErrorMsg.value = '';

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 60000);

    try {
        const response = await fetch(`/api/ai/summary?fileId=${props.fileId}`, {
            method: 'GET',
            signal: controller.signal
        });

        clearTimeout(timeoutId);
        const resData = await response.json();

        if (resData.code === 200) {
            aiResult.value = resData.data;
            // 🚨 前端极其丝滑的乐观扣减反馈法：不再重复请求增加网络压力，直接更新视口数据
            if (remainQuota.value > 0) {
                remainQuota.value--;
            }
        } else {
            // 后端主动拦截或解析抛错返回的异常提示
            aiErrorMsg.value = resData.info || "当前文档无法被有效读取或提取失败。";
        }
    } catch (error) {
        // 请求被本地 60 秒强行打断 或者 断网
        if (error.name === 'AbortError') {
            aiErrorMsg.value = "AI 模型思考时间过长。\n文档可能极大，后台正在努力处理中。\n请稍后再次点击重试即可极速读取！";
        } else {
            aiErrorMsg.value = "网络异常：请检查服务通道状态并稍后重试。";
        }
    } finally {
        aiLoading.value = false;
    }
};
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}

.scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
}

:deep(.markdown-body p) {
    margin-bottom: 14px;
}

:deep(.markdown-body h1),
:deep(.markdown-body h2),
:deep(.markdown-body h3),
:deep(.markdown-body h4) {
    font-weight: 600;
    margin-top: 20px;
    margin-bottom: 10px;
    line-height: 1.4;
    color: inherit !important;
}

:deep(.markdown-body strong),
:deep(.markdown-body b) {
    font-weight: 700;
    color: inherit !important;
}

:deep(.markdown-body h1) {
    font-size: 1.25em;
}

:deep(.markdown-body h2) {
    font-size: 1.15em;
}

:deep(.markdown-body h3) {
    font-size: 1.05em;
}

:deep(.markdown-body ul) {
    list-style-type: disc;
    padding-left: 18px;
    margin-bottom: 14px;
}

:deep(.markdown-body ol) {
    list-style-type: decimal;
    padding-left: 18px;
    margin-bottom: 14px;
}

:deep(.markdown-body li) {
    margin-bottom: 6px;
}
</style>