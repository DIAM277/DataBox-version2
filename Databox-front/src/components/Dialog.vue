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
                    <el-button link @click="close" v-if="showCancel">取消</el-button>
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
    //margin: 30px auto 10px !important;
    border-radius: 12px;
    overflow: hidden;
    background-color: #ffffff;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

    :deep(.el-dialog__header) {
        padding: 20px 24px;
        margin-right: 0;
        background-color: #f5f7fa;
        border-bottom: 1px solid #0045e7;
    }

    :deep(.el-dialog__headerbtn) {
        top: 20px; // 根据新的padding调整
        right: 24px; // 统一关闭按钮位置

        .el-dialog__close {
            color: #909399;

            &:hover {
                color: #409EFF; // 悬停时使用主题色
            }
        }
    }

    :deep(.el-dialog__title) {
        font-size: 20px;
        font-weight: 600;
        color: #303133;
    }

    :deep(.el-dialog__body) {
        padding: 0px;
    }

    .dialog-title {
        text-align: center;
        font-size: 22px;
        font-weight: 600;
        margin: 10px 0 25px;
        color: #409EFF;
        position: relative;
        display: inline-block;
        left: 50%;
        transform: translateX(-50%);

        &::after {
            content: '';
            position: absolute;
            bottom: -10px;
            left: 0;
            width: 100%;
            height: 2px;
            background-color: #409EFF;
        }
    }

    .dialog-body {
        min-height: 80px;
        overflow: auto;
    }

    .dialog-footer {
        text-align: right;
        padding: 16px 24px;
        border-top: 2px solid #ebeef5;
        background-color: #fcfcfc;

        .el-button {
            margin-left: 10px;

            &:first-child {
                margin-left: 0;
            }
        }
    }
}
</style>
