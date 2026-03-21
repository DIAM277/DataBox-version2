<template>
    <div>
        <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="400px"
            :showCancel="true" @close="dialogConfig.show = false" :showCustomTitle="true">
            <div class="flex flex-col items-center py-6">
                <!-- 红底图标容器，适配了暗色模式的微弱红底 -->
                <div
                    class="w-16 h-16 rounded-full bg-[#fff2f0] dark:bg-red-900/30 flex items-center justify-center mb-5 shadow-sm">
                    <span class="iconfont icon-close text-[32px] text-[#ff4d4f]"></span>
                </div>
                <!-- 退出提示文字 -->
                <div class="text-[17px] font-medium text-[#1d1d1f] dark:text-[#f5f5f7] tracking-wide">
                    确定要退出登录吗？
                </div>
            </div>
        </Dialog>
    </div>
</template>

<script setup>
import request from '@/utils/Request';
import Dialog from '../Common/Dialog.vue';
import { ref, reactive, getCurrentInstance } from "vue";
const { proxy } = getCurrentInstance();
import { useRouter } from 'vue-router';
const router = useRouter();

const api = {
    logout: '/logout'
}

const dialogConfig = ref({
    show: false,
    title: "退出登录",
    buttons: [
        {
            text: "确定",
            type: "danger",
            click: () => {
                logout();
            }
        }
    ]
});

// 退出登录
const logout = async () => {
    let result = await proxy.Request({
        url: api.logout
    })
    if (!result) {
        return;
    }
    proxy.VueCookies.remove('userInfo');
    router.push('/login');
    proxy.Message.success('退出成功');
    dialogConfig.value.show = false;
}

// 暴露 show 方法给父组件调用
const show = () => {
    dialogConfig.value.show = true;
}

defineExpose({ show });
</script>