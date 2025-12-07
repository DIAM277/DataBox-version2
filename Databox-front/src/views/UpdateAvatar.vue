<template>
    <div>
        <Dialog :show="dialogConfig.show" :title="dialogConfig.title" :buttons="dialogConfig.buttons" width="400px"
            :showCancel="true" @close="dialogConfig.show = false" :showCustomTitle="true">
            <el-form :model="formData" ref="formDataRef" label-width="95px" @submit.prevent>
                <el-form-item>
                    <AvatarUpload v-model="formData.avatar"></AvatarUpload>
                </el-form-item>
                <el-form-item label="用户名">
                    <el-input v-model="formData.newUserName" placeholder="请输入新用户名"></el-input>
                </el-form-item>
            </el-form>
        </Dialog>
    </div>
</template>

<script setup>
import AvatarUpload from "@/components/AvatarUpload.vue";
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from 'vue-router';
const route = useRoute();
const router = useRouter();

const api = {
    updateUserAvatar: 'updateUserAvatar'
}

const formData = ref({});
const formDataRef = ref();

const show = (data) => {
    formData.value = Object.assign({}, data);
    formData.value.avatar = { userId: data.userId }
    formData.value.newUserName = data.userName; // 初始化为当前用户名
    dialogConfig.value.show = true;
}
defineExpose({ show });

const dialogConfig = ref({
    show: false,
    title: "修改资料",
    buttons: [
        {
            type: "primary",
            text: "确定",
            click: (e) => {
                submitForm();
            },
        },
    ],
});

const emit = defineEmits()
// 提交表单
const submitForm = async () => {
    // 检查用户名是否为空
    if (!formData.value.newUserName || formData.value.newUserName.trim() === '') {
        proxy.Message.warning('用户名不能为空')
        return;
    }

    // 如果没有选择新头像且用户名没有变化，则不需要提交
    if (!(formData.value.avatar instanceof File) && formData.value.newUserName === formData.value.userName) {
        proxy.Message.warning('请选择新头像或修改用户名')
        return;
    }

    let params = {
        newUserName: formData.value.newUserName
    };

    // 如果有新头像，添加到参数中
    if (formData.value.avatar instanceof File) {
        params.avatar = formData.value.avatar;
    }

    let result = await proxy.Request({
        url: api.updateUserAvatar,
        params: params
    })

    if (!result) {
        return;
    }

    proxy.Message.success("修改成功");
    dialogConfig.value.show = false;

    // 更新Cookie中的用户信息
    const userInfoCookie = proxy.VueCookies.get('userInfo');
    if (formData.value.newUserName !== formData.value.userName) {
        userInfoCookie.userName = formData.value.newUserName;
    }
    if (formData.value.avatar instanceof File) {
        delete userInfoCookie.avatar;
    }
    proxy.VueCookies.set('userInfo', userInfoCookie, 0);

    // 通知父组件更新头像和用户名
    emit('updateAvatar', {
        avatar: result.data,
        userName: formData.value.newUserName
    });
};
</script>

<style lang="scss" scoped></style>