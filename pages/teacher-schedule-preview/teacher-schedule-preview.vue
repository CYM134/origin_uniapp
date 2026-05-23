<template>
    <view>
        <!-- pages/teacher-schedule-preview/teacher-schedule-preview.wxml -->
        <navigation-bar title="课表预览" :back="true" color="white" background="#3a7bd5"></navigation-bar>

        <view class="container">
            <!-- 日期选择器 -->
            <view class="date-selector">
                <view class="date-picker" @tap="showDatePicker">
                    <text class="selected-date">{{ selectedDate }}</text>
                    <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                </view>
                <view class="quick-dates">
                    <view :class="'quick-date ' + (isToday ? 'active' : '')" @tap="selectToday">
                        <text>今天</text>
                    </view>
                    <view :class="'quick-date ' + (isTomorrow ? 'active' : '')" @tap="selectTomorrow">
                        <text>明天</text>
                    </view>
                    <view class="quick-date" @tap="selectThisWeek">
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

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
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
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            // 日期相关
            selectedDate: '',

            isToday: true,
            isTomorrow: false,
            showDateModal: false,

            // 视图模式
            viewMode: 'day' as 'day' | 'week',

            // 'day' | 'week'

            // 实验室筛选
            selectedLab: 'all',

            selectedLabName: '全部实验室',
            showLabPickerModal: false,
            labPickerIndex: [0],

            labs: [
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
            ],

            // 时间段
            timeSlots: [
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
            ] as TimeSlot[],

            // 课程数据
            courses: [] as Course[],

            filteredCourses: [] as Course[],

            // 周视图相关
            weekRange: '',

            weekDays: [] as WeekDay[],
            currentWeekStart: null as Date | null,

            // 课程详情弹窗
            showCourseDetail: false,

            selectedCourse: {} as Course,

            // picker-view日期选择相关
            years: [] as number[],

            months: [] as number[],
            days: [] as number[],
            tempYearIndex: 0,
            tempMonthIndex: 0,
            tempDayIndex: 0,

            labPickerOptions: [] as {
                id: string;
                name: string;
            }[],

            tempLabIndex: 0,

            // 教师信息
            teacherInfo: {} as TeacherInfo,

            // 统计信息
            todayStats: {
                totalCourses: 0,
                availableSlots: 0,
                totalStudents: 0
            } as TodayStats,

            dayIndex: 0,

            day: {
                courses: ''
            },

            type: '',
            name: '',
            lab: ''
        };
    },
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad() {
        this.loadTeacherInfo();
        this.initializeDate();
        this.loadCourses();
    },
    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
        this.loadCourses();
    },
    /**
     * 页面相关事件处理函数--监听用户下拉动作
     */
    onPullDownRefresh() {
        this.loadCourses();
        uni.stopPullDownRefresh();
    },
    methods: {
        /**
         * 加载教师信息
         */
        loadTeacherInfo() {
            try {
                const teacherInfo = uni.getStorageSync('teacherInfo');
                if (teacherInfo) {
                    this.setData({
                        teacherInfo: teacherInfo
                    });
                }
            } catch (error) {
                console.log('CatchClause', error);
                console.log('CatchClause', error);
                console.error('加载教师信息失败:', error);
            }
        },

        /**
         * 初始化日期
         */
        initializeDate() {
            const today = new Date();
            const year = today.getFullYear();
            const month = String(today.getMonth() + 1).padStart(2, '0');
            const day = String(today.getDate()).padStart(2, '0');
            const selectedDate = `${year}-${month}-${day}`;
            this.setData({
                selectedDate,
                isToday: true,
                isTomorrow: false
            });
            this.initializeWeek(today);
        },

        /**
         * 初始化周视图
         */
        initializeWeek(date: Date) {
            const startOfWeek = new Date(date);
            const day = startOfWeek.getDay();
            const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1); // 调整为周一开始
            startOfWeek.setDate(diff);
            const endOfWeek = new Date(startOfWeek);
            endOfWeek.setDate(startOfWeek.getDate() + 6);
            const weekDays: WeekDay[] = [];
            const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
            for (let i = 0; i < 7; i++) {
                const currentDay = new Date(startOfWeek);
                currentDay.setDate(startOfWeek.getDate() + i);
                weekDays.push({
                    name: dayNames[i],
                    date: `${currentDay.getMonth() + 1}/${currentDay.getDate()}`,
                    fullDate: this.formatDate(currentDay),
                    courses: []
                });
            }
            const weekRange = `${startOfWeek.getMonth() + 1}月${startOfWeek.getDate()}日 - ${endOfWeek.getMonth() + 1}月${endOfWeek.getDate()}日`;
            this.setData({
                weekRange,
                weekDays,
                currentWeekStart: startOfWeek
            });
        },

        /**
         * 格式化日期
         */
        formatDate(date: Date): string {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        },

        /**
         * 加载课程数据
         */
        loadCourses() {
            // 模拟课程数据
            const mockCourses: Course[] = [
                {
                    id: 'course1',
                    name: '数据结构与算法',
                    lab: '新商科实验室',
                    labId: 'lab2',
                    teacher: this.teacherInfo.name || '张教授',
                    startTime: '08:00',
                    endTime: '09:50',
                    date: this.selectedDate,
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
                    teacher: this.teacherInfo.name || '张教授',
                    startTime: '10:00',
                    endTime: '11:50',
                    date: this.selectedDate,
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
                    teacher: this.teacherInfo.name || '张教授',
                    startTime: '14:00',
                    endTime: '15:50',
                    date: this.selectedDate,
                    timeSlot: '14:00',
                    studentCount: 20,
                    type: 'practice',
                    typeText: '实践课',
                    remark: '学习软件开发的全生命周期管理，包括需求分析、系统设计、编码实现、测试和维护。'
                }
            ];
            this.setData({
                courses: mockCourses
            });
            this.filterCourses();
            this.loadWeekCourses();
            this.updateStatistics();
        },

        /**
         * 更新统计信息
         */
        updateStatistics() {
            const { courses } = this;
            const todayCourses = courses.filter((course) => course.date === this.selectedDate);
            this.setData({
                todayStats: {
                    totalCourses: todayCourses.length,
                    availableSlots: 5 - todayCourses.length,
                    totalStudents: todayCourses.reduce((sum, course) => sum + course.studentCount, 0)
                }
            });
        },

        /**
         * 筛选课程
         */
        filterCourses() {
            const { courses, selectedLab, selectedDate } = this;
            let filtered = courses.filter((course) => course.date === selectedDate);
            if (selectedLab !== 'all') {
                filtered = filtered.filter((course) => course.labId === selectedLab);
            }
            this.setData({
                filteredCourses: filtered
            });
        },

        /**
         * 加载周视图课程
         */
        loadWeekCourses() {
            const { courses, weekDays } = this;
            const updatedWeekDays = weekDays.map((day) => {
                const dayCourses = courses.filter((course) => course.date === day.fullDate);
                return {
                    ...day,
                    courses: dayCourses
                };
            });
            this.setData({
                weekDays: updatedWeekDays
            });
        },

        /**
         * 显示实验室选择器
         */
        showLabPicker() {
            this.setData({
                labPickerOptions: this.labs,
                tempLabIndex: this.labPickerIndex[0] || 0,
                showLabPickerModal: true
            });
        },

        /**
         * 隐藏实验室选择器
         */
        hideLabPicker() {
            this.setData({
                showLabPickerModal: false
            });
        },

        /**
         * 实验室选择器变化
         */
        onLabPickerChange(e: any) {
            const index = e.detail.value[0];
            this.setData({
                tempLabIndex: index
            });
        },

        /**
         * 确认实验室选择
         */
        confirmLabSelection() {
            const lab = this.labPickerOptions[this.tempLabIndex];
            this.setData({
                labPickerIndex: [this.tempLabIndex],
                selectedLab: lab.id,
                selectedLabName: lab.name,
                showLabPickerModal: false
            });
            this.filterCourses();
        },

        /**
         * 初始化picker-view日期数据
         */
        initDatePickerData(selectedDate: any = '') {
            const today = new Date();
            const startYear = today.getFullYear() - 2;
            const years = [];
            for (let i = 0; i <= 2026 - startYear; i++) {
                years.push(startYear + i);
            }
            let year;
            let month;
            let day;
            if (selectedDate) {
                [year, month, day] = selectedDate.split('-').map(Number);
            } else {
                year = today.getFullYear();
                month = today.getMonth() + 1;
                day = today.getDate();
            }
            let months;
            if (year === 2026) {
                months = Array.from(
                    {
                        length: 9
                    },
                    (_, i) => i + 1
                );
                if (month > 9) {
                    month = 9;
                }
            } else {
                months = Array.from(
                    {
                        length: 12
                    },
                    (_, i) => i + 1
                );
            }
            const days = Array.from(
                {
                    length: new Date(year, month, 0).getDate()
                },
                (_, i) => i + 1
            );
            const tempYearIndex = years.indexOf(year);
            const tempMonthIndex = months.indexOf(month);
            const tempDayIndex = days.indexOf(day);
            this.setData({
                years,
                months,
                days,
                tempYearIndex,
                tempMonthIndex,
                tempDayIndex
            });
        },

        /**
         * 显示日期选择器
         */
        showDatePicker() {
            this.initDatePickerData(this.selectedDate);
            this.setData({
                showDateModal: true
            });
        },

        /**
         * 关闭日期选择器
         */
        closeDateModal() {
            this.setData({
                showDateModal: false
            });
        },

        /**
         * 确认日期
         */
        confirmDatePicker() {
            const year = this.years[this.tempYearIndex];
            const month = this.months[this.tempMonthIndex];
            const day = this.days[this.tempDayIndex];
            const selectedDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const today = this.formatDate(new Date());
            const tomorrow = this.formatDate(new Date(Date.now() + 86400 * 1000));
            this.setData({
                selectedDate,
                isToday: selectedDate === today,
                isTomorrow: selectedDate === tomorrow,
                showDateModal: false
            });
            this.filterCourses();
            this.updateStatistics();
        },

        /**
         * 日期选择器变化
         */
        onDatePickerChange(e: any) {
            const [yearIdx, monthIdx, dayIdx] = e.detail.value;
            const year = this.years[yearIdx];
            let months;
            if (year === 2026) {
                months = Array.from(
                    {
                        length: 9
                    },
                    (_, i) => i + 1
                );
            } else {
                months = Array.from(
                    {
                        length: 12
                    },
                    (_, i) => i + 1
                );
            }
            let month = months[monthIdx];
            const days = Array.from(
                {
                    length: new Date(year, month, 0).getDate()
                },
                (_, i) => i + 1
            );
            let tempDayIndex = dayIdx;
            if (tempDayIndex >= days.length) {
                tempDayIndex = days.length - 1;
            }
            this.setData({
                tempYearIndex: yearIdx,
                months,
                tempMonthIndex: monthIdx,
                days,
                tempDayIndex
            });
        },

        /**
         * 选择今天
         */
        selectToday() {
            const today = this.formatDate(new Date());
            this.setData({
                selectedDate: today,
                isToday: true,
                isTomorrow: false,
                viewMode: 'day'
            });
            this.filterCourses();
            this.updateStatistics();
        },

        /**
         * 选择明天
         */
        selectTomorrow() {
            const tomorrow = this.formatDate(new Date(Date.now() + 86400 * 1000));
            this.setData({
                selectedDate: tomorrow,
                isToday: false,
                isTomorrow: true,
                viewMode: 'day'
            });
            this.filterCourses();
            this.updateStatistics();
        },

        /**
         * 选择本周
         */
        selectThisWeek() {
            this.setData({
                viewMode: 'week'
            });
        },

        /**
         * 切换视图模式
         */
        switchViewMode(e: any) {
            const mode = e.currentTarget.dataset.mode;
            this.setData({
                viewMode: mode
            });
        },

        /**
         * 上一周
         */
        prevWeek() {
            const { currentWeekStart } = this;
            if (currentWeekStart) {
                const prevWeek = new Date(currentWeekStart);
                prevWeek.setDate(prevWeek.getDate() - 7);
                this.initializeWeek(prevWeek);
                this.loadWeekCourses();
            }
        },

        /**
         * 下一周
         */
        nextWeek() {
            const { currentWeekStart } = this;
            if (currentWeekStart) {
                const nextWeek = new Date(currentWeekStart);
                nextWeek.setDate(nextWeek.getDate() + 7);
                this.initializeWeek(nextWeek);
                this.loadWeekCourses();
            }
        },

        /**
         * 查看课程详情
         */
        showCourseDetailFun(e: any) {
            const course = e.currentTarget.dataset.course;
            this.setData({
                selectedCourse: course,
                showCourseDetail: true
            });
        },

        /**
         * 关闭课程详情
         */
        closeCourseDetail() {
            this.setData({
                showCourseDetail: false,
                selectedCourse: {} as Course
            });
        },

        /**
         * 快速预约
         */
        quickReservation() {
            uni.navigateTo({
                url: '/pages/teacher-reservation-apply/teacher-reservation-apply?quick=true&date=' + this.selectedDate
            });
        },

        /**
         * 阻止事件冒泡
         */
        stopPropagation() {
            // 阻止事件冒泡
        }
    }
});
</script>
<style>
@import './teacher-schedule-preview.css';
</style>
