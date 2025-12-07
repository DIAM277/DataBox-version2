<template>
    <div class="text94" @click="changeGradient">{{ title }}</div>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
    title: {
        type: String,
        default: 'DataBox'
    }
});

// 定义渐变样式对象
const gradientStyle = ref({
    backgroundImage: 'linear-gradient(97deg, #EB7500, #F2416B 42%, #BB64FF 74%, #0096FF)'
});

// 生成随机颜色函数
const getRandomColor = () => {
    return '#' + Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0');
};

// 点击时更改渐变色
const changeGradient = () => {
    const color1 = getRandomColor();
    const color2 = getRandomColor();
    const color3 = getRandomColor();
    const color4 = getRandomColor();

    gradientStyle.value.backgroundImage = `linear-gradient(97deg, ${color1}, ${color2} 42%, ${color3} 74%, ${color4})`;
};
</script>

<style lang="scss" scoped>
.text94 {
    width: max-content;
    font-size: 3em;
    font-weight: 600;
    color: transparent;
    background-clip: text;
    -webkit-background-clip: text;
    background-image: v-bind('gradientStyle.backgroundImage');
    cursor: pointer;
    transition: background-image 0.5s ease;
}
</style>