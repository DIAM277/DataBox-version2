<template>
    <div class="skeleton-container">
        <div class="skeleton-list">
            <div class="skeleton-row" v-for="i in rowCount" :key="i">
                <div class="skeleton-item skeleton-icon"></div>
                <div class="skeleton-item skeleton-name"></div>
                <div class="skeleton-item skeleton-size"></div>
                <div class="skeleton-item skeleton-type"></div>
                <div class="skeleton-item skeleton-time"></div>
            </div>
        </div>
    </div>
</template>

<script setup>
// 定义组件属性
const props = defineProps({
    // 骨架行数
    rowCount: {
        type: Number,
        default: 5
    }
})
</script>

<style lang="scss" scoped>
.skeleton-container {
    padding-right: 20px;
    margin-top: 10px;
}

.skeleton-header {
    margin-bottom: 15px;
}

.skeleton-item {
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: 12px;
}

.skeleton-title {
    height: 24px;
    width: 120px;
}

.skeleton-list {
    background-color: rgba(255, 255, 255, 0.5);
    border-radius: 4px;
}

.skeleton-row {
    display: flex;
    align-items: center;
    padding: 12px 8px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.skeleton-icon {
    width: 32px;
    height: 32px;
    border-radius: 4px;
    margin-right: 12px;
}

.skeleton-name {
    height: 16px;
    width: 50%;
    margin-right: 20px;
}

.skeleton-size {
    height: 16px;
    width: 10%;
    margin-right: 20px;
}

.skeleton-type {
    height: 16px;
    width: 10%;
    margin-right: 20px;
}

.skeleton-time {
    height: 16px;
    width: 30%px;
}

@keyframes skeleton-loading {
    0% {
        background-position: 200% 0;
    }

    100% {
        background-position: -200% 0;
    }
}
</style>