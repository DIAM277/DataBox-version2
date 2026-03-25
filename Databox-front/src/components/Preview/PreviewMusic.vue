<template>
    <!-- 最外层全屏渲染包裹（透明黑底，承载沉浸式模糊） -->
    <div
        class="w-full h-full flex flex-col items-center justify-center relative overflow-hidden bg-[#000] rounded-2xl md:rounded-[2rem] font-sans">

        <!-- 原生底层隐藏的 Audio 引擎 -->
        <audio ref="audioRef" :src="`/api/${props.url}`" autoplay @timeupdate="onTimeUpdate"
            @loadedmetadata="onLoadedMetadata" @play="isPlaying = true" @pause="isPlaying = false" @ended="onEnded">
        </audio>

        <!-- 背景气氛虚化层 -->
        <div
            class="absolute inset-0 z-0 pointer-events-none w-full h-full scale-125 transition-transform duration-[10s]">
            <img src="@/assets/music_cover.png"
                class="w-full h-full object-cover blur-[80px] opacity-40 brightness-75 mix-blend-screen" />
        </div>

        <!-- 核心播放卡片 (无边框悬浮层) -->
        <div class="relative z-10 w-[85%] max-w-[400px] flex flex-col items-center select-none pt-4 pb-4">

            <!-- 动态呼吸缩放大封面 -->
            <div class="w-[85%] sm:w-full max-w-[340px] aspect-square rounded-3xl overflow-hidden shadow-[0_30px_60px_rgba(0,0,0,0.6)] transition-all duration-700 ease-[cubic-bezier(0.25,0.8,0.25,1)]"
                :class="isPlaying ? 'scale-100 opacity-100' : 'scale-[0.82] opacity-80 shadow-[0_15px_30px_rgba(0,0,0,0.3)]'">
                <img src="@/assets/music_cover.png"
                    class="w-full h-full object-cover pointer-events-none select-none" />
            </div>

            <!-- 左对齐信息排版 -->
            <div class="w-full flex flex-col items-start mt-8 mb-6 px-2">
                <h2 class="text-[22px] sm:text-2xl font-bold text-white tracking-wide truncate w-full"
                    :title="fileName">
                    {{ fileName || '未知歌曲' }}
                </h2>
                <span class="text-white/60 text-[16px] font-medium mt-1">本地音乐</span>
            </div>

            <!-- Apple 原生级细轨道进度条 -->
            <div class="w-full flex flex-col mb-2 px-2">
                <el-slider v-model="displayTime" :max="duration" :show-tooltip="false" @input="onSliderInput"
                    @change="onSeek" class="apple-music-slider" />
                <!-- 播放时钟字距 -->
                <div
                    class="flex justify-between w-full text-[11.5px] text-white/50 font-medium tracking-widest mt-1.5 px-0.5">
                    <span>{{ formatTime(displayTime) }}</span>
                    <span>-{{ formatTime(Math.max(0, duration - displayTime)) }}</span>
                </div>
            </div>

            <!-- 极大化中央控制簇 -->
            <div class="w-full flex justify-center items-center mt-2.5">
                <div @click="togglePlay"
                    class="w-[76px] h-[76px] rounded-full bg-white/10 hover:bg-white/20 active:scale-95 text-white flex items-center justify-center backdrop-blur-2xl transition-all duration-200 cursor-pointer shadow-lg border border-white/10 group">

                    <!-- 播放/暂停动态切换 SVG -->
                    <svg v-if="!isPlaying" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"
                        class="w-10 h-10 ml-1.5 opacity-90 group-hover:opacity-100 transition-opacity">
                        <path fill-rule="evenodd"
                            d="M4.5 5.653c0-1.426 1.529-2.33 2.779-1.643l11.54 6.348c1.295.712 1.295 2.573 0 3.285L7.28 19.991c-1.25.687-2.779-.217-2.779-1.643V5.653z"
                            clip-rule="evenodd" />
                    </svg>
                    <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"
                        class="w-9 h-9 opacity-90 group-hover:opacity-100 transition-opacity">
                        <path fill-rule="evenodd"
                            d="M6.75 5.25a.75.75 0 01.75-.75H9a.75.75 0 01.75.75v13.5a.75.75 0 01-.75.75H7.5a.75.75 0 01-.75-.75V5.25zm7.5 0A.75.75 0 0115 4.5h1.5a.75.75 0 01.75.75v13.5a.75.75 0 01-.75.75H15a.75.75 0 01-.75-.75V5.25z"
                            clip-rule="evenodd" />
                    </svg>
                </div>
            </div>

            <!-- iOS Control Center 级音量控制条 (沉浸式弱化显色) -->
            <div
                class="w-[92%] flex items-center justify-between gap-4 mt-7 px-2 opacity-60 hover:opacity-100 transition-opacity duration-300">
                <!-- 喇叭小图标 (静音/非静音) -->
                <svg @click="toggleMute" class="w-4 h-4 text-white cursor-pointer active:scale-90 transition-transform"
                    viewBox="0 0 24 24" fill="currentColor">
                    <!-- 静音状态带斜杠的喇叭 -->
                    <path v-if="isMuted || volume === 0" fill-rule="evenodd"
                        d="M13.5 4.06c0-1.336-1.616-2.005-2.56-1.06l-4.5 4.5H4.508c-1.141 0-2.318.664-2.66 1.905A9.76 9.76 0 001.5 12c0 .898.121 1.768.35 2.595.341 1.24 1.518 1.905 2.659 1.905h1.93l4.5 4.5c.945.945 2.561.276 2.561-1.06V4.06zm-7.97 10.74l8.486-8.485a.75.75 0 011.061 1.06l-8.486 8.486a.75.75 0 01-1.06-1.061z"
                        clip-rule="evenodd" />
                    <!-- 最小音量非静音喇叭 -->
                    <path v-else
                        d="M13.5 4.06c0-1.336-1.616-2.005-2.56-1.06l-4.5 4.5H4.508c-1.141 0-2.318.664-2.66 1.905A9.76 9.76 0 001.5 12c0 .898.121 1.768.35 2.595.341 1.24 1.518 1.905 2.659 1.905h1.93l4.5 4.5c.945.945 2.561.276 2.561-1.06V4.06z" />
                </svg>

                <!-- 音量滑轨 -->
                <el-slider v-model="volume" :max="100" :show-tooltip="false" @input="onVolumeChange"
                    class="apple-music-slider flex-1" />

                <!-- 最大响度音量喇叭 -->
                <svg @click="volume = 100; onVolumeChange(100)"
                    class="w-[18px] h-[18px] text-white cursor-pointer active:scale-90 transition-transform"
                    viewBox="0 0 24 24" fill="currentColor">
                    <path
                        d="M13.5 4.06c0-1.336-1.616-2.005-2.56-1.06l-4.5 4.5H4.508c-1.141 0-2.318.664-2.66 1.905A9.76 9.76 0 001.5 12c0 .898.121 1.768.35 2.595.341 1.24 1.518 1.905 2.659 1.905h1.93l4.5 4.5c.945.945 2.561.276 2.561-1.06V4.06zM17.78 9.22a.75.75 0 10-1.06 1.06A2.5 2.5 0 0117.5 12a2.5 2.5 0 01-.78 1.72.75.75 0 001.06 1.06A4 4 0 0019 12a4 4 0 00-1.22-2.78zM20.25 6.75a.75.75 0 10-1.06 1.06A6 6 0 0120.5 12a6 6 0 01-1.31 4.19.75.75 0 101.06 1.06A7.5 7.5 0 0022 12a7.5 7.5 0 00-1.75-5.25z" />
                </svg>
            </div>

        </div>
    </div>
</template>


<script setup>
import { ref, onUnmounted } from "vue"

const props = defineProps({
    url: {
        type: String,
    },
    fileName: {
        type: String,
    }
})

// ======================= 核心状态 =======================
const audioRef = ref(null)
const isPlaying = ref(false)
const duration = ref(0)
const displayTime = ref(0) // 用于双向绑定滑动条显示的时间
const isDragging = ref(false) // 标记推拽状态防抖

// ======================= 音量控制状态 ====================
const volume = ref(50)
const isMuted = ref(false)
let lastVolume = 100

// ======================= 业务逻辑 =======================
const togglePlay = () => {
    if (!audioRef.value) return
    if (isPlaying.value) {
        audioRef.value.pause()
    } else {
        audioRef.value.play()
    }
}

// 获取元数据后读取总长 (单位:秒)
const onLoadedMetadata = () => {
    if (audioRef.value) {
        duration.value = audioRef.value.duration || 0
        audioRef.value.volume = volume.value / 100 // 初始化音量
    }
}

// 自动随时间推进引擎
const onTimeUpdate = () => {
    if (!audioRef.value || isDragging.value) return
    displayTime.value = audioRef.value.currentTime
}

const onEnded = () => {
    isPlaying.value = false
    displayTime.value = 0
    if (audioRef.value) {
        audioRef.value.currentTime = 0
    }
}

// 在进度条上拖拽中，避免原生事件把滑块吸回去
const onSliderInput = (val) => {
    isDragging.value = true
    displayTime.value = val
}

// 释放滑块，完成强制跳转跳跃
const onSeek = (val) => {
    if (!audioRef.value) return
    audioRef.value.currentTime = val
    displayTime.value = val
    isDragging.value = false
    // 拖动完后保证继续播放态
    if (!isPlaying.value) {
        audioRef.value.play()
    }
}

// ------ 新增音量控制逻辑 ------
const onVolumeChange = (val) => {
    if (!audioRef.value) return
    audioRef.value.volume = val / 100
    isMuted.value = (val === 0)
}

const toggleMute = () => {
    if (!audioRef.value) return
    if (isMuted.value || volume.value === 0) {
        // 取消静音并恢复之前的音量
        isMuted.value = false
        volume.value = lastVolume === 0 ? 50 : lastVolume
        audioRef.value.volume = volume.value / 100
    } else {
        // 静音
        lastVolume = volume.value
        isMuted.value = true
        volume.value = 0
        audioRef.value.volume = 0
    }
}

// 时间转换格式化 mm:ss
const formatTime = (seconds) => {
    if (!seconds || isNaN(seconds)) return '0:00'
    const mins = Math.floor(seconds / 60)
    const secs = Math.floor(seconds % 60)
    return `${mins}:${secs.toString().padStart(2, '0')}`
}

// 组件卸载时释放内存和音频线
onUnmounted(() => {
    if (audioRef.value) {
        audioRef.value.pause()
        audioRef.value.src = ''
    }
})
</script>

<style scoped>
/* =======================================================
   穿透 el-slider：重写为极致纤细的白底 Apple 风格轨道
======================================================== */

:deep(.apple-music-slider .el-slider__runway) {
    background-color: rgba(255, 255, 255, 0.2) !important;
    height: 5px !important;
    border-radius: 6px !important;
}

:deep(.apple-music-slider .el-slider__bar) {
    background-color: rgba(255, 255, 255, 0.95) !important;
    height: 5px !important;
    border-radius: 6px !important;
    transition: width 0.1s linear;
}

:deep(.apple-music-slider .el-slider__button-wrapper) {
    height: 36px !important;
    width: 36px !important;
    top: -15.5px !important;
}

/* 隐没但有阻尼感回弹的白点指引器 */
:deep(.apple-music-slider .el-slider__button) {
    width: 8px !important;
    height: 8px !important;
    border: none !important;
    background-color: white !important;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3) !important;
    transition: transform 0.2s cubic-bezier(0.25, 0.8, 0.25, 1), opacity 0.2s ease !important;
    opacity: 0;
    /* 默认纯粹无节点隐藏 */
}

/* 鼠标悬浮和拖动时弹出触控球 */
:deep(.apple-music-slider:hover .el-slider__button),
:deep(.apple-music-slider .el-slider__button.dragging) {
    opacity: 1;
    transform: scale(2);
}
</style>