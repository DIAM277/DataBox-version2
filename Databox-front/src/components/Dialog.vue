<template>
    <div>
        <el-dialog :show-close="showClose" :draggable="true" :model-value="show" style="border-radius: 12px"
            :close-on-click-model="false" :title="showCustomTitle ? '' : title" class="cust-dialog" :top="top"
            :width="width" @close="close">
            <div class="dialog-title" v-if="showCustomTitle">{{ title }}</div>
            <div class="dialog-body" :style="{ 'max-height': maxHeight + 'px', padding: padding + 'px' }">
                <slot></slot>
            </div>
            <template v-if="(buttons && buttons.length > 0) || showCancel">
                <div class="dialog-footer">
                    <!-- <el-button link @click="close" v-if="showCancel">取消</el-button> -->
                    <el-button v-for="(btn, index) in buttons" :key="index" :type="btn.type || 'primary'"
                        @click="btn.click">
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

const maxHeight = window.innerHeight - props.top - 100;

const emit = defineEmits()
const close = () => {
    emit('close')
}
</script>

<style lang="scss" scoped>
.cust-dialog {

    /* 强行覆盖 Element Plus 默认样式，注入苹果毛玻璃基因 */
    :deep(.el-dialog) {
        @apply bg-white/80 dark:bg-[#1c1c1e]/80 backdrop-blur-3xl rounded-[20px] shadow-2xl overflow-hidden border border-white/40 dark:border-white/10 !important;

        /* 头部背景纯透明且无下划线 */
        .el-dialog__header {
            @apply bg-transparent p-6 mr-0 border-none !important;
        }

        /* 细化右上角的关闭按钮 */
        .el-dialog__headerbtn {
            @apply top-5 right-5 w-8 h-8 flex items-center justify-center rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition-colors focus:outline-none !important;

            .el-dialog__close {
                @apply text-[#86868b] dark:text-[#a1a1a6] text-[16px] transition-colors focus:outline-none;
            }
        }

        /* 默认标题属性保持极简 */
        .el-dialog__title {
            @apply text-[17px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-wide;
        }

        /* 原有自带内补清零，全部交由自定义容器接管 */
        .el-dialog__body {
            @apply p-0 bg-transparent;
        }
    }

    /* ------------------------------
       自定义内容区域
    ------------------------------ */

    /* 替换之前冗余带下划线的旧标题，改为居中平滑大字 */
    .dialog-title {
        @apply text-center text-[20px] font-semibold text-[#1d1d1f] dark:text-[#f5f5f7] tracking-wider mt-2 mb-6 block w-full;
    }

    .dialog-body {
        @apply overflow-auto min-h-[80px];

        /* 苹果原生滚动条微调 */
        &::-webkit-scrollbar {
            width: 6px;
            height: 6px;
        }

        &::-webkit-scrollbar-thumb {
            @apply bg-gray-300 dark:bg-gray-600 rounded-full;
        }

        &::-webkit-scrollbar-track {
            @apply bg-transparent;
        }
    }

    /* 底部操作区变透明，去除顶部生硬的横线 */
    .dialog-footer {
        @apply flex justify-end items-center px-6 py-5 bg-transparent border-none;

        .el-button {
            @apply ml-3 rounded-xl font-medium tracking-wide transition-all;

            &:first-child {
                @apply ml-0;
            }
        }
    }
}
</style>
