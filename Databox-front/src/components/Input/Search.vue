<template>
    <label class="label77">
        <input class="inp77" size="16" type="search" placeholder="请输入文件名搜索" required v-model="searchText"
            @keyup.enter="handleSearch" @input="handleInput" @compositionstart="onCompositionStart"
            @compositionend="onCompositionEnd">
        <span class="search-btn77" @click="handleSearch"></span>
    </label>
</template>

<script setup>
import { ref, watch } from 'vue';

const searchText = ref('');
const isComposing = ref(false); // 标记是否正在输入中文
const emit = defineEmits(['search', 'update:modelValue']);

// 处理搜索按钮点击或回车事件
const handleSearch = () => {
    emit('search', searchText.value);
};

// 中文输入法开始输入
const onCompositionStart = () => {
    isComposing.value = true;
};

// 中文输入法结束输入
const onCompositionEnd = () => {
    isComposing.value = false;
    // 中文输入完成后，如果输入框为空，触发搜索
    if (searchText.value === '') {
        handleSearch();
    }
};

// 处理输入事件，实现双向绑定
const handleInput = () => {
    emit('update:modelValue', searchText.value);

    // 只有在非中文输入状态下，且输入框为空时，才触发搜索
    if (!isComposing.value && searchText.value === '') {
        handleSearch();
    }
};

// 接收外部传入的值
const props = defineProps({
    modelValue: {
        type: String,
        default: ''
    }
});

// 监听外部传入的值变化
watch(() => props.modelValue, (newVal) => {
    searchText.value = newVal;
}, { immediate: true });
</script>

<style lang="scss" scoped>
.label77 {
    width: 400px;
    height: 42px;
    position: relative;
    overflow: hidden;
}

.inp77 {
    width: 400px;
    height: 42px;
    line-height: 42px;
    padding: 0 16px;
    padding-right: 42px;
    border: 1px solid rgba(0, 0, 0, 0.2);
    background-color: #ffffff;
    box-sizing: border-box;
    transition: all 0.3s;
    font-size: 14px;
    border-radius: 21px;
    color: #333;
    position: absolute;
    list-style: none;
    outline-style: none;
}

.search-btn77 {
    position: absolute;
    top: 23px;
    right: 35px;
}

.search-btn77:before {
    content: '';
    width: 16px;
    height: 16px;
    border: 2px solid rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    display: block;
    position: absolute;
    top: -14px;
    transition: border 0.32s linear;
    cursor: pointer;
}

.search-btn77:after {
    content: '';
    width: 2px;
    height: 8px;
    background-color: rgba(0, 0, 0, 0.6);
    display: block;
    position: absolute;
    top: 3px;
    right: -19px;
    transform: rotate(-45deg);
    transition: background-color 0.32s linear;
}

.label77:hover.inp77,
.inp77:focus,
.inp77:valid {
    color: #000000;
    border: 1px solid rgba(29, 52, 202, 0.4);
}

.inp77:valid+.search-btn77:before {
    border: 2px solid rgba(29, 52, 202, 1);
}

.inp77:valid+.search-btn77:after {
    background-color: rgba(29, 52, 202, 1);
}
</style>
