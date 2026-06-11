<template>
    <view class="page-wrapper">
        <!-- pages/teacher-login/teacher-login.wxml -->
        <navigation-bar title="教师登录" :back="true" color="white" background="#10B981"></navigation-bar>
        <view class="container">
            <view class="logo-container">
                <image class="logo" src="/static/images/天空实验室.png" mode="aspectFit"></image>
                <text class="title">实验室预约管理系统</text>
                <text class="subtitle">教师登录</text>
            </view>

            <view class="login-form">
                <view class="input-group">
                    <view class="input-label">工号</view>
                    <input class="input" type="text" placeholder="请输入工号" @input="onUsernameInput" :value="username" />
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
// pages/teacher-login/teacher-login.ts

const username = ref<string>('');
const password = ref<string>('');
const rememberPassword = ref<boolean>(false);
const showPassword = ref<boolean>(false);

onLoad(() => {
    const savedUsername = uni.getStorageSync('teacherUsername');
    const savedPassword = uni.getStorageSync('teacherPassword');
    const savedRememberPassword = uni.getStorageSync('teacherRememberPassword');
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
            title: '请输入工号',
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

    // 简单的工号格式验证（假设工号为字母数字组合）
    if (!/^[a-zA-Z0-9]+$/.test(usernameVal)) {
        uni.showToast({
            title: '工号格式不正确',
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
            role: 'teacher'
        });

        if (rememberVal) {
            uni.setStorageSync('teacherUsername', usernameVal);
            uni.setStorageSync('teacherPassword', passwordVal);
            uni.setStorageSync('teacherRememberPassword', true);
        } else {
            uni.removeStorageSync('teacherUsername');
            uni.removeStorageSync('teacherPassword');
            uni.removeStorageSync('teacherRememberPassword');
        }

        saveAuthSession(result.accessToken, result.user);
        uni.hideLoading();
        uni.reLaunch({
            url: '/pages/teacher-work/teacher-work',
            success: () => {
                setTimeout(() => {
                    uni.showToast({
                        title: '登录成功',
                        icon: 'success'
                    });
                }, 500);
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
        url: '../teacher-register/teacher-register'
    });
};
</script>
<style lang="less">
@import './teacher-login.less';
</style>
