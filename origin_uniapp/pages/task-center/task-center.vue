<template>
  <view class="page">
    <view class="hd">
      <view class="hd-back" @tap="goBack">
        <text class="hd-back-ico">‹</text>
        <text class="hd-back-txt">返回</text>
      </view>
      <text class="hd-title">任务中心</text>
    </view>

    <view class="body">
      <!-- 统计卡 -->
      <view class="stats">
        <view class="stat-card" @tap="switchTab('todo')">
          <text class="stat-num">{{ stats.todo || 0 }}</text>
          <text class="stat-label">待办</text>
        </view>
        <view class="stat-card" @tap="switchTab('done')">
          <text class="stat-num">{{ stats.done || 0 }}</text>
          <text class="stat-label">已办</text>
        </view>
        <view class="stat-card" @tap="switchTab('mine')">
          <text class="stat-num">{{ stats.mine || 0 }}</text>
          <text class="stat-label">我发起的</text>
        </view>
      </view>

      <!-- tabs -->
      <view class="tabs">
        <view
          v-for="t in tabs"
          :key="t.key"
          :class="['tab', activeTab === t.key ? 'tab-active' : '']"
          @tap="switchTab(t.key)"
        >
          <text>{{ t.name }}</text>
        </view>
      </view>

      <!-- loading -->
      <view v-if="loading" class="tip">加载中...</view>

      <!-- empty -->
      <view v-else-if="!list.length" class="tip">暂无数据</view>

      <!-- list -->
      <view v-else class="list">
        <view
          class="task-card"
          v-for="item in list"
          :key="item.id"
          @tap="openDetail(item)"
        >
          <view class="task-top">
            <text class="task-title">{{ item.title || item.taskTypeName || '审批任务' }}</text>
            <text :class="['badge', statusClass(item.status)]">{{ item.statusText || item.status }}</text>
          </view>

          <view class="task-row">
            <text class="task-label">申请人</text>
            <text class="task-value">{{ item.applicantName || item.applicant || '-' }}</text>
          </view>
          <view class="task-row">
            <text class="task-label">实验室</text>
            <text class="task-value">{{ item.labName || '-' }}</text>
          </view>
          <view class="task-row">
            <text class="task-label">时间</text>
            <text class="task-value">{{ item.date || '-' }} {{ item.timeSlot || '' }}</text>
          </view>

          <view
            v-if="activeTab === 'todo' && canApprove"
            class="task-actions"
            @tap.stop="noop"
          >
            <button class="act-btn reject" @tap.stop="onReject(item)">驳回</button>
            <button class="act-btn approve" @tap.stop="onApprove(item)">通过</button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import {
  getTaskStats,
  getTasksTodo,
  getTasksDone,
  getTasksMine,
  getTaskDetail,
  teacherApproveTask,
  teacherRejectTask,
  adminApproveTask,
  adminRejectTask,
} from '@/api/portal';
import { getStoredRole } from '@/api/storage';

const goBack = () => uni.navigateBack({ delta: 1 });

const role = ref(getStoredRole() || '');
const loading = ref(false);
const stats = ref<any>({ todo: 0, done: 0, mine: 0, taskTypes: [] });
const list = ref<any[]>([]);
const activeTab = ref<'todo' | 'done' | 'mine'>('todo');

const tabs = [
  { key: 'todo', name: '待办' },
  { key: 'done', name: '已办' },
  { key: 'mine', name: '我发起的' },
] as const;

const canApprove = computed(() => role.value === 'teacher' || role.value === 'admin');

function statusClass(status: string) {
  const s = (status || '').toLowerCase();
  if (s.indexOf('pending') > -1 || s.indexOf('wait') > -1) return 'pending';
  if (s.indexOf('approve') > -1 || s.indexOf('pass') > -1 || s.indexOf('success') > -1 || s.indexOf('done') > -1) return 'success';
  if (s.indexOf('reject') > -1 || s.indexOf('fail') > -1) return 'error';
  if (s.indexOf('cancel') > -1) return 'cancel';
  return 'info';
}

function noop() {}

async function loadStats() {
  try {
    const res = await getTaskStats();
    stats.value = res || { todo: 0, done: 0, mine: 0, taskTypes: [] };
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  }
}

async function loadList() {
  loading.value = true;
  try {
    let res: any[] = [];
    if (activeTab.value === 'todo') res = await getTasksTodo();
    else if (activeTab.value === 'done') res = await getTasksDone();
    else res = await getTasksMine();
    list.value = Array.isArray(res) ? res : [];
  } catch (e: any) {
    list.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
}

async function refresh() {
  await Promise.all([loadStats(), loadList()]);
}

function switchTab(key: 'todo' | 'done' | 'mine') {
  if (activeTab.value === key) return;
  activeTab.value = key;
  loadList();
}

async function onApprove(item: any) {
  if (!item?.id) return;
  try {
    if (role.value === 'teacher') await teacherApproveTask(item.id, '');
    else await adminApproveTask(item.id, '');
    uni.showToast({ title: '已通过', icon: 'none' });
    await refresh();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
  }
}

function onReject(item: any) {
  if (!item?.id) return;
  uni.showModal({
    title: '驳回',
    editable: true,
    placeholderText: '请输入驳回意见',
    content: '',
    success: async (res) => {
      if (!res.confirm) return;
      const comment = (res.content || '').trim();
      try {
        if (role.value === 'teacher') await teacherRejectTask(item.id, comment);
        else await adminRejectTask(item.id, comment);
        uni.showToast({ title: '已驳回', icon: 'none' });
        await refresh();
      } catch (e: any) {
        uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
      }
    },
  });
}

async function openDetail(item: any) {
  if (!item?.id) return;
  try {
    const detail: any = await getTaskDetail(item.id);
    const d = detail || item;
    const content =
      '标题：' + (d?.title || d?.taskTypeName || '审批任务') + '\n' +
      '申请人：' + (d?.applicantName || d?.applicant || '-') + '\n' +
      '实验室：' + (d?.labName || '-') + '\n' +
      '时间：' + (d?.date || '-') + ' ' + (d?.timeSlot || '') + '\n' +
      '状态：' + (d?.statusText || d?.status || '-');
    uni.showModal({ title: '任务详情', content, showCancel: false });
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  }
}

onLoad(() => {
  role.value = getStoredRole() || '';
  refresh();
});

onShow(() => {
  refresh();
});

onPullDownRefresh(async () => {
  await refresh();
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './task-center.less';</style>
