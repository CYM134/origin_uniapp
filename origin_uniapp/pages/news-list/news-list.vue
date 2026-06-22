<template>
  <view class="page">
    <view class="hd">
      <text class="hd-title">校园资讯</text>
      <view class="search-box">
        <input
          class="search-input"
          v-model="keyword"
          type="text"
          placeholder="搜索资讯标题"
          placeholder-class="search-ph"
          confirm-type="search"
          @confirm="onSearch"
        />
        <view class="search-btn" @tap="onSearch"><text>搜索</text></view>
      </view>
    </view>

    <scroll-view class="tabs" scroll-x :show-scrollbar="false">
      <view class="tabs-inner">
        <view
          class="tab"
          :class="{ active: activeCategoryId === '' }"
          @tap="selectCategory('')"
        >
          <text>全部</text>
        </view>
        <view
          class="tab"
          :class="{ active: activeCategoryId === cat.id }"
          v-for="cat in categories"
          :key="cat.id"
          @tap="selectCategory(cat.id)"
        >
          <text>{{ cat.categoryName }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="body">
      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>

      <view class="empty" v-else-if="!newsList.length">
        <text>暂无数据</text>
      </view>

      <view v-else>
        <view
          class="news-card"
          v-for="item in newsList"
          :key="item.id"
          @tap="goDetail(item.id)"
        >
          <image
            class="cover"
            v-if="item.coverImage"
            :src="item.coverImage"
            mode="aspectFill"
          ></image>
          <view class="cover cover-ph" v-else>
            <text class="cover-ph-text">{{ (item.categoryName || '资讯').slice(0, 2) }}</text>
          </view>

          <view class="news-info">
            <view class="news-title-row">
              <text class="news-top" v-if="item.top">置顶</text>
              <text class="news-title">{{ item.title }}</text>
            </view>
            <text class="news-summary">{{ item.summary || '暂无摘要' }}</text>
            <view class="news-meta">
              <text class="meta-author">{{ item.author || item.categoryName || '校园资讯' }}</text>
              <text class="meta-dot">·</text>
              <text class="meta-time">{{ formatTime(item.publishTime) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { getNewsCategories, getNewsList } from '@/api/portal';
import { getStoredRole } from '@/api/storage';

interface NewsCategory {
  id: any;
  categoryName: string;
  categoryCode?: string;
}
interface NewsItem {
  id: any;
  title: string;
  summary?: string;
  coverImage?: string;
  categoryName?: string;
  author?: string;
  publishTime?: string;
  top?: boolean;
}

const loading = ref(false);
const categories = ref<NewsCategory[]>([]);
const newsList = ref<NewsItem[]>([]);
const activeCategoryId = ref<any>('');
const keyword = ref('');

const role = getStoredRole();

function formatTime(t?: string): string {
  if (!t) return '';
  return String(t).replace('T', ' ').slice(0, 16);
}

async function loadCategories() {
  try {
    const list = await getNewsCategories();
    categories.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    categories.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  }
}

async function loadNews() {
  loading.value = true;
  try {
    const params: any = {};
    if (activeCategoryId.value !== '') params.categoryId = activeCategoryId.value;
    if (keyword.value) params.keyword = keyword.value;
    const list = await getNewsList(params);
    newsList.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    newsList.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function selectCategory(id: any) {
  if (activeCategoryId.value === id) return;
  activeCategoryId.value = id;
  loadNews();
}

function onSearch() {
  loadNews();
}

function goDetail(id: any) {
  if (id === undefined || id === null) return;
  uni.navigateTo({ url: '/pages/news-detail/news-detail?id=' + id });
}

onLoad(() => {
  loadCategories();
  loadNews();
});

onShow(() => {});

onPullDownRefresh(async () => {
  await Promise.all([loadCategories(), loadNews()]);
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './news-list.less';</style>
