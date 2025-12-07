<template>
    <span class="op">
        <template v-if="showOp && fileData.fileId && fileData.status == 2 && !fileData.showEdit">
            <span class="iconfont icon-share1" @click="handleShare">分享</span>
            <span class="iconfont icon-download" @click.stop="handleDownload">下载</span>
            <span class="iconfont icon-del" @click.stop="handleDelete">删除</span>
            <span class="iconfont icon-edit" @click.stop="handleRename">重命名</span>
            <span class="iconfont icon-move" @click.stop="handleMove">移动</span>
        </template>
    </span>
</template>

<script setup>

const props = defineProps({
    fileData: {
        type: Object,
        required: true
    },
    index: {
        type: Number,
        required: true
    },
    showOp: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits([
    'share',
    'download',
    'delete',
    'rename',
    'move'
]);

const handleShare = () => {
    emit('share', props.fileData);
};

const handleDownload = (event) => {
    emit('download', props.fileData);
};

const handleDelete = (event) => {
    emit('delete', props.index);
};

const handleRename = (event) => {
    emit('rename', props.index);
};

const handleMove = (event) => {
    emit('move', props.fileData);
};
</script>

<style lang="scss" scoped>
.op {
    position: absolute;
    right: 5px;

    .iconfont {
        margin-left: 8px;
        font-size: 12px;
        color: #409EFF;
        cursor: pointer;

        &:hover {
            color: #66b1ff;
        }

        &.icon-del:hover {
            color: #F56C6C;
        }
    }
}
</style>