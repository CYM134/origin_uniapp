<template>
    <view class="page-wrapper">
        <!-- pages/teacher-schedule-preview/teacher-schedule-preview.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="课表预览" color="white" background="#10B981" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <navigation-bar title="课表预览" :back="true" color="white" background="#10B981"></navigation-bar>
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
                            <text class="time-text">{{ item.time }}</text>
                        </view>
                    </view>

                    <!-- 课程列表 -->
                    <view class="course-list">
                        <view v-if="filteredCourses.length === 0" class="empty-state">
                            <image src="/static/images/icons/empty-schedule.svg" mode="aspectFit"></image>
                            <text>暂无课程安排</text>
                        </view>
                        <view class="course-item" @tap="showCourseDetailFun" :data-course="item" v-for="(item, index) in filteredCourses" :key="index">
                            <view class="course-time">
                                <text class="start-time">{{ item.startTime }}</text>
                                <text class="end-time">{{ item.endTime }}</text>
                            </view>

                            <view class="course-info">
                                <view class="course-header">
                                    <text class="course-name">{{ item.name }}</text>
                                    <view :class="'course-status ' + item.type">
                                        <text>{{ item.typeText }}</text>
                                    </view>
                                </view>
                                <view class="course-details">
                                    <text class="lab-name">{{ item.lab }}</text>
                                    <text class="teacher-name">{{ item.teacher }}</text>
                                </view>
                                <view class="course-meta">
                                    <text class="student-count">{{ item.studentCount }}人</text>
                                    <text class="course-type">{{ item.typeText }}</text>
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
                                    <text>{{ item.time }}</text>
                                </view>

                                <view class="course-cell" v-for="(day, dayIndex) in weekDays" :key="dayIndex">
                                    <view v-if="day.courses[index]" :class="'week-course ' + day.courses[index].type" @tap="showCourseDetailFun" :data-course="day.courses[index]">
                                        <text class="course-name">{{ day.courses[index].name }}</text>
                                        <text class="lab-name">{{ day.courses[index].lab }}</text>
                                    </view>
                                </view>
                            </view>
                        </scroll-view>
                    </view>
                </view>
            </view>

            <!-- 统计信息 -->
            <view class="statistics">
                <view class="stat-item">
                    <view class="stat-number">{{ todayStats.totalCourses }}</view>
                    <view class="stat-label">今日课程</view>
                </view>
                <view class="stat-item">
                    <view class="stat-number">{{ todayStats.availableSlots }}</view>
                    <view class="stat-label">空闲时段</view>
                </view>
                <view class="stat-item">
                    <view class="stat-number">{{ todayStats.totalStudents }}</view>
                    <view class="stat-label">学生人数</view>
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
                <view class="action-btn" @tap="quickReservation">
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
        <view v-if="showCourseDetail" class="modal-overlay" @tap="closeCourseDetail">
            <view class="modal-content course-modal" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">课程详情</text>
                    <view class="close-btn" @tap="closeCourseDetail">×</view>
                </view>
                <view class="modal-body">
                    <view class="course-detail">
                        <view class="detail-item">
                            <text class="label">课程名称：</text>
                            <text class="value">{{ selectedCourse.name }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">实验室：</text>
                            <text class="value">{{ selectedCourse.lab }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">授课教师：</text>
                            <text class="value">{{ selectedCourse.teacher }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">上课时间：</text>
                            <text class="value">{{ selectedCourse.startTime || selectedCourse.time }} - {{ selectedCourse.endTime || '' }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">课程类型：</text>
                            <text class="value">{{ selectedCourse.typeText }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">人数限制：</text>
                            <text class="value">{{ selectedCourse.studentCount }}人</text>
                        </view>
                        <view class="detail-item">
                            <text class="label">课程描述：</text>
                            <text class="value description">{{ selectedCourse.remark || '暂无描述' }}</text>
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
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
// pages/teacher-schedule-preview/teacher-schedule-preview.ts

interface TeacherInfo {
    name: string;
    [key: string]: any;
}
interface Course {
    id: string;
    name: string;
    lab: string;
    labId: string;
    teacher: string;
    startTime: string;
    endTime: string;
    time?: string;
    date: string;
    timeSlot: string;
    studentCount: number;
    type: string;
    typeText: string;
    remark?: string;
}
interface WeekDay {
    name: string;
    date: string;
    fullDate: string;
    courses: Course[];
}
interface TimeSlot {
    time: string;
    course: Course | null;
}
interface TodayStats {
    totalCourses: number;
    availableSlots: number;
    totalStudents: number;
}

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
    },
    {
        id: 'lab1',
        name: '国际课程实验室'
    },
    {
        id: 'lab2',
        name: '新商科实验室'
    },
    {
        id: 'lab3',
        name: 'VR实验室'
    },
    {
        id: 'lab4',
        name: '法语实验室'
    },
    {
        id: 'lab5',
        name: '402实验室'
    }
]);

// 时间段
const timeSlots = ref<TimeSlot[]>([
    {
        time: '08:00',
        course: null
    },
    {
        time: '09:00',
        course: null
    },
    {
        time: '10:00',
        course: null
    },
    {
        time: '11:00',
        course: null
    },
    {
        time: '14:00',
        course: null
    },
    {
        time: '15:00',
        course: null
    },
    {
        time: '16:00',
        course: null
    },
    {
        time: '17:00',
        course: null
    },
    {
        time: '19:00',
        course: null
    },
    {
        time: '20:00',
        course: null
    },
    {
        time: '21:00',
        course: null
    }
]);

// 课程数据
const courses = ref<Course[]>([]);
const filteredCourses = ref<Course[]>([]);

// 周视图相关
const weekRange = ref<string>('');
const weekDays = ref<WeekDay[]>([]);
const currentWeekStart = ref<Date | null>(null);

// 课程详情弹窗
const showCourseDetail = ref<boolean>(false);
const selectedCourse = ref<Course>({} as Course);

// picker-view日期选择相关
const years = ref<number[]>([]);
const months = ref<number[]>([]);
const days = ref<number[]>([]);
const tempYearIndex = ref<number>(0);
const tempMonthIndex = ref<number>(0);
const tempDayIndex = ref<number>(0);

const labPickerOptions = ref<{ id: string; name: string }[]>([]);
const tempLabIndex = ref<number>(0);

// 教师信息
const teacherInfo = ref<TeacherInfo>({} as TeacherInfo);

// 统计信息
const todayStats = ref<TodayStats>({
    totalCourses: 0,
    availableSlots: 0,
    totalStudents: 0
});

const dayIndex = ref<number>(0);

const day = ref<any>({
    courses: ''
});

const type = ref<string>('');
const name = ref<string>('');
const lab = ref<string>('');

/**
 * 生命周期函数--监听页面加载
 */
onLoad(() => {
    loadTeacherInfo();
    initializeDate();
    loadCourses();
});

/**
 * 生命周期函数--监听页面显示
 */
onShow(() => {
    loadCourses();
});

/**
 * 页面相关事件处理函数--监听用户下拉动作
 */
onPullDownRefresh(() => {
    loadCourses();
    uni.stopPullDownRefresh();
});

/**
 * 加载教师信息
 */
const loadTeacherInfo = () => {
    try {
        const info = uni.getStorageSync('teacherInfo');
        if (info) {
            teacherInfo.value = info;
        }
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载教师信息失败:', error);
    }
};

/**
 * 初始化日期
 */
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

/**
 * 初始化周视图
 */
const initializeWeek = (date: Date) => {
    const startOfWeek = new Date(date);
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1); // 调整为周一开始
    startOfWeek.setDate(diff);
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    const newWeekDays: WeekDay[] = [];
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

/**
 * 格式化日期
 */
const formatDate = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

/**
 * 加载课程数据
 */
const loadCourses = () => {
    // 模拟课程数据
    const mockCourses: Course[] = [
        {
            id: 'course1',
            name: '数据结构与算法',
            lab: '新商科实验室',
            labId: 'lab2',
            teacher: teacherInfo.value.name || '张教授',
            startTime: '08:00',
            endTime: '09:50',
            date: selectedDate.value,
            timeSlot: '08:00',
            studentCount: 25,
            type: 'theory',
            typeText: '理论课',
            remark: '学习基本的数据结构和算法设计方法，包括线性表、栈、队列、树、图等数据结构的实现和应用。'
        },
        {
            id: 'course2',
            name: '计算机网络',
            lab: '国际课程实验室',
            labId: 'lab1',
            teacher: teacherInfo.value.name || '张教授',
            startTime: '10:00',
            endTime: '11:50',
            date: selectedDate.value,
            timeSlot: '10:00',
            studentCount: 30,
            type: 'theory',
            typeText: '理论课',
            remark: '深入学习计算机网络的基本原理、协议和技术，包括TCP/IP协议栈、网络安全等内容。'
        },
        {
            id: 'course3',
            name: '软件工程',
            lab: '法语实验室',
            labId: 'lab4',
            teacher: teacherInfo.value.name || '张教授',
            startTime: '14:00',
            endTime: '15:50',
            date: selectedDate.value,
            timeSlot: '14:00',
            studentCount: 20,
            type: 'practice',
            typeText: '实践课',
            remark: '学习软件开发的全生命周期管理，包括需求分析、系统设计、编码实现、测试和维护。'
        }
    ];
    courses.value = mockCourses;
    filterCourses();
    loadWeekCourses();
    updateStatistics();
};

/**
 * 更新统计信息
 */
const updateStatistics = () => {
    const todayCourses = courses.value.filter((course) => course.date === selectedDate.value);
    todayStats.value = {
        totalCourses: todayCourses.length,
        availableSlots: 5 - todayCourses.length,
        totalStudents: todayCourses.reduce((sum, course) => sum + course.studentCount, 0)
    };
};

/**
 * 筛选课程
 */
const filterCourses = () => {
    let filtered = courses.value.filter((course) => course.date === selectedDate.value);
    if (selectedLab.value !== 'all') {
        filtered = filtered.filter((course) => course.labId === selectedLab.value);
    }
    filteredCourses.value = filtered;
};

/**
 * 加载周视图课程
 */
const loadWeekCourses = () => {
    const updatedWeekDays = weekDays.value.map((day) => {
        const dayCourses = courses.value.filter((course) => course.date === day.fullDate);
        return {
            ...day,
            courses: dayCourses
        };
    });
    weekDays.value = updatedWeekDays;
};

/**
 * 显示实验室选择器
 */
const showLabPicker = () => {
    labPickerOptions.value = labs.value;
    tempLabIndex.value = labPickerIndex.value[0] || 0;
    showLabPickerModal.value = true;
};

/**
 * 隐藏实验室选择器
 */
const hideLabPicker = () => {
    showLabPickerModal.value = false;
};

/**
 * 实验室选择器变化
 */
const onLabPickerChange = (e: any) => {
    const index = e.detail.value[0];
    tempLabIndex.value = index;
};

/**
 * 确认实验室选择
 */
const confirmLabSelection = () => {
    const labItem = labPickerOptions.value[tempLabIndex.value];
    labPickerIndex.value = [tempLabIndex.value];
    selectedLab.value = labItem.id;
    selectedLabName.value = labItem.name;
    showLabPickerModal.value = false;
    filterCourses();
};

/**
 * 初始化picker-view日期数据
 */
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

/**
 * 显示日期选择器
 */
const showDatePicker = () => {
    initDatePickerData(selectedDate.value);
    showDateModal.value = true;
};

/**
 * 关闭日期选择器
 */
const closeDateModal = () => {
    showDateModal.value = false;
};

/**
 * 确认日期
 */
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
    updateStatistics();
};

/**
 * 日期选择器变化
 */
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

/**
 * 选择今天
 */
const selectToday = () => {
    const today = formatDate(new Date());
    selectedDate.value = today;
    isToday.value = true;
    isTomorrow.value = false;
    viewMode.value = 'day';
    filterCourses();
    updateStatistics();
};

/**
 * 选择明天
 */
const selectTomorrow = () => {
    const tomorrow = formatDate(new Date(Date.now() + 86400 * 1000));
    selectedDate.value = tomorrow;
    isToday.value = false;
    isTomorrow.value = true;
    viewMode.value = 'day';
    filterCourses();
    updateStatistics();
};

/**
 * 选择本周
 */
const selectThisWeek = () => {
    viewMode.value = 'week';
};

/**
 * 切换视图模式
 */
const switchViewMode = (e: any) => {
    const mode = e.currentTarget.dataset.mode;
    viewMode.value = mode;
};

/**
 * 上一周
 */
const prevWeek = () => {
    if (currentWeekStart.value) {
        const prevWeekDate = new Date(currentWeekStart.value);
        prevWeekDate.setDate(prevWeekDate.getDate() - 7);
        initializeWeek(prevWeekDate);
        loadWeekCourses();
    }
};

/**
 * 下一周
 */
const nextWeek = () => {
    if (currentWeekStart.value) {
        const nextWeekDate = new Date(currentWeekStart.value);
        nextWeekDate.setDate(nextWeekDate.getDate() + 7);
        initializeWeek(nextWeekDate);
        loadWeekCourses();
    }
};

/**
 * 查看课程详情
 */
const showCourseDetailFun = (e: any) => {
    const courseItem = e.currentTarget.dataset.course;
    selectedCourse.value = courseItem;
    showCourseDetail.value = true;
};

/**
 * 关闭课程详情
 */
const closeCourseDetail = () => {
    showCourseDetail.value = false;
    selectedCourse.value = {} as Course;
};

/**
 * 快速预约
 */
const quickReservation = () => {
    uni.navigateTo({
        url: '/pages/teacher-reservation-apply/teacher-reservation-apply?quick=true&date=' + selectedDate.value
    });
};

/**
 * 阻止事件冒泡
 */
const stopPropagation = () => {
    // 阻止事件冒泡
};
</script>
<style lang="less">
@import './teacher-schedule-preview.less';
</style>
