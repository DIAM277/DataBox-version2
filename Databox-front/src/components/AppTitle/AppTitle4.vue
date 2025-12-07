<template>
    <div class="loading17">
        <span class="load-span17">D</span>
        <span class="load-span17">A</span>
        <span class="load-span17">T</span>
        <span class="load-span17">A</span>
        <span class="load-span17">B</span>
        <span class="load-span17">O</span>
        <span class="load-span17">X</span>
    </div>
</template>

<script setup>

</script>

<style lang="scss" scoped>
.loading17 {
    height: 48px;
    font-size: 32px;
    font-weight: 700;
    letter-spacing: 24px;
    color: black;
    display: flex;
    justify-content: center;
    align-items: center;
}

.loading17 .load-span17 {
    animation: text-load17 2.8s linear infinite;
}

.loading17 .load-span17:nth-of-type(1) {
    color: #f41e1e;
    animation-delay: 0.4s;
}

.loading17 .load-span17:nth-of-type(2) {
    color: #f87816;
    animation-delay: 0.8s;
}

.loading17 .load-span17:nth-of-type(3) {
    color: #fcbb02;
    animation-delay: 0.12s;
}

.loading17 .load-span17:nth-of-type(4) {
    color: #2ab148;
    animation-delay: 1.6s;
}

.loading17 .load-span17:nth-of-type(5) {
    color: #0cf6a4;
    animation-delay: 2.0s;
}

.loading17 .load-span17:nth-of-type(6) {
    color: #1954f6;
    animation-delay: 2.4s;
}

.loading17 .load-span17:nth-of-type(7) {
    color: #4c15f3;
    animation-delay: 2.4s;
}

@keyframes text-load17 {
    0% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-10px);
    }

    100% {
        transform: translateY(0);
    }
}
</style>