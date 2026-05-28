<template>
    <view>
        <!-- admin-reservation-approval.wxml -->
        <navigation-bar title="预约审批" :back="true" color="white" background="#F5A623"></navigation-bar>

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
                            <text>{{ labIndex === 0 ? '全部教室' : labOptions[labIndex].name }}</text>
                            <image class="arrow-icon" src="/static/images/icons/search-icon.png"></image>
                        </view>
                    </view>
                </view>

                <view class="filter-item">
                    <view class="filter-label">日期</view>
                    <view class="custom-picker" @tap="showDatePicker">
                        <view class="picker">
                            <text>{{ dateFilter || '全部日期' }}</text>
                            <image class="arrow-icon" src="/static/images/icons/search-icon.png"></image>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 预约列表 -->
            <view class="reservation-list">
                <block v-if="filteredReservations.length > 0">
                    <view class="reservation-item" @tap="showReservationDetail" :data-id="item.id" v-for="(item, index) in filteredReservations" :key="index">
                        <view class="reservation-header">
                            <view class="reservation-id">预约编号：{{ item.id }}</view>
                            <view :class="'reservation-status ' + (item.status === '待审批' ? 'pending' : item.status === '已通过' ? 'approved' : 'rejected')">
                                {{ item.status }}
                            </view>
                        </view>

                        <view class="reservation-info">
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
                                <view class="info-label">申请时间：</view>
                                <view class="info-value">{{ item.applyTime }}</view>
                            </view>
                        </view>

                        <view class="reservation-purpose">
                            <text class="purpose-label">用途：</text>
                            <text class="purpose-content">{{ item.purpose }}</text>
                        </view>

                        <view class="reservation-actions" v-if="item.status === '待审批'" @tap.stop.prevent="stopPropagation">
                            <button class="action-btn reject-btn" @tap="showRejectModalFun" :data-id="item.id">拒绝</button>
                            <button class="action-btn approve-btn" @tap="approveReservation" :data-id="item.id">通过</button>
                        </view>
                    </view>
                </block>

                <view class="empty-list" v-else>
                    <image src="/static/images/icons/empty.png"></image>
                    <text>暂无预约申请</text>
                </view>
            </view>

            <!-- 详情弹窗 -->
            <view class="modal" v-if="showDetailModal">
                <view class="modal-mask" @tap="hideDetailModal"></view>
                <view class="modal-content detail-modal">
                    <view class="modal-header">
                        <text>预约详情</text>
                        <view class="close-btn" @tap="hideDetailModal">×</view>
                    </view>

                    <view class="modal-body">
                        <view :class="'detail-status ' + (currentReservation.status === '待审批' ? 'pending' : currentReservation.status === '已通过' ? 'approved' : 'rejected')">
                            {{ currentReservation.status }}
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">预约编号</view>
                                <view class="detail-value">{{ currentReservation.id }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">申请人</view>
                                <view class="detail-value">{{ currentReservation.applicant }} ({{ currentReservation.applicantType }})</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">联系方式</view>
                                <view class="detail-value">{{ currentReservation.contact }}</view>
                            </view>
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">实验室</view>
                                <view class="detail-value">{{ currentReservation.labName }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">预约日期</view>
                                <view class="detail-value">{{ currentReservation.date }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">时间段</view>
                                <view class="detail-value">{{ currentReservation.timeSlot }}</view>
                            </view>
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">用途说明</view>
                                <view class="detail-value purpose-box">{{ currentReservation.purpose }}</view>
                            </view>

                            <view class="detail-item" v-if="currentReservation.attachments && currentReservation.attachments.length > 0">
                                <view class="detail-label">附件</view>
                                <view class="detail-value attachments">
                                    <view
                                        class="attachment-item"
                                        @tap="previewAttachment"
                                        :data-url="item.url"
                                        v-for="(item, index) in currentReservation.attachments"
                                        :key="index"
                                    >
                                        <image src="/static/images/file.png"></image>

                                        <text>{{ item.name }}</text>
                                    </view>
                                </view>
                            </view>
                        </view>

                        <view class="detail-section" v-if="currentReservation.status !== '待审批'">
                            <view class="detail-item">
                                <view class="detail-label">审批时间</view>
                                <view class="detail-value">{{ currentReservation.approvalTime || '-' }}</view>
                            </view>

                            <view class="detail-item" v-if="currentReservation.status === '已拒绝'">
                                <view class="detail-label">拒绝原因</view>
                                <view class="detail-value reason-box">{{ currentReservation.rejectReason || '-' }}</view>
                            </view>
                        </view>
                    </view>

                    <view class="modal-footer" v-if="currentReservation.status === '待审批'">
                        <button class="footer-btn reject-btn" @tap="showRejectModalFun" :data-id="currentReservation.id">拒绝</button>
                        <button class="footer-btn approve-btn" @tap="approveReservation" :data-id="currentReservation.id">通过</button>
                    </view>
                </view>
            </view>

            <!-- 拒绝原因弹窗 -->
            <view class="modal" v-if="showRejectModal">
                <view class="modal-mask"></view>
                <view class="modal-content">
                    <view class="modal-header">
                        <text>拒绝原因</text>
                        <view class="close-btn" @tap="hideRejectModal">×</view>
                    </view>

                    <view class="modal-body">
                        <textarea class="reject-reason" placeholder="请输入拒绝原因" @input="onReasonInput" :value="rejectReason"></textarea>

                        <view class="reason-templates">
                            <view class="template-title">快速选择：</view>
                            <view class="template-list">
                                <view class="template-item" @tap="selectReasonTemplate" :data-reason="item" v-for="(item, index) in reasonTemplates" :key="index">{{ item }}</view>
                            </view>
                        </view>
                    </view>

                    <view class="modal-footer">
                        <button class="footer-btn cancel-btn" @tap="hideRejectModal">取消</button>
                        <button class="footer-btn confirm-btn" @tap="rejectReservation">确定</button>
                    </view>
                </view>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>

            <!-- 自定义状态选择器弹窗 -->
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

            <!-- 自定义实验室选择器弹窗 -->
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

            <!-- 自定义日期选择器弹窗 -->
            <view class="modal custom-picker-modal" v-if="showDatePickerModal">
                <view class="modal-mask" @tap="hideDatePicker"></view>
                <view class="modal-content picker-modal-content">
                    <view class="modal-header">
                        <text>选择日期</text>
                    </view>
                    <view class="modal-body date-picker-body">
                        <!-- 年份选择 -->
                        <picker-view
                            indicator-style="height: 100rpx;"
                            style="width: 100%; height: 400rpx"
                            :value="[tempYear - 2023, tempMonth - 1, tempDay - 1]"
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
                        <button class="cancel-btn" @tap="hideDatePicker">取消</button>
                        <button class="confirm-btn" @tap="confirmDatePicker">确定</button>
                    </view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onMounted } from 'vue';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
// admin-reservation-approval.ts

// 筛选选项
const statusOptions = ref<any[]>(['全部状态', '待审批', '已通过', '已拒绝']);

const statusIndex = ref<number>(0);

const labOptions = ref<any[]>([
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
]);

const labIndex = ref<number>(0);
const dateFilter = ref<string>('');

// 自定义选择器控制
const showStatusPickerModal = ref<boolean>(false);

const showLabPickerModal = ref<boolean>(false);
const showDatePickerModal = ref<boolean>(false);
const tempStatusIndex = ref<number>(0);
const tempLabIndex = ref<number>(0);

// 日期选择器数据
const years = ref<any[]>([2023, 2024, 2025]);

const months = ref<any[]>(Array.from(
    {
        length: 12
    },
    (_, i) => i + 1
));

const days = ref<any[]>(Array.from(
    {
        length: 31
    },
    (_, i) => i + 1
));

const tempYear = ref<number>(new Date().getFullYear());
const tempMonth = ref<number>(new Date().getMonth() + 1);
const tempDay = ref<number>(new Date().getDate());

// 预约列表
const reservations = ref<any[]>([
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
        status: '待审批',
        applyTime: '2023-11-10 14:30:25',
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
        status: '待审批',
        applyTime: '2023-11-12 09:10:28'
    }
]);

const filteredReservations = ref<any[]>([]);

// 详情弹窗
const showDetailModal = ref<boolean>(false);

const currentReservation = ref<any>({});

// 拒绝弹窗
const showRejectModal = ref<boolean>(false);

const rejectReason = ref<string>('');
const currentRejectId = ref<string>('');

// 拒绝原因模板
const reasonTemplates = ref<any[]>(['该时段实验室已被预约', '实验室设备正在维修', '申请信息不完整', '申请用途不符合实验室使用规定', '需要提供更详细的实验计划']);

const name = ref<string>('');

onMounted(() => {
    // 处理小程序 attached 生命周期
    // 初始化过滤后的预约列表
    filterReservations();
});

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
    filterReservations();
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
    filterReservations();
};

// 显示日期选择器
const showDatePicker = () => {
    // 如果已有日期，解析为年月日
    if (dateFilter.value) {
        const parts = dateFilter.value.split('-');
        tempYear.value = parseInt(parts[0]);
        tempMonth.value = parseInt(parts[1]);
        tempDay.value = parseInt(parts[2]);
    } else {
        // 否则使用当前日期
        const now = new Date();
        tempYear.value = now.getFullYear();
        tempMonth.value = now.getMonth() + 1;
        tempDay.value = now.getDate();
    }
    showDatePickerModal.value = true;
};

// 隐藏日期选择器
const hideDatePicker = () => {
    showDatePickerModal.value = false;
};

// 日期选择变化
const onDatePickerChange = (e: any) => {
    const values = e.detail.value;
    tempYear.value = years.value[values[0]];
    tempMonth.value = months.value[values[1]];
    tempDay.value = days.value[values[2]];
};

// 确认日期选择
const confirmDatePicker = () => {
    const formattedDate = `${tempYear.value}-${String(tempMonth.value).padStart(2, '0')}-${String(tempDay.value).padStart(2, '0')}`;
    dateFilter.value = formattedDate;
    showDatePickerModal.value = false;
    filterReservations();
};

// 过滤预约列表
const filterReservations = () => {
    let filtered = [...reservations.value];

    // 按状态筛选
    if (statusIndex.value > 0) {
        const status = statusOptions.value[statusIndex.value];
        filtered = filtered.filter((item) => item.status === status);
    }

    // 按实验室筛选
    if (labIndex.value > 0) {
        const labId = labOptions.value[labIndex.value].id;
        filtered = filtered.filter((item) => item.labId === labId);
    }

    // 按日期筛选
    if (dateFilter.value) {
        filtered = filtered.filter((item) => item.date === dateFilter.value);
    }

    // 更新过滤后的列表
    filteredReservations.value = filtered;
};

// 显示预约详情
const showReservationDetail = (e: any) => {
    const id = e.currentTarget.dataset.id;
    const reservation = reservations.value.find((item) => item.id === id);
    if (reservation) {
        showDetailModal.value = true;
        currentReservation.value = reservation;
    }
};

// 隐藏详情弹窗
const hideDetailModal = () => {
    showDetailModal.value = false;
};

// 显示拒绝弹窗
const showRejectModalFun = (e: any) => {
    const id = e.currentTarget.dataset.id;
    showRejectModal.value = true;
    currentRejectId.value = id;
    rejectReason.value = '';

    // 阻止事件冒泡
    e.stopPropagation();
};

// 隐藏拒绝弹窗
const hideRejectModal = () => {
    showRejectModal.value = false;
};

// 拒绝原因输入
const onReasonInput = (e: any) => {
    rejectReason.value = e.detail.value;
};

// 选择拒绝原因模板
const selectReasonTemplate = (e: any) => {
    const reason = e.currentTarget.dataset.reason;
    rejectReason.value = reason;
};

// 阻止事件冒泡
const stopPropagation = (e: any) => {
    e.stopPropagation();
};

// 通过预约
const approveReservation = (e: any) => {
    const id = e.currentTarget.dataset.id;
    // 关闭预约详情弹窗
    showDetailModal.value = false;
    
    setTimeout(() => {
        uni.showModal({
            title: '确认通过',
            content: '确定通过此预约申请吗？',
            confirmColor: '#1890FF',
            success: (res) => {
                if (res.confirm) {
                    updateReservationStatus(id, '已通过');
                }
            }
        });
    }, 100);

    // 阻止事件冒泡
    e.stopPropagation();
};

// 拒绝预约
const rejectReservation = () => {
    if (!rejectReason.value.trim()) {
        uni.showToast({
            title: '请输入拒绝原因',
            icon: 'none'
        });
        return;
    }
    updateReservationStatus(currentRejectId.value, '已拒绝', rejectReason.value);

    // 隐藏拒绝弹窗
    hideRejectModal();
};

// 更新预约状态
const updateReservationStatus = (id: string, status: string, rejectReasonArg?: string) => {
    const now = new Date();
    const approvalTime = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(
        2,
        '0'
    )}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;

    // 更新预约状态
    const newReservations = reservations.value.map((item) => {
        if (item.id === id) {
            const updatedItem = {
                ...item,
                status,
                approvalTime
            };
            if (status === '已拒绝' && rejectReasonArg) {
                updatedItem.rejectReason = rejectReasonArg;
            }
            return updatedItem;
        }
        return item;
    });

    // 更新数据
    reservations.value = newReservations;
    currentReservation.value = newReservations.find((item) => item.id === id) || {};
    // 重新过滤列表
    filterReservations();

    // 显示成功提示
    uni.showToast({
        title: status === '已通过' ? '已通过申请' : '已拒绝申请',
        icon: 'success'
    });
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
@import './admin-reservation-approval.less';
</style>
