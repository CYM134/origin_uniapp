<template>
  <view class="page">
    <view class="hd">
      <view class="hd-back" @tap="goBack">
        <text class="hd-back-ico">‹</text>
        <text class="hd-back-txt">返回</text>
      </view>
      <text class="hd-title">资讯详情</text>
    </view>

    <view class="body">
      <!-- 加载态 -->
      <view v-if="loading" class="state">
        <text class="state-txt">加载中…</text>
      </view>

      <!-- 错误/空态 -->
      <view v-else-if="!detail" class="state">
        <text class="state-txt">暂无数据</text>
      </view>

      <!-- 详情 -->
      <view v-else class="card">
        <text class="title">{{ detail.title || '无标题' }}</text>

        <view class="meta">
          <text v-if="detail.categoryName" class="meta-item">{{ detail.categoryName }}</text>
          <text v-if="detail.categoryName && (detail.author || detail.publishTime)" class="meta-dot">·</text>
          <text v-if="detail.author" class="meta-item">{{ detail.author }}</text>
          <text v-if="detail.author && detail.publishTime" class="meta-dot">·</text>
          <text v-if="detail.publishTime" class="meta-item">{{ formatTime(detail.publishTime) }}</text>
          <text class="meta-dot">·</text>
          <text class="meta-item">浏览 {{ detail.viewCount ?? 0 }}</text>
        </view>

        <text v-if="detail.summary" class="summary">{{ detail.summary }}</text>

        <image
          v-if="detail.coverImage"
          class="cover"
          :src="detail.coverImage"
          mode="widthFix"
        />

        <text class="content">{{ detail.content || '' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getNewsDetail } from '@/api/portal';
import { getStoredUser, getStoredRole } from '@/api/storage';

const loading = ref(false);
const detail = ref<any>(null);
const newsId = ref<string | number>('');

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

const loadDetail = async () => {
  if (!newsId.value) {
    detail.value = null;
    return;
  }
  loading.value = true;
  try {
    const res = await getNewsDetail(newsId.value);
    detail.value = res || null;
  } catch (e: any) {
    detail.value = null;
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  const pages = getCurrentPages();
  if (pages && pages.length > 1) {
    uni.navigateBack();
  } else {
    uni.reLaunch({ url: '/pages/news-list/news-list' });
  }
};

onLoad((options: any) => {
  newsId.value = options?.id || '';
  loadDetail();
});
</script>

<style lang="less">@import './news-detail.less';</style>
