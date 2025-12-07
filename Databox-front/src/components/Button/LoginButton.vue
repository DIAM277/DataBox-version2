<template>
    <div class="btn-box85" :style="{ width: width + 'px' }">
        <button class="btn85" @click="handleClick" :type="nativeType">{{ text }}</button>
        <span class="btn-style85"></span>
    </div>
</template>

<script setup>

const props = defineProps({
    text: {
        type: String,
        default: 'BUTTON'
    },
    width: {
        type: [Number, String],
        default: 140
    },
    nativeType: {
        type: String,
        default: 'button'
    }
});

const emit = defineEmits(['click']);

const handleClick = (event) => {
    emit('click', event);
};
</script>

<style lang="scss" scoped>
.btn-box85 {
    height: 48px;
    position: relative;
    margin: 0 auto;
}

.btn85 {
    width: 100%;
    height: 100%;
    font-size: 16px;
    letter-spacing: 2px;
    border-radius: 7px;
    color: #ffffff;
    background-color: #1787f7;
    border: none;
    position: absolute;
    outline: none;
    cursor: pointer;
    z-index: 1;
    transition: all 0.2s cubic-bezier(0.23, 1, 0.320, 1);
}

.btn85:hover {
    background-color: #3f98f7;
    transform: translateY(-2px);
}

.btn-style85 {
    width: 100%;
    height: 100%;
    position: absolute;
    z-index: 0;
}

.btn-style85:before {
    content: '';
    width: 100%;
    height: 100%;
    border-radius: 7px;
    display: block;
    position: absolute;
    top: 4px;
    left: 0;
    background-color: #3356f3;
    z-index: -1;
}

.btn-style85:after {
    content: '';
    width: 100%;
    height: 100%;
    border-radius: 7px;
    display: block;
    position: absolute;
    top: 6px;
    left: 0;
    background-color: rgba(0, 0, 0, 0.4);
    z-index: -2;
    transition: all 0.2s cubic-bezier(0.23, 1, 0.320, 1);
}

.btn85:hover~.btn-style85:after {
    transform: translateY(2px);
}

.btn85:active {
    transform: translateY(2px);
}

.btn85:active~.btn-style85:after {
    transform: translateY(-1px);
}
</style>