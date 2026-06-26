<template>
    <view class="page-wrapper">
        <!-- admin-work.wxml -->
        <view class="container">
            <view class="welcome">
                <text>欢迎管理员：{{ username }}</text>
            </view>

            <view class="function-container">
                <!-- 综合服务工作台 -->
                <view class="function-card" @tap="goWorkbench">
                    <view class="card-icon">
                        <image src="/static/images/icons/system-icon.png" mode="aspectFit"></image>
                    </view>
                    <view class="card-content">
                        <text class="card-title">综合服务工作台</text>
                        <text class="card-desc">数字概览 · 应用 · 通知 · 资讯管理</text>
                    </view>
                </view>

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
import { fetchCurrentUser } from '@/api/auth';
import { getStoredRole, getStoredUser, hasCompleteUserProfile } from '@/api/storage';
import { getDashboardSummary } from '@/api/admin';

const username = ref('管理员');

// 仪表盘待办计数（如 pendingReservations 等）。本页模板暂无展示位，
// 仅加载备用，不强行改动 <template>。
const dashboardSummary = ref<any>({});

const loadDashboardSummary = async () => {
    try {
        const summary: any = await getDashboardSummary();
        dashboardSummary.value = summary || {};
    } catch (err: any) {
        dashboardSummary.value = {};
        console.error('加载仪表盘统计失败', err);
    }
};

const loadCurrentAdmin = async () => {
    const storedRole = getStoredRole();
    const storedUser: any = getStoredUser();

    if (storedRole && storedRole !== 'admin') {
        uni.reLaunch({ url: '/pages/login-select/login-select' });
        return;
    }

    if (storedUser?.realName) {
        username.value = storedUser.realName;
    } else if (storedUser?.accountNo) {
        username.value = storedUser.accountNo;
    }

    if (!hasCompleteUserProfile(storedUser)) {
        try {
            const user: any = await fetchCurrentUser();
            username.value = user.realName || user.accountNo || '管理员';
        } catch (error) {
            console.error('加载管理员信息失败', error);
        }
    }
};

/**
 * 跳转到管理员综合服务工作台
 */
const goWorkbench = () => {
    uni.navigateTo({ url: '/pages/admin-workbench/admin-workbench' });
};

onLoad(() => {
    setTimeout(() => {
        loadCurrentAdmin();
        loadDashboardSummary();
    }, 0);
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

const navigateToSystemManagement = () => {
    uni.navigateTo({
        url: '../admin-system-management/admin-system-management',
        fail: () => {
            uni.showToast({ title: '功能开发中', icon: 'none', duration: 2000 });
        }
    });
};

</script>
<style lang="less">
@import './admin-work.less';
</style>
