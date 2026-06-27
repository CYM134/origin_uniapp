<template>
  <view class="page">
    <!-- 顶部 accent 渐变 header -->
    <view class="hd">
      <text class="hd-title">综合服务工作台</text>
      <view class="hd-logout" @tap="logout">
        <text class="hd-logout-text">退出</text>
      </view>
    </view>

    <view class="body">
      <!-- 加载中 -->
      <view v-if="loading" class="loading">加载中...</view>

      <block v-else>
        <!-- 欢迎卡 -->
        <view class="welcome">
          <view class="welcome-avatar">{{ adminInitial }}</view>
          <view class="welcome-info">
            <text class="welcome-name">{{ adminName }}</text>
            <text class="welcome-tip">{{ todayTip }}</text>
          </view>
        </view>

        <!-- 1) 数字概览 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">数据概览</text>
          </view>
          <view class="stat-grid">
            <view class="stat-cell" v-for="(s, idx) in statCells" :key="idx">
              <text class="stat-num">{{ s.value }}</text>
              <text class="stat-label">{{ s.label }}</text>
            </view>
          </view>
          <!-- 实验室使用率进度条 -->
          <view class="usage">
            <view class="usage-top">
              <text class="usage-label">实验室使用率</text>
              <text class="usage-val">{{ labUsageRate }}%</text>
            </view>
            <view class="usage-track">
              <view class="usage-fill" :style="{ width: labUsageRate + '%' }"></view>
            </view>
          </view>
        </view>

        <!-- 2) 状态分布横向条形图 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">预约状态分布</text>
          </view>
          <view v-if="(statusBars?.length || 0) > 0" class="bars">
            <view class="bar-row" v-for="(b, idx) in statusBars" :key="idx">
              <text class="bar-name">{{ b.label }}</text>
              <view class="bar-track">
                <view class="bar-fill" :class="'bar-' + b.key" :style="{ width: b.pct + '%' }"></view>
              </view>
              <text class="bar-count">{{ b.count }}</text>
            </view>
          </view>
          <view v-else class="empty">暂无数据</view>
        </view>

        <!-- 3) 应用中心入口宫格 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">管理入口</text>
          </view>
          <view class="entry-grid">
            <view class="entry-cell" :class="{ 'entry-alert': en.alert }" v-for="(en, idx) in entries" :key="idx" @tap="goEntry(en.url)">
              <view class="entry-icon-wrap">
                <text class="entry-emoji">{{ en.emoji }}</text>
                <view v-if="en.badge" class="entry-badge">{{ en.badge > 99 ? '99+' : en.badge }}</view>
              </view>
              <text class="entry-text">{{ en.text }}</text>
            </view>
          </view>
        </view>

        <!-- 4) 通知中心 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">通知中心</text>
            <text class="section-more" @tap="goNoticeManage">更多</text>
          </view>
          <view v-if="(recentNotices?.length || 0) > 0" class="list">
            <view class="row-item" v-for="n in recentNotices" :key="n.id">
              <text class="row-title">{{ n.title }}</text>
              <text class="row-time">{{ fmtTime(n.publishTime || n.createTime) }}</text>
            </view>
          </view>
          <view v-else class="empty">暂无数据</view>
        </view>

        <!-- 5) 资讯中心 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">资讯中心</text>
            <text class="section-more" @tap="goNewsManage">更多</text>
          </view>
          <view v-if="(recentNews?.length || 0) > 0" class="list">
            <view class="row-item" v-for="item in recentNews" :key="item.id">
              <text class="row-title">{{ item.title }}</text>
              <text class="row-time">{{ fmtTime(item.publishTime || item.createTime) }}</text>
            </view>
          </view>
          <view v-else class="empty">暂无数据</view>
        </view>

        <!-- 6) 最近预约 -->
        <view class="section">
          <view class="section-hd">
            <text class="section-title">最近预约</text>
          </view>
          <view v-if="(recentReservations?.length || 0) > 0" class="table">
            <view class="tr th">
              <text class="td td-user">申请人</text>
              <text class="td td-lab">实验室</text>
              <text class="td td-date">日期</text>
              <text class="td td-status">状态</text>
            </view>
            <view class="tr" v-for="(r, idx) in recentReservations" :key="idx">
              <text class="td td-user">{{ r.applicantName || r.userName || r.realName || '-' }}</text>
              <text class="td td-lab">{{ r.labName || '-' }}</text>
              <text class="td td-date">{{ fmtDate(r.reserveDate || r.date) }}</text>
              <view class="td td-status">
                <text class="status-tag" :class="'st-' + (r.status || 'pending')">{{ statusText(r.status) }}</text>
              </view>
            </view>
          </view>
          <view v-else class="empty">暂无数据</view>
        </view>
      </block>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { getAdminPortalDashboard } from '@/api/portal';
import { logout as logoutAuth } from '@/api/auth';
import { getStoredUser } from '@/api/storage';

const loading = ref(false);
const user = ref<any>({});
const summary = ref<any>({});
const taskStats = ref<any>({});
const statusDistribution = ref<any[]>([]);
const recentNotices = ref<any[]>([]);
const recentNews = ref<any[]>([]);
const recentReservations = ref<any[]>([]);

const STATUS_TEXT: Record<string, string> = {
  pending: '待审核',
  teacher_approved: '待终审',
  approved: '已通过',
  rejected: '已驳回',
  cancelled: '已取消',
  completed: '已完成'
};

const adminName = computed(() => user.value?.realName || user.value?.name || '管理员');
const adminInitial = computed(() => {
  const n = adminName.value || '管';
  return String(n).charAt(0);
});
const todayTip = computed(() => {
  const todo = Number(summary.value?.pendingReservations) || 0;
  if (todo > 0) return `今日有 ${todo} 条预约待处理`;
  return '今日暂无待处理预约，状态良好';
});

const labUsageRate = computed(() => {
  const v = Number(summary.value?.labUsageRate);
  if (!v || isNaN(v)) return 0;
  return Math.max(0, Math.min(100, Math.round(v)));
});

const statCells = computed(() => {
  const s = summary.value || {};
  return [
    { label: '待办预约', value: Number(s.pendingReservations) || 0 },
    { label: '待教师初审', value: Number(s.pendingTeacherReviews) || 0 },
    { label: '待审报修', value: Number(s.pendingRepairs) || 0 },
    { label: '今日预约', value: Number(s.todayReservations) || 0 },
    { label: '本周预约', value: Number(s.weekReservations) || 0 },
    { label: '实验室', value: Number(s.totalLabs) || 0 },
    { label: '用户', value: Number(s.totalUsers) || 0 },
    { label: '应用', value: Number(s.totalApps) || 0 },
    { label: '已发布公告', value: Number(s.publishedNotices) || 0 }
  ];
});

const statusBars = computed(() => {
  const list = statusDistribution.value || [];
  const total = list.reduce((sum: number, it: any) => sum + (Number(it?.count) || 0), 0);
  return list.map((it: any) => {
    const count = Number(it?.count) || 0;
    const key = it?.status || 'pending';
    return {
      key,
      label: STATUS_TEXT[key] || key,
      count,
      pct: total > 0 ? Math.round((count / total) * 100) : 0
    };
  });
});

const entries = computed(() => {
  const s = summary.value || {};
  const pendingReservations = Number(s.pendingReservations) || 0;
  const pendingTeacherReviews = Number(s.pendingTeacherReviews) || 0;
  const pendingReservationApprovals = pendingReservations + pendingTeacherReviews;
  const pendingTeacherRegistrations = Number(s.pendingTeacherRegistrations) || 0;
  const pendingRepairs = Number(s.pendingRepairs) || 0;
  return [
    { emoji: '📱', text: '应用管理', url: '/pages/admin-app-manage/admin-app-manage' },
    { emoji: '📢', text: '通知管理', url: '/pages/admin-notice-manage/admin-notice-manage' },
    { emoji: '📰', text: '资讯管理', url: '/pages/admin-news-manage/admin-news-manage' },
    {
      emoji: '🔬',
      text: '实验室管理',
      url: '/pages/admin-work/admin-work',
      badge: pendingReservationApprovals,
      alert: pendingReservationApprovals > 0
    },
    { emoji: '📅', text: '课表管理', url: '/pages/admin-schedule-management/admin-schedule-management' },
    {
      emoji: '🛠',
      text: '报修审核',
      url: '/pages/admin-repair-review/admin-repair-review',
      badge: pendingRepairs,
      alert: pendingRepairs > 0
    },
    {
      emoji: '👨‍🏫',
      text: '注册审核',
      url: '/pages/admin-teacher-registration/admin-teacher-registration',
      badge: pendingTeacherRegistrations,
      alert: pendingTeacherRegistrations > 0
    },
    { emoji: '👥', text: '用户管理', url: '/pages/admin-system-management/admin-system-management' }
  ];
});

const statusText = (st: any): string => STATUS_TEXT[st] || STATUS_TEXT.pending;

const fmtTime = (t: any): string => {
  if (!t) return '';
  return String(t).replace('T', ' ').slice(0, 16);
};
const fmtDate = (t: any): string => {
  if (!t) return '-';
  return String(t).replace('T', ' ').slice(0, 10);
};

const loadDashboard = async () => {
  loading.value = true;
  try {
    const data: any = await getAdminPortalDashboard();
    summary.value = data?.summary || {};
    taskStats.value = data?.taskStats || {};
    statusDistribution.value = data?.statusDistribution || [];
    recentNotices.value = (data?.recentNotices || []).slice(0, 5);
    recentNews.value = (data?.recentNews || []).slice(0, 5);
    recentReservations.value = (data?.recentReservations || []).slice(0, 8);
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const goEntry = (url: string) => {
  if (!url) {
    uni.showToast({ title: '功能建设中', icon: 'none' });
    return;
  }
  uni.navigateTo({ url });
};
const goNoticeManage = () => uni.navigateTo({ url: '/pages/admin-notice-manage/admin-notice-manage' });
const goNewsManage = () => uni.navigateTo({ url: '/pages/admin-news-manage/admin-news-manage' });

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

onLoad(() => {
  user.value = getStoredUser() || {};
  loadDashboard();
});

onShow(() => {
  loadDashboard();
});

onPullDownRefresh(async () => {
  await loadDashboard();
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './admin-workbench.less';</style>
