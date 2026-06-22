<template>
  <view class="page">
    <view class="hd">
      <text class="hd-title">通知公告</text>
      <view class="search">
        <input
          class="search-input"
          v-model="keyword"
          type="text"
          confirm-type="search"
          placeholder="搜索通知标题"
          placeholder-class="search-ph"
          @confirm="onSearch"
        />
        <text v-if="keyword" class="search-clear" @tap="onClear">×</text>
      </view>
    </view>

    <view class="body">
      <!-- 加载态 -->
      <view v-if="loading" class="state">
        <text class="state-text">加载中...</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="!notices.length" class="state">
        <text class="state-text">暂无数据</text>
      </view>

      <!-- 列表 -->
      <view v-else class="list">
        <view
          class="card"
          v-for="item in notices"
          :key="item.id"
          @tap="openDetail(item)"
        >
          <view class="card-title-row">
            <text v-if="item.top" class="tag-top">置顶</text>
            <view v-if="!item.read" class="unread-dot"></view>
            <text class="card-title" :class="{ 'card-title--unread': !item.read }">{{ item.title }}</text>
          </view>
          <view class="card-meta">
            <text class="type-tag">{{ typeText(item.noticeType) }}</text>
            <text class="meta-time">{{ formatTime(item.publishTime) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { getNotices } from '@/api/portal';

const loading = ref(false);
const keyword = ref('');
const notices = ref<any[]>([]);

const typeMap: Record<string, string> = {
  system: '系统通知',
  academic: '教学通知',
  activity: '活动通知',
  urgent: '紧急通知',
  general: '普通通知',
  notice: '通知'
};

const typeText = (t: any): string => {
  if (!t) return '通知';
  return typeMap[String(t)] || String(t);
};

const formatTime = (value: any): string => {
  if (!value) return '';
  const date = new Date(value);
  if (isNaN(date.getTime())) return String(value);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const hh = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${d} ${hh}:${mm}`;
};

const loadNotices = async () => {
  loading.value = true;
  try {
    const list = await getNotices(keyword.value || '');
    notices.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    notices.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const onSearch = () => {
  loadNotices();
};

const onClear = () => {
  keyword.value = '';
  loadNotices();
};

const openDetail = (item: any) => {
  if (!item?.id) return;
  uni.navigateTo({ url: '/pages/notice-detail/notice-detail?id=' + item.id });
};

onLoad(() => {
  loadNotices();
});

// 已读状态可能在详情页变化，返回时刷新
onShow(() => {
  loadNotices();
});

onPullDownRefresh(async () => {
  await loadNotices();
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './notice-list.less';</style>
