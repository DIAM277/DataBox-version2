<template>
    <div class="bg-switch-container" :class="{ 'expanded': isExpanded }" @mouseenter="expandButton"
        @mouseleave="startCollapseTimer">
        <button class="bg-switch-btn" data-text="切换背景" @click="changeBg"></button>
    </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue';

const emit = defineEmits(['change-background']);
const isExpanded = ref(false);
const collapseTimer = ref(null);

const changeBg = () => {
    emit('change-background');
}

const expandButton = () => {
    isExpanded.value = true;
    if (collapseTimer.value) {
        clearTimeout(collapseTimer.value);
        collapseTimer.value = null;
    }
}

const startCollapseTimer = () => {
    collapseTimer.value = setTimeout(() => {
        isExpanded.value = false;
    }, 100); // 2秒后自动收起
}

// 组件卸载时清除定时器
const clearTimers = () => {
    if (collapseTimer.value) {
        clearTimeout(collapseTimer.value);
    }
}

// 在组件卸载时清除定时器
onUnmounted(() => {
    clearTimers();
});
</script>

<style lang="scss" scoped>
.bg-switch-container {
    position: fixed;
    top: 20px;
    left: 0;
    z-index: 100;
    transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    transform: translateX(-80%);

    &.expanded {
        transform: translateX(0);
    }
}

.bg-switch-btn {
    width: 120px;
    height: 40px;
    border: none;
    border-radius: 0 7px 7px 0; // 只保留右侧圆角
    background-color: rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(10px);
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;
    cursor: pointer;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
}

.bg-switch-btn::before {
    content: '';
    width: 160px;
    height: 160px;
    position: absolute;
    background-image: linear-gradient(97deg, #3356f3, #1787f7 42%, #409EFF 74%, #66b1ff);
    filter: blur(12px);
    opacity: 0.7;
    animation: rotate-bg 6s linear infinite;
}

.bg-switch-btn::after {
    content: attr(data-text);
    font-size: 15px;
    font-weight: 600;
    color: #ffffff;
    letter-spacing: 1px;
    position: absolute;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
    transition: all 0.3s ease;
}

@keyframes rotate-bg {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

.bg-switch-btn:hover {
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
}

.bg-switch-btn:hover::before {
    opacity: 0.9;
    filter: blur(8px);
}

.bg-switch-btn:hover::after {
    transform: scale(1.05);
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
}

.bg-switch-btn:active {
    transform: scale(0.95);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
</style>