<template>
    <view class="page-wrapper">
        <!-- admin-work.wxml -->
        <view class="container">
            <view class="welcome">
                <text>欢迎管理员：{{ username }}</text>
            </view>

            <view class="function-container">
                <!-- 实验室信息管理 -->
                <view class="function-card" @tap="navigateToLabManagement">
                    <view class="card-icon">
                        <image src="/static/images/icons/lab-icon.png" mode="aspectFit"></image>
                    </view>
                    <view class="card-content">
                        <text class="card-title">实验室管理</text>
                        <text class="card-desc">添加、删除、修改实验室信息</text>
                    </view>
                </view>

                <!-- 课表导入导出 -->
                <view class="function-card" @tap="navigateToScheduleManagement">
                    <view class="card-icon">
                        <image src="/static/images/icons/schedule-icon.png" mode="aspectFit"></image>
                    </view>
                    <view class="card-content">
                        <text class="card-title">课表管理</text>
                        <text class="card-desc">批量导入课表，导出最终课表</text>
                    </view>
                </view>

                <!-- 预约审批 -->
                <view class="function-card" @tap="navigateToReservationApproval">
                    <view class="card-icon">
                        <image src="/static/images/icons/approval-icon.png" mode="aspectFit"></image>
                    </view>
                    <view class="card-content">
                        <text class="card-title">预约审批</text>
                        <text class="card-desc">查看并审批预约申请</text>
                    </view>
                </view>

                <!-- 审批记录 -->
                <view class="function-card" @tap="navigateToApprovalRecords">
                    <view class="card-icon">
                        <image src="/static/images/icons/record-icon.png" mode="aspectFit"></image>
                    </view>
                    <view class="card-content">
                        <text class="card-title">审批记录</text>
                        <text class="card-desc">查看已审批的记录</text>
                    </view>
                </view>

                <!-- 教师注册审核 -->
                <view class="function-card" @tap="navigateToTeacherRegistration">
                    <view class="card-icon">
                        <image src="/static/images/icons/teacher-icon.png" mode="aspectFit"></image>
                    </view>
                    <view class="card-content">
                        <text class="card-title">注册审核</text>
                        <text class="card-desc">审核教师账号注册申请</text>
                    </view>
                </view>

                <!-- 系统管理 -->
                <view class="function-card" @tap="navigateToSystemManagement">
                    <view class="card-icon">
                        <image src="/static/images/icons/system-icon.png" mode="aspectFit"></image>
                    </view>
                    <view class="card-content">
                        <text class="card-title">系统管理</text>
                        <text class="card-desc">系统设置、数据备份、用户管理</text>
                    </view>
                </view>

                <!-- 退出登录 -->
                <view class="function-card" @tap="logout">
                    <view class="card-icon">
                        <image src="/static/images/icons/logout.png" mode="aspectFit"></image>
                    </view>
                    <view class="card-content">
                        <text class="card-title">退出登录</text>
                        <text class="card-desc">退出管理员账号</text>
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
import { ref, onMounted } from 'vue';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';

const username = ref('管理员');

onMounted(() => {
    const savedUsername = uni.getStorageSync('adminUsername');
    if (savedUsername) {
        username.value = savedUsername;
    }
});

const navigateToLabManagement = () => {
    uni.navigateTo({
        url: '../admin-lab-management/admin-lab-management',
        fail: () => {
            uni.showToast({ title: '功能开发中', icon: 'none', duration: 2000 });
        }
    });
};

const navigateToScheduleManagement = () => {
    uni.navigateTo({
        url: '../admin-schedule-management/admin-schedule-management',
        fail: () => {
            uni.showToast({ title: '功能开发中', icon: 'none', duration: 2000 });
        }
    });
};

const navigateToReservationApproval = () => {
    uni.navigateTo({
        url: '../admin-reservation-approval/admin-reservation-approval',
        fail: () => {
            uni.showToast({ title: '功能开发中', icon: 'none', duration: 2000 });
        }
    });
};

const navigateToApprovalRecords = () => {
    uni.navigateTo({
        url: '../admin-approval-records/admin-approval-records',
        fail: () => {
            uni.showToast({ title: '功能开发中', icon: 'none', duration: 2000 });
        }
    });
};

const navigateToTeacherRegistration = () => {
    uni.navigateTo({
        url: '../admin-teacher-registration/admin-teacher-registration',
        fail: () => {
            uni.showToast({ title: '功能开发中', icon: 'none', duration: 2000 });
        }
    });
};

const navigateToSystemManagement = () => {
    uni.navigateTo({
        url: '../admin-system-management/admin-system-management',
        fail: () => {
            uni.showToast({ title: '功能开发中', icon: 'none', duration: 2000 });
        }
    });
};

const logout = () => {
    uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (res) => {
            if (res.confirm) {
                try {
                    uni.removeStorageSync('isStudentLoggedIn');
                    uni.removeStorageSync('studentInfo');
                    uni.showToast({ title: '已退出登录', icon: 'success' });
                    uni.reLaunch({ url: '/pages/login-select/login-select' });
                } catch (error) {
                    console.error('退出登录失败', error);
                    uni.showToast({ title: '退出失败', icon: 'error' });
                }
            }
        }
    });
};
</script>
<style lang="less">
@import './admin-work.less';
</style>
