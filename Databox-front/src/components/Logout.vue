<template>
    <div>
        <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="400px"
            :showCancel="true" @close="dialogConfig.show = false" :showCustomTitle="true">
            <div class="logout-content">
                <div class="icon-container">
                    <span class="iconfont icon-close3"></span>
                </div>
                <div class="message">确定要退出登录吗？</div>
            </div>
        </Dialog>
    </div>
</template>

<script setup>
import request from '@/utils/Request';
import Dialog from './Dialog.vue';
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

<style lang="scss" scoped>
.logout-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;

    .icon-container {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        background-color: #fff2f0;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 20px;

        .iconfont {
            font-size: 30px;
            color: #ff4d4f;
        }
    }

    .message {
        font-size: 16px;
        color: #333;
        margin-bottom: 10px;
    }
}
</style>