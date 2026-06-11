<template>
    <view class="page-wrapper">
        <!-- pages/student-login/student-login.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="学生登录" color="white" background="#3B82F6" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <navigation-bar title="学生登录" :back="true" color="white" background="#3B82F6"></navigation-bar>
        <!-- #endif -->
        <view class="container">
            <view class="logo-container">
                <image class="logo" src="/static/images/天空实验室.png" mode="aspectFit"></image>
                <text class="title">实验室预约管理系统</text>
                <text class="subtitle">学生登录</text>
            </view>

            <view class="login-form">
                <view class="input-group">
                    <view class="input-label">学号</view>
                    <input class="input" type="text" placeholder="请输入学号" @input="onUsernameInput" :value="username" />
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

                <view class="forgot-password">
                    <text @tap="forgotPassword">忘记密码？</text>
                </view>

                <button class="login-btn" @tap="login">登录</button>

                <view class="register-section">
                    <text class="register-text">还没有账号？</text>
                    <text class="register-link" @tap="goToRegister">立即注册</text>
                </view>
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
// pages/student-login/student-login.ts

const username = ref<string>('');
const password = ref<string>('');
const rememberPassword = ref<boolean>(false);
const showPassword = ref<boolean>(false);

onLoad(() => {
    const savedUsername = uni.getStorageSync('studentUsername');
    const savedPassword = uni.getStorageSync('studentPassword');
    const savedRemember = uni.getStorageSync('studentRememberPassword');
    if (savedRemember && savedUsername && savedPassword) {
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

const forgotPassword = () => {
    uni.showModal({
        title: '忘记密码',
        content: '请联系管理员重置密码，或通过注册新账号的方式找回。',
        showCancel: false,
        confirmText: '知道了'
    });
};

const login = async () => {
    const usernameVal = username.value;
    const passwordVal = password.value;
    const rememberVal = rememberPassword.value;
    if (!usernameVal.trim()) {
        uni.showToast({
            title: '请输入学号',
            icon: 'none'
        });
        return;
    }
    if (!passwordVal.trim()) {
        uni.showToast({
            title: '请输入密码',
            icon: 'none'
        });
        return;
    }

    if (!/^[a-zA-Z0-9]+$/.test(usernameVal)) {
        uni.showToast({
            title: '学号格式不正确',
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
            accountNo: usernameVal.trim(),
            password: passwordVal,
            role: 'student'
        });

        if (rememberVal) {
            uni.setStorageSync('studentUsername', usernameVal);
            uni.setStorageSync('studentPassword', passwordVal);
            uni.setStorageSync('studentRememberPassword', true);
        } else {
            uni.removeStorageSync('studentUsername');
            uni.removeStorageSync('studentPassword');
            uni.removeStorageSync('studentRememberPassword');
        }

        saveAuthSession(result.accessToken, result.user);
        uni.hideLoading();
        uni.reLaunch({
            url: '/pages/student-work/student-work',
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

const goToRegister = () => {
    uni.navigateTo({
        url: '../student-register/student-register'
    });
};
</script>
<style lang="less">
@import './student-login.less';
</style>
