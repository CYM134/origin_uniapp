<template>
    <view class="page-wrapper">
        <!-- pages/student-schedule-preview/student-schedule-preview.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="课表预览" color="white" background="#3B82F6" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <app-navigation-bar title="课表预览" :back="true" color="white" background="#3B82F6"></app-navigation-bar>
        <!-- #endif -->

        <view class="container">
            <!-- 日期选择器 -->
            <view class="date-selector">
                <view class="date-picker" @tap="showDatePicker">
                    <text class="selected-date">{{ selectedDate }}</text>
                    <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                </view>
                <view class="quick-dates">
                    <view :class="'quick-date ' + (isToday && viewMode === 'day' ? 'active' : '')" @tap="selectToday">
                        <text>今天</text>
                    </view>
                    <view :class="'quick-date ' + (isTomorrow && viewMode === 'day' ? 'active' : '')" @tap="selectTomorrow">
                        <text>明天</text>
                    </view>
                    <view :class="'quick-date ' + (viewMode === 'week' ? 'active' : '')" @tap="selectThisWeek">
                        <text>本周</text>
                    </view>
                </view>
            </view>

            <!-- 实验室筛选 -->
            <view class="lab-filter">
                <view class="filter-item">
                    <view class="filter-label">实验室</view>
                    <view class="custom-picker" @tap="showLabPicker">
                        <view class="picker">
                            <text>{{ selectedLabName }}</text>
                            <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 课表内容 -->
            <view class="schedule-content">
                <view v-if="viewMode === 'day'" class="day-view">
                    <!-- 时间轴 -->
                    <view class="time-axis">
                        <view class="time-slot" v-for="(item, index) in timeSlots" :key="index">
                            <text class="time-text">{{ item }}</text>
                        </view>
                    </view>

                    <!-- 课程列表 -->
                    <view class="course-list">
                        <view v-if="filteredCourses.length === 0" class="empty-state">
                            <image src="/static/images/icons/empty-schedule.svg" mode="aspectFit"></image>
                            <text>暂无课程安排</text>
                        </view>
                        <view class="course-item" @tap="viewCourseDetail" :data-course="item" v-for="(item, index) in filteredCourses" :key="index">
                            <view class="course-time">
                                <text class="start-time">{{ item.startTime }}</text>
                                <text class="end-time">{{ item.endTime }}</text>
                            </view>

                            <view class="course-info">
                                <view class="course-header">
                                    <text class="course-name">{{ item.courseName }}</text>
                                    <view :class="'course-status ' + item.status">
                                        <text>{{ item.statusText }}</text>
                                    </view>
                                </view>
                                <view class="course-details">
                                    <text class="lab-name">{{ item.labName }}</text>
                                    <text class="teacher-name">{{ item.teacherName }}</text>
                                </view>
                                <view class="course-meta">
                                    <text class="student-count">{{ item.currentStudents }}/{{ item.maxStudents }}人</text>
                                    <text class="course-type">{{ item.courseType }}</text>
                                </view>
                            </view>
                        </view>
                    </view>
                </view>

                <!-- 周视图 -->
                <view v-if="viewMode === 'week'" class="week-view">
                    <view class="week-header">
                        <view class="week-nav">
                            <view class="nav-btn" @tap="prevWeek">
                                <text>‹</text>
                            </view>
                            <text class="week-range">{{ weekRange }}</text>
                            <view class="nav-btn" @tap="nextWeek">
                                <text>›</text>
                            </view>
                        </view>
                    </view>

                    <view class="week-grid">
                        <view class="week-days">
                            <view class="day-header time-header"></view>
                            <view class="day-header" v-for="(item, index) in weekDays" :key="index">
                                <text class="day-name">{{ item.name }}</text>

                                <text class="day-date">{{ item.date }}</text>
                            </view>
                        </view>

                        <scroll-view class="week-content" :scroll-y="true">
                            <view class="time-row" v-for="(item, index) in timeSlots" :key="index">
                                <view class="time-cell">
                                    <text>{{ item }}</text>
                                </view>

                                <view class="course-cell" v-for="(day, dayIndex) in weekDays" :key="dayIndex">
                                    <view
                                        :class="'week-course ' + course.status"
                                        @tap="viewCourseDetail"
                                        :data-course="course"
                                        v-for="(course, index) in day.courses.filter((c) => c.hourSlot === item)"
                                        :key="index"
                                    >
                                        <text class="course-name">{{ course.courseName }}</text>

                                        <text class="lab-name">{{ course.labName }}</text>
                                    </view>
                                </view>
                            </view>
                        </scroll-view>
                    </view>
                </view>
            </view>

            <!-- 底部操作栏 -->
            <view class="bottom-actions">
                <view class="view-toggle">
                    <view :class="'toggle-btn ' + (viewMode === 'day' ? 'active' : '')" @tap="switchViewMode" data-mode="day">
                        <text>日视图</text>
                    </view>
                    <view :class="'toggle-btn ' + (viewMode === 'week' ? 'active' : '')" @tap="switchViewMode" data-mode="week">
                        <text>周视图</text>
                    </view>
                </view>
                <view class="action-btn" @tap="goToReservation">
                    <image src="/static/images/icons/add.svg" mode="aspectFit"></image>
                    <text>新建预约</text>
                </view>
            </view>

            <!-- 页脚 -->
            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>

        <!-- 日期选择弹窗 -->
        <view v-if="showDateModal" class="modal-overlay" @tap="closeDateModal">
            <view class="modal-content picker-modal-content" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">选择日期</text>
                    <view class="close-btn" @tap="closeDateModal">×</view>
                </view>
                <view class="modal-body date-picker-body">
                    <picker-view
                        indicator-style="height: 100rpx;"
                        style="width: 100%; height: 400rpx"
                        :value="[tempYearIndex, tempMonthIndex, tempDayIndex]"
                        @change="onDatePickerChange"
                    >
                        <picker-view-column>
                            <view class="picker-view-item" v-for="(item, index) in years" :key="index">{{ item }}年</view>
                        </picker-view-column>
                        <picker-view-column>
                            <view class="picker-view-item" v-for="(item, index) in months" :key="index">{{ item }}月</view>
                        </picker-view-column>
                        <picker-view-column>
                            <view class="picker-view-item" v-for="(item, index) in days" :key="index">{{ item }}日</view>
                        </picker-view-column>
                    </picker-view>
                </view>
                <view class="modal-footer">
                    <button class="cancel-btn" @tap="closeDateModal">取消</button>
                    <button class="confirm-btn" @tap="confirmDatePicker">确定</button>
                </view>
            </view>
        </view>

        <!-- 课程详情弹窗 -->
        <view v-if="showCourseModal" class="modal-overlay" @tap="closeCourseModal">
            <view class="modal-content course-modal" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">课程详情</text>
                    <view class="close-btn" @tap="closeCourseModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="course-detail">
                        <view class="detail-item">
                            <text class="label">课程名称：</text>
                            <text class="value">{{ selectedCourse.courseName }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">实验室：</text>
                            <text class="value">{{ selectedCourse.labName }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">授课教师：</text>
                            <text class="value">{{ selectedCourse.teacherName }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">上课时间：</text>
                            <text class="value">{{ selectedCourse.startTime }} - {{ selectedCourse.endTime }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">课程类型：</text>
                            <text class="value">{{ selectedCourse.courseType }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">人数限制：</text>
                            <text class="value">{{ selectedCourse.currentStudents }}/{{ selectedCourse.maxStudents }}人</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">课程描述：</text>
                            <text class="value description">{{ selectedCourse.description || '暂无描述' }}</text>
                        </view>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="info-btn">仅供查看</button>
                </view>
            </view>
        </view>

        <!-- 实验室选择器弹窗 -->
        <view v-if="showLabPickerModal" class="modal-overlay" @tap="hideLabPicker">
            <view class="modal-content picker-modal-content custom-picker-modal" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text>选择实验室</text>
                    <view class="close-btn" @tap="hideLabPicker">×</view>
                </view>

                <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[tempLabIndex]" @change="onLabPickerChange">
                    <picker-view-column>
                        <view class="picker-view-item" v-for="(item, index) in labPickerOptions" :key="index">{{ item.name }}</view>
                    </picker-view-column>
                </picker-view>

                <view class="modal-footer">
                    <button class="cancel-btn" @tap="hideLabPicker">取消</button>
                    <button class="confirm-btn" @tap="confirmLabSelection">确定</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
import { getCourseSchedulesByDate, getCourseSchedulesByWeek, getLabs } from '@/api/student';
// pages/student-schedule-preview/student-schedule-preview.ts

// 日期相关
const selectedDate = ref<string>('');
const isToday = ref<boolean>(true);
const isTomorrow = ref<boolean>(false);
const showDateModal = ref<boolean>(false);

// 视图模式
const viewMode = ref<string>('day');

// 'day' | 'week'

// 实验室筛选
const selectedLab = ref<string>('all');
const selectedLabName = ref<string>('全部实验室');
const showLabPickerModal = ref<boolean>(false);
const labPickerIndex = ref<number[]>([0]);

const labs = ref<any[]>([
    {
        id: 'all',
        name: '全部实验室'
    }
]);

// 加载实验室列表
const loadLabs = async () => {
    try {
        const list = await getLabs();
        const labOptions = (Array.isArray(list) ? list : []).map((lab: any) => ({
            id: lab.id,
            name: lab.name
        }));
        labs.value = [
            {
                id: 'all',
                name: '全部实验室'
            },
            ...labOptions
        ];
    } catch (err: any) {
        uni.showToast({
            title: err?.data?.message || '加载失败',
            icon: 'none'
        });
    }
};

// 时间段
const timeSlots = ref<string[]>(['08:00', '09:00', '10:00', '11:00', '14:00', '15:00', '16:00', '17:00', '19:00', '20:00', '21:00']);

// 课程数据
const courses = ref<any[]>([]);
const filteredCourses = ref<any[]>([]);

// 周视图相关
const weekRange = ref<string>('');
const weekDays = ref<any[]>([]);
const currentWeekStart = ref<Date | null>(null);

// 课程详情弹窗
const showCourseModal = ref<boolean>(false);
const selectedCourse = ref<any>(null);

// picker-view日期选择相关
const years = ref<number[]>([]);
const months = ref<number[]>([]);
const days = ref<number[]>([]);
const tempYearIndex = ref<number>(0);
const tempMonthIndex = ref<number>(0);
const tempDayIndex = ref<number>(0);
const labPickerOptions = ref<any[]>([]);
const tempLabIndex = ref<number>(0);
const dayIndex = ref<number>(0);

const course = ref<any>({
    timeSlot: '',
    status: '',
    courseName: '',
    labName: ''
});

const day = ref<any>({
    courses: []
});

onLoad(() => {
    initializeDate();
    loadLabs();
    loadCourses();
});

onShow(() => {
    loadCourses();
});

onPullDownRefresh(async () => {
    await loadCourses();
    uni.stopPullDownRefresh();
});

// 初始化日期
const initializeDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    selectedDate.value = `${year}-${month}-${day}`;
    isToday.value = true;
    isTomorrow.value = false;
    initializeWeek(today);
};

// 初始化周视图
const initializeWeek = (date: Date) => {
    const startOfWeek = new Date(date);
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1); // 调整为周一开始
    startOfWeek.setDate(diff);
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    const newWeekDays = [];
    const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
    for (let i = 0; i < 7; i++) {
        const currentDay = new Date(startOfWeek);
        currentDay.setDate(startOfWeek.getDate() + i);
        newWeekDays.push({
            name: dayNames[i],
            date: `${currentDay.getMonth() + 1}/${currentDay.getDate()}`,
            fullDate: formatDate(currentDay),
            courses: []
        });
    }
    weekRange.value = `${startOfWeek.getMonth() + 1}月${startOfWeek.getDate()}日 - ${endOfWeek.getMonth() + 1}月${endOfWeek.getDate()}日`;
    weekDays.value = newWeekDays;
    currentWeekStart.value = startOfWeek;
};

// 格式化日期
const formatDate = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

// 计算课程归属的整点行（取 startTime 或 timeSlot 起点的小时，如 '08:00-09:50' -> '08:00'）
const hourSlot = (item: any): string => {
    const src = item.startTime || item.timeSlot || '';
    const hh = src.slice(0, 2);
    return hh ? `${hh}:00` : '';
};

// 适配课程字段到模板需要的形状
const mapCourse = (item: any) => ({
    ...item,
    id: item.id,
    courseName: item.courseName || item.name,
    labName: item.labName || item.lab,
    labId: item.labId,
    teacherName: item.teacherName || item.teacher,
    startTime: item.startTime,
    endTime: item.endTime,
    date: item.date,
    timeSlot: item.timeSlot,
    hourSlot: hourSlot(item),
    currentStudents: item.currentStudents ?? item.studentCount,
    maxStudents: item.maxStudents,
    courseType: item.courseType || item.typeText,
    status: item.status,
    statusText: item.statusText,
    canReserve: item.canReserve,
    description: item.description || item.remark
});

// 加载课程数据（日视图，后端已按日期+实验室筛选返回）
const loadCourses = async () => {
    const labId = selectedLab.value !== 'all' ? selectedLab.value : null;
    try {
        const list = await getCourseSchedulesByDate(selectedDate.value, labId);
        const mapped = (Array.isArray(list) ? list : []).map(mapCourse);
        courses.value = mapped;
        filteredCourses.value = mapped;
    } catch (err: any) {
        courses.value = [];
        filteredCourses.value = [];
        uni.showToast({
            title: err?.data?.message || '加载失败',
            icon: 'none'
        });
    }
    await loadWeekCourses();
};

// 筛选课程（实验室切换时重新拉取日视图数据）
const filterCourses = () => {
    loadCourses();
};

// 加载周视图课程（后端按周一~周日范围返回，再按日期归入每天）
const loadWeekCourses = async () => {
    if (!weekDays.value || weekDays.value.length === 0) {
        return;
    }
    const startDate = weekDays.value[0].fullDate;
    const endDate = weekDays.value[weekDays.value.length - 1].fullDate;
    try {
        const list = await getCourseSchedulesByWeek(startDate, endDate);
        const mapped = (Array.isArray(list) ? list : []).map(mapCourse);
        const updatedWeekDays = weekDays.value.map((day) => ({
            ...day,
            courses: mapped.filter((course) => course.date === day.fullDate)
        }));
        weekDays.value = updatedWeekDays;
    } catch (err: any) {
        const updatedWeekDays = weekDays.value.map((day) => ({
            ...day,
            courses: []
        }));
        weekDays.value = updatedWeekDays;
        uni.showToast({
            title: err?.data?.message || '加载失败',
            icon: 'none'
        });
    }
};

// 显示实验室选择器
const showLabPicker = () => {
    labPickerOptions.value = labs.value;
    tempLabIndex.value = labPickerIndex.value[0] || 0;
    showLabPickerModal.value = true;
};

const hideLabPicker = () => {
    showLabPickerModal.value = false;
};

// 实验室选择器变化
const onLabPickerChange = (e: any) => {
    const index = e.detail.value[0];
    tempLabIndex.value = index;
};

// 确认实验室选择
const confirmLabSelection = () => {
    const lab = labPickerOptions.value[tempLabIndex.value];
    labPickerIndex.value = [tempLabIndex.value];
    selectedLab.value = lab.id;
    selectedLabName.value = lab.name;
    showLabPickerModal.value = false;
    filterCourses();
};

// 取消实验室选择
const cancelLabSelection = () => {
    showLabPickerModal.value = false;
};

// 初始化picker-view日期数据
const initDatePickerData = (selectedDateArg: any = '') => {
    const today = new Date();
    const startYear = today.getFullYear() - 2;
    const newYears = [];
    for (let i = 0; i <= 2026 - startYear; i++) {
        newYears.push(startYear + i);
    }
    let year;
    let month;
    let day;
    if (selectedDateArg) {
        [year, month, day] = selectedDateArg.split('-').map(Number);
    } else {
        year = today.getFullYear();
        month = today.getMonth() + 1;
        day = today.getDate();
    }
    let newMonths;
    if (year === 2026) {
        newMonths = Array.from(
            {
                length: 9
            },
            (_, i) => i + 1
        );
        if (month > 9) {
            month = 9;
        }
    } else {
        newMonths = Array.from(
            {
                length: 12
            },
            (_, i) => i + 1
        );
    }
    const newDays = Array.from(
        {
            length: new Date(year, month, 0).getDate()
        },
        (_, i) => i + 1
    );
    years.value = newYears;
    months.value = newMonths;
    days.value = newDays;
    tempYearIndex.value = newYears.indexOf(year);
    tempMonthIndex.value = newMonths.indexOf(month);
    tempDayIndex.value = newDays.indexOf(day);
};

// 显示日期选择器
const showDatePicker = () => {
    initDatePickerData(selectedDate.value);
    showDateModal.value = true;
};

// 关闭日期选择器
const closeDateModal = () => {
    showDateModal.value = false;
};

// 日期改变
const onDateChange = (e: any) => {
    selectedDate.value = e.detail.value;
};

// 确认日期
const confirmDate = () => {
    const today = formatDate(new Date());
    const tomorrow = formatDate(new Date(Date.now() + 86400 * 1000));
    isToday.value = selectedDate.value === today;
    isTomorrow.value = selectedDate.value === tomorrow;
    showDateModal.value = false;
    filterCourses();
};

// 选择今天
const selectToday = () => {
    const today = formatDate(new Date());
    selectedDate.value = today;
    isToday.value = true;
    isTomorrow.value = false;
    viewMode.value = 'day';
    filterCourses();
};

// 选择明天
const selectTomorrow = () => {
    const tomorrow = formatDate(new Date(Date.now() + 86400 * 1000));
    selectedDate.value = tomorrow;
    isToday.value = false;
    isTomorrow.value = true;
    viewMode.value = 'day';
    filterCourses();
};

// 选择本周
const selectThisWeek = () => {
    viewMode.value = 'week';
};

// 切换视图模式
const switchViewMode = (e: any) => {
    const mode = e.currentTarget.dataset.mode;
    viewMode.value = mode;
};

// 上一周
const prevWeek = () => {
    const prevWeekDate = new Date(currentWeekStart.value!);
    prevWeekDate.setDate(prevWeekDate.getDate() - 7);
    initializeWeek(prevWeekDate);
    loadWeekCourses();
};

// 下一周
const nextWeek = () => {
    const nextWeekDate = new Date(currentWeekStart.value!);
    nextWeekDate.setDate(nextWeekDate.getDate() + 7);
    initializeWeek(nextWeekDate);
    loadWeekCourses();
};

// 查看课程详情
const viewCourseDetail = (e: any) => {
    const courseItem = e.currentTarget.dataset.course;
    selectedCourse.value = courseItem;
    showCourseModal.value = true;
};

// 关闭课程详情
const closeCourseModal = () => {
    showCourseModal.value = false;
    selectedCourse.value = null;
};

// 查看课程详情（仅供查看）
const viewCourseInfo = () => {
    uni.showToast({
        title: '仅供查看，无法预约',
        icon: 'none'
    });
};

// 跳转到预约申请页面
const goToReservation = () => {
    uni.navigateTo({
        url: '/pages/student-reservation-apply/student-reservation-apply'
    });
};

// 阻止事件冒泡
const stopPropagation = () => {
    // 阻止事件冒泡
};

const onDatePickerChange = (e: any) => {
    const [yearIdx, monthIdx, dayIdx] = e.detail.value;
    const year = years.value[yearIdx];
    let newMonths;
    if (year === 2026) {
        newMonths = Array.from(
            {
                length: 9
            },
            (_, i) => i + 1
        );
    } else {
        newMonths = Array.from(
            {
                length: 12
            },
            (_, i) => i + 1
        );
    }
    let month = newMonths[monthIdx];
    const newDays = Array.from(
        {
            length: new Date(year, month, 0).getDate()
        },
        (_, i) => i + 1
    );
    let newTempDayIndex = dayIdx;
    if (newTempDayIndex >= newDays.length) {
        newTempDayIndex = newDays.length - 1;
    }
    tempYearIndex.value = yearIdx;
    months.value = newMonths;
    tempMonthIndex.value = monthIdx;
    days.value = newDays;
    tempDayIndex.value = newTempDayIndex;
};

const confirmDatePicker = () => {
    const year = years.value[tempYearIndex.value];
    const month = months.value[tempMonthIndex.value];
    const day = days.value[tempDayIndex.value];
    const newSelectedDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    const today = formatDate(new Date());
    const tomorrow = formatDate(new Date(Date.now() + 86400 * 1000));
    selectedDate.value = newSelectedDate;
    isToday.value = newSelectedDate === today;
    isTomorrow.value = newSelectedDate === tomorrow;
    showDateModal.value = false;
    filterCourses();
};
</script>
<style lang="less">
@import './student-schedule-preview.less';
</style>
