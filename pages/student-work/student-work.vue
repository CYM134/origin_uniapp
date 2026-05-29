<template>
    <view>
        <!-- pages/student-work/student-work.wxml -->

        <view class="container">
            <!-- 欢迎信息 -->
            <view class="welcome-section">
                <view class="user-info">
                    <image class="avatar" src="/static/images/icons/user-avatar.svg" mode="aspectFit"></image>
                    <view class="user-details">
                        <text class="username">{{ studentInfo.name || '学生' }}</text>

                        <text class="user-id">学号：{{ studentInfo.studentId || '' }}</text>
                        <text class="user-college">{{ studentInfo.college || '' }} {{ studentInfo.major || '' }}</text>
                    </view>
                </view>
            </view>

            <!-- 功能模块 -->
            <view class="function-grid">
                <!-- 个人信息 -->
                <view class="function-item" @tap="goToPersonalInfo">
                    <view class="function-icon">
                        <image src="/static/images/icons/personal-info.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">个人信息</text>
                    <text class="function-desc">查看和编辑个人资料</text>
                </view>

                <!-- 课表预览 -->
                <view class="function-item" @tap="goToSchedulePreview">
                    <view class="function-icon">
                        <image src="/static/images/icons/schedule.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">课表预览</text>
                    <text class="function-desc">查看实验室排课情况</text>
                </view>

                <!-- 预约申请 -->
                <view class="function-item" @tap="goToReservation">
                    <view class="function-icon">
                        <image src="/static/images/icons/reservation.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">预约申请</text>
                    <text class="function-desc">申请实验室使用</text>
                </view>

                <!-- 待办流程 -->
                <view class="function-item" @tap="goToPendingProcess">
                    <view class="function-icon">
                        <image src="/static/images/icons/pending.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">待办流程</text>
                    <text class="function-desc">查看待处理申请</text>
                    <view v-if="pendingCount > 0" class="badge">{{ pendingCount }}</view>
                </view>

                <!-- 已办流程 -->
                <view class="function-item" @tap="goToCompletedProcess">
                    <view class="function-icon">
                        <image src="/static/images/icons/completed.svg" mode="aspectFit"></image>
                    </view>
                    <text class="function-title">已办流程</text>
                    <text class="function-desc">查看已处理申请</text>
                </view>

                <view class="function-item" @tap="logout">
                    <view class="function-icon">
                        <image src="/static/images/icons/logout.svg" mode="aspectFit"></image>
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
// pages/student-work/student-work.ts

const studentInfo = ref<any>({
    name: '',
    studentId: '',
    college: '',
    major: '',
    phone: ''
});
const pendingCount = ref<number>(0);
// 待办事项数量
const notificationCount = ref<number>(0); // 通知数量

/**
 * 加载学生信息
 */
const loadStudentInfo = () => {
    try {
        const info = uni.getStorageSync('studentInfo');
        if (info) {
            studentInfo.value = info;
        }
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载学生信息失败:', error);
    }
};

/**
 * 加载待办事项数量
 */
const loadPendingCount = () => {
    try {
        // 获取当前登录学生信息
        const currentStudent = uni.getStorageSync('studentInfo');
        if (!currentStudent || !currentStudent.studentId) {
            pendingCount.value = 0;
            return;
        }

        // 获取待办事项数量（待审核状态的申请，且属于当前登录学生）
        const allApplications = uni.getStorageSync('studentApplications') || [];
        const myPendingApplications = allApplications.filter((app: any) => {
            // 首先过滤出当前学生的申请
            const isMyApplication = app.studentId === currentStudent.studentId || app.applicant === currentStudent.name || app.studentName === currentStudent.name;

            // 然后过滤出待审核状态的申请（包括待教师审核和待管理员审核）
            const isPending = app.status === 'pending' || app.status === 'teacher_approved';
            return isMyApplication && isPending;
        });
        pendingCount.value = myPendingApplications.length;
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
        const notifications = uni.getStorageSync('notifications') || [];
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
    loadStudentInfo();
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
    loadStudentInfo();
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
                    uni.removeStorageSync('isStudentLoggedIn');
                    uni.removeStorageSync('studentInfo');
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
        url: '/pages/student-personal-info/student-personal-info'
    });
};

/**
 * 跳转到课表预览页面
 */
const goToSchedulePreview = () => {
    uni.navigateTo({
        url: '/pages/student-schedule-preview/student-schedule-preview'
    });
};

/**
 * 跳转到预约申请页面
 */
const goToReservation = () => {
    uni.navigateTo({
        url: '/pages/student-reservation-apply/student-reservation-apply'
    });
};

/**
 * 跳转到待办流程页面
 */
const goToPendingProcess = () => {
    uni.navigateTo({
        url: '/pages/student-pending-process/student-pending-process'
    });
};

/**
 * 跳转到已办流程页面
 */
const goToCompletedProcess = () => {
    uni.navigateTo({
        url: '/pages/student-completed-process/student-completed-process'
    });
};

/**
 * 快速预约
 */
const quickReservation = () => {
    uni.navigateTo({
        url: '/pages/student-reservation-apply/student-reservation-apply?quick=true'
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
        url: '/pages/student-schedule-preview/student-schedule-preview?date=' + getTodayDate()
    });
};

/**
 * 查看消息通知
 */
const viewNotifications = () => {
    uni.navigateTo({
        url: '/pages/student-notifications/student-notifications'
    });
};
</script>
<style lang="less">
@import './student-work.less';
</style>
