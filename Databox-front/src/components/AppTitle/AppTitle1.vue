<template>
    <div class="text87">
        <span class="text-def87">&nbsp;DataBox&nbsp;</span>
        <span class="text-hover87">&nbsp;DataBox&nbsp;</span>
    </div>
</template>

<script setup>

</script>

<style lang="scss" scoped>
.text87 {
    width: 166px;
    height: 40px;
    position: relative;
    display: flex;
    cursor: default;
}

.text-def87,
.text-hover87 {
    font-size: 2em;
    font-family: Arial, Helvetica, sans-serif;
    font-weight: 600;
    color: #ffffff;
    letter-spacing: 3px;
    position: absolute;
}

.text-hover87 {
    width: 0;
    border-right: 4px solid #ffffff;
    overflow: hidden;
    transition: all 0.3s linear;
}

.text87:hover .text-hover87 {
    width: 100%;
    color: #ffffff;
    box-shadow: 0 0 60px #ffffff;
    text-shadow: 0 0 24px #ffffff;
    animation: text-eff-87 1.8s linear infinite;
    animation-delay: 0.3s;
}

@keyframes text-eff-87 {
    0% {
        box-shadow: 0 0 60px #ffffff;
        text-shadow: 0 0 24px #ffffff;
    }

    50% {
        box-shadow: 0 0 30px #ffffff;
        text-shadow: 0 0 12px #ffffff;
    }

    100% {
        box-shadow: 0 0 60px #ffffff;
        text-shadow: 0 0 24px #ffffff;
    }
}
</style>
