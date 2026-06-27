<template>
  <view class="page">
    <view class="hd">
      <view class="hd-back" @tap="goBack">
        <text class="hd-back-ico">‹</text>
        <text class="hd-back-txt">返回</text>
      </view>
      <view class="hd-row">
        <text class="hd-title">通知公告管理</text>
        <view class="hd-btn" @tap="openCreate">
          <text class="hd-btn-text">发布通知</text>
        </view>
      </view>
      <scroll-view class="tabs" scroll-x :show-scrollbar="false">
        <view class="tabs-inner">
          <view
            class="tab"
            v-for="t in statusTabs"
            :key="t.value"
            :class="{ 'tab--active': statusFilter === t.value }"
            @tap="changeStatus(t.value)"
          >
            <text class="tab-text">{{ t.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="body">
      <!-- 加载态 -->
      <view v-if="loading" class="state">
        <text class="state-text">加载中...</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="!notices.length" class="state">
        <text class="state-text">暂无数据</text>
      </view>

      <!-- 列表 -->
      <view v-else class="list">
        <view class="card" v-for="item in notices" :key="item.id">
          <view class="card-title-row">
            <text v-if="item.top" class="tag-top">置顶</text>
            <text class="card-title">{{ item.title }}</text>
            <text class="status-tag" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
          </view>

          <view class="card-meta">
            <text class="meta-chip">{{ typeText(item.noticeType) }}</text>
            <text class="meta-chip">{{ scopeText(item) }}</text>
          </view>

          <view class="card-foot">
            <text class="foot-time">{{ formatTime(item.publishTime) }}</text>
            <text class="foot-view">浏览 {{ item.viewCount || 0 }}</text>
          </view>

          <view class="card-actions">
            <view class="act-btn" @tap="openEdit(item)">
              <text class="act-text">编辑</text>
            </view>
            <view
              class="act-btn"
              :class="item.status === 'published' ? 'act-btn--warn' : 'act-btn--ok'"
              @tap="toggleStatus(item)"
            >
              <text class="act-text">{{ item.status === 'published' ? '下线' : '发布' }}</text>
            </view>
            <view class="act-btn act-btn--danger" @tap="confirmDelete(item)">
              <text class="act-text">删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 新增 / 编辑 弹层 -->
    <view v-if="showForm" class="mask" @tap="closeForm">
      <view class="sheet" @tap.stop>
        <view class="sheet-hd">
          <text class="sheet-title">{{ editingId ? '编辑通知' : '发布通知' }}</text>
          <text class="sheet-close" @tap="closeForm">×</text>
        </view>

        <scroll-view class="sheet-body" scroll-y>
          <view class="field">
            <text class="field-label">标题</text>
            <input
              class="field-input"
              v-model="form.title"
              type="text"
              placeholder="请输入通知标题"
              placeholder-class="field-ph"
            />
          </view>

          <view class="field">
            <text class="field-label">内容</text>
            <textarea
              class="field-textarea"
              v-model="form.content"
              placeholder="请输入通知内容"
              placeholder-class="field-ph"
              :maxlength="-1"
            />
          </view>

          <view class="field">
            <text class="field-label">类型</text>
            <view class="seg">
              <view
                class="seg-item"
                v-for="t in typeOptions"
                :key="t.value"
                :class="{ 'seg-item--active': form.noticeType === t.value }"
                @tap="form.noticeType = t.value"
              >
                <text class="seg-text">{{ t.label }}</text>
              </view>
            </view>
          </view>

          <view class="field">
            <text class="field-label">发布范围</text>
            <view class="seg">
              <view
                class="seg-item"
                :class="{ 'seg-item--active': form.publishScope === 'all' }"
                @tap="form.publishScope = 'all'"
              >
                <text class="seg-text">全体</text>
              </view>
              <view
                class="seg-item"
                :class="{ 'seg-item--active': form.publishScope === 'role' }"
                @tap="form.publishScope = 'role'"
              >
                <text class="seg-text">指定角色</text>
              </view>
            </view>
          </view>

          <view class="field" v-if="form.publishScope === 'role'">
            <text class="field-label">目标角色</text>
            <view class="checks">
              <view
                class="check"
                v-for="r in roleOptions"
                :key="r.value"
                :class="{ 'check--active': form.targetRoles.indexOf(r.value) > -1 }"
                @tap="toggleRole(r.value)"
              >
                <text class="check-box">{{ form.targetRoles.indexOf(r.value) > -1 ? '✓' : '' }}</text>
                <text class="check-text">{{ r.label }}</text>
              </view>
            </view>
          </view>

          <view class="field field--row">
            <text class="field-label">置顶</text>
            <switch :checked="form.top" color="#F5A623" @change="onTopChange" />
          </view>

          <view class="field">
            <text class="field-label">状态</text>
            <view class="seg">
              <view
                class="seg-item"
                :class="{ 'seg-item--active': form.status === 'draft' }"
                @tap="form.status = 'draft'"
              >
                <text class="seg-text">草稿</text>
              </view>
              <view
                class="seg-item"
                :class="{ 'seg-item--active': form.status === 'published' }"
                @tap="form.status = 'published'"
              >
                <text class="seg-text">发布</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="sheet-foot">
          <view class="foot-btn foot-btn--cancel" @tap="closeForm">
            <text class="foot-btn-text">取消</text>
          </view>
          <view class="foot-btn foot-btn--submit" :class="{ 'foot-btn--disabled': submitting }" @tap="submitForm">
            <text class="foot-btn-text">{{ submitting ? '提交中...' : '保存' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import {
  getAdminNotices,
  createNotice,
  updateNotice,
  setNoticeStatus,
  deleteNotice
} from '@/api/portal';
import { getStoredUser, getStoredRole } from '@/api/storage';

const goBack = () => uni.navigateBack({ delta: 1 });

const loading = ref(false);
const submitting = ref(false);
const notices = ref<any[]>([]);
const statusFilter = ref('');

const statusTabs = [
  { label: '全部', value: '' },
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' },
  { label: '已下线', value: 'offline' }
];

const typeOptions = [
  { label: '系统', value: 'system' },
  { label: '活动', value: 'activity' },
  { label: '维护', value: 'maintenance' }
];

const roleOptions = [
  { label: '学生', value: 'student' },
  { label: '教师', value: 'teacher' },
  { label: '管理员', value: 'admin' }
];

const typeMap: Record<string, string> = {
  system: '系统通知',
  activity: '活动通知',
  maintenance: '维护通知'
};

const statusMap: Record<string, string> = {
  draft: '草稿',
  published: '已发布',
  offline: '已下线'
};

const typeText = (t: any): string => {
  if (!t) return '通知';
  return typeMap[String(t)] || String(t);
};

const statusText = (s: any): string => {
  if (!s) return '草稿';
  return statusMap[String(s)] || String(s);
};

const statusClass = (s: any): string => {
  const v = String(s || 'draft');
  if (v === 'published') return 'status-tag--ok';
  if (v === 'offline') return 'status-tag--off';
  return 'status-tag--draft';
};

const scopeText = (item: any): string => {
  if (!item) return '';
  if (item.publishScope === 'role') {
    const roles = Array.isArray(item.targetRoles) ? item.targetRoles : [];
    if (!roles.length) return '指定角色';
    const labels = roles.map((r: any) => {
      const found = roleOptions.find((o) => o.value === String(r));
      return found ? found.label : String(r);
    });
    return labels.join('/');
  }
  return '全体';
};

const formatTime = (value: any): string => {
  if (!value) return '未发布';
  const date = new Date(value);
  if (isNaN(date.getTime())) return String(value);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const hh = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${d} ${hh}:${mm}`;
};

const loadNotices = async () => {
  loading.value = true;
  try {
    const list = await getAdminNotices({
      status: statusFilter.value || '',
      keyword: ''
    });
    notices.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    notices.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const changeStatus = (value: string) => {
  if (statusFilter.value === value) return;
  statusFilter.value = value;
  loadNotices();
};

// ============ 表单 ============
const showForm = ref(false);
const editingId = ref<any>(null);

const form = reactive<{
  title: string;
  content: string;
  noticeType: string;
  publishScope: string;
  targetRoles: string[];
  top: boolean;
  status: string;
}>({
  title: '',
  content: '',
  noticeType: 'system',
  publishScope: 'all',
  targetRoles: [],
  top: false,
  status: 'draft'
});

const resetForm = () => {
  form.title = '';
  form.content = '';
  form.noticeType = 'system';
  form.publishScope = 'all';
  form.targetRoles = [];
  form.top = false;
  form.status = 'draft';
};

const openCreate = () => {
  editingId.value = null;
  resetForm();
  showForm.value = true;
};

const openEdit = (item: any) => {
  if (!item?.id) return;
  editingId.value = item.id;
  form.title = item.title || '';
  form.content = item.content || '';
  form.noticeType = item.noticeType || 'system';
  form.publishScope = item.publishScope === 'role' ? 'role' : 'all';
  form.targetRoles = Array.isArray(item.targetRoles) ? item.targetRoles.map((r: any) => String(r)) : [];
  form.top = !!item.top;
  form.status = item.status === 'published' ? 'published' : 'draft';
  showForm.value = true;
};

const closeForm = () => {
  showForm.value = false;
};

const onTopChange = (e: any) => {
  form.top = !!(e?.detail?.value);
};

const toggleRole = (role: string) => {
  const idx = form.targetRoles.indexOf(role);
  if (idx > -1) {
    form.targetRoles.splice(idx, 1);
  } else {
    form.targetRoles.push(role);
  }
};

const submitForm = async () => {
  if (submitting.value) return;
  if (!form.title || !form.title.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' });
    return;
  }
  if (!form.content || !form.content.trim()) {
    uni.showToast({ title: '请输入内容', icon: 'none' });
    return;
  }
  if (form.publishScope === 'role' && !form.targetRoles.length) {
    uni.showToast({ title: '请选择目标角色', icon: 'none' });
    return;
  }

  const payload: any = {
    title: form.title.trim(),
    content: form.content.trim(),
    noticeType: form.noticeType,
    publishScope: form.publishScope,
    targetRoles: form.publishScope === 'role' ? form.targetRoles.slice() : [],
    top: form.top,
    status: form.status
  };

  submitting.value = true;
  try {
    if (editingId.value) {
      await updateNotice(editingId.value, payload);
    } else {
      await createNotice(payload);
    }
    uni.showToast({ title: '保存成功', icon: 'none' });
    showForm.value = false;
    await loadNotices();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '保存失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
};

const toggleStatus = async (item: any) => {
  if (!item?.id) return;
  const next = item.status === 'published' ? 'offline' : 'published';
  try {
    await setNoticeStatus(item.id, next);
    uni.showToast({ title: next === 'published' ? '已发布' : '已下线', icon: 'none' });
    await loadNotices();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
  }
};

const confirmDelete = (item: any) => {
  if (!item?.id) return;
  uni.showModal({
    title: '删除通知',
    content: `确定删除「${item.title || ''}」吗？`,
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await deleteNotice(item.id);
        uni.showToast({ title: '已删除', icon: 'none' });
        await loadNotices();
      } catch (e: any) {
        uni.showToast({ title: e?.data?.message || '删除失败', icon: 'none' });
      }
    }
  });
};

onLoad(() => {
  // 角色感知：管理员页面使用 admin 配色（见样式）
  getStoredUser();
  getStoredRole();
  loadNotices();
});

onShow(() => {
  loadNotices();
});

onPullDownRefresh(async () => {
  await loadNotices();
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './admin-notice-manage.less';</style>
