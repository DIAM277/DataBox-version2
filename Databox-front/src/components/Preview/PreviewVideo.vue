<template>
    <div ref="player" id="player"></div>
</template>

<script setup>
import DPlayer from "dplayer"
import Hls from 'hls.js'
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onBeforeUnmount, watch } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
    url: {
        type: String,
    }
})

const videoInfo = ref({
    video: null
})
const player = ref()
let dp = null // 存储播放器实例
let hls = null // 存储 HLS 实例

const initPlayer = () => {
    // 确保在初始化前销毁之前的实例
    destroyPlayer()

    dp = new DPlayer({
        element: player.value,
        theme: '#b7daff',
        screenshot: true,
        video: {
            url: `/api/${props.url}`,
            type: 'customHls',
            customType: {
                customHls: function (video, player) {
                    if (Hls.isSupported()) {
                        hls = new Hls();
                        hls.loadSource(video.src);
                        hls.attachMedia(video);
                    } else {
                        console.error('HLS 不支持');
                    }
                }
            }
        }
    })

    // 监听播放器错误
    dp.on('error', () => {
        console.error('视频播放错误');
    });
}

// 销毁播放器实例和释放资源
const destroyPlayer = () => {
    if (dp) {
        dp.destroy(); // 销毁 DPlayer 实例
        dp = null;
    }

    if (hls) {
        hls.destroy(); // 销毁 HLS 实例
        hls = null;
    }
}

// 监听 url 变化，重新初始化播放器
watch(() => props.url, (newVal) => {
    if (newVal) {
        nextTick(() => {
            initPlayer();
        });
    }
}, { immediate: false });

onMounted(() => {
    if (props.url) {
        initPlayer()
    }
})

// 组件卸载前销毁播放器
onBeforeUnmount(() => {
    destroyPlayer();
})

// 暴露销毁方法给父组件
defineExpose({
    destroyPlayer
})
</script>

<style lang="scss" scoped>
#player {
    width: 100%;

    :deep(.dplayer-video-wrap) {
        text-align: center;

        .dplayer-video {
            margin: 0px auto;
            max-height: calc(100vh - 100px); // 增加最大高度
        }
    }
}
</style>
