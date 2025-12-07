<template>
    <div class="no-data">
        <div class="empty-image">
            <img :src="imgSrc" alt="无数据" />
        </div>
        <div class="msg">{{ msg }}</div>
    </div>
</template>

<script setup>
const props = defineProps({
    msg: {
        type: String,
    },
    imgSrc: {
        type: String,
        default: () => {
            return new URL('@/assets/icon-image/no-data.png', import.meta.url).href;
        }
    },
})
</script>

<style lang="scss" scoped>
.no-data {
    text-align: center;
    padding: 10px 0px;

    .empty-image {
        img {
            width: 300px;
            height: auto;
        }
    }

    .msg {
        margin-top: 10px;
        color: #6062668a;
        font-size: 15px;
        font-weight: 500;
    }
}
</style>
