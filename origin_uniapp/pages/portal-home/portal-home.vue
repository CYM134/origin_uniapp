<template>
  <view class="page">
    <!-- 顶部 accent 渐变 header -->
    <view class="hd">
      <view class="hd-left">
        <text class="hd-title">校园综合服务平台</text>
        <text class="hd-sub">欢迎，{{ userName }}（{{ roleName }}）</text>
      </view>
      <view class="hd-msg" @tap="goMessages">
        <text class="hd-msg-icon">消息</text>
        <view v-if="unreadMessages > 0" class="hd-badge">{{ unreadMessages > 99 ? '99+' : unreadMessages }}</view>
      </view>
    </view>

    <view class="body">
      <!-- 加载中 -->
      <view v-if="loading" class="loading">加载中...</view>

      <block v-else>
        <!-- 快捷入口行 -->
        <view class="quick-row">
          <view class="quick-item" @tap="goReservation">
            <text class="quick-emoji">🔬</text>
            <text class="quick-text">实验室预约</text>
          </view>
          <view class="quick-item" @tap="goAppCenter">
            <text class="quick-emoji">📱</text>
            <text class="quick-text">应用中心</text>
          </view>
          <view class="quick-item" @tap="goNotices">
            <text class="quick-emoji">📢</text>
            <text class="quick-text">通知公告</text>
          </view>
          <view class="quick-item" @tap="goCalendar">
            <text class="quick-emoji">📅</text>
            <text class="quick-text">我的日历</text>
          </view>
          <view v-if="showTaskEntry" class="quick-item" @tap="goTasks">
            <text class="quick-emoji">✅</text>
            <text class="quick-text">任务中心({{ pendingTaskCount }})</text>
          </view>
        </view>

        <!-- 我的应用 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">我的应用</text>
          </view>
          <view v-if="(myApps?.length || 0) > 0" class="app-grid">
            <view class="app-cell" v-for="app in myApps" :key="app.id" @tap="openApp(app)">
              <view class="app-icon">{{ appGlyph(app) }}</view>
              <text class="app-name">{{ app.appName }}</text>
            </view>
          </view>
          <view v-else class="empty-fav">
            <text class="empty-fav-tip">还没有收藏常用应用</text>
            <view class="empty-fav-btn" @tap="goAppCenter">去应用中心收藏常用应用</view>
          </view>
        </view>

        <!-- 最新应用 -->
        <view class="section" v-if="(latestApps?.length || 0) > 0">
          <view class="section-hd">
            <text class="section-title">最新应用</text>
          </view>
          <view class="app-grid">
            <view class="app-cell" v-for="app in latestApps" :key="app.id" @tap="openApp(app)">
              <view class="app-icon">{{ appGlyph(app) }}</view>
              <text class="app-name">{{ app.appName }}</text>
            </view>
          </view>
        </view>

        <!-- 热门应用 -->
        <view class="section" v-if="(hotApps?.length || 0) > 0">
          <view class="section-hd">
            <text class="section-title">热门应用</text>
          </view>
          <view class="app-grid">
            <view class="app-cell" v-for="app in hotApps" :key="app.id" @tap="openApp(app)">
              <view class="app-icon">{{ appGlyph(app) }}</view>
              <text class="app-name">{{ app.appName }}</text>
            </view>
          </view>
        </view>

        <!-- 通知公告 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">通知公告</text>
            <text class="section-more" @tap="goNotices">更多</text>
          </view>
          <view v-if="(topNotices?.length || 0) > 0" class="list">
            <view class="notice-item" v-for="n in topNotices" :key="n.id" @tap="openNotice(n)">
              <view class="notice-line">
                <text v-if="n.top" class="tag-top">置顶</text>
                <text class="notice-title">{{ n.title }}</text>
                <view v-if="!n.read" class="red-dot"></view>
              </view>
              <text class="notice-time">{{ fmtTime(n.publishTime) }}</text>
            </view>
          </view>
          <view v-else class="empty">暂无数据</view>
        </view>

        <!-- 校园资讯 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">校园资讯</text>
            <text class="section-more" @tap="goNews">更多</text>
          </view>
          <view v-if="(topNews?.length || 0) > 0" class="list">
            <view class="news-item" v-for="item in topNews" :key="item.id" @tap="openNews(item)">
              <text class="news-title">{{ item.title }}</text>
              <text class="news-summary" v-if="item.summary">{{ item.summary }}</text>
              <text class="news-time">{{ fmtTime(item.publishTime) }}</text>
            </view>
          </view>
          <view v-else class="empty">暂无数据</view>
        </view>

        <!-- 我的日程 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">我的日程</text>
          </view>
          <view v-if="(upcomingEvents?.length || 0) > 0" class="list">
            <view class="event-item" v-for="(ev, idx) in upcomingEvents" :key="idx">
              <text class="event-title">{{ ev.title }}</text>
              <text class="event-time">{{ fmtTime(ev.startTime) }}<text v-if="ev.endTime"> - {{ fmtTime(ev.endTime) }}</text></text>
              <text class="event-loc" v-if="ev.location">📍 {{ ev.location }}</text>
            </view>
          </view>
          <view v-else class="empty">暂无日程</view>
        </view>
      </block>
    </view>

    <!-- AI 悬浮按钮 -->
    <view class="fab-ai" @tap="goAi"><text class="fab-ai-text">AI</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { getPortalHome, visitApp } from '@/api/portal';
import { getStoredUser, getStoredRole } from '@/api/storage';

const loading = ref(false);
const user = ref<any>({});
const myApps = ref<any[]>([]);
const hotApps = ref<any[]>([]);
const latestApps = ref<any[]>([]);
const notices = ref<any[]>([]);
const news = ref<any[]>([]);
const unreadMessages = ref<number>(0);
const upcomingEvents = ref<any[]>([]);
const pendingTaskCount = ref<number>(0);

const ROLE_TEXT: Record<string, string> = { student: '学生', teacher: '教师', admin: '管理员' };

const userName = computed(() => user.value?.name || user.value?.realName || '同学');
const roleName = computed(() => ROLE_TEXT[user.value?.role || getStoredRole() || ''] || '用户');

const topNotices = computed(() => (notices.value || []).slice(0, 5));
const topNews = computed(() => (news.value || []).slice(0, 5));
const showTaskEntry = computed(() => {
  const r = user.value?.role || getStoredRole();
  return (r === 'teacher' || r === 'admin') && pendingTaskCount.value > 0;
});

const appGlyph = (app: any): string => {
  if (app?.icon) return app.icon;
  const name = app?.appName || '';
  return name ? name.charAt(0) : '应';
};

const fmtTime = (t: any): string => {
  if (!t) return '';
  return String(t).replace('T', ' ').slice(0, 16);
};

const loadHome = async () => {
  loading.value = true;
  try {
    const data: any = await getPortalHome();
    user.value = data?.user || getStoredUser() || {};
    myApps.value = data?.myApps || [];
    hotApps.value = data?.hotApps || [];
    latestApps.value = data?.latestApps || [];
    notices.value = data?.notices || [];
    news.value = data?.news || [];
    unreadMessages.value = Number(data?.unreadMessages) || 0;
    upcomingEvents.value = data?.upcomingEvents || [];
    pendingTaskCount.value = Number(data?.pendingTaskCount) || 0;
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

// ====== 应用点击导航（APP NAVIGATION MAPPING）======
const openApp = (app: any) => {
  if (!app) return;
  visitApp(app.id, 'h5').catch(() => {});

  if (app.openType === 'external' && app.externalUrl) {
    uni.showModal({ title: '外部链接', content: app.externalUrl });
    return;
  }

  const role = getStoredRole();
  const path = app.routePath || '';
  let url = '';
  switch (path) {
    case '/lab/reservation':
      url = role === 'teacher'
        ? '/pages/teacher-reservation-apply/teacher-reservation-apply'
        : '/pages/student-reservation-apply/student-reservation-apply';
      break;
    case '/portal/apps':
      url = '/pages/app-center/app-center';
      break;
    case '/notices':
      url = '/pages/notice-list/notice-list';
      break;
    case '/news':
      url = '/pages/news-list/news-list';
      break;
    case '/tasks':
      url = '/pages/task-center/task-center';
      break;
    case '/messages':
      url = '/pages/message-center/message-center';
      break;
    case '/calendar':
      url = '/pages/calendar/calendar';
      break;
    case '/ai/assistant':
      url = '/pages/ai-assistant/ai-assistant';
      break;
    case '/admin/dashboard':
      url = '/pages/admin-workbench/admin-workbench';
      break;
    case '/system/users':
      url = '/pages/admin-system-management/admin-system-management';
      break;
    case '/admin/apps':
      url = '/pages/admin-app-manage/admin-app-manage';
      break;
    case '/schedule':
      url = role === 'teacher'
        ? '/pages/teacher-schedule-preview/teacher-schedule-preview'
        : '/pages/student-schedule-preview/student-schedule-preview';
      break;
    default:
      url = '';
  }

  if (url) {
    uni.navigateTo({ url });
  } else {
    uni.showToast({ title: '功能建设中', icon: 'none' });
  }
};

// ====== 快捷入口 / 区块导航 ======
const goReservation = () => {
  const role = getStoredRole();
  uni.navigateTo({
    url: role === 'teacher'
      ? '/pages/teacher-reservation-apply/teacher-reservation-apply'
      : '/pages/student-reservation-apply/student-reservation-apply'
  });
};
const goAppCenter = () => uni.navigateTo({ url: '/pages/app-center/app-center' });
const goNotices = () => uni.navigateTo({ url: '/pages/notice-list/notice-list' });
const goCalendar = () => uni.navigateTo({ url: '/pages/calendar/calendar' });
const goTasks = () => uni.navigateTo({ url: '/pages/task-center/task-center' });
const goNews = () => uni.navigateTo({ url: '/pages/news-list/news-list' });
const goMessages = () => uni.navigateTo({ url: '/pages/message-center/message-center' });
const goAi = () => uni.navigateTo({ url: '/pages/ai-assistant/ai-assistant' });

const openNotice = (n: any) => {
  if (!n?.id) return;
  uni.navigateTo({ url: '/pages/notice-detail/notice-detail?id=' + n.id });
};
const openNews = (item: any) => {
  if (!item?.id) return;
  uni.navigateTo({ url: '/pages/news-detail/news-detail?id=' + item.id });
};

onLoad(() => {
  user.value = getStoredUser() || {};
  loadHome();
});

onShow(() => {
  loadHome();
});

onPullDownRefresh(async () => {
  await loadHome();
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './portal-home.less';</style>
