<template>
  <view class="page">
    <!-- 顶部问候（白底，深色文字，避开状态栏与右上角胶囊）-->
    <view class="top" :style="{ paddingTop: statusBarH + 'px' }">
      <view class="top-nav">
        <text class="greet">{{ greeting }}，<text class="greet-name">{{ userName }}</text></text>
      </view>
      <view class="top-sub">
        <text class="sub-meta">{{ todayLabel }} · {{ roleName }}</text>
        <view class="sub-acts">
          <view class="chip" hover-class="tap" @tap="goMessages">
            <text class="chip-t">消息</text>
            <view v-if="unreadMessages > 0" class="chip-dot">{{ unreadMessages > 99 ? '99+' : unreadMessages }}</view>
          </view>
          <view class="chip" hover-class="tap" @tap="logout">
            <text class="chip-t">退出</text>
          </view>
        </view>
      </view>
    </view>

    <view class="body">
      <!-- 加载中 -->
      <view v-if="loading" class="loading">加载中...</view>

      <block v-else>
        <!-- 快捷入口行 -->
        <view class="quick-row">
          <view class="quick-item q-lab" hover-class="tap-press" @tap="goReservation">
            <view class="quick-disc"><text class="quick-emoji">🔬</text></view>
            <text class="quick-text">实验室预约</text>
          </view>
          <view class="quick-item q-app" hover-class="tap-press" @tap="goAppCenter">
            <view class="quick-disc"><text class="quick-emoji">📱</text></view>
            <text class="quick-text">应用中心</text>
          </view>
          <view class="quick-item q-notice" hover-class="tap-press" @tap="goNotices">
            <view class="quick-disc"><text class="quick-emoji">📢</text></view>
            <text class="quick-text">通知公告</text>
          </view>
          <view class="quick-item q-cal" hover-class="tap-press" @tap="goCalendar">
            <view class="quick-disc"><text class="quick-emoji">📅</text></view>
            <text class="quick-text">我的日历</text>
          </view>
          <view v-if="showTaskEntry" class="quick-item q-task" hover-class="tap-press" @tap="goTasks">
            <view class="quick-disc">
              <text class="quick-emoji">✅</text>
              <view v-if="pendingTaskCount > 0" class="quick-badge">{{ pendingTaskCount > 99 ? '99+' : pendingTaskCount }}</view>
            </view>
            <text class="quick-text">任务中心</text>
          </view>
        </view>

        <!-- 我的应用 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">我的应用</text>
          </view>
          <view v-if="(myApps?.length || 0) > 0" class="app-grid">
            <view class="app-cell" hover-class="tap-press" v-for="app in myApps" :key="app.id" @tap="openApp(app)">
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
            <view class="app-cell" hover-class="tap-press" v-for="app in latestApps" :key="app.id" @tap="openApp(app)">
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
            <view class="app-cell" hover-class="tap-press" v-for="app in hotApps" :key="app.id" @tap="openApp(app)">
              <view class="app-icon">{{ appGlyph(app) }}</view>
              <text class="app-name">{{ app.appName }}</text>
            </view>
          </view>
        </view>

        <!-- 通知公告 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">通知公告</text>
            <text class="section-more" hover-class="tap" @tap="goNotices">更多</text>
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
            <text class="section-more" hover-class="tap" @tap="goNews">更多</text>
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
    <view class="fab-ai" hover-class="tap" @tap="goAi"><text class="fab-ai-text">AI</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { getPortalHome, visitApp } from '@/api/portal';
import { logout as logoutAuth } from '@/api/auth';
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

// 自定义导航（navigationStyle: custom）：用状态栏高度把内容压到状态栏之下，
// 导航行 padding-right 给右上角胶囊留白，不与之重叠。
const statusBarH = ref<number>(20);
try {
  statusBarH.value = uni.getSystemInfoSync().statusBarHeight || 20;
} catch (e) { /* ignore */ }

const ROLE_TEXT: Record<string, string> = { student: '学生', teacher: '教师', admin: '管理员' };

const userName = computed(() => user.value?.name || user.value?.realName || '同学');
const roleName = computed(() => ROLE_TEXT[user.value?.role || getStoredRole() || ''] || '用户');

// ====== 顶部「今日概览」面板数据 ======
const isStaff = computed(() => {
  const r = user.value?.role || getStoredRole();
  return r === 'teacher' || r === 'admin';
});
const eventCount = computed(() => (upcomingEvents.value || []).length);
const greeting = computed(() => {
  const h = new Date().getHours();
  if (h < 6) return '夜深了';
  if (h < 11) return '上午好';
  if (h < 13) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
});
const WEEK = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
const todayLabel = computed(() => {
  const d = new Date();
  return `${d.getMonth() + 1}月${d.getDate()}日 ${WEEK[d.getDay()]}`;
});

const topNotices = computed(() => (notices.value || []).slice(0, 5));
const topNews = computed(() => (news.value || []).slice(0, 5));
const showTaskEntry = computed(() => {
  const r = user.value?.role || getStoredRole();
  return (r === 'teacher' || r === 'admin') && pendingTaskCount.value > 0;
});

const appGlyph = (app: any): string => {
  const icon = (app?.icon || '').trim();
  const chars = [...icon];
  // 仅当 icon 是单个字符或纯 emoji（无英文字母）时直接展示；
  // 形如 "calendar"、"news"、"ai" 等图标名或 URL 一律回退到应用名首字，避免撑破图标框
  const isGlyph = chars.length === 1 || (chars.length <= 2 && !/[a-zA-Z]/.test(icon));
  if (icon && isGlyph) return icon;
  const name = (app?.appName || '').trim();
  return name ? name.charAt(0) : '应';
};

const fmtTime = (t: any): string => {
  if (!t) return '';
  return String(t).replace('T', ' ').slice(0, 16);
};

const getReservationManagementUrl = () => {
  const role = getStoredRole();
  if (role === 'teacher') return '/pages/teacher-work/teacher-work';
  if (role === 'student') return '/pages/student-work/student-work';
  return '/pages/admin-work/admin-work';
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
      url = getReservationManagementUrl();
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
    case '/repair':
      url = role === 'admin'
        ? '/pages/admin-repair-review/admin-repair-review'
        : '/pages/repair-service/repair-service';
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
  uni.navigateTo({
    url: getReservationManagementUrl()
  });
};
const goAppCenter = () => uni.navigateTo({ url: '/pages/app-center/app-center' });
const goNotices = () => uni.navigateTo({ url: '/pages/notice-list/notice-list' });
const goCalendar = () => uni.navigateTo({ url: '/pages/calendar/calendar' });
const goTasks = () => uni.navigateTo({ url: '/pages/task-center/task-center' });
const goNews = () => uni.navigateTo({ url: '/pages/news-list/news-list' });
const goMessages = () => uni.navigateTo({ url: '/pages/message-center/message-center' });
const goAi = () => uni.navigateTo({ url: '/pages/ai-assistant/ai-assistant' });

const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (!res.confirm) return;
      logoutAuth();
      uni.showToast({ title: '已退出登录', icon: 'success' });
      uni.reLaunch({ url: '/pages/login-select/login-select' });
    }
  });
};

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
  // 兜底：无论 loadHome 成功、失败还是卡住，最多 3 秒后一定收回刷新，避免卡死
  const timer = setTimeout(() => uni.stopPullDownRefresh(), 3000);
  try {
    await loadHome();
  } finally {
    clearTimeout(timer);
    uni.stopPullDownRefresh();
  }
});
</script>

<style lang="less">@import './portal-home.less';</style>
