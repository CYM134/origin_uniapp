<template>
    <view class="page-wrapper">
        <!-- admin-approval-records.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="审批记录" color="white" background="#F5A623" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <app-navigation-bar title="审批记录" :back="true" color="white" background="#F5A623"></app-navigation-bar>
        <!-- #endif -->

        <view class="container">
            <!-- 筛选区域 -->
            <view class="filter-section">
                <view class="filter-item">
                    <view class="filter-label">状态</view>
                    <view class="custom-picker" @tap="showStatusPicker">
                        <view class="picker">
                            <text>{{ statusOptions[statusIndex] }}</text>
                            <image class="arrow-icon" src="/static/images/icons/arrow-down.png"></image>
                        </view>
                    </view>
                </view>

                <view class="filter-item">
                    <view class="filter-label">实验室</view>
                    <view class="custom-picker" @tap="showLabPicker">
                        <view class="picker">
                            <text>{{ labIndex === 0 ? '全部实验室' : labOptions[labIndex].name }}</text>
                            <image class="arrow-icon" src="/static/images/icons/arrow-down.png"></image>
                        </view>
                    </view>
                </view>

                <view class="filter-item date-range-item">
                    <view class="filter-label">日期范围</view>
                    <view class="date-pickers-row">
                        <view class="custom-picker" @tap="showStartDatePicker">
                            <view class="date-picker">
                                <text>{{ startDate || '开始日期' }}</text>
                            </view>
                        </view>
                        <text class="date-separator">至</text>
                        <view class="custom-picker" @tap="showEndDatePicker">
                            <view class="date-picker">
                                <text>{{ endDate || '结束日期' }}</text>
                            </view>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 搜索区域 -->
            <view class="search-section">
                <view class="search-box">
                    <input type="text" placeholder="搜索申请人或编号" @input="onSearchInput" :value="searchKeyword" confirm-type="search" />
                    <view class="search-icon">
                        <image src="/static/images/icons/search-icon.png" @tap="searchRecords"></image>
                    </view>
                </view>
                <button class="search-btn" @tap="searchRecords">搜索</button>
            </view>

            <!-- 统计信息 -->
            <view class="stats-section">
                <view class="stats-item">
                    <view class="stats-value">{{ stats.total }}</view>
                    <view class="stats-label">总记录</view>
                </view>
                <view class="stats-item approved">
                    <view class="stats-value">{{ stats.approved }}</view>
                    <view class="stats-label">已通过</view>
                </view>
                <view class="stats-item rejected">
                    <view class="stats-value">{{ stats.rejected }}</view>
                    <view class="stats-label">已拒绝</view>
                </view>
            </view>

            <!-- 记录列表 -->
            <view class="records-list">
                <block v-if="filteredRecords.length > 0">
                    <view :class="'record-item ' + (item.status === '已通过' ? 'status-approved' : 'status-rejected')" @tap="showRecordDetail" :data-id="item.id" v-for="(item, index) in filteredRecords" :key="index">
                        <view class="record-header">
                            <view class="record-id">{{ item.id }}</view>
                            <view :class="'record-status ' + (item.status === '已通过' ? 'approved' : 'rejected')">
                                {{ item.status }}
                            </view>
                        </view>

                        <view class="record-info">
                            <view class="info-row">
                                <view class="info-label">申请人：</view>
                                <view class="info-value">{{ item.applicant }} ({{ item.applicantType }})</view>
                            </view>

                            <view class="info-row">
                                <view class="info-label">实验室：</view>
                                <view class="info-value">{{ item.labName }}</view>
                            </view>

                            <view class="info-row">
                                <view class="info-label">预约时间：</view>
                                <view class="info-value">{{ item.date }} {{ item.timeSlot }}</view>
                            </view>

                            <view class="info-row">
                                <view class="info-label">审批时间：</view>
                                <view class="info-value">{{ item.approvalTime }}</view>
                            </view>

                            <view class="info-row" v-if="item.status === '已拒绝'">
                                <view class="info-label">拒绝原因：</view>
                                <view class="info-value reason-text">{{ item.rejectReason }}</view>
                            </view>
                        </view>
                    </view>
                </block>

                <view class="empty-list" v-else>
                    <image src="/static/images/icons/empty.png"></image>
                    <text>暂无审批记录</text>
                </view>
            </view>

            <!-- 加载更多 -->
            <view class="load-more" v-if="filteredRecords.length > 0 && hasMoreRecords">
                <button class="load-more-btn" @tap="loadMoreRecords" :loading="isLoading">加载更多</button>
            </view>

            <!-- 详情弹窗 -->
            <view class="modal" v-if="showDetailModal">
                <view class="modal-mask" @tap="hideDetailModal"></view>
                <view class="modal-content detail-modal">
                    <view class="modal-header">
                        <text>审批详情</text>
                        <view class="close-btn" @tap="hideDetailModal">×</view>
                    </view>

                    <view class="modal-body">
                        <view :class="'detail-status ' + (currentRecord.status === '已通过' ? 'approved' : 'rejected')">
                            {{ currentRecord.status }}
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">预约编号</view>
                                <view class="detail-value">{{ currentRecord.id }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">申请人</view>
                                <view class="detail-value">{{ currentRecord.applicant }} ({{ currentRecord.applicantType }})</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">联系方式</view>
                                <view class="detail-value">{{ currentRecord.contact }}</view>
                            </view>
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">实验室</view>
                                <view class="detail-value">{{ currentRecord.labName }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">预约日期</view>
                                <view class="detail-value">{{ currentRecord.date }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">时间段</view>
                                <view class="detail-value">{{ currentRecord.timeSlot }}</view>
                            </view>
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">用途说明</view>
                                <view class="detail-value purpose-box">{{ currentRecord.purpose }}</view>
                            </view>

                            <view class="detail-item" v-if="currentRecord.attachments && currentRecord.attachments.length > 0">
                                <view class="detail-label">附件</view>
                                <view class="detail-value attachments">
                                    <view class="attachment-item" @tap="previewAttachment" :data-url="item.url" v-for="(item, index) in currentRecord.attachments" :key="index">
                                        <image src="/static/images/icons/file.png"></image>

                                        <text>{{ item.name }}</text>
                                    </view>
                                </view>
                            </view>
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">申请时间</view>
                                <view class="detail-value">{{ currentRecord.applyTime }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">审批时间</view>
                                <view class="detail-value">{{ currentRecord.approvalTime }}</view>
                            </view>

                            <view class="detail-item" v-if="currentRecord.status === '已拒绝'">
                                <view class="detail-label">拒绝原因</view>
                                <view class="detail-value reason-box">{{ currentRecord.rejectReason }}</view>
                            </view>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 状态选择器弹窗 -->
            <view class="modal custom-picker-modal" v-if="showStatusPickerModal">
                <view class="modal-mask" @tap="hideStatusPicker"></view>
                <view class="modal-content picker-modal-content">
                    <view class="modal-header">
                        <text>选择状态</text>
                    </view>
                    <view class="modal-body">
                        <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[tempStatusIndex]" @change="onStatusPickerChange">
                            <picker-view-column>
                                <view class="picker-view-item" v-for="(item, index) in statusOptions" :key="index">{{ item }}</view>
                            </picker-view-column>
                        </picker-view>
                    </view>
                    <view class="modal-footer">
                        <button class="cancel-btn" @tap="hideStatusPicker">取消</button>
                        <button class="confirm-btn" @tap="confirmStatusPicker">确定</button>
                    </view>
                </view>
            </view>

            <!-- 实验室选择器弹窗 -->
            <view class="modal custom-picker-modal" v-if="showLabPickerModal">
                <view class="modal-mask" @tap="hideLabPicker"></view>
                <view class="modal-content picker-modal-content">
                    <view class="modal-header">
                        <text>选择实验室</text>
                    </view>
                    <view class="modal-body">
                        <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[tempLabIndex]" @change="onLabPickerChange">
                            <picker-view-column>
                                <view class="picker-view-item" v-for="(item, index) in labOptions" :key="index">{{ item.name }}</view>
                            </picker-view-column>
                        </picker-view>
                    </view>
                    <view class="modal-footer">
                        <button class="cancel-btn" @tap="hideLabPicker">取消</button>
                        <button class="confirm-btn" @tap="confirmLabPicker">确定</button>
                    </view>
                </view>
            </view>

            <!-- 开始日期选择器弹窗 -->
            <view class="modal custom-picker-modal" v-if="showStartDatePickerModal">
                <view class="modal-mask" @tap="hideStartDatePicker"></view>
                <view class="modal-content picker-modal-content">
                    <view class="modal-header">
                        <text>选择开始日期</text>
                    </view>
                    <view class="modal-body">
                        <view class="date-picker-body">
                            <picker-view
                                indicator-style="height: 100rpx;"
                                style="width: 100%; height: 400rpx"
                                :value="[startYearIndex, startMonthIndex, startDayIndex]"
                                @change="onStartDatePickerChange"
                            >
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in years" :key="index">{{ item }}年</view>
                                </picker-view-column>
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in months" :key="index">{{ item }}月</view>
                                </picker-view-column>
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in startDays" :key="index">{{ item }}日</view>
                                </picker-view-column>
                            </picker-view>
                        </view>
                    </view>
                    <view class="modal-footer">
                        <button class="cancel-btn" @tap="hideStartDatePicker">取消</button>
                        <button class="confirm-btn" @tap="confirmStartDatePicker">确定</button>
                    </view>
                </view>
            </view>

            <!-- 结束日期选择器弹窗 -->
            <view class="modal custom-picker-modal" v-if="showEndDatePickerModal">
                <view class="modal-mask" @tap="hideEndDatePicker"></view>
                <view class="modal-content picker-modal-content">
                    <view class="modal-header">
                        <text>选择结束日期</text>
                    </view>
                    <view class="modal-body">
                        <view class="date-picker-body">
                            <picker-view
                                indicator-style="height: 100rpx;"
                                style="width: 100%; height: 400rpx"
                                :value="[endYearIndex, endMonthIndex, endDayIndex]"
                                @change="onEndDatePickerChange"
                            >
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in years" :key="index">{{ item }}年</view>
                                </picker-view-column>
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in months" :key="index">{{ item }}月</view>
                                </picker-view-column>
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in endDays" :key="index">{{ item }}日</view>
                                </picker-view-column>
                            </picker-view>
                        </view>
                    </view>
                    <view class="modal-footer">
                        <button class="cancel-btn" @tap="hideEndDatePicker">取消</button>
                        <button class="confirm-btn" @tap="confirmEndDatePicker">确定</button>
                    </view>
                </view>
            </view>

            <view class="footer">
                <text>© SCNU IBC 实验室空间预约与协同管理系统</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onMounted } from 'vue';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
import { getApprovalRecords, getApprovalStats, getReservationDetail, getLabs } from '@/api/admin';
// admin-approval-records.ts

// 筛选选项
const statusOptions = ref<any[]>(['全部状态', '已通过', '已拒绝']);

const statusIndex = ref<number>(0);

const labOptions = ref<any[]>([
    {
        id: '0',
        name: '全部实验室'
    }
]);

const labIndex = ref<number>(0);
const startDate = ref<string>('');
const endDate = ref<string>('');

// 自定义选择器控制
const showStatusPickerModal = ref<boolean>(false);

const showLabPickerModal = ref<boolean>(false);
const showStartDatePickerModal = ref<boolean>(false);
const showEndDatePickerModal = ref<boolean>(false);

// 临时存储选择的索引
const tempStatusIndex = ref<number>(0);

const tempLabIndex = ref<number>(0);

// 日期选择器数据
const years = ref<number[]>([]);

const months = ref<number[]>([]);
const startDays = ref<number[]>([]);
const endDays = ref<number[]>([]);
const startYearIndex = ref<number>(0);
const startMonthIndex = ref<number>(0);
const startDayIndex = ref<number>(0);
const endYearIndex = ref<number>(0);
const endMonthIndex = ref<number>(0);
const endDayIndex = ref<number>(0);

// 搜索
const searchKeyword = ref<string>('');

// 统计信息
const stats = ref<any>({
    total: 0,
    approved: 0,
    rejected: 0
});

// 记录列表
const records = ref<any[]>([]);

const filteredRecords = ref<any[]>([]);

// 分页
const pageSize = ref<number>(10);

const currentPage = ref<number>(1);
const hasMoreRecords = ref<boolean>(true);
const isLoading = ref<boolean>(false);

// 详情弹窗
const showDetailModal = ref<boolean>(false);

const currentRecord = ref<any>({});
const name = ref<string>('');

onMounted(() => {
    // 处理小程序 attached 生命周期
    // 初始化日期选择器数据
    initDatePickerData();
    // 加载实验室选项、审批记录与统计
    loadLabOptions();
    loadStats();
    loadRecords();
});

// 加载实验室选项（前补"全部实验室"项）
const loadLabOptions = async () => {
    try {
        const list = await getLabs();
        const labs = (Array.isArray(list) ? list : []).map((item: any) => ({
            id: item.id,
            name: item.name
        }));
        labOptions.value = [{ id: '0', name: '全部实验室' }, ...labs];
    } catch (err: any) {
        uni.showToast({ title: err?.data?.message || '加载失败', icon: 'none' });
        labOptions.value = [{ id: '0', name: '全部实验室' }];
    }
};

// 加载统计信息
const loadStats = async () => {
    try {
        const data = await getApprovalStats();
        stats.value = {
            total: data?.total ?? 0,
            approved: data?.approved ?? 0,
            rejected: data?.rejected ?? 0
        };
    } catch (err: any) {
        uni.showToast({ title: err?.data?.message || '加载失败', icon: 'none' });
        stats.value = { total: 0, approved: 0, rejected: 0 };
    }
};

// 加载审批记录（已是 approved/rejected）
const loadRecords = async () => {
    try {
        const list = await getApprovalRecords();
        // 适配模板所需字段形状，status 使用中文 statusText 以匹配模板比较
        records.value = (Array.isArray(list) ? list : []).map((item: any) => ({
            ...item,
            applicant: item.applicant ?? item.applicantName ?? '',
            applicantType: item.applicantType ?? '',
            contact: item.contact ?? item.phone ?? item.applicantPhone ?? '',
            status: item.statusText ?? item.status,
            approvalTime: item.approvalTime ?? item.adminReviewTime ?? '',
            applyTime: item.applyTime ?? item.submitTime ?? '',
            rejectReason: item.rejectReason ?? item.adminReviewComment ?? ''
        }));
        filterAndCountRecords();
    } catch (err: any) {
        uni.showToast({ title: err?.data?.message || '加载失败', icon: 'none' });
        records.value = [];
        filterAndCountRecords();
    }
};

// 初始化日期选择器数据
const initDatePickerData = () => {
    const currentYear = new Date().getFullYear();
    const yearsArr: number[] = [];
    const monthsArr: number[] = [];
    const daysArr: number[] = [];

    // 生成年份数据（从2023年到当前年份后2年）
    for (let i = 2023; i <= currentYear + 2; i++) {
        yearsArr.push(i);
    }

    // 生成月份数据
    for (let i = 1; i <= 12; i++) {
        monthsArr.push(i);
    }

    // 生成天数数据（默认31天，后续会根据年月动态调整）
    for (let i = 1; i <= 31; i++) {
        daysArr.push(i);
    }
    years.value = yearsArr;
    months.value = monthsArr;
    startDays.value = daysArr;
    endDays.value = daysArr;

    // 如果已有开始日期，设置对应的索引
    if (startDate.value) {
        setDateIndices(startDate.value, 'start');
    }

    // 如果已有结束日期，设置对应的索引
    if (endDate.value) {
        setDateIndices(endDate.value, 'end');
    }
};

// 设置日期索引
const setDateIndices = (dateStr: string, type: 'start' | 'end') => {
    const [year, month, day] = dateStr.split('-').map(Number);
    const yearIndex = years.value.indexOf(year);
    const monthIndex = months.value.indexOf(month);

    // 更新对应类型的天数
    updateDays(year, month, type);
    const dayIndex = (type === 'start' ? startDays.value : endDays.value).indexOf(day);
    if (type === 'start') {
        startYearIndex.value = yearIndex >= 0 ? yearIndex : 0;
        startMonthIndex.value = monthIndex >= 0 ? monthIndex : 0;
        startDayIndex.value = dayIndex >= 0 ? dayIndex : 0;
    } else {
        endYearIndex.value = yearIndex >= 0 ? yearIndex : 0;
        endMonthIndex.value = monthIndex >= 0 ? monthIndex : 0;
        endDayIndex.value = dayIndex >= 0 ? dayIndex : 0;
    }
};

// 更新天数
const updateDays = (year: number, month: number, type: 'start' | 'end') => {
    let daysInMonth = 31;

    // 根据月份确定天数
    if (month === 4 || month === 6 || month === 9 || month === 11) {
        daysInMonth = 30;
    } else if (month === 2) {
        // 判断是否闰年
        daysInMonth = (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0 ? 29 : 28;
    }
    const daysArr: number[] = [];
    for (let i = 1; i <= daysInMonth; i++) {
        daysArr.push(i);
    }
    if (type === 'start') {
        startDays.value = daysArr;
    } else {
        endDays.value = daysArr;
    }
};

// 显示状态选择器
const showStatusPicker = () => {
    showStatusPickerModal.value = true;
    tempStatusIndex.value = statusIndex.value;
};

// 隐藏状态选择器
const hideStatusPicker = () => {
    showStatusPickerModal.value = false;
};

// 状态选择变化
const onStatusPickerChange = (e: any) => {
    tempStatusIndex.value = e.detail.value[0];
};

// 确认状态选择
const confirmStatusPicker = () => {
    statusIndex.value = tempStatusIndex.value;
    showStatusPickerModal.value = false;
    currentPage.value = 1;
    hasMoreRecords.value = true;
    filterAndCountRecords();
};

// 显示实验室选择器
const showLabPicker = () => {
    showLabPickerModal.value = true;
    tempLabIndex.value = labIndex.value;
};

// 隐藏实验室选择器
const hideLabPicker = () => {
    showLabPickerModal.value = false;
};

// 实验室选择变化
const onLabPickerChange = (e: any) => {
    tempLabIndex.value = e.detail.value[0];
};

// 确认实验室选择
const confirmLabPicker = () => {
    labIndex.value = tempLabIndex.value;
    showLabPickerModal.value = false;
    currentPage.value = 1;
    hasMoreRecords.value = true;
    filterAndCountRecords();
};

// 显示开始日期选择器
const showStartDatePicker = () => {
    // 如果已有开始日期，设置对应的索引
    if (startDate.value) {
        setDateIndices(startDate.value, 'start');
    }
    showStartDatePickerModal.value = true;
};

// 隐藏开始日期选择器
const hideStartDatePicker = () => {
    showStartDatePickerModal.value = false;
};

// 开始日期选择变化
const onStartDatePickerChange = (e: any) => {
    const [yearIndex, monthIndex, dayIndex] = e.detail.value;
    const year = years.value[yearIndex];
    const month = months.value[monthIndex];

    // 更新天数
    updateDays(year, month, 'start');
    startYearIndex.value = yearIndex;
    startMonthIndex.value = monthIndex;
    startDayIndex.value = dayIndex >= startDays.value.length ? 0 : dayIndex;
};

// 确认开始日期选择
const confirmStartDatePicker = () => {
    const year = years.value[startYearIndex.value];
    const month = months.value[startMonthIndex.value];
    const day = startDays.value[startDayIndex.value];

    // 格式化日期
    const formattedMonth = month < 10 ? `0${month}` : `${month}`;
    const formattedDay = day < 10 ? `0${day}` : `${day}`;
    const dateStr = `${year}-${formattedMonth}-${formattedDay}`;
    startDate.value = dateStr;
    showStartDatePickerModal.value = false;
    currentPage.value = 1;
    hasMoreRecords.value = true;
    filterAndCountRecords();
};

// 显示结束日期选择器
const showEndDatePicker = () => {
    // 如果已有结束日期，设置对应的索引
    if (endDate.value) {
        setDateIndices(endDate.value, 'end');
    }
    showEndDatePickerModal.value = true;
};

// 隐藏结束日期选择器
const hideEndDatePicker = () => {
    showEndDatePickerModal.value = false;
};

// 结束日期选择变化
const onEndDatePickerChange = (e: any) => {
    const [yearIndex, monthIndex, dayIndex] = e.detail.value;
    const year = years.value[yearIndex];
    const month = months.value[monthIndex];

    // 更新天数
    updateDays(year, month, 'end');
    endYearIndex.value = yearIndex;
    endMonthIndex.value = monthIndex;
    endDayIndex.value = dayIndex >= endDays.value.length ? 0 : dayIndex;
};

// 确认结束日期选择
const confirmEndDatePicker = () => {
    const year = years.value[endYearIndex.value];
    const month = months.value[endMonthIndex.value];
    const day = endDays.value[endDayIndex.value];

    // 格式化日期
    const formattedMonth = month < 10 ? `0${month}` : `${month}`;
    const formattedDay = day < 10 ? `0${day}` : `${day}`;
    const dateStr = `${year}-${formattedMonth}-${formattedDay}`;
    endDate.value = dateStr;
    showEndDatePickerModal.value = false;
    currentPage.value = 1;
    hasMoreRecords.value = true;
    filterAndCountRecords();
};

// 搜索输入
const onSearchInput = (e: any) => {
    searchKeyword.value = e.detail.value;
};

// 搜索记录
const searchRecords = () => {
    currentPage.value = 1;
    hasMoreRecords.value = true;
    filterAndCountRecords();
};

// 过滤和统计记录
const filterAndCountRecords = () => {
    // 应用所有筛选条件
    let filtered = [...records.value];

    // 按状态筛选
    if (statusIndex.value > 0) {
        const status = statusOptions.value[statusIndex.value];
        filtered = filtered.filter((item) => item.status === status);
    }

    // 按实验室筛选
    if (labIndex.value > 0) {
        const labId = labOptions.value[labIndex.value].id;
        filtered = filtered.filter((item) => item.labId == labId);
    }

    // 按日期范围筛选
    if (startDate.value && endDate.value) {
        filtered = filtered.filter((item) => {
            return item.date >= startDate.value && item.date <= endDate.value;
        });
    } else if (startDate.value) {
        filtered = filtered.filter((item) => item.date >= startDate.value);
    } else if (endDate.value) {
        filtered = filtered.filter((item) => item.date <= endDate.value);
    }

    // 按关键词搜索
    if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase();
        filtered = filtered.filter((item) => {
            return (
                String(item.applicant || '')
                    .toLowerCase()
                    .includes(keyword) ||
                String(item.id || '')
                    .toLowerCase()
                    .includes(keyword)
            );
        });
    }

    // 应用分页
    const paged = filtered.slice(0, currentPage.value * pageSize.value);
    const hasMore = paged.length < filtered.length;

    // 更新数据（统计信息来自 getApprovalStats，不在此覆盖）
    filteredRecords.value = paged;
    hasMoreRecords.value = hasMore;
};

// 加载更多记录
const loadMoreRecords = () => {
    if (!hasMoreRecords.value || isLoading.value) {
        return;
    }
    isLoading.value = true;

    // 模拟加载延迟
    setTimeout(() => {
        currentPage.value = currentPage.value + 1;
        isLoading.value = false;
        filterAndCountRecords();
    }, 500);
};

// 显示记录详情
const showRecordDetail = async (e: any) => {
    const id = e.currentTarget.dataset.id;
    const record = records.value.find((item) => item.id == id);
    if (!record) {
        return;
    }
    // 先用列表数据兜底展示
    currentRecord.value = record;
    showDetailModal.value = true;
    try {
        const detail = await getReservationDetail(id);
        if (detail) {
            // 适配模板所需字段形状，status 使用中文 statusText 以匹配模板比较
            currentRecord.value = {
                ...record,
                ...detail,
                applicant: detail.applicant ?? detail.applicantName ?? record.applicant ?? '',
                applicantType: detail.applicantType ?? record.applicantType ?? '',
                contact: detail.contact ?? detail.phone ?? detail.applicantPhone ?? record.contact ?? '',
                status: detail.statusText ?? record.status,
                approvalTime: detail.approvalTime ?? detail.adminReviewTime ?? record.approvalTime ?? '',
                applyTime: detail.applyTime ?? detail.submitTime ?? record.applyTime ?? '',
                rejectReason: detail.rejectReason ?? detail.adminReviewComment ?? record.rejectReason ?? ''
            };
        }
    } catch (err: any) {
        uni.showToast({ title: err?.data?.message || '加载失败', icon: 'none' });
    }
};

// 隐藏详情弹窗
const hideDetailModal = () => {
    showDetailModal.value = false;
};

// 预览附件
const previewAttachment = (e: any) => {
    const url = e.currentTarget.dataset.url;

    // 实际开发中可能需要下载文件后预览
    uni.showToast({
        title: '附件预览功能开发中',
        icon: 'none'
    });
};
</script>
<style lang="less">
@import './admin-approval-records.less';
</style>
