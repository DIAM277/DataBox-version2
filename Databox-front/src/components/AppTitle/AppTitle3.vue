<template>
    <div class="spotlight18" data-cont="DataBox">DataBox</div>
</template>

<script setup>

</script>

<style lang="scss" scoped>
.spotlight18 {
    color: #eaeaea;
    font-size: 40px;
    font-weight: 900;
    text-transform: uppercase;
    position: relative;
}

.spotlight18:before {
    width: inherit;
    height: inherit;
    content: attr(data-cont);
    clip-path: ellipse(32px 32px at 0 50%);
    color: transparent;
    background-image: linear-gradient(90deg, #4158D0 0%, #C850C0 30%, #FFCC70 66%, #56e28d 100%);
    -webkit-background-clip: text;
    position: absolute;
    top: 0;
    left: 0;
    animation: spotlight18 8s linear infinite;
}

@keyframes spotlight18 {
    0% {
        clip-path: ellipse(32px 32px at 0 50%);
    }

    50% {
        clip-path: ellipse(32px 32px at 100% 50%);
    }

    100% {
        clip-path: ellipse(32px 32px at 0 50%);
    }
}
</style>
