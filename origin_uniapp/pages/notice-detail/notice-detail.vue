<template>
  <view class="page">
    <view class="hd">
      <view class="hd-back" @tap="goBack">
        <text class="hd-back-icon">‹</text>
        <text class="hd-back-text">返回</text>
      </view>
      <text class="hd-title">通知详情</text>
    </view>

    <view class="body">
      <view v-if="loading" class="state">
        <text class="state-text">加载中...</text>
      </view>

      <view v-else-if="!notice" class="state">
        <text class="state-text">暂无数据</text>
        <view class="state-btn" @tap="goBack">
          <text class="state-btn-text">返回</text>
        </view>
      </view>

      <view v-else class="card">
        <text class="notice-title">{{ notice.title || '无标题' }}</text>

        <view class="meta">
          <text class="meta-item">{{ notice.publisherName || '系统' }}</text>
          <text class="meta-dot">·</text>
          <text class="meta-item">{{ formatTime(notice.publishTime) }}</text>
          <text class="meta-dot">·</text>
          <text class="meta-item">浏览 {{ notice.viewCount || 0 }}</text>
        </view>

        <view class="divider"></view>

        <text class="content">{{ notice.content || '暂无内容' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { getNoticeDetail } from '@/api/portal';
import { getStoredRole } from '@/api/storage';

const loading = ref(false);
const notice = ref<any>(null);
const role = ref<string>('student');
let noticeId = '';

function goBack() {
  uni.navigateBack({ delta: 1 });
}

function formatTime(t: any): string {
  if (!t) return '';
  return String(t).replace('T', ' ').slice(0, 16);
}

async function loadDetail() {
  if (!noticeId) {
    uni.showToast({ title: '参数缺失', icon: 'none' });
    setTimeout(goBack, 800);
    return;
  }
  loading.value = true;
  try {
    const res = await getNoticeDetail(noticeId);
    notice.value = res || null;
    if (!notice.value) {
      uni.showToast({ title: '通知不存在', icon: 'none' });
      setTimeout(goBack, 800);
    }
  } catch (e: any) {
    notice.value = null;
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
    setTimeout(goBack, 800);
  } finally {
    loading.value = false;
  }
}

onLoad((options: any) => {
  role.value = getStoredRole() || 'student';
  noticeId = options?.id || '';
  loadDetail();
});
</script>

<style lang="less">@import './notice-detail.less';</style>
