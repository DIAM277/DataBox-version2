<template>
    <div class="random-quote" @click="changeQuote" @contextmenu.prevent="copyQuote">「{{ currentQuote }}」</div>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue';
import quotes from '@/assets/data/quotes.js';

const { proxy } = getCurrentInstance();
const currentQuote = ref('');

// 随机选择一条文案
const getRandomQuote = () => {
    const randomIndex = Math.floor(Math.random() * quotes.length);
    return quotes[randomIndex];
};

// 切换文案
const changeQuote = () => {
    let newQuote = getRandomQuote();
    // 确保不会连续出现相同的文案
    while (newQuote === currentQuote.value && quotes.length > 1) {
        newQuote = getRandomQuote();
    }
    currentQuote.value = newQuote;
};

// 复制文案
const copyQuote = () => {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(currentQuote.value)
            .then(() => {
                proxy.$message({
                    message: '已复制到剪贴板',
                    type: 'success'
                });
            })
            .catch(err => {
                proxy.$message.error('复制失败，请手动复制');
                console.error('复制失败:', err);
            });
    } else {
        // 兼容不支持clipboard API的浏览器
        const textarea = document.createElement('textarea');
        textarea.value = currentQuote.value;
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand('copy');
            proxy.$message({
                message: '已复制到剪贴板',
                type: 'success'
            });
        } catch (err) {
            proxy.$message.error('复制失败，请手动复制');
            console.error('复制失败:', err);
        }
        document.body.removeChild(textarea);
    }
};

// 组件挂载时初始化文案
onMounted(() => {
    changeQuote();
});
</script>

<style lang="scss" scoped>
.random-quote {
    position: absolute;
    bottom: 10px;
    left: 50%;
    transform: translateX(-50%);
    color: #fff;
    font-size: 14px;
    text-align: center;
    padding: 8px 15px;
    border-radius: 5px;
    max-width: 80%;
    cursor: pointer;
    user-select: none;
    transition: all 0.3s ease;
    text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.8);

    &:hover {
        background-color: transparent;
        transform: translateX(-50%) scale(1.05);
    }
}
</style>