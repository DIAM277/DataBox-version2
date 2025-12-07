<template>
    <div class="music">
        <div class="body-content">
            <div class="record-player">
                <div class="vinyl" :class="{ 'rotating': isPlaying }" :style="vinylStyle">
                    <div class="cover">
                        <img src="@/assets/music_cover.png" />
                    </div>
                </div>
                <div class="tone-arm" :class="{ 'playing': isPlaying }"></div>
            </div>
            <div class="music-info">
                <h3 class="song-name">{{ props.fileName || '未知歌曲' }}</h3>
            </div>
            <div class="music-player" ref="playerRef"></div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted, watch, computed } from "vue"
const { proxy } = getCurrentInstance();
import APlayer from 'aplayer'
import 'Aplayer/dist/APlayer.min.css'

const props = defineProps({
    url: {
        type: String,
    },
    fileName: {
        type: String,
    }
})

const playerRef = ref()
const player = ref()
const isPlaying = ref(false)
const rotationDegree = ref(0)
const lastTimestamp = ref(0)

// 计算唱片的样式，包括旋转角度
const vinylStyle = computed(() => {
    return {
        transform: `rotate(${rotationDegree.value}deg)`
    }
})

// 动画帧函数，用于平滑旋转
const animateRotation = (timestamp) => {
    if (!lastTimestamp.value) {
        lastTimestamp.value = timestamp
    }
    const elapsed = timestamp - lastTimestamp.value
    if (isPlaying.value) {
        // 当播放时，以固定速度旋转
        rotationDegree.value = (rotationDegree.value + (elapsed * 0.036)) % 360
    }
    lastTimestamp.value = timestamp
    requestAnimationFrame(animateRotation)
}

// 监听播放状态
const setupEventListeners = () => {
    if (!player.value) return

    // 播放时旋转唱片
    player.value.on('play', () => {
        isPlaying.value = true
    })

    // 暂停时停止旋转，但保持当前角度
    player.value.on('pause', () => {
        isPlaying.value = false
    })

    // 播放结束时停止旋转，但保持当前角度
    player.value.on('ended', () => {
        isPlaying.value = false
    })
}

onMounted(() => {
    player.value = new APlayer({
        container: playerRef.value,
        audio: {
            url: `/api/${props.url}`,
            name: props.fileName,
            cover: new URL('@/assets/music_cover.png', import.meta.url).href,
            artist: ''
        },
        theme: '#409EFF',
        autoplay: true,
        fixed: false,
        listFolded: true,
        listMaxHeight: 90
    })

    // 设置事件监听
    nextTick(() => {
        setupEventListeners()
        // 启动动画帧
        requestAnimationFrame(animateRotation)
    })
})

onUnmounted(() => {
    if (player.value) {
        player.value.destroy()
    }
})
</script>

<style lang="scss" scoped>
.music {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%); // 白色渐变背景
    padding: 20px;

    .body-content {
        text-align: center;
        width: 80%;
        max-width: 500px;
        background-color: #ffffff; // 纯白色背景
        border-radius: 15px;
        padding: 30px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1); // 减轻阴影

        .record-player {
            position: relative;
            width: 250px;
            height: 250px;
            margin: 0 auto 30px;

            .vinyl {
                position: relative;
                width: 220px;
                height: 220px;
                margin: 0 auto;
                border-radius: 50%;
                background: radial-gradient(circle, #151515 0%, #000 40%, #151515 60%, #000 100%);
                box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
                display: flex;
                align-items: center;
                justify-content: center;
                // 移除transition，使用requestAnimationFrame控制旋转

                &::before {
                    content: '';
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    width: 40px;
                    height: 40px;
                    background-color: #409EFF; // 更改为蓝色
                    border-radius: 50%;
                    z-index: 2;
                }

                &::after {
                    content: '';
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    width: 15px;
                    height: 15px;
                    background-color: #000;
                    border-radius: 50%;
                    z-index: 3;
                }

                // 移除旋转动画，使用JavaScript控制

                .cover {
                    width: 150px;
                    height: 150px;
                    border-radius: 50%;
                    overflow: hidden;
                    z-index: 1;

                    img {
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                    }
                }
            }

            .tone-arm {
                position: absolute;
                top: 41px;
                right: 20px;
                width: 130px;
                height: 15px;
                background-color: #666; // 更亮的颜色
                border-radius: 5px;
                transform-origin: right center;
                transform: rotate(-30deg);
                transition: transform 0.8s ease;
                z-index: 10;

                &::before {
                    content: '';
                    position: absolute;
                    top: -5px;
                    right: -5px;
                    width: 25px;
                    height: 25px;
                    background-color: #888; // 更亮的颜色
                    border-radius: 50%;
                }

                &::after {
                    content: '';
                    position: absolute;
                    top: 5px;
                    left: 5px;
                    width: 5px;
                    height: 5px;
                    background-color: #409EFF; // 更改为蓝色
                    border-radius: 50%;
                }

                &.playing {
                    transform: rotate(0deg);
                }
            }
        }

        .music-info {
            margin-bottom: 20px;

            .song-name {
                color: #333; // 更改为深灰色
                font-size: 18px;
                margin: 0;
                text-overflow: ellipsis;
                overflow: hidden;
                white-space: nowrap;
            }
        }

        .music-player {
            margin-top: 20px;
        }
    }
}

// 移除旋转动画

:deep(.aplayer) {
    background-color: #f5f7fa; // 更改为浅灰色背景
    border-radius: 10px;
    color: #333; // 更改为深灰色文字

    .aplayer-info {
        border-top: none;

        .aplayer-controller .aplayer-bar {
            background: #e4e7ed; // 更亮的背景

            .aplayer-loaded {
                background: #c0c4cc; // 更亮的加载条
            }

            .aplayer-played {
                background: #409EFF; // 蓝色播放条
            }
        }
    }

    .aplayer-time {
        color: #606266; // 更改为灰色
    }

    .aplayer-icon {
        color: #606266; // 更改为灰色

        &:hover {
            color: #409EFF; // 悬停时变为蓝色
        }
    }
}
</style>