<template>
    <a href="javascript:;" class="btn44">{{ userName }}</a>
</template>

<script setup>
const props = defineProps({
    userName: {
        type: String,
        default: '用户名'
    }
});
</script>

<style lang="scss" scoped>
.btn44 {
    font-size: 16px;
    font-weight: 600;
    height: 32px;
    line-height: 32px;
    color: #2980b9;
    text-decoration: none;
    cursor: pointer;
    position: relative;
    padding: 4px 10px;
    border-radius: 4px;
    transition: all 0.3s ease;
}

.btn44:hover {
    background-color: rgba(41, 128, 185, 0.1);
    color: #1565C0;
}

.btn44:after {
    content: '';
    width: 0;
    height: 2px;
    background-image: linear-gradient(97deg, #3356f3, #1787f7);
    position: absolute;
    right: 0;
    bottom: 0px;
    transition: width 0.4s ease;
}

.btn44:hover:after {
    width: 100%;
    left: 0;
}
</style>