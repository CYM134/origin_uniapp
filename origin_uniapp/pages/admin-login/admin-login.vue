<template>
    <view class="page-wrapper">
        <!-- admin-login.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="管理员登录" color="white" background="#F5A623" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <navigation-bar title="管理员登录" :back="true" color="white" background="#F5A623"></navigation-bar>
        <!-- #endif -->
        <view class="container">
            <view class="logo-container">
                <image class="logo" src="/static/images/天空实验室.png" mode="aspectFit"></image>
                <text class="title">实验室预约管理系统</text>
                <text class="subtitle">管理员登录</text>
            </view>

            <view class="login-form">
                <view class="input-group">
                    <view class="input-label">账号</view>
                    <input class="input" type="text" placeholder="请输入管理员账号" @input="onUsernameInput" :value="username" />
                </view>

                <view class="input-group">
                    <view class="input-label">密码</view>
                    <!-- 密码模式 input -->
                    <input v-if="!showPassword" class="input" password placeholder="请输入密码" @input="onPasswordInput" :value="password" />

                    <!-- 明文模式 input -->
                    <input v-else class="input" placeholder="请输入密码" @input="onPasswordInput" :value="password" />

                    <view class="password-icon" @tap="togglePasswordVisibility" hover-class="none">
                        <image :src="showPassword ? '/static/images/icons/eye-open.png' : '/static/images/icons/eye-close.png'" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="remember-password">
                    <checkbox :checked="rememberPassword" @tap="toggleRememberPassword"></checkbox>
                    <text>记住密码</text>
                </view>

                <button class="login-btn" @tap="login">登录</button>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
import { loginByPassword } from '@/api/auth';
import { saveAuthSession } from '@/api/storage';

const username = ref<string>('');
const password = ref<string>('');
const rememberPassword = ref<boolean>(false);
const showPassword = ref<boolean>(false);

onLoad(() => {
    const savedUsername = uni.getStorageSync('adminUsername');
    const savedPassword = uni.getStorageSync('adminPassword');
    const savedRememberPassword = uni.getStorageSync('adminRememberPassword');
    
    if (savedRememberPassword && savedUsername && savedPassword) {
        username.value = savedUsername;
        password.value = savedPassword;
        rememberPassword.value = true;
    }
});

const onUsernameInput = (e: any) => {
    username.value = e.detail.value;
};

const onPasswordInput = (e: any) => {
    password.value = e.detail.value;
};

const toggleRememberPassword = () => {
    rememberPassword.value = !rememberPassword.value;
};

const togglePasswordVisibility = () => {
    showPassword.value = !showPassword.value;
};

const login = async () => {
    if (!username.value.trim()) {
        uni.showToast({
            title: '请输入账号',
            icon: 'none'
        });
        return;
    }
    
    if (!password.value.trim()) {
        uni.showToast({
            title: '请输入密码',
            icon: 'none'
        });
        return;
    }
    
    uni.showLoading({
        title: '登录中...',
        mask: true
    });

    try {
        const result: any = await loginByPassword({
            accountNo: username.value.trim(),
            password: password.value,
            role: 'admin'
        });

        if (rememberPassword.value) {
            uni.setStorageSync('adminUsername', username.value);
            uni.setStorageSync('adminPassword', password.value);
            uni.setStorageSync('adminRememberPassword', true);
        } else {
            uni.removeStorageSync('adminUsername');
            uni.removeStorageSync('adminPassword');
            uni.removeStorageSync('adminRememberPassword');
        }

        saveAuthSession(result.accessToken, result.user);
        uni.hideLoading();

        uni.reLaunch({
            url: '/pages/admin-work/admin-work',
            success: () => {
                uni.showToast({
                    title: '登录成功',
                    icon: 'success'
                });
            }
        });
    } catch (error: any) {
        uni.hideLoading();
        uni.showToast({
            title: error?.data?.message || '登录失败',
            icon: 'none'
        });
    }
};
</script>
<style lang="less">
@import './admin-login.less';
</style>
