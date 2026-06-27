<template>
  <view class="page-wrapper">
    <app-navigation-bar title="报修服务" :back="true" color="white" :background="themeColor"></app-navigation-bar>

    <view class="container">
      <view class="tabs">
        <view :class="['tab', activeTab === 'form' ? 'active' : '']" @tap="activeTab = 'form'">提交报修</view>
        <view :class="['tab', activeTab === 'mine' ? 'active' : '']" @tap="activeTab = 'mine'">我的报修</view>
      </view>

      <view v-if="activeTab === 'form'" class="form-section">
        <view class="section-title">
          <image src="/static/images/icons/warning.svg" mode="aspectFit"></image>
          <text>报修信息</text>
        </view>

        <view class="form-item">
          <text class="label">报修实验室 <text class="required">*</text></text>
          <picker mode="selector" :range="labNames" :value="labIndex" @change="onLabChange">
            <view class="picker-field">{{ labNames[labIndex] || '请选择实验室' }}</view>
          </picker>
        </view>

        <view class="form-row">
          <view class="form-item row-item">
            <text class="label">故障类型 <text class="required">*</text></text>
            <picker mode="selector" :range="faultTypeNames" :value="faultTypeIndex" @change="onFaultTypeChange">
              <view class="picker-field">{{ faultTypeNames[faultTypeIndex] }}</view>
            </picker>
          </view>
          <view class="form-item row-item">
            <text class="label">紧急程度</text>
            <picker mode="selector" :range="urgencyNames" :value="urgencyIndex" @change="onUrgencyChange">
              <view class="picker-field">{{ urgencyNames[urgencyIndex] }}</view>
            </picker>
          </view>
        </view>

        <view class="form-item">
          <text class="label">报修标题 <text class="required">*</text></text>
          <input class="text-input" v-model="form.title" placeholder="例如：投影仪无法开机" placeholder-class="ph" />
        </view>

        <view class="form-item">
          <text class="label">故障描述 <text class="required">*</text></text>
          <textarea class="textarea-input" v-model="form.description" maxlength="500" placeholder="请描述故障现象、位置、影响范围等" placeholder-class="ph" />
          <view class="char-count">{{ form.description.length }}/500</view>
        </view>

        <view class="form-row">
          <view class="form-item row-item">
            <text class="label">联系人</text>
            <input class="text-input" v-model="form.contactName" placeholder="默认本人" placeholder-class="ph" />
          </view>
          <view class="form-item row-item">
            <text class="label">联系电话</text>
            <input class="text-input" v-model="form.contactPhone" placeholder="便于维修人员联系" placeholder-class="ph" />
          </view>
        </view>

        <view class="form-item">
          <text class="label">期望处理时间</text>
          <input class="text-input" v-model="form.preferredTime" placeholder="例如：今天下午、课前、工作日均可" placeholder-class="ph" />
        </view>

        <button class="submit-btn" :disabled="submitting" @tap="submitRepair">
          {{ submitting ? '提交中...' : '提交报修' }}
        </button>
      </view>

      <view v-else class="list-section">
        <view v-if="loading" class="empty">加载中...</view>
        <view v-else-if="!repairs.length" class="empty">暂无报修记录</view>
        <view v-else class="repair-card" v-for="item in repairs" :key="item.id">
          <view class="card-head">
            <text class="repair-no">{{ item.repairNo }}</text>
            <text :class="['status', 'status-' + item.status]">{{ item.statusText }}</text>
          </view>
          <text class="card-title">{{ item.title }}</text>
          <view class="meta">
            <text>{{ item.labName }}</text>
            <text>{{ item.faultTypeText }}</text>
            <text>{{ item.urgencyText }}</text>
          </view>
          <text class="desc">{{ item.description }}</text>
          <view class="foot">
            <text>{{ item.submittedAt }}</text>
            <view v-if="item.status === 'pending'" class="cancel-btn" @tap="cancel(item)">取消</view>
          </view>
          <view v-if="item.adminComment || item.rejectReason" class="review">
            <text>{{ item.adminComment || item.rejectReason }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
import { getStoredRole, getStoredUser } from '@/api/storage';
import { getLabs, createRepair, getMyRepairs, cancelRepair } from '@/api/repair';

const role = ref(getStoredRole() || 'student');
const themeColor = computed(() => role.value === 'teacher' ? '#10B981' : '#3B82F6');
const activeTab = ref<'form' | 'mine'>('form');
const labs = ref<any[]>([]);
const labIndex = ref(0);
const submitting = ref(false);
const loading = ref(false);
const repairs = ref<any[]>([]);

const faultTypes = [
  { label: '设备故障', value: 'equipment' },
  { label: '网络故障', value: 'network' },
  { label: '环境问题', value: 'environment' },
  { label: '安全隐患', value: 'safety' },
  { label: '其他', value: 'other' }
];
const urgencyOptions = [
  { label: '普通', value: 'normal' },
  { label: '紧急', value: 'urgent' },
  { label: '非常紧急', value: 'critical' }
];

const faultTypeIndex = ref(0);
const urgencyIndex = ref(0);
const faultTypeNames = computed(() => faultTypes.map((it) => it.label));
const urgencyNames = computed(() => urgencyOptions.map((it) => it.label));
const labNames = computed(() => labs.value.map((it) => `${it.name}（${it.location || '-'}）`));

const form = ref({
  title: '',
  description: '',
  contactName: '',
  contactPhone: '',
  preferredTime: ''
});

const loadLabs = async () => {
  try {
    const list = await getLabs();
    labs.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '实验室加载失败', icon: 'none' });
  }
};

const loadRepairs = async () => {
  loading.value = true;
  try {
    const list = await getMyRepairs();
    repairs.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    repairs.value = [];
    uni.showToast({ title: e?.data?.message || '报修记录加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const onLabChange = (e: any) => { labIndex.value = Number(e.detail.value) || 0; };
const onFaultTypeChange = (e: any) => { faultTypeIndex.value = Number(e.detail.value) || 0; };
const onUrgencyChange = (e: any) => { urgencyIndex.value = Number(e.detail.value) || 0; };

const resetForm = () => {
  const user: any = getStoredUser() || {};
  form.value = {
    title: '',
    description: '',
    contactName: user.realName || user.name || '',
    contactPhone: user.phone || '',
    preferredTime: ''
  };
  faultTypeIndex.value = 0;
  urgencyIndex.value = 0;
};

const submitRepair = async () => {
  if (submitting.value) return;
  const lab = labs.value[labIndex.value];
  if (!lab?.id) {
    uni.showToast({ title: '请选择实验室', icon: 'none' });
    return;
  }
  if (!form.value.title.trim() || !form.value.description.trim()) {
    uni.showToast({ title: '请填写标题和故障描述', icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    await createRepair({
      labId: lab.id,
      faultType: faultTypes[faultTypeIndex.value].value,
      urgency: urgencyOptions[urgencyIndex.value].value,
      ...form.value
    });
    uni.showToast({ title: '报修已提交', icon: 'success' });
    resetForm();
    activeTab.value = 'mine';
    await loadRepairs();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '提交失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
};

const cancel = (item: any) => {
  uni.showModal({
    title: '取消报修',
    content: `确定取消「${item.title}」吗？`,
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await cancelRepair(item.id);
        uni.showToast({ title: '已取消', icon: 'none' });
        loadRepairs();
      } catch (e: any) {
        uni.showToast({ title: e?.data?.message || '取消失败', icon: 'none' });
      }
    }
  });
};

onLoad(() => {
  resetForm();
  loadLabs();
  loadRepairs();
});

onShow(() => {
  loadRepairs();
});

onPullDownRefresh(async () => {
  await Promise.all([loadLabs(), loadRepairs()]);
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './repair-service.less';</style>
