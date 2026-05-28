<template>
    <view class="page-wrapper">
        <!-- login-select.wxml -->
        <navigation-bar title="实验室预约系统" :back="false" color="#ffffff" background="#4F46E5"></navigation-bar>
        <view class="container">
            <view class="logo-container">
                <image class="logo" src="/static/images/天空实验室.png" mode="aspectFit"></image>
                <text class="title">实验室预约管理系统</text>
                <text class="subtitle">请选择您的身份</text>
            </view>

            <view class="button-group">
                <view class="identity-card admin-card" @tap="navigateToAdmin">
                    <image class="card-icon" src="/static/images/icons/管理员_角色管理.png" mode="aspectFit"></image>
                    <text class="card-title">我是管理员</text>
                </view>

                <view class="identity-card student-card" @tap="navigateToStudent">
                    <image class="card-icon" src="/static/images/icons/学生.png" mode="aspectFit"></image>
                    <text class="card-title">我是学生</text>
                </view>

                <view class="identity-card teacher-card" @tap="navigateToTeacher">
                    <image class="card-icon" src="/static/images/icons/教师，领导中心.png" mode="aspectFit"></image>
                    <text class="card-title">我是教师</text>
                </view>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
// login-select.ts

// 跳转到管理员登录页面
const navigateToAdmin = () => {
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
};
// 跳转到学生登录页面
const navigateToStudent = () => {
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
};
// 跳转到教师登录页面
const navigateToTeacher = () => {
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
};
</script>
<style>
@import './login-select.css';
</style>
