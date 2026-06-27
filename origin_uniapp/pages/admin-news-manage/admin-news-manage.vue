<template>
  <view class="page">
    <view class="hd">
      <view class="hd-back" @tap="goBack">
        <text class="hd-back-ico">‹</text>
        <text class="hd-back-txt">返回</text>
      </view>
      <view class="hd-top">
        <text class="hd-title">校园资讯管理</text>
        <view class="hd-add" @tap="openCreate"><text>新增资讯</text></view>
      </view>
      <scroll-view class="status-bar" scroll-x :show-scrollbar="false">
        <view class="status-inner">
          <view
            class="status-chip"
            :class="{ active: activeStatus === s.value }"
            v-for="s in statusFilters"
            :key="s.value"
            @tap="selectStatus(s.value)"
          >
            <text>{{ s.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="body">
      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>

      <view class="empty" v-else-if="!newsList.length">
        <text>暂无数据</text>
      </view>

      <view v-else>
        <view class="news-card" v-for="item in newsList" :key="item.id">
          <view class="card-head">
            <text class="card-top" v-if="item.top">置顶</text>
            <text class="card-title">{{ item.title }}</text>
          </view>
          <text class="card-summary" v-if="item.summary">{{ item.summary }}</text>
          <view class="card-meta">
            <text class="meta-cat">{{ item.categoryName || '未分类' }}</text>
            <text class="status-tag" :class="'st-' + (item.status || 'draft')">{{ statusText(item.status) }}</text>
          </view>
          <view class="card-meta">
            <text class="meta-author">{{ item.author || '佚名' }}</text>
            <text class="meta-dot">·</text>
            <text class="meta-time">{{ formatTime(item.publishTime) }}</text>
            <text class="meta-dot">·</text>
            <text class="meta-view">浏览 {{ item.viewCount || 0 }}</text>
          </view>
          <view class="card-actions">
            <view class="act-btn" @tap="openEdit(item)"><text>编辑</text></view>
            <view
              class="act-btn"
              :class="item.status === 'published' ? 'act-warn' : 'act-ok'"
              @tap="toggleStatus(item)"
            >
              <text>{{ item.status === 'published' ? '下线' : '发布' }}</text>
            </view>
            <view class="act-btn act-danger" @tap="removeNews(item)"><text>删除</text></view>
          </view>
        </view>
      </view>
    </view>

    <view class="mask" v-if="showForm" @tap="closeForm">
      <view class="dialog" @tap.stop>
        <view class="dialog-hd">
          <text class="dialog-title">{{ editingId ? '编辑资讯' : '新增资讯' }}</text>
          <text class="dialog-close" @tap="closeForm">✕</text>
        </view>
        <scroll-view class="dialog-body" scroll-y>
          <view class="field">
            <text class="field-label">标题</text>
            <input class="field-input" v-model="form.title" placeholder="请输入标题" placeholder-class="ph" />
          </view>
          <view class="field">
            <text class="field-label">摘要</text>
            <input class="field-input" v-model="form.summary" placeholder="请输入摘要" placeholder-class="ph" />
          </view>
          <view class="field">
            <text class="field-label">正文内容</text>
            <textarea class="field-textarea" v-model="form.content" placeholder="请输入正文内容" placeholder-class="ph" />
          </view>
          <view class="field">
            <text class="field-label">分类</text>
            <picker
              class="field-picker"
              mode="selector"
              :range="categoryNames"
              :value="categoryIndex"
              @change="onCategoryChange"
            >
              <view class="picker-val">
                <text :class="{ ph: categoryIndex < 0 }">{{ categoryIndex >= 0 ? categoryNames[categoryIndex] : '请选择分类' }}</text>
              </view>
            </picker>
          </view>
          <view class="field">
            <text class="field-label">封面图链接</text>
            <input class="field-input" v-model="form.coverImage" placeholder="请输入封面图 URL" placeholder-class="ph" />
          </view>
          <view class="field">
            <text class="field-label">作者</text>
            <input class="field-input" v-model="form.author" placeholder="请输入作者" placeholder-class="ph" />
          </view>
          <view class="field field-row">
            <text class="field-label">置顶</text>
            <switch :checked="form.top" color="#F5A623" @change="onTopChange" />
          </view>
          <view class="field">
            <text class="field-label">状态</text>
            <view class="seg">
              <view
                class="seg-item"
                :class="{ active: form.status === 'draft' }"
                @tap="form.status = 'draft'"
              ><text>草稿</text></view>
              <view
                class="seg-item"
                :class="{ active: form.status === 'published' }"
                @tap="form.status = 'published'"
              ><text>发布</text></view>
            </view>
          </view>
        </scroll-view>
        <view class="dialog-ft">
          <view class="ft-btn ft-cancel" @tap="closeForm"><text>取消</text></view>
          <view class="ft-btn ft-confirm" :class="{ disabled: submitting }" @tap="submitForm"><text>{{ submitting ? '提交中...' : '确定' }}</text></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import {
  getAdminNews,
  getNewsCategories,
  createNews,
  updateNews,
  setNewsStatus,
  deleteNews
} from '@/api/portal';
import { getStoredUser } from '@/api/storage';

const goBack = () => uni.navigateBack({ delta: 1 });

interface NewsCategory {
  id: any;
  categoryName: string;
}
interface NewsItem {
  id: any;
  title: string;
  summary?: string;
  categoryName?: string;
  categoryId?: any;
  author?: string;
  status?: string;
  top?: boolean;
  publishTime?: string;
  viewCount?: number;
  coverImage?: string;
  content?: string;
}

const statusFilters = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
  { label: '已下线', value: 'offline' }
];

const loading = ref(false);
const submitting = ref(false);
const newsList = ref<NewsItem[]>([]);
const categories = ref<NewsCategory[]>([]);
const activeStatus = ref<string>('');

const showForm = ref(false);
const editingId = ref<any>(null);
const categoryIndex = ref(-1);

const form = ref<{
  title: string;
  summary: string;
  content: string;
  categoryId: any;
  coverImage: string;
  author: string;
  top: boolean;
  status: string;
}>({
  title: '',
  summary: '',
  content: '',
  categoryId: '',
  coverImage: '',
  author: '',
  top: false,
  status: 'draft'
});

const categoryNames = computed(() => categories.value.map((c) => c.categoryName));

function formatTime(t?: string): string {
  if (!t) return '未发布';
  return String(t).replace('T', ' ').slice(0, 16);
}

function statusText(s?: string): string {
  if (s === 'published') return '已发布';
  if (s === 'offline') return '已下线';
  return '草稿';
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
    if (activeStatus.value !== '') params.status = activeStatus.value;
    const list = await getAdminNews(params);
    newsList.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    newsList.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

function selectStatus(v: string) {
  if (activeStatus.value === v) return;
  activeStatus.value = v;
  loadNews();
}

function resetForm() {
  const user = getStoredUser() || ({} as any);
  form.value = {
    title: '',
    summary: '',
    content: '',
    categoryId: '',
    coverImage: '',
    author: user?.realName || '',
    top: false,
    status: 'draft'
  };
  categoryIndex.value = -1;
}

function openCreate() {
  editingId.value = null;
  resetForm();
  showForm.value = true;
}

function openEdit(item: NewsItem) {
  editingId.value = item.id;
  form.value = {
    title: item.title || '',
    summary: item.summary || '',
    content: item.content || '',
    categoryId: item.categoryId !== undefined && item.categoryId !== null ? item.categoryId : '',
    coverImage: item.coverImage || '',
    author: item.author || '',
    top: !!item.top,
    status: item.status === 'published' ? 'published' : 'draft'
  };
  const idx = categories.value.findIndex(
    (c) => String(c.id) === String(form.value.categoryId) || c.categoryName === item.categoryName
  );
  categoryIndex.value = idx;
  if (idx >= 0) form.value.categoryId = categories.value[idx].id;
  showForm.value = true;
}

function closeForm() {
  showForm.value = false;
}

function onCategoryChange(e: any) {
  const idx = Number(e?.detail?.value);
  categoryIndex.value = idx;
  if (idx >= 0 && categories.value[idx]) {
    form.value.categoryId = categories.value[idx].id;
  }
}

function onTopChange(e: any) {
  form.value.top = !!e?.detail?.value;
}

async function submitForm() {
  if (submitting.value) return;
  if (!form.value.title) {
    uni.showToast({ title: '请输入标题', icon: 'none' });
    return;
  }
  if (categoryIndex.value < 0 || form.value.categoryId === '') {
    uni.showToast({ title: '请选择分类', icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    const payload: any = {
      title: form.value.title,
      summary: form.value.summary,
      content: form.value.content,
      categoryId: form.value.categoryId,
      coverImage: form.value.coverImage,
      author: form.value.author,
      top: form.value.top,
      status: form.value.status
    };
    if (editingId.value) {
      await updateNews(editingId.value, payload);
      uni.showToast({ title: '已更新', icon: 'none' });
    } else {
      await createNews(payload);
      uni.showToast({ title: '已新增', icon: 'none' });
    }
    showForm.value = false;
    await loadNews();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '提交失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

async function toggleStatus(item: NewsItem) {
  const next = item.status === 'published' ? 'offline' : 'published';
  try {
    await setNewsStatus(item.id, next);
    uni.showToast({ title: next === 'published' ? '已发布' : '已下线', icon: 'none' });
    await loadNews();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
  }
}

function removeNews(item: NewsItem) {
  uni.showModal({
    title: '删除资讯',
    content: '确定删除「' + (item.title || '') + '」吗？',
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await deleteNews(item.id);
        uni.showToast({ title: '已删除', icon: 'none' });
        await loadNews();
      } catch (e: any) {
        uni.showToast({ title: e?.data?.message || '删除失败', icon: 'none' });
      }
    }
  });
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

<style lang="less">@import './admin-news-manage.less';</style>
