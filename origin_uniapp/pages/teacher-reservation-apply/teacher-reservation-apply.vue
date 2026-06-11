<template>
    <view class="page-wrapper">
        <!-- pages/teacher-reservation-apply/teacher-reservation-apply.wxml -->
        <navigation-bar title="预约申请" :back="true" color="white" background="#10B981"></navigation-bar>

        <view class="container">
            <!-- 申请表单 -->
            <view class="form-section">
                <view class="section-title">预约信息</view>

                <!-- 实验室选择 -->
                <view class="form-item" id="labItem">
                    <view class="form-label">
                        选择实验室
                        <text class="required">*</text>
                    </view>
                    <view :class="'picker-display ' + (!selectedLab.name ? 'placeholder' : '')" @tap="showLabPicker">
                        <text>{{ selectedLab.name || '请选择实验室' }}</text>
                        <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                    </view>
                    <view v-if="errorLab" class="error-tip">{{ errorLab }}</view>
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

                <!-- 日期选择 -->
                <view class="form-item" id="dateItem">
                    <view class="form-label">
                        预约日期
                        <text class="required">*</text>
                    </view>
                    <view :class="'picker-display ' + (!selectedDate ? 'placeholder' : '')" @tap="showDatePicker">
                        <text>{{ selectedDate || '请选择日期' }}</text>
                        <image class="arrow-icon" src="/static/images/icons/calendar.svg" mode="aspectFit"></image>
                    </view>
                    <view v-if="errorDate" class="error-tip">{{ errorDate }}</view>
                </view>
                <!-- 日期选择器弹窗 -->
                <view v-if="showDateModal" class="modal-overlay" @tap="hideDatePicker">
                    <view class="modal-content picker-modal-content custom-picker-modal" @tap.stop.prevent="stopPropagation">
                        <view class="modal-header">
                            <text>选择日期</text>
                            <view class="close-btn" @tap="hideDatePicker">×</view>
                        </view>
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
                        <view class="modal-footer">
                            <button class="cancel-btn" @tap="hideDatePicker">取消</button>
                            <button class="confirm-btn" @tap="confirmDatePicker">确定</button>
                        </view>
                    </view>
                </view>

                <!-- 时间段选择 -->
                <view class="form-item" id="timeItem">
                    <view class="form-label">
                        时间段
                        <text class="required">*</text>
                    </view>
                    <view class="time-picker-row">
                        <view class="picker-display" @tap="showStartTimePickerFun">
                            <text>{{ startTime || '开始时间' }}</text>
                            <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                        </view>
                        <text style="margin: 0 16rpx">-</text>
                        <view class="picker-display" @tap="showEndTimePickerFun">
                            <text>{{ endTime || '结束时间' }}</text>
                            <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                        </view>
                    </view>
                    <view v-if="errorTime" class="error-tip">{{ errorTime }}</view>
                </view>
                <!-- 开始时间picker-view弹窗 -->
                <view v-if="showStartTimePicker" class="modal-overlay" @tap="hideStartTimePicker">
                    <view class="modal-content picker-modal-content custom-picker-modal" @tap.stop.prevent="stopPropagation">
                        <view class="modal-header">
                            <text>选择开始时间</text>
                            <view class="close-btn" @tap="hideStartTimePicker">×</view>
                        </view>
                        <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[startHourIndex, startMinuteIndex]" @change="onStartTimeChange">
                            <picker-view-column>
                                <view class="picker-view-item" v-for="(item, index) in hours" :key="index">{{ item }}时</view>
                            </picker-view-column>
                            <picker-view-column>
                                <view class="picker-view-item" v-for="(item, index) in minutes" :key="index">{{ item }}分</view>
                            </picker-view-column>
                        </picker-view>
                        <view class="modal-footer">
                            <button class="cancel-btn" @tap="hideStartTimePicker">取消</button>
                            <button class="confirm-btn" @tap="confirmStartTime">确定</button>
                        </view>
                    </view>
                </view>
                <!-- 结束时间picker-view弹窗 -->
                <view v-if="showEndTimePicker" class="modal-overlay" @tap="hideEndTimePicker">
                    <view class="modal-content picker-modal-content custom-picker-modal" @tap.stop.prevent="stopPropagation">
                        <view class="modal-header">
                            <text>选择结束时间</text>
                            <view class="close-btn" @tap="hideEndTimePicker">×</view>
                        </view>
                        <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[endHourIndex, endMinuteIndex]" @change="onEndTimeChange">
                            <picker-view-column>
                                <view class="picker-view-item" v-for="(item, index) in hours" :key="index">{{ item }}时</view>
                            </picker-view-column>
                            <picker-view-column>
                                <view class="picker-view-item" v-for="(item, index) in minutes" :key="index">{{ item }}分</view>
                            </picker-view-column>
                        </picker-view>
                        <view class="modal-footer">
                            <button class="cancel-btn" @tap="hideEndTimePicker">取消</button>
                            <button class="confirm-btn" @tap="confirmEndTime">确定</button>
                        </view>
                    </view>
                </view>

                <!-- 课程信息 -->
                <view class="form-item" id="courseItem">
                    <view class="form-label">
                        课程名称
                        <text class="required">*</text>
                    </view>
                    <input class="form-input" placeholder="请输入课程名称" :value="courseName" @input="onCourseNameInput" />
                    <view v-if="errorCourseName" class="error-tip">{{ errorCourseName }}</view>
                </view>

                <view class="form-item" id="countItem">
                    <view class="form-label">
                        预计学生人数
                        <text class="required">*</text>
                    </view>
                    <input class="form-input" type="number" placeholder="请输入学生人数" :value="studentCount" @input="onStudentCountInput" />
                    <view v-if="errorStudentCount" class="error-tip">{{ errorStudentCount }}</view>
                </view>

                <!-- 课程类型 -->
                <view class="form-item">
                    <view class="form-label">课程类型</view>
                    <view class="course-types">
                        <view
                            :class="'course-type ' + (item.selected ? 'selected' : '')"
                            @tap="selectCourseType"
                            :data-index="index"
                            v-for="(item, index) in courseTypes"
                            :key="index"
                        >
                            <text>{{ item.name }}</text>
                        </view>
                    </view>
                </view>

                <!-- 备注信息 -->
                <view class="form-item">
                    <view class="form-label">备注说明</view>
                    <textarea class="form-textarea" placeholder="请输入备注信息（选填）" :value="remark" @input="onRemarkInput" maxlength="200"></textarea>
                    <view class="char-count">{{ remark.length }}/200</view>
                </view>
            </view>

            <!-- 实验室信息 -->
            <view v-if="selectedLab.name" class="lab-info">
                <view class="section-title">实验室信息</view>
                <view class="info-card">
                    <view class="info-row">
                        <text class="info-label">实验室名称：</text>
                        <text class="info-value">{{ selectedLab.name }}</text>
                    </view>
                    <view class="info-row">
                        <text class="info-label">容纳人数：</text>
                        <text class="info-value">{{ selectedLab.capacity }}人</text>
                    </view>
                    <view class="info-row">
                        <text class="info-label">设备配置：</text>
                        <text class="info-value">{{ selectedLab.equipment }}</text>
                    </view>
                    <view class="info-row">
                        <text class="info-label">位置：</text>
                        <text class="info-value">{{ selectedLab.location }}</text>
                    </view>
                </view>
            </view>

            <!-- 提交按钮 -->
            <view class="submit-section">
                <button class="submit-btn" @tap="submitApplication" :disabled="!canSubmit">提交申请</button>
                <button class="draft-btn" @tap="saveDraft">保存草稿</button>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>

        <!-- 确认提交弹窗 -->
        <view v-if="showConfirmModal" class="modal-overlay" @tap="closeConfirmModal">
            <view class="modal-content" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">确认提交</text>
                    <view class="close-btn" @tap="closeConfirmModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="confirm-info">
                        <view class="confirm-item">
                            <text class="confirm-label">实验室：</text>
                            <text class="confirm-value">{{ selectedLab.name }}</text>
                        </view>
                        <view class="confirm-item">
                            <text class="confirm-label">日期：</text>
                            <text class="confirm-value">{{ selectedDate }}</text>
                        </view>
                        <view class="confirm-item">
                            <text class="confirm-label">时间：</text>
                            <text class="confirm-value">{{ selectedTimeText }}</text>
                        </view>
                        <view class="confirm-item">
                            <text class="confirm-label">课程：</text>
                            <text class="confirm-value">{{ courseName }}</text>
                        </view>
                        <view class="confirm-item">
                            <text class="confirm-label">学生人数：</text>
                            <text class="confirm-value">{{ studentCount }}人</text>
                        </view>
                    </view>
                    <view class="confirm-note">
                        <text>提交后将等待管理员审核，请确认信息无误。</text>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="cancel-btn" @tap="closeConfirmModal">取消</button>
                    <button class="confirm-btn" @tap="confirmSubmit">确认提交</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
// pages/teacher-reservation-apply/teacher-reservation-apply.ts
interface Lab {
    id: string;
    name: string;
    capacity: number;
    equipment: string;
    location: string;
}
interface TeacherInfo {
    teacherId: string;
    name: string;
    phone?: string;
    email?: string;
    department?: string;
}
interface CourseType {
    name: string;
    selected: boolean;
}

const teacherInfo = ref<TeacherInfo>({} as TeacherInfo);
const labOptions = ref<Lab[]>([
    {
        id: 'lab001',
        name: '国际课程实验室',
        capacity: 65,
        equipment: '高配置电脑、投影仪、白板',
        location: '综合楼东A301'
    },
    {
        id: 'lab002',
        name: '新商科实验室',
        capacity: 80,
        equipment: '标准配置电脑、投影仪',
        location: '综合楼西A303'
    },
    {
        id: 'lab003',
        name: '法语实验室',
        capacity: 60,
        equipment: '网络设备、服务器、交换机',
        location: '综合楼西A305'
    },
    {
        id: 'lab004',
        name: '402实验室',
        capacity: 80,
        equipment: '开发环境、测试工具',
        location: '综合楼西A402'
    }
]);
const selectedLabIndex = ref<number>(-1);
const selectedLab = ref<Lab>({} as Lab);
const selectedDate = ref<string>('');
const minDate = ref<string>('');
const maxDate = ref<string>('');
const courseName = ref<string>('');
const studentCount = ref<string>('');
const courseTypes = ref<CourseType[]>([
    {
        name: '理论课',
        selected: false
    },
    {
        name: '实验课',
        selected: true
    },
    {
        name: '考试',
        selected: false
    },
    {
        name: '其它活动',
        selected: false
    }
]);
const remark = ref<string>('');
const canSubmit = ref<boolean>(false);
const showConfirmModal = ref<boolean>(false);
const selectedTimeText = ref<string>('');
const isQuickMode = ref<boolean>(false);
// 实验室选择器相关
const showLabPickerModal = ref<boolean>(false);
const labPickerOptions = ref<Lab[]>([]);
const tempLabIndex = ref<number>(0);
// 日期选择器相关
const showDateModal = ref<boolean>(false);
const years = ref<number[]>([]);
const months = ref<number[]>([]);
const days = ref<number[]>([]);
const tempYearIndex = ref<number>(0);
const tempMonthIndex = ref<number>(0);
const tempDayIndex = ref<number>(0);
// 时间选择器相关
const showStartTimePicker = ref<boolean>(false);
const showEndTimePicker = ref<boolean>(false);
const hours = ref<number[]>(Array.from(
    {
        length: 24
    },
    (_, i) => i
));
const minutes = ref<number[]>(Array.from(
    {
        length: 60
    },
    (_, i) => i
));
const startHourIndex = ref<number>(8);
const startMinuteIndex = ref<number>(0);
const endHourIndex = ref<number>(10);
const endMinuteIndex = ref<number>(0);
const startTime = ref<string>('');
const endTime = ref<string>('');
// 错误提示
const errorLab = ref<string>('');
const errorDate = ref<string>('');
const errorTime = ref<string>('');
const errorCourseName = ref<string>('');
const errorStudentCount = ref<string>('');

/**
 * 生命周期函数--监听页面加载
 */
onLoad((options: any) => {
    loadTeacherInfo();
    initDates();

    // 检查是否是快速预约模式
    if (options.quick === 'true') {
        isQuickMode.value = true;
    }

    // 如果有传入日期，设置默认日期
    if (options.date) {
        selectedDate.value = options.date;
    }
    validateForm();
});

/**
 * 生命周期函数--监听页面显示
 */
onShow(() => {
    // 尝试加载草稿
    loadDraft();
});

/**
 * 加载教师信息
 */
const loadTeacherInfo = () => {
    try {
        const _teacherInfo = uni.getStorageSync('teacherInfo');
        if (_teacherInfo) {
            // 确保教师信息包含必要的姓名和电话字段
            const completeTeacherInfo = {
                teacherId: _teacherInfo.teacherId || '',
                name: _teacherInfo.name || _teacherInfo.teacherName || '',
                phone: _teacherInfo.phone || _teacherInfo.phoneNumber || '',
                email: _teacherInfo.email || '',
                department: _teacherInfo.department || ''
            };
            teacherInfo.value = completeTeacherInfo;

            // 验证必要信息是否完整
            if (!completeTeacherInfo.name || !completeTeacherInfo.phone) {
                uni.showModal({
                    title: '信息不完整',
                    content: '申请人姓名或电话号码信息缺失，请先完善个人信息',
                    showCancel: false,
                    success: () => {
                        uni.navigateTo({
                            url: '/pages/teacher-personal-info/teacher-personal-info'
                        });
                    }
                });
            }
        } else {
            // 如果没有教师信息，跳转到登录页面
            uni.showModal({
                title: '未登录',
                content: '请先登录后再进行预约申请',
                showCancel: false,
                success: () => {
                    uni.redirectTo({
                        url: '/pages/teacher-login/teacher-login'
                    });
                }
            });
        }
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载教师信息失败:', error);
        uni.showToast({
            title: '获取用户信息失败',
            icon: 'error'
        });
    }
};

/**
 * 初始化日期范围
 */
const initDates = () => {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    const _maxDate = new Date(today);
    _maxDate.setDate(today.getDate() + 30); // 最多提前30天预约

    minDate.value = formatDate(tomorrow);
    maxDate.value = formatDate(_maxDate);

    // 初始化日期选择器数据
    initDatePickerData();
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
 * 初始化日期选择器数据
 */
const initDatePickerData = (selDate: any = '') => {
    const today = new Date();
    const startYear = today.getFullYear();
    const _years = [];
    for (let i = 0; i <= 2; i++) {
        _years.push(startYear + i);
    }
    let year;
    let month;
    let day;
    if (selDate) {
        [year, month, day] = selDate.split('-').map(Number);
    } else {
        year = today.getFullYear();
        month = today.getMonth() + 1;
        day = today.getDate() + 1; // 默认明天
    }
    const _months = Array.from(
        {
            length: 12
        },
        (_, i) => i + 1
    );
    const _days = Array.from(
        {
            length: new Date(year, month, 0).getDate()
        },
        (_, i) => i + 1
    );
    const _tempYearIndex = _years.indexOf(year);
    const _tempMonthIndex = _months.indexOf(month) !== -1 ? _months.indexOf(month) : 0;
    const _tempDayIndex = _days.indexOf(day) !== -1 ? _days.indexOf(day) : 0;
    years.value = _years;
    months.value = _months;
    days.value = _days;
    tempYearIndex.value = _tempYearIndex;
    tempMonthIndex.value = _tempMonthIndex;
    tempDayIndex.value = _tempDayIndex;
};

/**
 * 显示实验室选择器
 */
const showLabPicker = () => {
    labPickerOptions.value = labOptions.value;
    tempLabIndex.value = selectedLabIndex.value === -1 ? 0 : selectedLabIndex.value;
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
    const lab = labPickerOptions.value[tempLabIndex.value];
    selectedLabIndex.value = tempLabIndex.value;
    selectedLab.value = lab;
    showLabPickerModal.value = false;
    errorLab.value = '';
    validateForm();
};

/**
 * 显示日期选择器
 */
const showDatePicker = () => {
    initDatePickerData(selectedDate.value);
    showDateModal.value = true;
};

/**
 * 隐藏日期选择器
 */
const hideDatePicker = () => {
    showDateModal.value = false;
};

/**
 * 日期选择器变化
 */
const onDatePickerChange = (e: any) => {
    const [yearIdx, monthIdx, dayIdx] = e.detail.value;
    const year = years.value[yearIdx];
    const month = months.value[monthIdx];

    // 根据年月重新计算天数
    const _days = Array.from(
        {
            length: new Date(year, month, 0).getDate()
        },
        (_, i) => i + 1
    );

    // 确保选中的日不超过当月最大天数
    let _tempDayIndex = dayIdx;
    if (_tempDayIndex >= _days.length) {
        _tempDayIndex = _days.length - 1;
    }
    tempYearIndex.value = yearIdx;
    tempMonthIndex.value = monthIdx;
    days.value = _days;
    tempDayIndex.value = _tempDayIndex;
};

/**
 * 确认日期选择
 */
const confirmDatePicker = () => {
    const year = years.value[tempYearIndex.value];
    const month = months.value[tempMonthIndex.value];
    const day = days.value[tempDayIndex.value];
    const _selectedDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    selectedDate.value = _selectedDate;
    showDateModal.value = false;
    errorDate.value = '';
    validateForm();
};

/**
 * 显示开始时间选择器
 */
const showStartTimePickerFun = () => {
    showStartTimePicker.value = true;
};

/**
 * 隐藏开始时间选择器
 */
const hideStartTimePicker = () => {
    showStartTimePicker.value = false;
};

/**
 * 开始时间选择器变化
 */
const onStartTimeChange = (e: any) => {
    const [hourIdx, minIdx] = e.detail.value;
    startHourIndex.value = hourIdx;
    startMinuteIndex.value = minIdx;
};

/**
 * 确认开始时间选择
 */
const confirmStartTime = () => {
    const hour = hours.value[startHourIndex.value];
    const min = minutes.value[startMinuteIndex.value];
    const _startTime = `${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
    startTime.value = _startTime;
    showStartTimePicker.value = false;
    validateTimeRange();
};

/**
 * 显示结束时间选择器
 */
const showEndTimePickerFun = () => {
    showEndTimePicker.value = true;
};

/**
 * 隐藏结束时间选择器
 */
const hideEndTimePicker = () => {
    showEndTimePicker.value = false;
};

/**
 * 结束时间选择器变化
 */
const onEndTimeChange = (e: any) => {
    const [hourIdx, minIdx] = e.detail.value;
    endHourIndex.value = hourIdx;
    endMinuteIndex.value = minIdx;
};

/**
 * 确认结束时间选择
 */
const confirmEndTime = () => {
    const hour = hours.value[endHourIndex.value];
    const min = minutes.value[endMinuteIndex.value];
    const _endTime = `${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
    endTime.value = _endTime;
    showEndTimePicker.value = false;
    validateTimeRange();
};

/**
 * 验证时间范围
 */
const validateTimeRange = () => {
    const _startTime = startTime.value;
    const _endTime = endTime.value;
    if (_startTime && _endTime) {
        const [sh, sm] = _startTime.split(':').map(Number);
        const [eh, em] = _endTime.split(':').map(Number);
        const start = sh * 60 + sm;
        const end = eh * 60 + em;
        if (end <= start) {
            errorTime.value = '结束时间必须大于开始时间';
            endTime.value = '';

            // 显示提示信息
            uni.showToast({
                title: '结束时间不能早于开始时间',
                icon: 'none',
                duration: 2000
            });
        } else {
            errorTime.value = '';
            selectedTimeText.value = `${_startTime}-${_endTime}`;
        }
    }
    validateForm();
};

/**
 * 课程名称输入
 */
const onCourseNameInput = (e: any) => {
    courseName.value = e.detail.value;
    errorCourseName.value = e.detail.value.trim() ? '' : '请输入课程名称';
    validateForm();
};

/**
 * 学生人数输入
 */
const onStudentCountInput = (e: any) => {
    const count = e.detail.value;
    let errorMsg = '';
    if (!count.trim()) {
        errorMsg = '请输入学生人数';
    } else if (parseInt(count) <= 0) {
        errorMsg = '学生人数必须大于0';
    } else if (selectedLab.value.capacity && parseInt(count) > selectedLab.value.capacity) {
        errorMsg = `学生人数不能超过实验室容量(${selectedLab.value.capacity}人)`;
    }
    studentCount.value = count;
    errorStudentCount.value = errorMsg;
    validateForm();
};

/**
 * 选择课程类型
 */
const selectCourseType = (e: any) => {
    const index = e.currentTarget.dataset.index;
    const _courseTypes = courseTypes.value;

    // 单选模式
    _courseTypes.forEach((type, i) => {
        type.selected = i === index;
    });
    courseTypes.value = _courseTypes;
};

/**
 * 备注输入
 */
const onRemarkInput = (e: any) => {
    remark.value = e.detail.value;
};

/**
 * 验证表单
 */
const validateForm = () => {
    const _selectedLab = selectedLab.value;
    const _selectedDate = selectedDate.value;
    const _startTime = startTime.value;
    const _endTime = endTime.value;
    const _courseName = courseName.value;
    const _studentCount = studentCount.value;
    let _errorLab = '';
    let _errorDate = '';
    let _errorTime = '';
    let _errorCourseName = '';
    let _errorStudentCount = ''; // 验证实验室
    if (!_selectedLab.id) {
        _errorLab = '请选择实验室';
    }

    // 验证日期
    if (!_selectedDate) {
        _errorDate = '请选择预约日期';
    }

    // 验证时间
    if (!_startTime || !_endTime) {
        _errorTime = '请选择时间段';
    } else {
        const [sh, sm] = _startTime.split(':').map(Number);
        const [eh, em] = _endTime.split(':').map(Number);
        if (eh * 60 + em <= sh * 60 + sm) {
            _errorTime = '结束时间必须大于开始时间';
        }
    }

    // 验证课程名称
    if (!_courseName.trim()) {
        _errorCourseName = '请输入课程名称';
    }

    // 验证学生人数
    if (!_studentCount.trim()) {
        _errorStudentCount = '请输入学生人数';
    } else if (parseInt(_studentCount) <= 0) {
        _errorStudentCount = '学生人数必须大于0';
    } else if (_selectedLab.capacity && parseInt(_studentCount) > _selectedLab.capacity) {
        _errorStudentCount = `学生人数不能超过实验室容量(${_selectedLab.capacity}人)`;
    }
    const _canSubmit = !_errorLab && !_errorDate && !_errorTime && !_errorCourseName && !_errorStudentCount;
    errorLab.value = _errorLab;
    errorDate.value = _errorDate;
    errorTime.value = _errorTime;
    errorCourseName.value = _errorCourseName;
    errorStudentCount.value = _errorStudentCount;
    canSubmit.value = _canSubmit;
};

/**
 * 提交申请
 */
const submitApplication = () => {
    if (!canSubmit.value) {
        uni.showToast({
            title: '请完善申请信息',
            icon: 'none'
        });

        // 自动滚动到第一个未填写项
        const _selectedLab = selectedLab.value;
        const _selectedDate = selectedDate.value;
        const _startTime = startTime.value;
        const _endTime = endTime.value;
        const _courseName = courseName.value;
        const _studentCount = studentCount.value;
        let targetId = '';
        if (!_selectedLab.id) targetId = 'labItem';
        else if (!_selectedDate) targetId = 'dateItem';
        else if (!_startTime || !_endTime) targetId = 'timeItem';
        else if (!_courseName.trim()) targetId = 'courseItem';
        else if (!_studentCount.trim() || parseInt(_studentCount) <= 0) {
            targetId = 'countItem';
        }
        if (targetId) {
            uni.pageScrollTo({
                selector: `#${targetId}`,
                duration: 300
            });
        }
        return;
    }

    // 获取选中的时间段文本
    const _selectedTimeText = `${startTime.value}-${endTime.value}`;
    selectedTimeText.value = _selectedTimeText;
    showConfirmModal.value = true;
};

/**
 * 关闭确认弹窗
 */
const closeConfirmModal = () => {
    showConfirmModal.value = false;
};

/**
 * 阻止事件冒泡
 */
const stopPropagation = () => {
    // 阻止点击弹窗内容时关闭弹窗
};

/**
 * 确认提交
 */
const confirmSubmit = () => {
    try {
        // 再次验证申请人信息是否完整
        if (!teacherInfo.value.name || !teacherInfo.value.phone) {
            uni.showModal({
                title: '信息不完整',
                content: '申请人姓名或电话号码信息缺失，无法提交申请',
                showCancel: false
            });
            return;
        }
        const selectedCourseType = courseTypes.value.find((type) => type.selected);
        const application = {
            id: 'app_' + Date.now(),
            teacherId: teacherInfo.value.teacherId || '',
            teacherName: teacherInfo.value.name || '未知教师',
            teacherPhone: teacherInfo.value.phone || '',
            // 确保包含申请人电话号码
            applicantName: teacherInfo.value.name || '',
            // 申请人姓名
            applicantPhone: teacherInfo.value.phone || '',
            // 申请人电话号码
            labId: selectedLab.value.id,
            labName: selectedLab.value.name,
            date: selectedDate.value,
            startTime: startTime.value,
            endTime: endTime.value,
            timeSlot: `${startTime.value}-${endTime.value}`,
            courseName: courseName.value,
            courseType: selectedCourseType ? selectedCourseType.name : '实验课',
            studentCount: parseInt(studentCount.value),
            remark: remark.value,
            status: 'pending',
            submitTime: new Date().toISOString(),
            type: 'teacher'
        };

        // 保存到本地存储
        const applications = uni.getStorageSync('teacherApplications') || [];
        applications.unshift(application);
        uni.setStorageSync('teacherApplications', applications);

        // 同时保存到学生申请列表（供管理员查看）
        const allApplications = uni.getStorageSync('studentApplications') || [];
        allApplications.unshift(application);
        uni.setStorageSync('studentApplications', allApplications);

        // 清除保存的草稿
        uni.removeStorageSync('teacherApplicationDraft');
        showConfirmModal.value = false;
        uni.showToast({
            title: '申请提交成功',
            icon: 'success'
        });

        // 延迟返回上一页
        setTimeout(() => {
            uni.navigateBack();
        }, 1500);
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('提交申请失败:', error);
        uni.showToast({
            title: '提交失败',
            icon: 'error'
        });
    }
};

/**
 * 保存草稿
 */
const saveDraft = () => {
    try {
        const selectedCourseType = courseTypes.value.find((type) => type.selected);
        const draft = {
            teacherId: teacherInfo.value.teacherId || '',
            labId: selectedLab.value.id || '',
            labName: selectedLab.value.name || '',
            date: selectedDate.value,
            startTime: startTime.value,
            endTime: endTime.value,
            courseName: courseName.value,
            courseType: selectedCourseType ? selectedCourseType.name : '',
            studentCount: studentCount.value,
            remark: remark.value,
            saveTime: new Date().toISOString()
        };
        uni.setStorageSync('teacherApplicationDraft', draft);
        uni.showToast({
            title: '草稿保存成功',
            icon: 'success'
        });
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('保存草稿失败:', error);
        uni.showToast({
            title: '保存失败',
            icon: 'error'
        });
    }
};

/**
 * 加载草稿
 */
const loadDraft = () => {
    try {
        const draft = uni.getStorageSync('teacherApplicationDraft');
        if (draft && draft.teacherId === teacherInfo.value.teacherId) {
            // 询问是否加载草稿
            uni.showModal({
                title: '发现草稿',
                content: '是否加载之前保存的草稿？',
                success: (res) => {
                    if (res.confirm) {
                        applyDraft(draft);
                    }
                }
            });
        }
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载草稿失败:', error);
    }
};

/**
 * 应用草稿数据
 */
const applyDraft = (draft: any) => {
    // 设置实验室
    const _labIndex = labOptions.value.findIndex((lab) => lab.id === draft.labId);
    if (_labIndex !== -1) {
        selectedLabIndex.value = _labIndex;
        selectedLab.value = labOptions.value[_labIndex];
    }

    // 设置其他数据
    selectedDate.value = draft.date;
    startTime.value = draft.startTime || '';
    endTime.value = draft.endTime || '';
    courseName.value = draft.courseName;
    studentCount.value = draft.studentCount;
    remark.value = draft.remark;

    // 设置课程类型
    if (draft.courseType) {
        const _courseTypes = courseTypes.value.map((type) => ({
            ...type,
            selected: type.name === draft.courseType
        }));
        courseTypes.value = _courseTypes;
    }
    validateForm();
    uni.showToast({
        title: '草稿加载成功',
        icon: 'success'
    });
};
</script>
<style>
@import './teacher-reservation-apply.css';
</style>
