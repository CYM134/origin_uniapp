<template>
    <view class="page-wrapper">
        <!-- pages/student-pending-process/student-pending-process.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="待办流程" color="white" background="#3B82F6" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <navigation-bar title="待办流程" :back="true" color="white" background="#3B82F6"></navigation-bar>
        <!-- #endif -->

        <view class="container">
            <view class="main-content">
                <view class="filter-sidebar">
                    <view :class="'filter-item ' + (selectedStatus === 'all' ? 'active' : '')" @tap="filterByStatus" data-status="all">
                        <text>全部</text>
                        <view v-if="statusCounts.all > 0" class="count-badge">{{ statusCounts.all }}</view>
                    </view>
                    <view :class="'filter-item ' + (selectedStatus === 'pending' ? 'active' : '')" @tap="filterByStatus" data-status="pending">
                        <text>待教师审核</text>
                        <view v-if="statusCounts.pending > 0" class="count-badge pending">{{ statusCounts.pending }}</view>
                    </view>
                    <view :class="'filter-item ' + (selectedStatus === 'teacher_approved' ? 'active' : '')" @tap="filterByStatus" data-status="teacher_approved">
                        <text>待管理员审核</text>
                        <view v-if="statusCounts.teacher_approved > 0" class="count-badge teacher-approved">{{ statusCounts.teacher_approved }}</view>
                    </view>
                    <view :class="'filter-item ' + (selectedStatus === 'approved' ? 'active' : '')" @tap="filterByStatus" data-status="approved">
                        <text>已通过</text>
                        <view v-if="statusCounts.approved > 0" class="count-badge approved">{{ statusCounts.approved }}</view>
                    </view>
                    <view :class="'filter-item ' + (selectedStatus === 'rejected' ? 'active' : '')" @tap="filterByStatus" data-status="rejected">
                        <text>已拒绝</text>
                        <view v-if="statusCounts.rejected > 0" class="count-badge rejected">{{ statusCounts.rejected }}</view>
                    </view>
                    <view :class="'filter-item ' + (selectedStatus === 'cancelled' ? 'active' : '')" @tap="filterByStatus" data-status="cancelled">
                        <text>已取消</text>
                        <view v-if="statusCounts.cancelled > 0" class="count-badge cancelled">{{ statusCounts.cancelled }}</view>
                    </view>
                </view>
                <view class="application-list">
                    <view v-if="filteredApplications.length === 0" class="empty-state">
                        <image src="/static/images/icons/empty-list.svg" mode="aspectFit"></image>
                        <text class="empty-title">暂无{{ selectedStatus === 'all' ? '' : statusMap[selectedStatus] }}申请</text>
                        <text class="empty-desc">{{ selectedStatus === 'all' ? '您还没有提交过任何申请' : '当前筛选条件下没有找到相关申请' }}</text>
                        <button v-if="selectedStatus === 'all'" class="new-application-btn" @tap="goToNewApplication">
                            <image src="/static/images/icons/add.svg" mode="aspectFit"></image>
                            <text>新建申请</text>
                        </button>
                    </view>

                    <view class="application-item" @tap="viewApplicationDetail" :data-application="item" v-for="(item, index) in filteredApplications" :key="index">
                        <view class="application-header">
                            <view class="title-section">
                                <text class="application-title">{{ item.title }}</text>
                                <view :class="'status-badge ' + item.status">
                                    <text>{{ item.statusText }}</text>
                                </view>
                            </view>
                            <view class="time-section">
                                <text class="submit-time">{{ item.submitTimeText }}</text>
                            </view>
                        </view>

                        <view class="application-content">
                            <view class="info-row">
                                <view class="info-item">
                                    <image class="info-icon" src="/static/images/icons/location.svg" mode="aspectFit"></image>
                                    <text class="info-text">{{ item.lab.name }}</text>
                                </view>
                                <view class="info-item">
                                    <image class="info-icon" src="/static/images/icons/calendar.svg" mode="aspectFit"></image>
                                    <text class="info-text">{{ item.date }}</text>
                                </view>
                            </view>

                            <view class="info-row">
                                <view class="info-item">
                                    <image class="info-icon" src="/static/images/icons/clock.svg" mode="aspectFit"></image>
                                    <text class="info-text">{{ item.timeText }}</text>
                                </view>
                                <view class="info-item">
                                    <image class="info-icon" src="/static/images/icons/user.svg" mode="aspectFit"></image>
                                    <text class="info-text">{{ item.studentCount }}人</text>
                                </view>
                            </view>

                            <view class="purpose-section">
                                <text class="purpose-label">使用目的：</text>
                                <text class="purpose-text">{{ item.purpose }}</text>
                            </view>
                        </view>

                        <view class="application-footer">
                            <view class="progress-info">
                                <view v-if="item.status === 'pending'" class="progress-text">
                                    <text>等待教师审核...</text>
                                </view>
                                <view v-else-if="item.status === 'teacher_approved'" class="progress-text">
                                    <text>教师已审核通过，等待管理员最终审核...</text>
                                </view>
                                <view v-else-if="item.status === 'approved'" class="progress-text success">
                                    <text>审核通过，可正常使用</text>
                                </view>
                                <view v-else-if="item.status === 'rejected'" class="progress-text error">
                                    <text>审核未通过：{{ item.rejectReason || '未说明原因' }}</text>
                                </view>
                                <view v-else-if="item.status === 'cancelled'" class="progress-text cancelled">
                                    <text>申请已取消</text>
                                </view>
                            </view>

                            <view class="action-buttons">
                                <view v-if="item.status === 'pending'" class="action-btn cancel" @tap="cancelApplication" :data-id="item.id" @tap.stop.prevent="stopPropagation">
                                    <text>取消申请</text>
                                </view>
                                <view
                                    v-else-if="item.status === 'rejected'"
                                    class="action-btn reapply"
                                    @tap="reapplyApplication"
                                    :data-application="item"
                                    @tap.stop.prevent="stopPropagation"
                                >
                                    <text>重新申请</text>
                                </view>
                                <view
                                    v-else-if="item.status === 'approved' && item.canModify"
                                    class="action-btn modify"
                                    @tap="modifyApplication"
                                    :data-application="item"
                                    @tap.stop.prevent="stopPropagation"
                                >
                                    <text>修改申请</text>
                                </view>
                            </view>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 浮动操作按钮 -->
            <view class="fab" @tap="goToNewApplication">
                <image src="/static/images/icons/add.svg" mode="aspectFit"></image>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>

        <!-- 申请详情弹窗 -->
        <view v-if="showDetailModal" class="modal-overlay" @tap="closeDetailModal">
            <view class="modal-content detail-modal" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">申请详情</text>
                    <view class="close-btn" @tap="closeDetailModal">×</view>
                </view>
                <scroll-view class="modal-body" :scroll-y="true">
                    <view class="detail-section">
                        <view class="detail-title">
                            <text>{{ selectedApplication.title }}</text>
                            <view :class="'status-badge ' + selectedApplication.status">
                                <text>{{ selectedApplication.statusText }}</text>
                            </view>
                        </view>

                        <view class="detail-grid">
                            <view class="detail-item">
                                <text class="label">申请类型：</text>
                                <text class="value">{{ selectedApplication.type.name }}</text>
                            </view>
                            <view class="detail-item">
                                <text class="label">实验室：</text>
                                <text class="value">{{ selectedApplication.lab.name }}</text>
                            </view>
                            <view class="detail-item">
                                <text class="label">预约日期：</text>
                                <text class="value">{{ selectedApplication.date }}</text>
                            </view>
                            <view class="detail-item">
                                <text class="label">时间段：</text>
                                <text class="value">{{ selectedApplication.timeText }}</text>
                            </view>
                            <view class="detail-item">
                                <text class="label">预约人数：</text>
                                <text class="value">{{ selectedApplication.studentCount }}人</text>
                            </view>
                            <view class="detail-item">
                                <text class="label">联系方式：</text>
                                <text class="value">{{ selectedApplication.contact }}</text>
                            </view>
                            <view v-if="selectedApplication.teacher" class="detail-item">
                                <text class="label">指导老师：</text>
                                <text class="value">{{ selectedApplication.teacher }}</text>
                            </view>
                            <view class="detail-item full-width">
                                <text class="label">使用目的：</text>
                                <text class="value description">{{ selectedApplication.purpose }}</text>
                            </view>
                            <view v-if="selectedApplication.requirements" class="detail-item full-width">
                                <text class="label">特殊要求：</text>
                                <text class="value description">{{ selectedApplication.requirements }}</text>
                            </view>
                            <view class="detail-item">
                                <text class="label">提交时间：</text>
                                <text class="value">{{ selectedApplication.submitTimeText }}</text>
                            </view>
                            <view v-if="selectedApplication.reviewTime" class="detail-item">
                                <text class="label">审核时间：</text>
                                <text class="value">{{ selectedApplication.reviewTimeText }}</text>
                            </view>
                            <view v-if="selectedApplication.reviewer" class="detail-item">
                                <text class="label">审核人：</text>
                                <text class="value">{{ selectedApplication.reviewer }}</text>
                            </view>
                            <view v-if="selectedApplication.rejectReason" class="detail-item full-width">
                                <text class="label">拒绝原因：</text>
                                <text class="value description error">{{ selectedApplication.rejectReason }}</text>
                            </view>
                        </view>

                        <!-- 审批流水 -->
                        <view v-if="selectedApplication.approvalLogs && selectedApplication.approvalLogs.length > 0" class="approval-flow">
                            <text class="approval-flow-title">审批流水</text>
                            <view class="approval-log-item" v-for="(log, logIndex) in selectedApplication.approvalLogs" :key="logIndex">
                                <view class="approval-log-head">
                                    <text class="approval-log-action">{{ log.action }}</text>
                                    <text class="approval-log-time">{{ log.createTime }}</text>
                                </view>
                                <view class="approval-log-meta">
                                    <text v-if="log.reviewerName" class="approval-log-reviewer">{{ log.reviewerName }}</text>
                                    <text v-if="log.stage" class="approval-log-stage">{{ log.stage }}</text>
                                </view>
                                <text v-if="log.comment" class="approval-log-comment">{{ log.comment }}</text>
                            </view>
                        </view>
                    </view>
                </scroll-view>
                <view class="modal-footer">
                    <button v-if="selectedApplication.status === 'pending'" class="cancel-btn" @tap="cancelApplicationFromDetail">取消申请</button>
                    <button v-else-if="selectedApplication.status === 'rejected'" class="reapply-btn" @tap="reapplyApplicationFromDetail">重新申请</button>
                    <button v-else-if="selectedApplication.status === 'approved' && selectedApplication.canModify" class="modify-btn" @tap="modifyApplicationFromDetail">
                        修改申请
                    </button>
                    <button class="close-btn-footer" @tap="closeDetailModal">关闭</button>
                </view>
            </view>
        </view>

        <!-- 取消确认弹窗 -->
        <view v-if="showCancelModal" class="modal-overlay" @tap="closeCancelModal">
            <view class="modal-content cancel-modal" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">确认取消</text>
                </view>
                <view class="modal-body">
                    <text class="cancel-message">确定要取消这个申请吗？取消后将无法恢复。</text>
                </view>
                <view class="modal-footer">
                    <button class="cancel-btn" @tap="closeCancelModal">我再想想</button>
                    <button class="confirm-btn" @tap="confirmCancel">确认取消</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
import { getMyReservations, cancelReservation, getReservationDetail } from '@/api/student.js';
// pages/student-pending-process/student-pending-process.ts

// 筛选状态
const selectedStatus = ref('all');

const statusMap = ref({
    all: '全部',
    pending: '待教师审核',
    teacher_approved: '待管理员审核',
    approved: '已通过',
    rejected: '已拒绝',
    cancelled: '已取消'
});

// 申请数据
const applications = ref<any[]>([]);

const filteredApplications = ref<any[]>([]);

// 状态统计
const statusCounts = ref({
    all: 0,
    pending: 0,
    teacher_approved: 0,
    approved: 0,
    rejected: 0,
    cancelled: 0
});

// 弹窗相关
const showDetailModal = ref(false);

const showCancelModal = ref(false);
const selectedApplication = ref<any>(null);
const cancelApplicationId = ref('');
const name = ref('');

onLoad(() => {
    loadApplications();
});

onShow(() => {
    loadApplications();
});

onPullDownRefresh(() => {
    loadApplications();
    uni.stopPullDownRefresh();
});

// 格式化日期时间
const formatDateTime = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
};

// 加载申请数据
const loadApplications = async () => {
    try {
        // 从后端接口加载当前学生的预约申请
        const list = (await getMyReservations()) || [];

        // 待办流程只展示进行中/已结审的申请；completed 交由「已办流程」页处理
        const myApplications = (Array.isArray(list) ? list : [])
            .filter((item: any) => item.status !== 'completed')
            // 适配模板所需字段形状（lab.name / type.name）
            .map((item: any) => ({
                ...item,
                lab: { id: item.labId, name: item.labName },
                type: { name: item.applicationTypeName }
            }));

        // 处理申请数据，添加显示用的字段
        const processedApplications = myApplications.map((app: any) => {
            const submitTime = new Date(app.submitTime);
            const reviewTime = app.reviewTime ? new Date(app.reviewTime) : null;
            const teacherReviewTime = app.teacherReviewTime ? new Date(app.teacherReviewTime) : null;
            let timeText = app.timeText;
            if (!timeText) {
                if (app.startTime && app.endTime) {
                    timeText = `${app.startTime}-${app.endTime}`;
                } else {
                    timeText = '未选择时间段';
                }
            }

            // 状态文本/样式优先复用后端算好的 statusText / statusClass，
            // 自然覆盖 completed 等所有状态，避免出现「未知状态」。
            const progressTextMap: { [key: string]: string } = {
                pending: '等待教师审核...',
                teacher_approved: '教师已审核通过，等待管理员最终审核...',
                approved: '审核通过，可正常使用',
                rejected: '申请被拒绝',
                cancelled: '申请已取消',
                completed: '申请已完成使用'
            };
            const statusText = app.statusText || app.status;
            const statusClass = app.statusClass || 'status-' + String(app.status).replace(/_/g, '-');
            const progressText = progressTextMap[app.status] || statusText;
            const canModify = app.status === 'approved' && new Date(app.date).getTime() > Date.now() + 86400 * 1000;
            return {
                ...app,
                timeText,
                canModify,
                statusText,
                statusClass,
                progressText,
                submitTimeText: formatDateTime(submitTime),
                reviewTimeText: reviewTime ? formatDateTime(reviewTime) : '',
                teacherReviewTimeText: teacherReviewTime ? formatDateTime(teacherReviewTime) : '',
                teacherReviewerName: app.teacherReviewerName || '',
                showTeacherReview: app.status === 'teacher_approved' || app.status === 'approved'
            };
        });

        // 按提交时间倒序排列
        processedApplications.sort((a: any, b: any) => new Date(b.submitTime).getTime() - new Date(a.submitTime).getTime());
        applications.value = processedApplications;
        calculateStatusCounts();
        filterApplications();
    } catch (error: any) {
        console.error('加载申请数据失败:', error);
        applications.value = [];
        calculateStatusCounts();
        filterApplications();
        uni.showToast({
            title: error?.data?.message || '加载失败',
            icon: 'none'
        });
    }
};

// 计算状态统计
const calculateStatusCounts = () => {
    const counts = {
        all: applications.value.length,
        pending: 0,
        teacher_approved: 0,
        approved: 0,
        rejected: 0,
        cancelled: 0
    };
    applications.value.forEach((app: any) => {
        if (counts.hasOwnProperty(app.status)) {
            counts[app.status as keyof typeof counts]++;
        }
    });
    statusCounts.value = counts;
};

// 按状态筛选
const filterByStatus = (e: any) => {
    const status = e.currentTarget.dataset.status;
    selectedStatus.value = status;
    filterApplications();
};

// 筛选申请
const filterApplications = () => {
    let filtered = applications.value;
    if (selectedStatus.value !== 'all') {
        filtered = applications.value.filter((app: any) => app.status === selectedStatus.value);
    }
    filteredApplications.value = filtered;
};

// 查看申请详情：列表项不含 approvalLogs，需拉取详情接口
const viewApplicationDetail = async (e: any) => {
    const item = e.currentTarget.dataset.application;
    if (!item || !item.id) {
        return;
    }
    uni.showLoading({ title: '加载中...' });
    try {
        const detail: any = (await getReservationDetail(item.id)) || {};
        // 映射详情字段到模板所需形状（lab.name / type.name）及真实审核人/时间别名
        const reviewer = detail.adminReviewerName || detail.teacherReviewerName || '';
        const reviewTime = detail.adminReviewTime || detail.teacherReviewTime || '';
        selectedApplication.value = {
            ...detail,
            lab: { id: detail.labId, name: detail.labName },
            type: { name: detail.applicationTypeName },
            timeText: detail.timeText || (detail.startTime && detail.endTime ? `${detail.startTime}-${detail.endTime}` : '未选择时间段'),
            submitTimeText: detail.submitTime ? formatDateTime(new Date(detail.submitTime)) : '',
            reviewer,
            reviewTime,
            reviewTimeText: reviewTime ? formatDateTime(new Date(reviewTime)) : '',
            approvalLogs: Array.isArray(detail.approvalLogs) ? detail.approvalLogs : []
        };
        uni.hideLoading();
        showDetailModal.value = true;
    } catch (error: any) {
        uni.hideLoading();
        console.error('加载详情失败:', error);
        uni.showToast({
            title: error?.data?.message || '加载详情失败',
            icon: 'none'
        });
    }
};

// 关闭详情弹窗
const closeDetailModal = () => {
    showDetailModal.value = false;
    selectedApplication.value = null;
};

// 取消申请
const cancelApplication = (e: any) => {
    const id = e.currentTarget.dataset.id;
    cancelApplicationId.value = id;
    showCancelModal.value = true;
};

// 从详情页取消申请
const cancelApplicationFromDetail = () => {
    if (selectedApplication.value) {
        cancelApplicationId.value = (selectedApplication.value as any).id;
        showCancelModal.value = true;
        showDetailModal.value = false;
    }
};

// 关闭取消确认弹窗
const closeCancelModal = () => {
    showCancelModal.value = false;
    cancelApplicationId.value = '';
};

// 确认取消申请
const confirmCancel = async () => {
    if (!cancelApplicationId.value) {
        return;
    }
    uni.showLoading({
        title: '取消中...'
    });

    try {
        // 调用后端取消接口
        await cancelReservation(cancelApplicationId.value);
        uni.hideLoading();
        uni.showToast({
            title: '申请已取消',
            icon: 'success'
        });
        showCancelModal.value = false;
        cancelApplicationId.value = '';
        loadApplications();
    } catch (error: any) {
        uni.hideLoading();
        console.error('取消申请失败:', error);
        uni.showToast({
            title: error?.data?.message || '取消失败，请重试',
            icon: 'none'
        });
    }
};

// 重新申请
const reapplyApplication = (e: any) => {
    const application = e.currentTarget.dataset.application;

    // 跳转到申请页面，并传递原申请数据
    uni.navigateTo({
        url: `/pages/student-reservation-apply/student-reservation-apply?reapply=true&data=${encodeURIComponent(JSON.stringify(application))}`
    });
};

// 从详情页重新申请
const reapplyApplicationFromDetail = () => {
    if (selectedApplication.value) {
        uni.navigateTo({
            url: `/pages/student-reservation-apply/student-reservation-apply?reapply=true&data=${encodeURIComponent(JSON.stringify(selectedApplication.value))}`
        });
    }
};

// 修改申请
const modifyApplication = (e: any) => {
    const application = e.currentTarget.dataset.application;

    // 跳转到申请页面，并传递原申请数据
    uni.navigateTo({
        url: `/pages/student-reservation-apply/student-reservation-apply?modify=true&data=${encodeURIComponent(JSON.stringify(application))}`
    });
};

// 从详情页修改申请
const modifyApplicationFromDetail = () => {
    if (selectedApplication.value) {
        uni.navigateTo({
            url: `/pages/student-reservation-apply/student-reservation-apply?modify=true&data=${encodeURIComponent(JSON.stringify(selectedApplication.value))}`
        });
    }
};

// 跳转到新建申请
const goToNewApplication = () => {
    uni.navigateTo({
        url: '/pages/student-reservation-apply/student-reservation-apply'
    });
};

// 阻止事件冒泡
const stopPropagation = () => {
    // 阻止事件冒泡
};
</script>
<style lang="less">
@import './student-pending-process.less';
</style>
