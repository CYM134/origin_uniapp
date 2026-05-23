<template>
    <view>
        <!-- admin-approval-records.wxml -->
        <navigation-bar title="审批记录" :back="true" color="white" background="#3a7bd5"></navigation-bar>

        <view class="container">
            <!-- 筛选区域 -->
            <view class="filter-section">
                <view class="filter-item">
                    <view class="filter-label">状态</view>
                    <view class="custom-picker" @tap="showStatusPicker">
                        <view class="picker">
                            <text>{{ statusOptions[statusIndex] }}</text>
                            <image class="arrow-icon" src="/static/images/icons/search-icon.png"></image>
                        </view>
                    </view>
                </view>

                <view class="filter-item">
                    <view class="filter-label">实验室</view>
                    <view class="custom-picker" @tap="showLabPicker">
                        <view class="picker">
                            <text>{{ labIndex === 0 ? '全部实验室' : labOptions[labIndex].name }}</text>
                            <image class="arrow-icon" src="/static/images/icons/search-icon.png"></image>
                        </view>
                    </view>
                </view>

                <view class="filter-item">
                    <view class="filter-label">日期范围</view>
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
                    <view class="record-item" @tap="showRecordDetail" :data-id="item.id" v-for="(item, index) in filteredRecords" :key="index">
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
                    <image src="/images//icons/empty.png"></image>
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
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>
    </view>
</template>

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
// admin-approval-records.ts
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            // 筛选选项
            statusOptions: ['全部状态', '已通过', '已拒绝'],

            statusIndex: 0,

            labOptions: [
                {
                    id: '0',
                    name: '全部实验室'
                },
                {
                    id: '1',
                    name: '国际课程实验室'
                },
                {
                    id: '2',
                    name: 'IBC实验中心'
                },
                {
                    id: '3',
                    name: '互联网+新商科实验室'
                }
            ],

            labIndex: 0,
            startDate: '',
            endDate: '',

            // 自定义选择器控制
            showStatusPickerModal: false,

            showLabPickerModal: false,
            showStartDatePickerModal: false,
            showEndDatePickerModal: false,

            // 临时存储选择的索引
            tempStatusIndex: 0,

            tempLabIndex: 0,

            // 日期选择器数据
            years: [] as number[],

            months: [] as number[],
            startDays: [] as number[],
            endDays: [] as number[],
            startYearIndex: 0,
            startMonthIndex: 0,
            startDayIndex: 0,
            endYearIndex: 0,
            endMonthIndex: 0,
            endDayIndex: 0,

            // 搜索
            searchKeyword: '',

            // 统计信息
            stats: {
                total: 0,
                approved: 0,
                rejected: 0
            },

            // 记录列表
            records: [
                {
                    id: 'R20230001',
                    applicant: '张三',
                    applicantType: '教师',
                    contact: '13800138000',
                    labId: '1',
                    labName: '互联网+新商科实验室',
                    date: '2023-11-15',
                    timeSlot: '08:00-10:00',
                    purpose: '网络协议分析实验，需要使用实验室的路由器和交换机设备进行数据包捕获和分析。',
                    status: '已通过',
                    applyTime: '2023-11-10 14:30:25',
                    approvalTime: '2023-11-11 09:15:36',
                    attachments: [
                        {
                            name: '实验计划.docx',
                            url: 'https://example.com/plan.docx'
                        }
                    ]
                },
                {
                    id: 'R20230002',
                    applicant: '李四',
                    applicantType: '学生',
                    contact: '13900139000',
                    labId: '2',
                    labName: '国际课程实验室',
                    date: '2023-11-16',
                    timeSlot: '14:00-16:00',
                    purpose: '课程设计小组讨论，需要使用数据库进行项目开发讨论事宜。',
                    status: '已通过',
                    applyTime: '2023-11-09 10:15:36',
                    approvalTime: '2023-11-10 09:20:15'
                },
                {
                    id: 'R20230003',
                    applicant: '王五',
                    applicantType: '教师',
                    contact: '13700137000',
                    labId: '3',
                    labName: '互联网+新商科实验室',
                    date: '2023-11-17',
                    timeSlot: '10:00-12:00',
                    purpose: '深度学习模型训练，需要使用GPU工作站进行大规模数据处理。',
                    status: '已拒绝',
                    applyTime: '2023-11-11 16:45:12',
                    approvalTime: '2023-11-12 11:30:45',
                    rejectReason: '该时段实验室已被其他教师预约使用。而且该实验室没有算力。'
                },
                {
                    id: 'R20230004',
                    applicant: '赵六',
                    applicantType: '学生',
                    contact: '13600136000',
                    labId: '1',
                    labName: '国际课程实验室',
                    date: '2023-11-18',
                    timeSlot: '16:00-18:00',
                    purpose: '毕业设计实验，需要搭建小型网络环境进行测试。',
                    status: '已通过',
                    applyTime: '2023-11-12 09:10:28',
                    approvalTime: '2023-11-13 14:25:36'
                },
                {
                    id: 'R20230005',
                    applicant: '钱七',
                    applicantType: '教师',
                    contact: '13500135000',
                    labId: '2',
                    labName: '互联网+新商科实验室',
                    date: '2023-11-19',
                    timeSlot: '08:00-10:00',
                    purpose: '数据库性能优化实验，需要使用实验室的服务器进行测试。',
                    status: '已拒绝',
                    applyTime: '2023-11-13 11:20:45',
                    approvalTime: '2023-11-14 10:15:30',
                    rejectReason: '申请信息不完整，请补充实验详细计划。'
                },
                {
                    id: 'R20230006',
                    applicant: '孙八',
                    applicantType: '学生',
                    contact: '13400134000',
                    labId: '3',
                    labName: '互联网+新商科实验室',
                    date: '2023-11-20',
                    timeSlot: '14:00-16:00',
                    purpose: '机器学习算法实现，需要使用实验室的GPU工作站。',
                    status: '已通过',
                    applyTime: '2023-11-14 15:30:20',
                    approvalTime: '2023-11-15 09:45:12'
                }
            ],

            filteredRecords: [] as any[],

            // 分页
            pageSize: 10,

            currentPage: 1,
            hasMoreRecords: true,
            isLoading: false,

            // 详情弹窗
            showDetailModal: false,

            currentRecord: {} as any,
            name: ''
        };
    },
    mounted() {
        // 处理小程序 attached 生命周期
        this.attached();
    },
    methods: {
        attached() {
            // 初始化过滤后的记录列表和统计信息
            this.filterAndCountRecords();
            // 初始化日期选择器数据
            this.initDatePickerData();
        },

        // 初始化日期选择器数据
        initDatePickerData() {
            const currentYear = new Date().getFullYear();
            const years = [];
            const months = [];
            const days = [];

            // 生成年份数据（从2023年到当前年份后2年）
            for (let i = 2023; i <= currentYear + 2; i++) {
                years.push(i);
            }

            // 生成月份数据
            for (let i = 1; i <= 12; i++) {
                months.push(i);
            }

            // 生成天数数据（默认31天，后续会根据年月动态调整）
            for (let i = 1; i <= 31; i++) {
                days.push(i);
            }
            this.setData({
                years,
                months,
                startDays: days,
                endDays: days
            });

            // 如果已有开始日期，设置对应的索引
            if (this.startDate) {
                this.setDateIndices(this.startDate, 'start');
            }

            // 如果已有结束日期，设置对应的索引
            if (this.endDate) {
                this.setDateIndices(this.endDate, 'end');
            }
        },

        // 设置日期索引
        setDateIndices(dateStr: string, type: 'start' | 'end') {
            const [year, month, day] = dateStr.split('-').map(Number);
            const yearIndex = this.years.indexOf(year);
            const monthIndex = this.months.indexOf(month);

            // 更新对应类型的天数
            this.updateDays(year, month, type);
            const dayIndex = (type === 'start' ? this.startDays : this.endDays).indexOf(day);
            if (type === 'start') {
                this.setData({
                    startYearIndex: yearIndex >= 0 ? yearIndex : 0,
                    startMonthIndex: monthIndex >= 0 ? monthIndex : 0,
                    startDayIndex: dayIndex >= 0 ? dayIndex : 0
                });
            } else {
                this.setData({
                    endYearIndex: yearIndex >= 0 ? yearIndex : 0,
                    endMonthIndex: monthIndex >= 0 ? monthIndex : 0,
                    endDayIndex: dayIndex >= 0 ? dayIndex : 0
                });
            }
        },

        // 更新天数
        updateDays(year: number, month: number, type: 'start' | 'end') {
            let daysInMonth = 31;

            // 根据月份确定天数
            if (month === 4 || month === 6 || month === 9 || month === 11) {
                daysInMonth = 30;
            } else if (month === 2) {
                // 判断是否闰年
                daysInMonth = (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0 ? 29 : 28;
            }
            const days = [];
            for (let i = 1; i <= daysInMonth; i++) {
                days.push(i);
            }
            if (type === 'start') {
                this.setData({
                    startDays: days
                });
            } else {
                this.setData({
                    endDays: days
                });
            }
        },

        // 显示状态选择器
        showStatusPicker() {
            this.setData({
                showStatusPickerModal: true,
                tempStatusIndex: this.statusIndex
            });
        },

        // 隐藏状态选择器
        hideStatusPicker() {
            this.setData({
                showStatusPickerModal: false
            });
        },

        // 状态选择变化
        onStatusPickerChange(e: any) {
            this.setData({
                tempStatusIndex: e.detail.value[0]
            });
        },

        // 确认状态选择
        confirmStatusPicker() {
            this.setData(
                {
                    statusIndex: this.tempStatusIndex,
                    showStatusPickerModal: false,
                    currentPage: 1,
                    hasMoreRecords: true
                },
                () => {
                    this.filterAndCountRecords();
                }
            );
        },

        // 显示实验室选择器
        showLabPicker() {
            this.setData({
                showLabPickerModal: true,
                tempLabIndex: this.labIndex
            });
        },

        // 隐藏实验室选择器
        hideLabPicker() {
            this.setData({
                showLabPickerModal: false
            });
        },

        // 实验室选择变化
        onLabPickerChange(e: any) {
            this.setData({
                tempLabIndex: e.detail.value[0]
            });
        },

        // 确认实验室选择
        confirmLabPicker() {
            this.setData(
                {
                    labIndex: this.tempLabIndex,
                    showLabPickerModal: false,
                    currentPage: 1,
                    hasMoreRecords: true
                },
                () => {
                    this.filterAndCountRecords();
                }
            );
        },

        // 显示开始日期选择器
        showStartDatePicker() {
            // 如果已有开始日期，设置对应的索引
            if (this.startDate) {
                this.setDateIndices(this.startDate, 'start');
            }
            this.setData({
                showStartDatePickerModal: true
            });
        },

        // 隐藏开始日期选择器
        hideStartDatePicker() {
            this.setData({
                showStartDatePickerModal: false
            });
        },

        // 开始日期选择变化
        onStartDatePickerChange(e: any) {
            const [yearIndex, monthIndex, dayIndex] = e.detail.value;
            const year = this.years[yearIndex];
            const month = this.months[monthIndex];

            // 更新天数
            this.updateDays(year, month, 'start');
            this.setData({
                startYearIndex: yearIndex,
                startMonthIndex: monthIndex,
                startDayIndex: dayIndex >= this.startDays.length ? 0 : dayIndex
            });
        },

        // 确认开始日期选择
        confirmStartDatePicker() {
            const year = this.years[this.startYearIndex];
            const month = this.months[this.startMonthIndex];
            const day = this.startDays[this.startDayIndex];

            // 格式化日期
            const formattedMonth = month < 10 ? `0${month}` : `${month}`;
            const formattedDay = day < 10 ? `0${day}` : `${day}`;
            const dateStr = `${year}-${formattedMonth}-${formattedDay}`;
            this.setData(
                {
                    startDate: dateStr,
                    showStartDatePickerModal: false,
                    currentPage: 1,
                    hasMoreRecords: true
                },
                () => {
                    this.filterAndCountRecords();
                }
            );
        },

        // 显示结束日期选择器
        showEndDatePicker() {
            // 如果已有结束日期，设置对应的索引
            if (this.endDate) {
                this.setDateIndices(this.endDate, 'end');
            }
            this.setData({
                showEndDatePickerModal: true
            });
        },

        // 隐藏结束日期选择器
        hideEndDatePicker() {
            this.setData({
                showEndDatePickerModal: false
            });
        },

        // 结束日期选择变化
        onEndDatePickerChange(e: any) {
            const [yearIndex, monthIndex, dayIndex] = e.detail.value;
            const year = this.years[yearIndex];
            const month = this.months[monthIndex];

            // 更新天数
            this.updateDays(year, month, 'end');
            this.setData({
                endYearIndex: yearIndex,
                endMonthIndex: monthIndex,
                endDayIndex: dayIndex >= this.endDays.length ? 0 : dayIndex
            });
        },

        // 确认结束日期选择
        confirmEndDatePicker() {
            const year = this.years[this.endYearIndex];
            const month = this.months[this.endMonthIndex];
            const day = this.endDays[this.endDayIndex];

            // 格式化日期
            const formattedMonth = month < 10 ? `0${month}` : `${month}`;
            const formattedDay = day < 10 ? `0${day}` : `${day}`;
            const dateStr = `${year}-${formattedMonth}-${formattedDay}`;
            this.setData(
                {
                    endDate: dateStr,
                    showEndDatePickerModal: false,
                    currentPage: 1,
                    hasMoreRecords: true
                },
                () => {
                    this.filterAndCountRecords();
                }
            );
        },

        // 搜索输入
        onSearchInput(e: any) {
            this.setData({
                searchKeyword: e.detail.value
            });
        },

        // 搜索记录
        searchRecords() {
            this.setData({
                currentPage: 1,
                hasMoreRecords: true
            });
            this.filterAndCountRecords();
        },

        // 过滤和统计记录
        filterAndCountRecords() {
            const { records, statusIndex, labIndex, startDate, endDate, searchKeyword, statusOptions, labOptions, pageSize, currentPage } = this;

            // 应用所有筛选条件
            let filtered = [...records];

            // 按状态筛选
            if (statusIndex > 0) {
                const status = statusOptions[statusIndex];
                filtered = filtered.filter((item) => item.status === status);
            }

            // 按实验室筛选
            if (labIndex > 0) {
                const labId = labOptions[labIndex].id;
                filtered = filtered.filter((item) => item.labId === labId);
            }

            // 按日期范围筛选
            if (startDate && endDate) {
                filtered = filtered.filter((item) => {
                    return item.date >= startDate && item.date <= endDate;
                });
            } else if (startDate) {
                filtered = filtered.filter((item) => item.date >= startDate);
            } else if (endDate) {
                filtered = filtered.filter((item) => item.date <= endDate);
            }

            // 按关键词搜索
            if (searchKeyword) {
                const keyword = searchKeyword.toLowerCase();
                filtered = filtered.filter((item) => {
                    return item.applicant.toLowerCase().includes(keyword) || item.id.toLowerCase().includes(keyword);
                });
            }

            // 计算统计信息
            const total = filtered.length;
            const approved = filtered.filter((item) => item.status === '已通过').length;
            const rejected = filtered.filter((item) => item.status === '已拒绝').length;

            // 应用分页
            const paged = filtered.slice(0, currentPage * pageSize);
            const hasMore = paged.length < filtered.length;

            // 更新数据
            this.setData({
                filteredRecords: paged,
                hasMoreRecords: hasMore,
                stats: {
                    total,
                    approved,
                    rejected
                }
            });
        },

        // 加载更多记录
        loadMoreRecords() {
            if (!this.hasMoreRecords || this.isLoading) {
                return;
            }
            this.setData({
                isLoading: true
            });

            // 模拟加载延迟
            setTimeout(() => {
                this.setData(
                    {
                        currentPage: this.currentPage + 1,
                        isLoading: false
                    },
                    () => {
                        this.filterAndCountRecords();
                    }
                );
            }, 500);
        },

        // 显示记录详情
        showRecordDetail(e: any) {
            const id = e.currentTarget.dataset.id;
            const record = this.records.find((item) => item.id === id);
            if (record) {
                this.setData({
                    showDetailModal: true,
                    currentRecord: record
                });
            }
        },

        // 隐藏详情弹窗
        hideDetailModal() {
            this.setData({
                showDetailModal: false
            });
        },

        // 预览附件
        previewAttachment(e: any) {
            const url = e.currentTarget.dataset.url;

            // 实际开发中可能需要下载文件后预览
            uni.showToast({
                title: '附件预览功能开发中',
                icon: 'none'
            });
        }
    },
    created: function () {}
});
</script>
<style lang="less">
@import './admin-approval-records.less';
</style>
