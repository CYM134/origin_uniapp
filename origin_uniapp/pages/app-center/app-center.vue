<template>
  <view class="page">
    <view class="hd">
      <view class="hd-back" @tap="goBack">
        <text class="hd-back-ico">‹</text>
        <text class="hd-back-txt">返回</text>
      </view>
      <text class="hd-title">应用中心</text>
      <text class="hd-sub">汇聚校园全部应用服务</text>
    </view>

    <view class="body">
      <!-- 搜索框 -->
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          type="text"
          v-model="keyword"
          placeholder="搜索应用名称"
          placeholder-class="search-ph"
          confirm-type="search"
          @input="onKeywordInput"
          @confirm="reload"
        />
        <text v-if="keyword" class="search-clear" @tap="clearKeyword">✕</text>
      </view>

      <!-- 分类筛选 -->
      <scroll-view class="cat-bar" scroll-x :show-scrollbar="false">
        <view
          class="cat-item"
          :class="{ active: activeCategoryId === '' }"
          @tap="selectCategory('')"
        >
          <text class="cat-text">全部</text>
        </view>
        <view
          class="cat-item"
          :class="{ active: activeCategoryId === cat.id }"
          v-for="cat in categories"
          :key="cat.id"
          @tap="selectCategory(cat.id)"
        >
          <text class="cat-text">{{ cat.categoryName }}</text>
        </view>
      </scroll-view>

      <!-- 加载态 -->
      <view v-if="loading" class="state">
        <text class="state-text">加载中...</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="!apps.length" class="state">
        <text class="state-icon">📭</text>
        <text class="state-text">暂无数据</text>
      </view>

      <!-- 应用宫格 -->
      <view v-else class="grid">
        <view
          class="grid-item"
          v-for="app in apps"
          :key="app.id"
          @tap="openApp(app)"
        >
          <text
            class="fav-star"
            :class="{ on: app.favorite }"
            @tap.stop="toggleFavorite(app)"
          >{{ app.favorite ? '★' : '☆' }}</text>
          <view class="app-icon">
            <text class="app-icon-text">{{ iconChar(app) }}</text>
          </view>
          <text class="app-name">{{ app.appName || app.name || '应用' }}</text>
          <text class="app-desc">{{ app.description || app.appDesc || '暂无描述' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import {
  getAppCategories,
  getApps,
  favoriteApp,
  unfavoriteApp,
  visitApp,
} from '@/api/portal';
import { getStoredRole } from '@/api/storage';

const goBack = () => uni.navigateBack({ delta: 1 });

const loading = ref(false);
const categories = ref<any[]>([]);
const apps = ref<any[]>([]);
const activeCategoryId = ref<string | number>('');
const keyword = ref('');
const role = ref<string>('student');

let debounceTimer: any = null;

async function loadCategories() {
  try {
    const list = await getAppCategories();
    categories.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    categories.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  }
}

async function loadApps() {
  loading.value = true;
  try {
    const params: any = {};
    if (activeCategoryId.value !== '') params.categoryId = activeCategoryId.value;
    if (keyword.value) params.keyword = keyword.value;
    const list = await getApps(params);
    apps.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    apps.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function reload() {
  loadApps();
}

function selectCategory(id: string | number) {
  activeCategoryId.value = id;
  loadApps();
}

function onKeywordInput() {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    loadApps();
  }, 400);
}

function clearKeyword() {
  keyword.value = '';
  loadApps();
}

function iconChar(app: any): string {
  const name = app?.appName || app?.name || '';
  return name ? name.charAt(0) : '应';
}

function getReservationManagementUrl() {
  const r = role.value;
  if (r === 'teacher') return '/pages/teacher-work/teacher-work';
  if (r === 'student') return '/pages/student-work/student-work';
  return '/pages/admin-work/admin-work';
}

async function toggleFavorite(app: any) {
  const next = !app?.favorite;
  try {
    if (next) {
      await favoriteApp(app.id);
    } else {
      await unfavoriteApp(app.id);
    }
    app.favorite = next;
    uni.showToast({ title: next ? '已收藏' : '已取消收藏', icon: 'none' });
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
  }
}

function openApp(app: any) {
  visitApp(app.id, 'h5').catch(() => {});

  if (app?.openType === 'external' && app?.externalUrl) {
    uni.showModal({ title: '外部链接', content: app.externalUrl });
    return;
  }

  const r = role.value;
  const routeMap: Record<string, string> = {
    '/portal/apps': '/pages/app-center/app-center',
    '/notices': '/pages/notice-list/notice-list',
    '/news': '/pages/news-list/news-list',
    '/tasks': '/pages/task-center/task-center',
    '/messages': '/pages/message-center/message-center',
    '/calendar': '/pages/calendar/calendar',
    '/ai/assistant': '/pages/ai-assistant/ai-assistant',
    '/repair': r === 'admin'
      ? '/pages/admin-repair-review/admin-repair-review'
      : '/pages/repair-service/repair-service',
    '/admin/dashboard': '/pages/admin-workbench/admin-workbench',
    '/system/users': '/pages/admin-system-management/admin-system-management',
    '/admin/apps': '/pages/admin-app-manage/admin-app-manage',
  };

  let url = '';
  const path = app?.routePath || '';

  if (path === '/lab/reservation') {
    url = getReservationManagementUrl();
  } else if (path === '/schedule') {
    url = r === 'teacher'
      ? '/pages/teacher-schedule-preview/teacher-schedule-preview'
      : '/pages/student-schedule-preview/student-schedule-preview';
  } else if (routeMap[path]) {
    url = routeMap[path];
  }

  if (url) {
    uni.navigateTo({ url });
  } else {
    uni.showToast({ title: '功能建设中', icon: 'none' });
  }
}

onLoad(() => {
  role.value = getStoredRole() || 'student';
  loadCategories();
  loadApps();
});

onShow(() => {
  // 收藏状态可能在别处变化，回到页面刷新
  if (categories.value.length) {
    loadApps();
  }
});

onPullDownRefresh(async () => {
  await loadCategories();
  await loadApps();
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './app-center.less';</style>
