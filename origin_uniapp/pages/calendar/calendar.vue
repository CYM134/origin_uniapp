<template>
  <view class="page">
    <view class="hd">
      <text class="hd-title">我的日历</text>
      <view class="month-bar">
        <text class="month-nav" @tap="prevMonth">‹ 上月</text>
        <text class="month-label" @tap="thisMonth">{{ monthLabel }}</text>
        <text class="month-nav" @tap="nextMonth">下月 ›</text>
      </view>
    </view>

    <view class="body">
      <!-- 加载态 -->
      <view v-if="loading" class="state">
        <text class="state-text">加载中...</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="!groups.length" class="state">
        <text class="state-text">暂无数据</text>
      </view>

      <!-- 分组列表 -->
      <view v-else class="list">
        <view class="group" v-for="g in groups" :key="g.date">
          <view class="group-hd">
            <text class="group-date">{{ g.date }}</text>
            <text class="group-week">{{ g.week }}</text>
          </view>
          <view
            class="card event"
            v-for="ev in g.events"
            :key="ev.id"
            @longpress="onLongPress(ev)"
          >
            <view class="event-bar" :style="{ background: typeColor(ev.eventType) }"></view>
            <view class="event-main">
              <view class="event-top">
                <text class="event-time">{{ timeRange(ev) }}</text>
                <text
                  class="type-tag"
                  :style="{ color: typeColor(ev.eventType), background: typeBg(ev.eventType) }"
                >{{ typeText(ev.eventType) }}</text>
              </view>
              <text class="event-title">{{ ev.title || '未命名日程' }}</text>
              <text v-if="ev.location" class="event-loc">📍 {{ ev.location }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 悬浮添加按钮 -->
    <view class="fab" @tap="openAdd">
      <text class="fab-icon">+</text>
    </view>

    <!-- 添加日程弹层 -->
    <view v-if="showAdd" class="mask" @tap="closeAdd">
      <view class="sheet" @tap.stop>
        <view class="sheet-hd">
          <text class="sheet-title">添加个人日程</text>
          <text class="sheet-close" @tap="closeAdd">×</text>
        </view>

        <view class="field">
          <text class="field-label">标题</text>
          <input
            class="field-input"
            v-model="form.title"
            type="text"
            placeholder="请输入日程标题"
            placeholder-class="field-ph"
          />
        </view>

        <view class="field">
          <text class="field-label">开始日期</text>
          <picker mode="date" :value="form.startDate" @change="onStartDate">
            <view class="field-picker">{{ form.startDate || '请选择日期' }}</view>
          </picker>
        </view>
        <view class="field">
          <text class="field-label">开始时间</text>
          <input
            class="field-input"
            v-model="form.startTimeStr"
            type="text"
            placeholder="HH:mm（如 09:00）"
            placeholder-class="field-ph"
          />
        </view>

        <view class="field">
          <text class="field-label">结束日期</text>
          <picker mode="date" :value="form.endDate" @change="onEndDate">
            <view class="field-picker">{{ form.endDate || '请选择日期' }}</view>
          </picker>
        </view>
        <view class="field">
          <text class="field-label">结束时间</text>
          <input
            class="field-input"
            v-model="form.endTimeStr"
            type="text"
            placeholder="HH:mm（如 10:30）"
            placeholder-class="field-ph"
          />
        </view>

        <view class="field">
          <text class="field-label">地点</text>
          <input
            class="field-input"
            v-model="form.location"
            type="text"
            placeholder="可选"
            placeholder-class="field-ph"
          />
        </view>

        <view class="sheet-actions">
          <view class="btn btn-cancel" @tap="closeAdd">取消</view>
          <view class="btn btn-primary" :class="{ 'btn-disabled': submitting }" @tap="submitAdd">
            {{ submitting ? '提交中...' : '保存' }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { getCalendarEvents, createCalendarEvent, deleteCalendarEvent } from '@/api/portal';

const loading = ref(false);
const submitting = ref(false);
const events = ref<any[]>([]);

// 当前显示的年月
const cursor = ref(new Date());

const pad = (n: number): string => String(n).padStart(2, '0');

const monthLabel = computed(() => {
  const d = cursor.value;
  return `${d.getFullYear()}年${d.getMonth() + 1}月`;
});

const weekNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];

const typeNameMap: Record<string, string> = {
  reservation: '预约',
  approval: '审批',
  custom: '个人'
};
const typeColorMap: Record<string, string> = {
  reservation: '#1D6FDB',
  approval: '#D97706',
  custom: '#059669'
};
const typeBgMap: Record<string, string> = {
  reservation: '#DBEAFE',
  approval: '#FEF3C7',
  custom: '#D1FAE5'
};

const typeText = (t: any): string => typeNameMap[String(t)] || '其他';
const typeColor = (t: any): string => typeColorMap[String(t)] || '#6B7280';
const typeBg = (t: any): string => typeBgMap[String(t)] || '#F3F4F6';

const dateKey = (value: any): string => {
  if (!value) return '';
  const d = new Date(value);
  if (isNaN(d.getTime())) return String(value).slice(0, 10);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

const timeOnly = (value: any): string => {
  if (!value) return '';
  const d = new Date(value);
  if (isNaN(d.getTime())) return '';
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const timeRange = (ev: any): string => {
  const s = timeOnly(ev?.startTime);
  const e = timeOnly(ev?.endTime);
  if (s && e) return `${s} - ${e}`;
  return s || e || '全天';
};

const weekOf = (key: string): string => {
  const d = new Date(key.replace(/-/g, '/'));
  if (isNaN(d.getTime())) return '';
  return weekNames[d.getDay()] || '';
};

// 按日期分组
const groups = computed(() => {
  const map: Record<string, any[]> = {};
  (events.value || []).forEach((ev: any) => {
    const key = dateKey(ev?.startTime);
    if (!key) return;
    if (!map[key]) map[key] = [];
    map[key].push(ev);
  });
  return Object.keys(map)
    .sort()
    .map((date) => ({
      date,
      week: weekOf(date),
      events: map[date].sort((a: any, b: any) => {
        const ta = new Date(a?.startTime || 0).getTime();
        const tb = new Date(b?.startTime || 0).getTime();
        return (ta || 0) - (tb || 0);
      })
    }));
});

const monthRange = () => {
  const d = cursor.value;
  const first = new Date(d.getFullYear(), d.getMonth(), 1);
  const last = new Date(d.getFullYear(), d.getMonth() + 1, 0);
  const startDate = `${first.getFullYear()}-${pad(first.getMonth() + 1)}-${pad(first.getDate())}`;
  const endDate = `${last.getFullYear()}-${pad(last.getMonth() + 1)}-${pad(last.getDate())}`;
  return { startDate, endDate };
};

const loadEvents = async () => {
  loading.value = true;
  try {
    const { startDate, endDate } = monthRange();
    const list = await getCalendarEvents(startDate, endDate);
    events.value = Array.isArray(list) ? list : [];
  } catch (e: any) {
    events.value = [];
    uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const prevMonth = () => {
  const d = cursor.value;
  cursor.value = new Date(d.getFullYear(), d.getMonth() - 1, 1);
  loadEvents();
};
const nextMonth = () => {
  const d = cursor.value;
  cursor.value = new Date(d.getFullYear(), d.getMonth() + 1, 1);
  loadEvents();
};
const thisMonth = () => {
  cursor.value = new Date();
  loadEvents();
};

// 删除（仅 editable）
const onLongPress = (ev: any) => {
  if (!ev?.editable) return;
  uni.showModal({
    title: '删除日程',
    content: `确定删除「${ev.title || '该日程'}」吗？`,
    success: (res) => {
      if (res.confirm) doDelete(ev);
    }
  });
};

const doDelete = async (ev: any) => {
  try {
    await deleteCalendarEvent(ev.id);
    uni.showToast({ title: '已删除', icon: 'none' });
    loadEvents();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '删除失败', icon: 'none' });
  }
};

// 添加表单
const showAdd = ref(false);
const todayKey = () => {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};
const form = ref({
  title: '',
  startDate: todayKey(),
  startTimeStr: '09:00',
  endDate: todayKey(),
  endTimeStr: '10:00',
  location: ''
});

const openAdd = () => {
  const t = todayKey();
  form.value = {
    title: '',
    startDate: t,
    startTimeStr: '09:00',
    endDate: t,
    endTimeStr: '10:00',
    location: ''
  };
  showAdd.value = true;
};
const closeAdd = () => {
  showAdd.value = false;
};
const onStartDate = (e: any) => {
  form.value.startDate = e?.detail?.value || form.value.startDate;
};
const onEndDate = (e: any) => {
  form.value.endDate = e?.detail?.value || form.value.endDate;
};

const normTime = (s: string): string => {
  const v = String(s || '').trim();
  if (!v) return '00:00';
  // 仅取 HH:mm 部分
  const m = v.match(/^(\d{1,2}):(\d{2})/);
  if (!m) return '00:00';
  return `${pad(Number(m[1]))}:${m[2]}`;
};

const submitAdd = async () => {
  if (submitting.value) return;
  const f = form.value;
  if (!f.title.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' });
    return;
  }
  if (!f.startDate || !f.endDate) {
    uni.showToast({ title: '请选择日期', icon: 'none' });
    return;
  }
  const startTime = `${f.startDate} ${normTime(f.startTimeStr)}:00`;
  const endTime = `${f.endDate} ${normTime(f.endTimeStr)}:00`;
  if (new Date(endTime.replace(/-/g, '/')).getTime() < new Date(startTime.replace(/-/g, '/')).getTime()) {
    uni.showToast({ title: '结束时间不能早于开始时间', icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    await createCalendarEvent({
      title: f.title.trim(),
      eventType: 'custom',
      startTime,
      endTime,
      location: f.location.trim()
    });
    uni.showToast({ title: '添加成功', icon: 'none' });
    showAdd.value = false;
    // 跳到对应月份并刷新
    const d = new Date(f.startDate.replace(/-/g, '/'));
    if (!isNaN(d.getTime())) cursor.value = new Date(d.getFullYear(), d.getMonth(), 1);
    loadEvents();
  } catch (e: any) {
    uni.showToast({ title: e?.data?.message || '添加失败', icon: 'none' });
  } finally {
    submitting.value = false;
  }
};

onLoad(() => {
  loadEvents();
});

onShow(() => {
  loadEvents();
});

onPullDownRefresh(async () => {
  await loadEvents();
  uni.stopPullDownRefresh();
});
</script>

<style lang="less">@import './calendar.less';</style>
