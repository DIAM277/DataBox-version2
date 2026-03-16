<template>
    <div>
        <el-dialog :show-close="showClose" :draggable="true" :model-value="show" :close-on-click-modal="false"
            :title="showCustomTitle ? '' : title" class="apple-dialog" :top="top" :width="width" @close="close">

            <div class="dialog-title" v-if="showCustomTitle">{{ title }}</div>

            <div class="dialog-body" :style="{ 'max-height': maxHeight + 'px', padding: padding + 'px' }">
                <slot></slot>
            </div>

            <template v-if="(buttons && buttons.length > 0) || showCancel">
                <div class="dialog-footer">
                    <el-button @click="close" v-if="showCancel" class="apple-btn cancel-btn">
                        取消
                    </el-button>
                    <!-- 动态绑定传入的按钮类型 -->
                    <el-button v-for="(btn, index) in buttons" :key="index" :type="btn.type || 'primary'"
                        @click="btn.click" class="apple-btn" :class="'apple-btn-' + (btn.type || 'primary')">
                        {{ btn.text }}
                    </el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
const props = defineProps({
    title: {
        type: String
    },
    show: {
        type: Boolean,
        default: false,
    },
    showClose: {
        type: Boolean,
        default: true,
    },
    showCancel: {
        type: Boolean,
        default: true,
    },
    top: {
        type: String,
        default: '15vh',
    },
    width: {
        type: String,
        default: '30%',
    },
    buttons: {
        type: Array
    },
    padding: {
        type: Number,
        default: 15
    },
    showCustomTitle: {
        type: Boolean,
        default: false,
    }
})

const maxHeight = window.innerHeight - parseInt(props.top) - 150; // 安全容差

const emit = defineEmits()
const close = () => {
    emit('close')
}
</script>

<style lang="scss" scoped>
/* ==============================================
   Apple/macOS Style Dialog (原生视觉级覆写)
============================================== */

.apple-dialog {

    /* 强核心接管 el-dialog 面板 */
    :deep(.el-dialog) {
        /* 高斯模糊与毛玻璃叠加材质 */
        background-color: rgba(255, 255, 255, 0.75) !important;
        backdrop-filter: blur(40px) saturate(150%) !important;
        -webkit-backdrop-filter: blur(40px) saturate(150%) !important;

        border-radius: 32px !important;
        border: 1px solid rgba(255, 255, 255, 0.6) !important;
        box-shadow: 0 25px 60px -10px rgba(0, 0, 0, 0.15), 0 0 1px rgba(0, 0, 0, 0.1) !important;
        overflow: hidden !important;

        /* 全局平滑进出动画支持 */
        transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important;

        /* 暗色模式材质匹配 */
        html.dark & {
            background-color: rgba(28, 28, 30, 0.75) !important;
            border: 1px solid rgba(255, 255, 255, 0.08) !important;
            box-shadow: 0 35px 80px -10px rgba(0, 0, 0, 0.6), 0 0 1px rgba(255, 255, 255, 0.05) !important;
        }

        /* 顶部Header (不含自定义Title时的原生Title区) */
        .el-dialog__header {
            padding: 24px 24px 10px !important;
            margin: 0 !important;
            border-bottom: none !important;
        }

        .el-dialog__title {
            font-size: 18px !important;
            font-weight: 600 !important;
            color: #1d1d1f !important;
            letter-spacing: 0.3px !important;

            html.dark & {
                color: #f5f5f7 !important;
            }
        }

        /* 高级圆润的关闭按钮组件 */
        .el-dialog__headerbtn {
            top: 20px !important;
            right: 20px !important;
            width: 32px !important;
            height: 32px !important;
            border-radius: 50% !important;
            background-color: rgba(0, 0, 0, 0.05) !important;
            transition: all 0.2s ease !important;

            &:hover {
                background-color: rgba(0, 0, 0, 0.1) !important;
                transform: scale(1.05);
            }

            .el-dialog__close {
                color: rgba(60, 60, 67, 0.7) !important;
                font-size: 15px !important;
                font-weight: bold !important;
            }

            html.dark & {
                background-color: rgba(255, 255, 255, 0.1) !important;

                &:hover {
                    background-color: rgba(255, 255, 255, 0.15) !important;
                }

                .el-dialog__close {
                    color: rgba(235, 235, 245, 0.7) !important;
                }
            }
        }

        /* 中部身体容器 */
        .el-dialog__body {
            padding: 0 !important;
            background: transparent !important;
        }
    }

    /* 当开启 CustomTitle 时用到的居中视觉级大标题 */
    .dialog-title {
        text-align: center;
        font-size: 22px;
        font-weight: 600;
        color: #1d1d1f;
        line-height: 1.4;
        padding: 30px 24px 0;
        letter-spacing: 0.5px;

        html.dark & {
            color: #f5f5f7;
        }
    }

    /* 本体滚动自洽 */
    .dialog-body {
        overflow-y: auto;
        overflow-x: hidden;
        min-height: 80px;

        /* iOS 微细滚动条 */
        &::-webkit-scrollbar {
            width: 6px;
            height: 6px;
        }

        &::-webkit-scrollbar-thumb {
            background-color: rgba(0, 0, 0, 0.15);
            border-radius: 999px;
        }

        html.dark &::-webkit-scrollbar-thumb {
            background-color: rgba(255, 255, 255, 0.2);
        }

        &::-webkit-scrollbar-track {
            background: transparent;
        }
    }

    /* 现代间隔打底底部操作群 */
    .dialog-footer {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        padding: 16px 24px 24px;
        gap: 12px;
        border-top: none;
        background: transparent;

        /* 统一的苹果操作按钮底座重写 */
        .apple-btn {
            margin: 0 !important;
            /* 切掉原来恶心的左偏置, 用父级Gap控制 */
            height: 44px !important;
            /* 🔴 配合弹窗略微增高操作区域高度 */
            border-radius: 14px !important;
            /* 🔴 按比例略微放大胶囊按钮圆角 */
            padding: 0 24px !important;
            font-size: 15px !important;
            font-weight: 600 !important;
            border: none !important;
            transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1) !important;

            /* 物理下陷反馈 */
            &:active {
                transform: scale(0.95) !important;
            }

            /* [灰色按钮] - 用于取消或者二级次要操作 */
            &.cancel-btn,
            &.apple-btn-info {
                background-color: #f2f2f7 !important;
                color: #007AFF !important;

                &:hover {
                    background-color: #e5e5ea !important;
                }

                html.dark & {
                    background-color: rgba(255, 255, 255, 0.12) !important;
                    color: #0a84ff !important;

                    &:hover {
                        background-color: rgba(255, 255, 255, 0.18) !important;
                    }
                }
            }

            /* [蓝色按钮] - 主推荐动作 */
            &.apple-btn-primary {
                background-color: #007AFF !important;
                color: #ffffff !important;
                box-shadow: 0 4px 12px rgba(0, 122, 255, 0.25) !important;

                &:hover {
                    background-color: #0066cc !important;
                    box-shadow: 0 6px 16px rgba(0, 122, 255, 0.35) !important;
                    transform: translateY(-1px);
                }

                html.dark & {
                    background-color: #0a84ff !important;
                }
            }

            /* [危险红按钮] - 用于退出、彻底删除等危险操作验证 */
            &.apple-btn-danger {
                background-color: #FF3B30 !important;
                color: #ffffff !important;
                box-shadow: 0 4px 12px rgba(255, 59, 48, 0.25) !important;

                &:hover {
                    background-color: #E6352B !important;
                    box-shadow: 0 6px 16px rgba(255, 59, 48, 0.35) !important;
                    transform: translateY(-1px);
                }

                html.dark & {
                    background-color: #FF453A !important;
                }
            }
        }
    }
}
</style>