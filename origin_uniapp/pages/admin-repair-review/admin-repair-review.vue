<template>
  <view class="page-wrapper">
    <navigation-bar title="报修审核" :back="true" color="white" background="#F5A623"></navigation-bar>
    <view class="container">
      <view class="tabs">
        <view
          v-for="tab in tabs"
          :key="tab.value"
          :class="['tab', statusFilter === tab.value ? 'active' : '']"
          @tap="changeStatus(tab.value)"
        >
          {{ tab.label }}
        </view>
      </view>

      <view v-if="loading" class="empty">加载中...</view>
      <view v-else-if="!repairs.length" class="empty">暂无报修申请</view>
      <view v-else class="repair-list">
        <view class="repair-card" v-for="item in repairs" :key="item.id" @tap="openDetail(item)">
          <view class="card-head">
            <text class="repair-no">{{ item.repairNo }}</text>
            <text :class="['status', 'status-' + item.status]">{{ item.statusText }}</text>
          </view>
          <text class="title">{{ item.title }}</text>
          <view class="meta">
            <text>{{ item.applicantName }}（{{ roleText(item.applicantRole) }}）</text>
            <text>{{ item.labName }}</text>
            <text>{{ item.faultTypeText }}</text>
            <text>{{ item.urgencyText }}</text>
          </view>
          <text class="desc">{{ item.description }}</text>
          <view class="card-actions" v-if="item.status === 'pending'" @tap.stop>
            <button class="action reject" @tap="showReject(item)">驳回</button>
            <button class="action approve" @tap="approve(item)">通过</button>
          </view>
        </view>
      </view>
    </view>

    <view v-if="detailVisible" class="mask" @tap="closeDetail">
      <view class="dialog" @tap.stop>
        <view class="dialog-hd">
          <text class="dialog-title">报修详情</text>
          <text class="dialog-close" @tap="closeDetail">×</text>
        </view>
        <view class="detail-row"><text class="label">编号</text><text>{{ current.repairNo }}</text></view>
        <view class="detail-row"><text class="label">申请人</text><text>{{ current.applicantName }}（{{ roleText(current.applicantRole) }}）</text></view>
        <view class="detail-row"><text class="label">联系方式</text><text>{{ current.contactPhone || current.applicantPhone || '-' }}</text></view>
        <view class="detail-row"><text class="label">实验室</text><text>{{ current.labName }} {{ current.labLocation || '' }}</text></view>
        <view class="detail-row"><text class="label">故障类型</text><text>{{ current.faultTypeText }} · {{ current.urgencyText }}</text></view>
        <view class="detail-block"><text class="label">标题</text><text>{{ current.title }}</text></view>
        <view class="detail-block"><text class="label">故障描述</text><text>{{ current.description }}</text></view>
        <view class="detail-row"><text class="label">期望时间</text><text>{{ current.preferredTime || '-' }}</text></view>
        <view class="detail-row"><text class="label">提交时间</text><text>{{ current.submittedAt || '-' }}</text></view>
        <view v-if="current.adminComment || current.rejectReason" class="detail-block">
          <text class="label">审核意见</text>
          <text>{{ current.adminComment || current.rejectReason }}</text>
        </view>
        <view v-if="current.status === 'pending'" class="dialog-actions">
          <button class="action reject" @tap="showReject(current)">驳回</button>
          <button class="action approve" @tap="approve(current)">通过</button>
        </view>
      </view>
    </view>

    <view v-if="rejectVisible" class="mask" @tap="rejectVisible = false">
      <view class="dialog small" @tap.stop>
        <view class="dialog-hd">
          <text class="dialog-title">驳回原因</text>
          <text class="dialog-close" @tap="rejectVisible = false">×</text>
        </view>
        <textarea class="reason-input" v-model="rejectReason" placeholder="请输入驳回原因" placeholder-class="ph" />
        <button class="submit-reject" @tap="confirmReject">确认驳回</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
import { getAdminRepairs, getAdminRepairDetail, approveRepair, rejectRepair } from '@/api/repair';

const tabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已驳回', value: 'rejected' },
  { label: '已取消', value: 'cancelled' }
];

const statusFilter = ref('');
const loading = ref(false);
const repairs = ref<any[]>([]);
const detailVisible = ref(false);
const current = ref<any>({});
const rejectVisible = ref(false);
const rejectReason = ref('');
const rejectTarget = ref<any>(null);

const roleText = (role: any) => role === 'teacher' ? '教师' : '学生';

const loadRepairs = async () => {
  loading.value = true;
  try {
    const list = await getAdminRepairs(statusFilter.value);
    repairs.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    repairs.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const changeStatus = (value: string) => {
  statusFilter.value = value;
  loadRepairs();
};

const openDetail = async (item: any) => {
  try {
    current.value = await getAdminRepairDetail(item.id);
  } catch (e) {
    current.value = item;
  }
  detailVisible.value = true;
};

const closeDetail = () => {
  detailVisible.value = false;
};

const approve = (item: any) => {
  uni.showModal({
    title: '通过报修',
    content: `确定通过「${item.title}」吗？`,
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await approveRepair(item.id, '已通过报修申请，请等待后续维修安排。');
        uni.showToast({ title: '已通过', icon: 'success' });
        detailVisible.value = false;
        loadRepairs();
      } catch (e: any) {
        uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
      }
    }
  });
};

const showReject = (item: any) => {
  rejectTarget.value = item;
  rejectReason.value = '';
  rejectVisible.value = true;
};

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    uni.showToast({ title: '请输入驳回原因', icon: 'none' });
    return;
  }
  try {
    await rejectRepair(rejectTarget.value.id, rejectReason.value.trim());
    uni.showToast({ title: '已驳回', icon: 'none' });
    rejectVisible.value = false;
    detailVisible.value = false;
    loadRepairs();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
  }
};

onLoad(loadRepairs);
onShow(loadRepairs);
onPullDownRefresh(async () => {
  await loadRepairs();
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './admin-repair-review.less';</style>
