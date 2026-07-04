<template>
    <view class="page-wrapper">
        <!-- pages/teacher-notifications/teacher-notifications.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="消息通知" color="white" background="#10B981" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <app-navigation-bar title="消息通知" :back="true" color="white" background="#10B981"></app-navigation-bar>
        <!-- #endif -->

        <view class="container">
            <!-- 顶部操作栏 -->
            <view class="action-bar">
                <text class="unread-tip">未读 {{ unreadCount }} 条</text>
                <view class="mark-all-btn" @tap="markAllRead">
                    <text>全部已读</text>
                </view>
            </view>

            <!-- 通知列表 -->
            <view class="notification-list">
                <view v-if="notifications.length === 0" class="empty-state">
                    <image src="/static/images/icons/empty-box.svg" mode="aspectFit"></image>
                    <text class="empty-title">暂无消息通知</text>
                    <text class="empty-desc">当有新的审核结果或系统消息时，会在这里显示</text>
                </view>

                <view
                    :class="'notification-item ' + (isRead(item) ? 'read' : 'unread')"
                    @tap="openNotification"
                    :data-item="item"
                    v-for="(item, index) in notifications"
                    :key="index"
                >
                    <view class="item-header">
                        <view class="title-section">
                            <view v-if="!isRead(item)" class="unread-dot"></view>
                            <text class="notification-title">{{ item.title }}</text>
                        </view>
                        <text class="notification-time">{{ item.createTimeText }}</text>
                    </view>
                    <view class="item-content">
                        <text class="notification-content">{{ item.content }}</text>
                    </view>
                </view>
            </view>

            <view class="footer">
                <text>© SCNU IBC 实验室空间预约与协同管理系统</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
import { getNotifications, markNotificationRead, markAllNotificationsRead } from '@/api/teacher';
// pages/teacher-notifications/teacher-notifications.ts

const notifications = ref<any[]>([]);

// 兼容 0/1 与 true/false 的已读判断
const isRead = (item: any): boolean => Boolean(item && item.read);

// 未读数量
const unreadCount = computed(() => notifications.value.filter((item: any) => !isRead(item)).length);

// 格式化时间
const formatDateTime = (value: any): string => {
    if (!value) {
        return '';
    }
    const date = new Date(value);
    if (isNaN(date.getTime())) {
        return String(value);
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
};

// 加载通知列表
const loadNotifications = async () => {
    try {
        const list = (await getNotifications()) || [];
        const processed = (Array.isArray(list) ? list : []).map((item: any) => ({
            ...item,
            createTimeText: formatDateTime(item.createTime)
        }));
        notifications.value = processed;
    } catch (error: any) {
        console.error('加载通知列表失败:', error);
        notifications.value = [];
        uni.showToast({
            title: error?.data?.message || '加载失败',
            icon: 'none'
        });
    }
};

onLoad(() => {
    loadNotifications();
});

onShow(() => {
    loadNotifications();
});

onPullDownRefresh(async () => {
    await loadNotifications();
    uni.stopPullDownRefresh();
});

// 点击单条通知：标记已读并本地置已读
const openNotification = async (e: any) => {
    const item = e.currentTarget.dataset.item;
    if (!item || !item.id) {
        return;
    }
    if (isRead(item)) {
        return;
    }
    try {
        await markNotificationRead(item.id);
        // 本地置已读
        notifications.value = notifications.value.map((n: any) =>
            n.id === item.id ? { ...n, read: true, readAt: new Date().toISOString() } : n
        );
    } catch (error: any) {
        console.error('标记已读失败:', error);
        uni.showToast({
            title: error?.data?.message || '操作失败',
            icon: 'none'
        });
    }
};

// 全部已读
const markAllRead = async () => {
    if (unreadCount.value === 0) {
        uni.showToast({ title: '没有未读消息', icon: 'none' });
        return;
    }
    try {
        await markAllNotificationsRead();
        notifications.value = notifications.value.map((n: any) => ({
            ...n,
            read: true,
            readAt: n.readAt || new Date().toISOString()
        }));
        uni.showToast({ title: '已全部标记为已读', icon: 'success' });
    } catch (error: any) {
        console.error('全部已读失败:', error);
        uni.showToast({
            title: error?.data?.message || '操作失败',
            icon: 'none'
        });
    }
};
</script>

<style lang="less">
.page-wrapper {
    min-height: 100vh;
    background: #f5f6f8;
}

.container {
    padding: 20rpx 24rpx 60rpx;
}

.action-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 8rpx;
}

.action-bar .unread-tip {
    font-size: 26rpx;
    color: #666;
}

.action-bar .mark-all-btn {
    padding: 12rpx 28rpx;
    background: #10b981;
    color: #fff;
    font-size: 26rpx;
    border-radius: 30rpx;
}

.notification-list {
    margin-top: 12rpx;
}

.notification-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.notification-item.unread {
    border-left: 6rpx solid #10b981;
}

.notification-item.read {
    opacity: 0.78;
}

.item-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.item-header .title-section {
    display: flex;
    align-items: center;
    flex: 1;
    overflow: hidden;
}

.item-header .unread-dot {
    width: 14rpx;
    height: 14rpx;
    border-radius: 50%;
    background: #ef4444;
    margin-right: 12rpx;
    flex-shrink: 0;
}

.item-header .notification-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #1f2937;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.item-header .notification-time {
    font-size: 22rpx;
    color: #9ca3af;
    margin-left: 16rpx;
    flex-shrink: 0;
}

.item-content {
    margin-top: 14rpx;
}

.item-content .notification-content {
    font-size: 26rpx;
    color: #4b5563;
    line-height: 1.6;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 120rpx 0;
}

.empty-state image {
    width: 160rpx;
    height: 160rpx;
    margin-bottom: 24rpx;
}

.empty-state .empty-title {
    font-size: 30rpx;
    color: #6b7280;
    margin-bottom: 12rpx;
}

.empty-state .empty-desc {
    font-size: 24rpx;
    color: #9ca3af;
}

.footer {
    margin-top: 40rpx;
    text-align: center;
}

.footer text {
    font-size: 22rpx;
    color: #b0b0b0;
}
</style>
