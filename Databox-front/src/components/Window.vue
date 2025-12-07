<template>
  <div class="window" v-if="show">
    <div class="window-mask" @click="close" v-if="show"></div>
    <div class="window-content"
      :style="{ top: '20px', left: windowContentLeft + 'px', width: windowContentWidth + 'px' }">
      <div class="title">
        {{ title }}
        <div class="close" @click="close">
          <span class="iconfont icon-close2"></span>
        </div>
      </div>
      <div class="content-body" :style="{ 'align-items': align }">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed, onMounted, onUnmounted } from "vue"
const { proxy } = getCurrentInstance();

const props = defineProps({
  show: {
    type: Boolean,
  },
  width: {
    type: Number,
    default: 1000
  },
  title: {
    type: String,
  },
  align: {
    type: String,
    default: 'top'
  }
})

const windowWidth = ref(window.innerWidth)
const windowContentWidth = computed(() => {
  return props.width > windowWidth.value ? windowWidth.value : props.width
})

const windowContentLeft = computed(() => {
  let left = windowWidth.value - props.width
  return left < 0 ? 0 : left / 2
})

const emit = defineEmits(['close'])
const close = () => {
  emit('close')
}

const resizeWindow = () => {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', resizeWindow)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeWindow)
})
</script>

<style lang="scss" scoped>
.window {
  .window-mask {
    top: 0px;
    left: 0px;
    width: 100%;
    height: calc(100vh);
    z-index: 200;
    opacity: 0.7; // 增加不透明度，使背景更暗
    background: #000;
    position: fixed;
    //backdrop-filter: blur(1px); // 添加模糊效果[ps.掉帧 慎用]
  }

  .window-content {
    top: 50px; // 从顶部留出空间
    z-index: 201;
    position: absolute;
    background: #fff;
    border-radius: 8px; // 圆角
    overflow: hidden; // 防止内容溢出
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15); // 阴影效果
    height: 95%;
    //max-height: calc(100vh - 100px); // 限制最大高度
    display: flex;
    flex-direction: column;

    .title {
      text-align: center;
      line-height: 50px;
      border-bottom: 1px solid #eee;
      font-weight: bold;
      font-size: 16px;
      color: #333;
      background-color: #f8f9fa;
      padding: 0 15px;
      position: relative;
      display: flex;
      justify-content: center;
      align-items: center;

      .close {
        position: absolute;
        right: 10px;
        top: 50%;
        transform: translateY(-50%);
        width: 30px;
        height: 30px;
        border-radius: 50%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        transition: all 0.3s ease;
        cursor: pointer;
        z-index: 10;

        &:hover {
          background: rgba(220, 53, 69, 0.8);
          transform: translateY(-50%) rotate(90deg);
        }

        .iconfont {
          font-size: 16px;
          color: #fff;
        }
      }

      &::after {
        // 添加底部装饰线
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 50px;
        height: 3px;
        background-color: #409EFF;
        border-radius: 3px;
      }
    }

    .content-body {
      height: calc(100vh - 100px); // 调整高度
      display: flex;
      overflow: auto;
      padding: 15px; // 添加内边距
      background-color: #fff; // 内容区背景色

      &::-webkit-scrollbar {
        width: 6px;
        height: 6px;
      }

      &::-webkit-scrollbar-thumb {
        background: #c0c4cc;
        border-radius: 3px;
      }

      &::-webkit-scrollbar-track {
        background: #f5f7fa;
      }
    }
  }
}
</style>
