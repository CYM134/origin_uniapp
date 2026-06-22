<template>
    <view class="page">
        <view class="hd">
            <text class="hd-title">消息中心</text>
            <view class="hd-action" @tap="onMarkAll">
                <text>全部已读</text>
            </view>
        </view>

        <view class="body">
            <!-- 筛选 tab -->
            <view class="tabs">
                <view
                    :class="'tab ' + (activeTab === 'all' ? 'tab--on' : '')"
                    @tap="switchTab('all')"
                >
                    <text>全部</text>
                </view>
                <view
                    :class="'tab ' + (activeTab === 'unread' ? 'tab--on' : '')"
                    @tap="switchTab('unread')"
                >
                    <text>未读{{ unreadCount > 0 ? '（' + unreadCount + '）' : '' }}</text>
                </view>
            </view>

            <!-- 加载态 -->
            <view v-if="loading" class="state">
                <text class="state-text">加载中...</text>
            </view>

            <!-- 空态 -->
            <view v-else-if="filteredList.length === 0" class="state">
                <text class="state-text">暂无数据</text>
            </view>

            <!-- 列表 -->
            <view v-else class="list">
                <view
                    class="msg-card"
                    v-for="item in filteredList"
                    :key="item.id"
                    @tap="onTapMessage(item)"
                >
                    <view :class="'dot dot--' + (item.type || 'info')"></view>
                    <view class="msg-main">
                        <view class="msg-row">
                            <text :class="'msg-title ' + (item.read ? '' : 'msg-title--unread')">{{ item.title }}</text>
                            <view v-if="!item.read" class="badge"></view>
                        </view>
                        <text class="msg-summary">{{ item.content }}</text>
                        <view class="msg-foot">
                            <text class="msg-time">{{ formatTime(item.createTime) }}</text>
                        </view>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getMessages, getMessageUnreadCount, markMessageRead, markAllMessagesRead } from '@/api/portal';

const loading = ref(false);
const messages = ref<any[]>([]);
const unreadCount = ref(0);
const activeTab = ref<'all' | 'unread'>('all');

const filteredList = computed(() => {
    const list = messages.value || [];
    if (activeTab.value === 'unread') {
        return list.filter((m: any) => !m?.read);
    }
    return list;
});

const formatTime = (value: any): string => {
    if (!value) {
        return '';
    }
    const date = new Date(value);
    if (isNaN(date.getTime())) {
        return String(value);
    }
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${month}-${day} ${hours}:${minutes}`;
};

const loadUnreadCount = async () => {
    try {
        const res = await getMessageUnreadCount();
        unreadCount.value = res?.count || 0;
    } catch (e: any) {
        unreadCount.value = 0;
    }
};

const loadMessages = async () => {
    loading.value = true;
    try {
        const list = (await getMessages()) || [];
        messages.value = Array.isArray(list) ? list : [];
    } catch (e: any) {
        messages.value = [];
        uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
    } finally {
        loading.value = false;
    }
};

const refresh = async () => {
    await loadMessages();
    await loadUnreadCount();
};

const switchTab = (tab: 'all' | 'unread') => {
    activeTab.value = tab;
};

const onTapMessage = async (item: any) => {
    if (!item || item.read) {
        return;
    }
    try {
        await markMessageRead(item.id);
        item.read = true;
        unreadCount.value = unreadCount.value > 0 ? unreadCount.value - 1 : 0;
    } catch (e: any) {
        uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
    }
};

const onMarkAll = () => {
    if (unreadCount.value === 0) {
        uni.showToast({ title: '暂无未读消息', icon: 'none' });
        return;
    }
    uni.showModal({
        title: '全部已读',
        content: '确定将全部消息标记为已读？',
        success: async (res) => {
            if (!res.confirm) {
                return;
            }
            try {
                await markAllMessagesRead();
                (messages.value || []).forEach((m: any) => {
                    if (m) {
                        m.read = true;
                    }
                });
                unreadCount.value = 0;
                uni.showToast({ title: '已全部标记为已读', icon: 'none' });
            } catch (e: any) {
                uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
            }
        }
    });
};

onShow(() => {
    refresh();
});
</script>

<style lang="less">@import './message-center.less';</style>
