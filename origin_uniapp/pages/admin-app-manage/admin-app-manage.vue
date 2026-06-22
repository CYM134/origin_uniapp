<template>
  <view class="page">
    <!-- 顶部 header -->
    <view class="hd">
      <view class="hd-row">
        <view class="hd-texts">
          <text class="hd-title">应用管理</text>
          <text class="hd-sub">管理校园应用的展示与状态</text>
        </view>
        <view class="hd-btn" @tap="openCreate">
          <text class="hd-btn-text">+ 新增应用</text>
        </view>
      </view>
    </view>

    <view class="body">
      <!-- 搜索 + 分类筛选 -->
      <view class="filter-bar">
        <view class="search-box">
          <text class="search-icon">🔍</text>
          <input
            class="search-input"
            type="text"
            v-model="keyword"
            placeholder="搜索应用名称/编码"
            placeholder-class="search-ph"
            confirm-type="search"
            @input="onKeywordInput"
            @confirm="reload"
          />
          <text v-if="keyword" class="search-clear" @tap="clearKeyword">✕</text>
        </view>
        <picker
          class="cat-picker"
          mode="selector"
          :range="categoryNames"
          :value="categoryIndex"
          @change="onCategoryChange"
        >
          <view class="cat-picker-inner">
            <text class="cat-picker-text">{{ categoryNames[categoryIndex] || '全部分类' }}</text>
            <text class="cat-picker-arrow">▾</text>
          </view>
        </picker>
      </view>

      <!-- 加载态 -->
      <view v-if="loading" class="state">
        <text class="state-text">加载中...</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="!apps.length" class="state">
        <text class="state-icon">📭</text>
        <text class="state-text">暂无数据</text>
      </view>

      <!-- 应用列表 -->
      <view v-else class="list">
        <view class="card" v-for="app in apps" :key="app.id">
          <view class="card-top">
            <view class="card-main">
              <text class="card-name">{{ app.appName || '未命名应用' }}</text>
              <text class="card-code">{{ app.appCode || '-' }}</text>
            </view>
            <text
              class="badge"
              :class="app.status === 1 ? 'badge-on' : 'badge-off'"
            >{{ app.status === 1 ? '启用' : '停用' }}</text>
          </view>

          <view class="meta">
            <view class="meta-item">
              <text class="meta-label">分类</text>
              <text class="meta-value">{{ app.categoryName || '未分类' }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">可见角色</text>
              <text class="meta-value">{{ formatRoles(app.visibleRoles) }}</text>
            </view>
            <view class="meta-item">
              <text class="meta-label">排序</text>
              <text class="meta-value">{{ app.sortOrder ?? 0 }}</text>
            </view>
          </view>

          <view class="ops">
            <text class="op op-edit" @tap="openEdit(app)">编辑</text>
            <text
              class="op op-toggle"
              @tap="toggleStatus(app)"
            >{{ app.status === 1 ? '停用' : '启用' }}</text>
            <text class="op op-del" @tap="removeApp(app)">删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 新增/编辑 弹层 -->
    <view v-if="modalVisible" class="modal-mask" @tap="closeModal">
      <view class="modal" @tap.stop>
        <view class="modal-hd">
          <text class="modal-title">{{ editingId ? '编辑应用' : '新增应用' }}</text>
          <text class="modal-close" @tap="closeModal">✕</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="field">
            <text class="field-label">应用名称</text>
            <input class="field-input" type="text" v-model="form.appName" placeholder="请输入应用名称" placeholder-class="ph" />
          </view>

          <view class="field">
            <text class="field-label">应用编码</text>
            <input class="field-input" type="text" v-model="form.appCode" placeholder="如 lab-reservation" placeholder-class="ph" />
          </view>

          <view class="field">
            <text class="field-label">所属分类</text>
            <picker
              class="field-picker"
              mode="selector"
              :range="formCategoryNames"
              :value="formCategoryIndex"
              @change="onFormCategoryChange"
            >
              <view class="field-picker-inner">
                <text class="field-picker-text">{{ formCategoryNames[formCategoryIndex] || '请选择分类' }}</text>
                <text class="field-picker-arrow">▾</text>
              </view>
            </picker>
          </view>

          <view class="field">
            <text class="field-label">图标</text>
            <input class="field-input" type="text" v-model="form.icon" placeholder="图标地址或字符" placeholder-class="ph" />
          </view>

          <view class="field">
            <text class="field-label">描述</text>
            <textarea class="field-textarea" v-model="form.description" placeholder="请输入应用描述" placeholder-class="ph" :maxlength="-1" />
          </view>

          <view class="field">
            <text class="field-label">打开方式</text>
            <view class="seg">
              <text
                class="seg-item"
                :class="{ active: form.openType === 'internal' }"
                @tap="form.openType = 'internal'"
              >内部页面</text>
              <text
                class="seg-item"
                :class="{ active: form.openType === 'external' }"
                @tap="form.openType = 'external'"
              >外部链接</text>
            </view>
          </view>

          <view class="field" v-if="form.openType === 'internal'">
            <text class="field-label">路由路径</text>
            <input class="field-input" type="text" v-model="form.routePath" placeholder="如 /notices" placeholder-class="ph" />
          </view>

          <view class="field" v-else>
            <text class="field-label">外部链接</text>
            <input class="field-input" type="text" v-model="form.externalUrl" placeholder="https://..." placeholder-class="ph" />
          </view>

          <view class="field">
            <text class="field-label">可见角色</text>
            <view class="checks">
              <view class="check" @tap="toggleRole('admin')">
                <text class="check-box" :class="{ on: roleAdmin }">{{ roleAdmin ? '✓' : '' }}</text>
                <text class="check-text">管理员</text>
              </view>
              <view class="check" @tap="toggleRole('teacher')">
                <text class="check-box" :class="{ on: roleTeacher }">{{ roleTeacher ? '✓' : '' }}</text>
                <text class="check-text">教师</text>
              </view>
              <view class="check" @tap="toggleRole('student')">
                <text class="check-box" :class="{ on: roleStudent }">{{ roleStudent ? '✓' : '' }}</text>
                <text class="check-text">学生</text>
              </view>
            </view>
          </view>

          <view class="field-row">
            <text class="field-label">热门</text>
            <switch :checked="form.hot" color="#F5A623" @change="onHotChange" />
          </view>

          <view class="field-row">
            <text class="field-label">最新</text>
            <switch :checked="form.latest" color="#F5A623" @change="onLatestChange" />
          </view>

          <view class="field">
            <text class="field-label">排序值</text>
            <input class="field-input" type="number" v-model="sortOrderStr" placeholder="0" placeholder-class="ph" />
          </view>

          <view class="field-row">
            <text class="field-label">启用状态</text>
            <switch :checked="form.status === 1" color="#F5A623" @change="onStatusChange" />
          </view>
        </scroll-view>

        <view class="modal-ft">
          <text class="ft-btn ft-cancel" @tap="closeModal">取消</text>
          <text class="ft-btn ft-confirm" :class="{ disabled: submitting }" @tap="submitForm">{{ submitting ? '提交中...' : '保存' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import {
  getAdminApps,
  getAppCategories,
  createApp,
  updateApp,
  setAppStatus,
  deleteApp,
} from '@/api/portal';

const loading = ref(false);
const submitting = ref(false);

const apps = ref<any[]>([]);
const categories = ref<any[]>([]);
const keyword = ref('');
const activeCategoryId = ref<string | number>('');

let debounceTimer: any = null;

/* 顶部分类下拉（含“全部分类”） */
const categoryNames = computed<string[]>(() => {
  return ['全部分类', ...categories.value.map((c) => c?.categoryName || '未命名')];
});
const categoryIndex = computed<number>(() => {
  if (activeCategoryId.value === '') return 0;
  const idx = categories.value.findIndex((c) => c?.id === activeCategoryId.value);
  return idx >= 0 ? idx + 1 : 0;
});

/* 表单分类下拉（不含全部） */
const formCategoryNames = computed<string[]>(() => {
  return categories.value.map((c) => c?.categoryName || '未命名');
});
const formCategoryIndex = ref(0);

/* ---- 表单 ---- */
const editingId = ref<string | number>('');
const modalVisible = ref(false);
const roleAdmin = ref(false);
const roleTeacher = ref(false);
const roleStudent = ref(false);
const sortOrderStr = ref('0');

const form = ref<any>({
  appName: '',
  appCode: '',
  categoryId: '',
  icon: '',
  description: '',
  routePath: '',
  externalUrl: '',
  openType: 'internal',
  hot: false,
  latest: false,
  status: 1,
});

function formatRoles(roles: any): string {
  if (!roles) return '全部';
  const map: Record<string, string> = { admin: '管理员', teacher: '教师', student: '学生' };
  const arr = String(roles)
    .split(',')
    .map((r) => r.trim())
    .filter(Boolean)
    .map((r) => map[r] || r);
  return arr.length ? arr.join('、') : '全部';
}

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
    if (keyword.value) params.keyword = keyword.value;
    if (activeCategoryId.value !== '') params.categoryId = activeCategoryId.value;
    const list = await getAdminApps(params);
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

function onCategoryChange(e: any) {
  const idx = Number(e?.detail?.value ?? 0);
  if (idx <= 0) {
    activeCategoryId.value = '';
  } else {
    activeCategoryId.value = categories.value[idx - 1]?.id ?? '';
  }
  loadApps();
}

/* ---- 弹层 ---- */
function resetForm() {
  form.value = {
    appName: '',
    appCode: '',
    categoryId: categories.value[0]?.id ?? '',
    icon: '',
    description: '',
    routePath: '',
    externalUrl: '',
    openType: 'internal',
    hot: false,
    latest: false,
    status: 1,
  };
  formCategoryIndex.value = 0;
  roleAdmin.value = false;
  roleTeacher.value = false;
  roleStudent.value = false;
  sortOrderStr.value = '0';
  editingId.value = '';
}

function openCreate() {
  resetForm();
  modalVisible.value = true;
}

function openEdit(app: any) {
  editingId.value = app?.id ?? '';
  const idx = categories.value.findIndex((c) => c?.id === app?.categoryId);
  formCategoryIndex.value = idx >= 0 ? idx : 0;
  form.value = {
    appName: app?.appName || '',
    appCode: app?.appCode || '',
    categoryId: app?.categoryId ?? (categories.value[0]?.id ?? ''),
    icon: app?.icon || '',
    description: app?.description || '',
    routePath: app?.routePath || '',
    externalUrl: app?.externalUrl || '',
    openType: app?.openType === 'external' ? 'external' : 'internal',
    hot: app?.hot === true || app?.hot === 1,
    latest: app?.latest === true || app?.latest === 1,
    status: app?.status === 1 ? 1 : 0,
  };
  const roles = String(app?.visibleRoles || '')
    .split(',')
    .map((r) => r.trim());
  roleAdmin.value = roles.includes('admin');
  roleTeacher.value = roles.includes('teacher');
  roleStudent.value = roles.includes('student');
  sortOrderStr.value = String(app?.sortOrder ?? 0);
  modalVisible.value = true;
}

function closeModal() {
  modalVisible.value = false;
}

function onFormCategoryChange(e: any) {
  const idx = Number(e?.detail?.value ?? 0);
  formCategoryIndex.value = idx;
  form.value.categoryId = categories.value[idx]?.id ?? '';
}

function toggleRole(r: string) {
  if (r === 'admin') roleAdmin.value = !roleAdmin.value;
  else if (r === 'teacher') roleTeacher.value = !roleTeacher.value;
  else if (r === 'student') roleStudent.value = !roleStudent.value;
}

function onHotChange(e: any) {
  form.value.hot = !!e?.detail?.value;
}
function onLatestChange(e: any) {
  form.value.latest = !!e?.detail?.value;
}
function onStatusChange(e: any) {
  form.value.status = e?.detail?.value ? 1 : 0;
}

function buildVisibleRoles(): string {
  const arr: string[] = [];
  if (roleAdmin.value) arr.push('admin');
  if (roleTeacher.value) arr.push('teacher');
  if (roleStudent.value) arr.push('student');
  return arr.join(',');
}

async function submitForm() {
  if (submitting.value) return;
  if (!form.value.appName) {
    uni.showToast({ title: '请填写应用名称', icon: 'none' });
    return;
  }
  if (!form.value.appCode) {
    uni.showToast({ title: '请填写应用编码', icon: 'none' });
    return;
  }

  const sortNum = Number(sortOrderStr.value);
  const payload: any = {
    appName: form.value.appName,
    appCode: form.value.appCode,
    categoryId: form.value.categoryId,
    icon: form.value.icon,
    description: form.value.description,
    routePath: form.value.openType === 'internal' ? form.value.routePath : '',
    externalUrl: form.value.openType === 'external' ? form.value.externalUrl : '',
    openType: form.value.openType,
    visibleRoles: buildVisibleRoles(),
    hot: form.value.hot ? 1 : 0,
    latest: form.value.latest ? 1 : 0,
    sortOrder: Number.isNaN(sortNum) ? 0 : sortNum,
    status: form.value.status,
  };

  submitting.value = true;
  try {
    if (editingId.value) {
      await updateApp(editingId.value, payload);
    } else {
      await createApp(payload);
    }
    uni.showToast({ title: '保存成功', icon: 'none' });
    modalVisible.value = false;
    await loadApps();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '保存失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
}

async function toggleStatus(app: any) {
  const next = app?.status === 1 ? 0 : 1;
  try {
    await setAppStatus(app.id, next);
    app.status = next;
    uni.showToast({ title: next === 1 ? '已启用' : '已停用', icon: 'none' });
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '操作失败', icon: 'none' });
  }
}

function removeApp(app: any) {
  uni.showModal({
    title: '删除应用',
    content: `确定删除「${app?.appName || '该应用'}」吗？`,
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await deleteApp(app.id);
        uni.showToast({ title: '已删除', icon: 'none' });
        await loadApps();
      } catch (e: any) {
        uni.showToast({ title: e?.data?.message || '删除失败', icon: 'none' });
      }
    },
  });
}

onLoad(() => {
  loadCategories();
  loadApps();
});

onShow(() => {
  // 弹层未打开时回到页面刷新列表
  if (!modalVisible.value && apps.value.length) {
    loadApps();
  }
});

onPullDownRefresh(async () => {
  await loadCategories();
  await loadApps();
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './admin-app-manage.less';</style>
