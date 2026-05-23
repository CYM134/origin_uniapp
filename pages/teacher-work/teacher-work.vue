<template>
    <view>
        <!-- pages/teacher-work/teacher-work.wxml -->
        <navigation-bar title="实验室预约系统" :back="false" color="white" background="#3a7bd5"></navigation-bar>

        <view class="container">
            <!-- 欢迎信息 -->
            <view class="welcome-section">
                <view class="user-info">
                    <image class="avatar" src="/static/images/icons/teacher-avatar.svg" mode="aspectFit"></image>
                    <view class="user-details">
                        <text class="username">{{ teacherInfo.name || '教师' }}</text>
                        <text class="user-id">工号：{{ teacherInfo.teacherId || '' }}</text>
                        <text class="user-college">{{ teacherInfo.college || '' }} {{ teacherInfo.department || '' }}</text>
                    </view>
                </view>
            </view>

            <!-- 功能模块 -->
            <view class="function-grid">
                <!-- 个人信息 -->
                <view class="function-item" @tap="goToPersonalInfo">
                    <view class="function-icon">
                        <image src="/static/images/icons/teacher-personal-info.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">个人信息</text>
                    <text class="function-desc">查看和编辑个人资料</text>
                </view>

                <!-- 课表预览 -->
                <view class="function-item" @tap="goToSchedulePreview">
                    <view class="function-icon">
                        <image src="/static/images/icons/teacher-schedule.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">课表预览</text>
                    <text class="function-desc">查看实验室排课情况</text>
                </view>

                <!-- 预约申请 -->
                <view class="function-item" @tap="goToReservation">
                    <view class="function-icon">
                        <image src="/static/images/icons/teacher-reservation.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">预约申请</text>
                    <text class="function-desc">申请实验室使用</text>
                </view>

                <!-- 待办流程 -->
                <view class="function-item" @tap="goToPendingProcess">
                    <view class="function-icon">
                        <image src="/static/images/icons/teacher-pending.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">待办流程</text>
                    <text class="function-desc">审核学生申请</text>
                    <view v-if="pendingCount > 0" class="badge">{{ pendingCount }}</view>
                </view>

                <!-- 已办流程 -->
                <view class="function-item" @tap="goToCompletedProcess">
                    <view class="function-icon">
                        <image src="/static/images/icons/teacher-completed.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">已办流程</text>
                    <text class="function-desc">查看已处理申请</text>
                </view>

                <view class="function-item" @tap="logout">
                    <view class="function-icon">
                        <image src="/static/images/icons/teacher-logout.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">退出登录</text>
                    <text class="function-desc">退出当前的账号</text>
                </view>
            </view>

            <!-- 快捷操作 -->
            <view class="quick-actions">
                <view class="section-title">快捷操作</view>
                <view class="action-list">
                    <view class="action-item" @tap="quickReservation">
                        <image src="/static/images/icons/quick-reserve.svg" mode="aspectFit"></image>
                        <text>快速预约</text>
                    </view>
                    <view class="action-item" @tap="viewTodaySchedule">
                        <image src="/static/images/icons/today-schedule.svg" mode="aspectFit"></image>
                        <text>今日课表</text>
                    </view>
                    <view class="action-item" @tap="viewNotifications">
                        <image src="/static/images/icons/notification.svg" mode="aspectFit"></image>
                        <text>消息通知</text>
                        <view v-if="notificationCount > 0" class="notification-badge">{{ notificationCount }}</view>
                    </view>
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
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
// pages/teacher-work/teacher-work.ts

const teacherInfo = ref<any>({
    name: '',
    teacherId: '',
    college: '',
    department: '',
    phone: ''
});
const pendingCount = ref<number>(0);
// 待办事项数量
const notificationCount = ref<number>(0); // 通知数量

/**
 * 加载教师信息
 */
const loadTeacherInfo = () => {
    try {
        const info = uni.getStorageSync('teacherInfo');
        if (info) {
            teacherInfo.value = info;
        }
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载教师信息失败:', error);
    }
};

/**
 * 加载待办事项数量
 */
const loadPendingCount = () => {
    try {
        // 获取需要教师审核的学生申请数量
        const applications = uni.getStorageSync('studentApplications') || [];

        // 只统计学生发送的待审核申请，排除教师申请
        const pendingApplications = applications.filter((app: any) => {
            // 排除教师申请（type为'teacher'的申请）
            // 只统计状态为'pending'的学生申请
            return app.status === 'pending' && app.type !== 'teacher';
        });
        pendingCount.value = pendingApplications.length;
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载待办事项失败:', error);
        pendingCount.value = 0;
    }
};

/**
 * 加载通知数量
 */
const loadNotificationCount = () => {
    try {
        // 模拟获取未读通知数量
        const notifications = uni.getStorageSync('teacherNotifications') || [];
        const unreadCount = notifications.filter((item: any) => !item.read).length;
        notificationCount.value = unreadCount;
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载通知数量失败:', error);
    }
};

/**
 * 生命周期函数--监听页面加载
 */
onLoad(() => {
    loadTeacherInfo();
    loadPendingCount();
    loadNotificationCount();
});

/**
 * 生命周期函数--监听页面显示
 */
onShow(() => {
    // 每次显示页面时刷新数据
    loadPendingCount();
    loadNotificationCount();
});

/**
 * 页面相关事件处理函数--监听用户下拉动作
 */
onPullDownRefresh(() => {
    loadTeacherInfo();
    loadPendingCount();
    loadNotificationCount();

    // 停止下拉刷新
    setTimeout(() => {
        uni.stopPullDownRefresh();
    }, 1000);
});

/**
 * 退出登录
 */
const logout = () => {
    uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (res) => {
            if (res.confirm) {
                try {
                    // 清除登录状态
                    uni.removeStorageSync('isTeacherLoggedIn');
                    uni.removeStorageSync('teacherInfo');
                    uni.showToast({
                        title: '已退出登录',
                        icon: 'success'
                    });

                    // 返回登录选择页面
                    uni.reLaunch({
                        url: '/pages/login-select/login-select'
                    });
                } catch (error) {
                    console.log('CatchClause', error);
                    console.log('CatchClause', error);
                    console.error('退出登录失败:', error);
                    uni.showToast({
                        title: '退出失败',
                        icon: 'error'
                    });
                }
            }
        }
    });
};

/**
 * 跳转到个人信息页面
 */
const goToPersonalInfo = () => {
    uni.navigateTo({
        url: '/pages/teacher-personal-info/teacher-personal-info'
    });
};

/**
 * 跳转到课表预览页面
 */
const goToSchedulePreview = () => {
    uni.navigateTo({
        url: '/pages/teacher-schedule-preview/teacher-schedule-preview'
    });
};

/**
 * 跳转到预约申请页面
 */
const goToReservation = () => {
    uni.navigateTo({
        url: '/pages/teacher-reservation-apply/teacher-reservation-apply'
    });
};

/**
 * 跳转到待办流程页面
 */
const goToPendingProcess = () => {
    uni.navigateTo({
        url: '/pages/teacher-pending-process/teacher-pending-process'
    });
};

/**
 * 跳转到已办流程页面
 */
const goToCompletedProcess = () => {
    uni.navigateTo({
        url: '/pages/teacher-completed-process/teacher-completed-process'
    });
};

/**
 * 快速预约
 */
const quickReservation = () => {
    uni.navigateTo({
        url: '/pages/teacher-reservation-apply/teacher-reservation-apply?quick=true'
    });
};

/**
 * 获取今日日期
 */
const getTodayDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

/**
 * 查看今日课表
 */
const viewTodaySchedule = () => {
    uni.navigateTo({
        url: '/pages/teacher-schedule-preview/teacher-schedule-preview?date=' + getTodayDate()
    });
};

/**
 * 查看消息通知
 */
const viewNotifications = () => {
    uni.navigateTo({
        url: '/pages/teacher-notifications/teacher-notifications'
    });
};
</script>
<style>
@import './teacher-work.css';
</style>
