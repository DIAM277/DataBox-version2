<template>
    <div class="avatar-container" @mouseenter="interactive && (showPopover = true)"
        @mouseleave="interactive && startHideTimer">
        <!-- 头像部分 -->
        <span class="avatar" :class="{ 'avatar-expanded': showPopover && interactive }"
            :style="{ width: width + 'px', height: width + 'px' }">
            <img :src="avatar && avatar != '' ? avatar : `${proxy.globalInfo.avatarUrl}${userId}?${timestamp}`"
                v-if="userId" />
        </span>

        <!-- 弹出信息部分 -->
        <transition name="popover-fade">
            <div v-if="showPopover && interactive" class="avatar-popover" @mouseenter="clearHideTimer"
                @mouseleave="startHideTimer">
                <div class="user-info">
                    <div class="user-name">{{ userName || '用户名' }}</div>
                    <div class="user-id">ID: {{ userId || 'Unknown' }}</div>
                    <div class="user-email">{{ userEmail || 'example@email.com' }}</div>
                </div>

                <div class="action-buttons" v-if="showActions">
                    <button class="action-btn" @click="handleAction('editAvatar')">
                        <i class="el-icon-picture"></i>修改资料
                    </button>
                    <button class="action-btn" @click="handleAction('changePassword')">
                        <i class="el-icon-lock"></i>修改密码
                    </button>
                    <button class="action-btn logout-btn" @click="handleAction('logout')">
                        <i class="el-icon-switch-button"></i>退出登录
                    </button>
                </div>
            </div>
        </transition>
    </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();
import { useRouter } from 'vue-router';

const router = useRouter();
const showPopover = ref(false);
const hideTimer = ref(null);

const props = defineProps({
    userId: {
        type: String,
    },
    userName: {
        type: String,
        default: ''
    },
    userEmail: {
        type: String,
        default: ''
    },
    avatar: {
        type: String,
    },
    timestamp: {
        type: Number,
        default: 0
    },
    width: {
        type: Number,
        default: 40
    },
    interactive: {
        type: Boolean,
        default: true
    },
    showActions: {
        type: Boolean,
        default: true
    }
});

const emit = defineEmits(['action']);

// 处理各种操作按钮的点击事件
const handleAction = (action) => {
    emit('action', action);
    showPopover.value = false;
};

// 开始隐藏计时器
const startHideTimer = () => {
    hideTimer.value = setTimeout(() => {
        showPopover.value = false;
    }, 100);
};

// 清除隐藏计时器
const clearHideTimer = () => {
    if (hideTimer.value) {
        clearTimeout(hideTimer.value);
        hideTimer.value = null;
    }
};

// 组件卸载时清除计时器
onUnmounted(() => {
    clearHideTimer();
});
</script>

<style lang="scss" scoped>
.avatar-container {
    position: relative;
    display: inline-block;
    z-index: 999;
}

.avatar {
    display: flex;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    position: relative;
    z-index: 1001;
    background-color: #f0f2f5;

    img {
        width: 100%;
        object-fit: cover;
    }

    &.avatar-expanded {
        transform: scale(2) translate(0px, 20px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        border: 2px solid white;
    }
}

.avatar-popover {
    position: absolute;
    //top: calc(100% + 40px);
    top: calc(100% + 20px);
    right: auto;
    left: 50%;
    transform: translateX(-50%);
    width: 220px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
    padding: 38px 16px 16px 16px;
    z-index: 1000;

    &::before {
        content: '';
        position: absolute;
        top: -8px;
        left: 50%;
        transform: translateX(-50%) rotate(45deg);
        width: 16px;
        height: 16px;
        background: white;
        box-shadow: -2px -2px 5px rgba(0, 0, 0, 0.04);
    }

    .user-info {
        text-align: center;
        margin-bottom: 16px;
        margin-top: 10px;

        .user-name {
            font-size: 18px;
            font-weight: 600;
            color: #333;
            margin-bottom: 4px;
        }

        .user-id {
            font-size: 12px;
            color: #999;
            margin-bottom: 4px;
        }

        .user-email {
            font-size: 14px;
            color: #666;
        }
    }

    .action-buttons {
        display: flex;
        flex-direction: column;
        gap: 8px;

        .action-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 8px 12px;
            border: none;
            border-radius: 4px;
            background: #f5f7fa;
            color: #5a6a7a;
            font-size: 14px;
            cursor: pointer;
            transition: all 0.2s ease;

            i {
                margin-right: 6px;
                font-size: 16px;
            }

            &:hover {
                background: #e9ecf2;
                color: #05a1f5;
            }
        }

        .logout-btn {
            margin-top: 4px;
            background: #fff2f0;
            color: #ff4d4f;

            &:hover {
                background: #fff1f0;
                color: #ff4d4f;
            }
        }
    }
}

// 弹出动画
.popover-fade-enter-active,
.popover-fade-leave-active {
    transition: opacity 0.3s, transform 0.3s;
}

.popover-fade-enter-from,
.popover-fade-leave-to {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
}
</style>