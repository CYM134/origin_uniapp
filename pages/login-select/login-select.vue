<template>
    <view>
        <!-- login-select.wxml -->
        <navigation-bar title="实验室预约系统" :back="false" color="white" background="#3a7bd5"></navigation-bar>
        <view class="container">
            <view class="logo-container">
                <image class="logo" src="/static/images/天空实验室.png" mode="aspectFit"></image>
                <text class="title">实验室预约管理系统</text>
                <text class="subtitle">请选择您的身份</text>
            </view>

            <view class="button-group">
                <button class="identity-btn admin-btn" @tap="navigateToAdmin">
                    <view class="btn-content">
                        <image class="btn-icon" src="/static/images/icons/管理员_角色管理.png" mode="aspectFit"></image>
                        <text>我是管理员</text>
                    </view>
                </button>

                <button class="identity-btn student-btn" @tap="navigateToStudent">
                    <view class="btn-content">
                        <image class="btn-icon" src="/static/images/icons/学生.png" mode="aspectFit"></image>
                        <text>我是学生</text>
                    </view>
                </button>

                <button class="identity-btn teacher-btn" @tap="navigateToTeacher">
                    <view class="btn-content">
                        <image class="btn-icon" src="/static/images/icons/教师，领导中心.png" mode="aspectFit"></image>
                        <text>我是教师</text>
                    </view>
                </button>
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
// login-select.ts
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            // 页面数据
        };
    },
    methods: {
        // 跳转到管理员登录页面
        navigateToAdmin() {
            uni.navigateTo({
                url: '../admin-login/admin-login',
                fail: () => {
                    // 如果页面不存在，给出提示
                    uni.showToast({
                        title: '页面开发中',
                        icon: 'none',
                        duration: 2000
                    });
                }
            });
        },
        // 跳转到学生登录页面
        navigateToStudent() {
            uni.navigateTo({
                url: '../student-login/student-login',
                fail: () => {
                    // 如果页面不存在，给出提示
                    uni.showToast({
                        title: '页面开发中',
                        icon: 'none',
                        duration: 2000
                    });
                }
            });
        },
        // 跳转到教师登录页面
        navigateToTeacher() {
            uni.navigateTo({
                url: '../teacher-login/teacher-login',
                fail: () => {
                    // 如果页面不存在，给出提示
                    uni.showToast({
                        title: '页面开发中',
                        icon: 'none',
                        duration: 2000
                    });
                }
            });
        }
    },
    created: function () {}
});
</script>
<style>
@import './login-select.css';
</style>
