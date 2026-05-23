<template>
    <view>
        <!-- pages/student-login/student-login.wxml -->
        <navigation-bar title="学生登录" :back="true" color="white" background=" #4f7eff"></navigation-bar>
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
                        <image :src="showPassword ? '/images/icons/eye-open.png' : '/images/icons/eye-close.png'" mode="aspectFit"></image>
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

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
// pages/student-login/student-login.ts
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            username: '',
            password: '',
            rememberPassword: false,
            showPassword: false
        };
    },
    onLoad() {
        const savedUsername = uni.getStorageSync('studentUsername');
        const savedPassword = uni.getStorageSync('studentPassword');
        const rememberPassword = uni.getStorageSync('studentRememberPassword');
        if (rememberPassword && savedUsername && savedPassword) {
            this.setData({
                username: savedUsername,
                password: savedPassword,
                rememberPassword: true
            });
        }
    },
    methods: {
        onUsernameInput(e: any) {
            this.setData({
                username: e.detail.value
            });
        },

        onPasswordInput(e: any) {
            this.setData({
                password: e.detail.value
            });
        },

        toggleRememberPassword() {
            this.setData({
                rememberPassword: !this.rememberPassword
            });
        },

        togglePasswordVisibility() {
            this.setData({
                showPassword: !this.showPassword
            });
        },

        forgotPassword() {
            uni.showModal({
                title: '忘记密码',
                content: '请联系管理员重置密码，或通过注册新账号的方式找回。',
                showCancel: false,
                confirmText: '知道了'
            });
        },

        login() {
            const { username, password, rememberPassword } = this;
            if (!username.trim()) {
                uni.showToast({
                    title: '请输入学号',
                    icon: 'none'
                });
                return;
            }
            if (!password.trim()) {
                uni.showToast({
                    title: '请输入密码',
                    icon: 'none'
                });
                return;
            }

            // 简单的学号格式验证（假设学号为数字）
            if (!/^\d+$/.test(username)) {
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

            // 模拟登录验证
            setTimeout(() => {
                uni.hideLoading();

                // 检查已注册的学生账号
                const registeredStudents = uni.getStorageSync('registeredStudents') || [];
                const student = registeredStudents.find((s: any) => s.studentId === username && s.password === password);
                if (student) {
                    if (rememberPassword) {
                        uni.setStorageSync('studentUsername', username);
                        uni.setStorageSync('studentPassword', password);
                        uni.setStorageSync('studentRememberPassword', true);
                    } else {
                        uni.removeStorageSync('studentUsername');
                        uni.removeStorageSync('studentPassword');
                        uni.removeStorageSync('studentRememberPassword');
                    }

                    // 保存学生登录状态
                    uni.setStorageSync('studentInfo', {
                        studentId: username,
                        name: student.name,
                        gender: student.gender,
                        college: student.college,
                        major: student.major,
                        phone: student.phone,
                        password: student.password,
                        registerTime: student.registerTime,
                        loginTime: new Date().getTime()
                    });

                    // 跳转到学生主页（这里需要根据实际情况修改）
                    setTimeout(() => {
                        uni.navigateTo({
                            url: '../student-work/student-work',
                            success: () => {
                                uni.showToast({
                                    title: '登录成功',
                                    icon: 'success'
                                });
                            }
                        });
                    }, 1500);
                } else {
                    uni.showToast({
                        title: '学号或密码错误',
                        icon: 'none'
                    });
                }
            }, 1500);
        },

        goToRegister() {
            uni.navigateTo({
                url: '../student-register/student-register'
            });
        }
    }
});
</script>
<style lang="less">
@import './student-login.less';
</style>
