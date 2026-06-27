<template>
    <view class="page-wrapper">
        <!-- pages/student-completed-process/student-completed-process.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="已办流程" color="white" background="#3B82F6" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <navigation-bar title="已办流程" :back="true" color="white" background="#3B82F6" />
        <!-- #endif -->

        <view class="container">
            <view class="main-content">
                <view class="filter-sidebar">
                    <view :class="'filter-item ' + (selectedStatus === 'all' ? 'active' : '')" @tap="filterByStatus" data-status="all">
                        <text>全部</text>
                        <view v-if="statusCounts.all > 0" class="count-badge">{{ statusCounts.all }}</view>
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
                    <view :class="'filter-item ' + (selectedStatus === 'completed' ? 'active' : '')" @tap="filterByStatus" data-status="completed">
                        <text>已完成</text>
                        <view v-if="statusCounts.completed > 0" class="count-badge info">{{ statusCounts.completed }}</view>
                    </view>
                </view>
                <view class="application-list">
                    <!-- 空状态 -->
                    <view class="empty-state" v-if="filteredApplications.length === 0">
                        <image class="empty-icon" src="/static/images/icons/empty-list.svg" mode="aspectFit" />
                        <text class="empty-text">暂无{{ statusMap[selectedStatus] }}申请</text>
                        <view class="empty-action">
                            <button class="new-application-btn" @tap="goToNewApplication">
                                <image class="btn-icon" src="/static/images/icons/add.svg" mode="aspectFit" />
                                新建申请
                            </button>
                        </view>
                    </view>

                    <!-- 申请项 -->
                    <view class="application-item" v-for="(item, index) in filteredApplications" :key="index">
                        <view class="item-header" @tap="viewApplicationDetail" :data-application="item">
                            <view class="item-title-row">
                                <text class="title-text">{{ item.title }}</text>
                                <view :class="'status-badge status-' + item.status">{{ item.statusText }}</view>
                            </view>
                            <text class="item-time">{{ item.submitTimeText }}</text>
                        </view>

                        <view class="item-content" @tap="viewApplicationDetail" :data-application="item">
                            <view class="content-row">
                                <image class="content-icon" src="/static/images/icons/location.svg" mode="aspectFit" />
                                <text class="content-text">{{ item.labName }}</text>
                            </view>
                            <view class="content-row">
                                <image class="content-icon" src="/static/images/icons/calendar.svg" mode="aspectFit" />
                                <text class="content-text">{{ item.date }} {{ item.timeText }}</text>
                            </view>
                            <view class="content-row">
                                <image class="content-icon" src="/static/images/icons/user.svg" mode="aspectFit" />
                                <text class="content-text">{{ item.studentCount }}人</text>
                            </view>
                            <view class="content-row" v-if="item.purpose">
                                <image class="content-icon" src="/static/images/icons/info.svg" mode="aspectFit" />
                                <text class="content-text purpose-text">{{ item.purpose }}</text>
                            </view>
                        </view>

                        <!-- 审核信息 -->

                        <view class="review-info" v-if="item.status !== 'pending'">
                            <view class="review-time">
                                <text class="review-label">审核时间：</text>
                                <text class="review-value">{{ item.reviewTimeText || '未知' }}</text>
                            </view>
                            <view class="review-comment" v-if="item.reviewComment">
                                <text class="review-label">审核意见：</text>
                                <text class="review-value">{{ item.reviewComment }}</text>
                            </view>
                            <view class="reviewer" v-if="item.reviewer">
                                <text class="review-label">审核人：</text>
                                <text class="review-value">{{ item.reviewer }}</text>
                            </view>
                        </view>

                        <!-- 操作按钮 -->

                        <view class="item-actions" v-if="item.status === 'approved' || item.status === 'rejected'">
                            <button class="action-btn secondary" v-if="item.status === 'rejected'" @tap="reapplyApplication" :data-application="item">
                                <image class="btn-icon" src="/static/images/icons/refresh.svg" mode="aspectFit" />
                                重新申请
                            </button>
                            <button class="action-btn secondary" v-if="item.status === 'approved' && item.canModify" @tap="modifyApplication" :data-application="item">
                                <image class="btn-icon" src="/static/images/icons/edit.svg" mode="aspectFit" />
                                修改申请
                            </button>
                            <button class="action-btn primary" v-if="item.status === 'approved' && !item.isCompleted" @tap="markAsCompleted" :data-application="item">
                                标记完成
                            </button>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 浮动操作按钮 -->
            <view class="fab" @tap="goToNewApplication">
                <image class="fab-icon" src="/static/images/icons/add.svg" mode="aspectFit" />
            </view>

            <!-- 页脚 -->
            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>

        <!-- 申请详情弹窗 -->
        <view class="modal-overlay" v-if="showDetailModal" @tap="closeDetailModal">
            <view class="modal-content" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">申请详情</text>
                    <view class="modal-close" @tap="closeDetailModal">
                        <image class="close-icon" src="/static/images/icons/close.svg" mode="aspectFit" />
                    </view>
                </view>

                <scroll-view class="modal-body" :scroll-y="true">
                    <view class="detail-section" v-if="selectedApplication">
                        <view class="detail-header">
                            <view class="detail-title">
                                <text class="title-text">
                                    {{ selectedApplication.applicationTypeName || selectedApplication.courseName || '预约申请' }}
                                </text>
                                <view :class="'status-badge status-' + selectedApplication.status">{{ selectedApplication.statusText }}</view>
                            </view>
                            <text class="detail-time">{{ selectedApplication.submitTimeText }}</text>
                        </view>

                        <view class="detail-item">
                            <text class="detail-label">实验室：</text>
                            <text class="detail-value">{{ selectedApplication.labName }}</text>
                        </view>

                        <view class="detail-item">
                            <text class="detail-label">使用日期：</text>
                            <text class="detail-value">{{ selectedApplication.date }}</text>
                        </view>

                        <view class="detail-item">
                            <text class="detail-label">使用时间：</text>
                            <text class="detail-value">{{ selectedApplication.timeText }}</text>
                        </view>

                        <view class="detail-item">
                            <text class="detail-label">使用人数：</text>
                            <text class="detail-value">{{ selectedApplication.studentCount }}人</text>
                        </view>

                        <view class="detail-item" v-if="selectedApplication.title">
                            <text class="detail-label">申请主题：</text>
                            <text class="detail-value">{{ selectedApplication.title }}</text>
                        </view>

                        <view class="detail-item" v-if="selectedApplication.purpose">
                            <text class="detail-label">使用目的：</text>
                            <text class="detail-value">{{ selectedApplication.purpose }}</text>
                        </view>

                        <view class="detail-item" v-if="selectedApplication.teacher">
                            <text class="detail-label">指导老师：</text>
                            <text class="detail-value">{{ selectedApplication.teacher }}</text>
                        </view>

                        <view class="detail-item" v-if="selectedApplication.contact">
                            <text class="detail-label">联系方式：</text>
                            <text class="detail-value">{{ selectedApplication.contact }}</text>
                        </view>

                        <view class="detail-item" v-if="selectedApplication.requirements">
                            <text class="detail-label">特殊要求：</text>
                            <text class="detail-value">{{ selectedApplication.requirements }}</text>
                        </view>

                        <view class="detail-item" v-if="selectedApplication.emergencyContact">
                            <text class="detail-label">紧急联系人：</text>
                            <text class="detail-value">{{ selectedApplication.emergencyContact }}</text>
                        </view>

                        <view class="detail-item" v-if="selectedApplication.emergencyPhone">
                            <text class="detail-label">紧急联系电话：</text>
                            <text class="detail-value">{{ selectedApplication.emergencyPhone }}</text>
                        </view>

                        <!-- 审核信息 -->
                        <view class="review-section" v-if="selectedApplication.status !== 'pending'">
                            <view class="section-title">审核信息</view>

                            <view class="detail-item">
                                <text class="detail-label">审核状态：</text>
                                <text :class="'detail-value status-' + selectedApplication.status">{{ selectedApplication.statusText }}</text>
                            </view>

                            <view class="detail-item" v-if="selectedApplication.reviewTimeText">
                                <text class="detail-label">审核时间：</text>
                                <text class="detail-value">{{ selectedApplication.reviewTimeText }}</text>
                            </view>

                            <view class="detail-item" v-if="selectedApplication.reviewer">
                                <text class="detail-label">审核人：</text>
                                <text class="detail-value">{{ selectedApplication.reviewer }}</text>
                            </view>

                            <view class="detail-item" v-if="selectedApplication.reviewComment">
                                <text class="detail-label">审核意见：</text>
                                <text class="detail-value">{{ selectedApplication.reviewComment }}</text>
                            </view>
                        </view>

                        <!-- 审批流水 -->
                        <view class="review-section" v-if="selectedApplication.approvalLogs && selectedApplication.approvalLogs.length > 0">
                            <view class="section-title">审批流水</view>
                            <view class="detail-item" v-for="(log, logIndex) in selectedApplication.approvalLogs" :key="logIndex">
                                <text class="detail-label">{{ log.action }}{{ log.reviewerName ? '（' + log.reviewerName + '）' : '' }}：</text>
                                <text class="detail-value">{{ log.createTime }}{{ log.comment ? ' ' + log.comment : '' }}</text>
                            </view>
                        </view>
                    </view>
                </scroll-view>

                <view class="modal-footer">
                    <button class="modal-btn secondary" v-if="selectedApplication.status === 'rejected'" @tap="reapplyApplicationFromDetail">重新申请</button>
                    <button class="modal-btn secondary" v-if="selectedApplication.status === 'approved' && selectedApplication.canModify" @tap="modifyApplicationFromDetail">
                        修改申请
                    </button>
                    <button class="modal-btn primary" v-if="selectedApplication.status === 'approved' && !selectedApplication.isCompleted" @tap="markAsCompletedFromDetail">
                        标记完成
                    </button>
                </view>
            </view>
        </view>

        <!-- 完成确认弹窗 -->
        <view class="modal-overlay" v-if="showCompleteModal" @tap="closeCompleteModal">
            <view class="modal-content small" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">确认完成</text>
                </view>

                <view class="modal-body">
                    <text class="confirm-text">确认标记该申请为已完成状态吗？</text>
                    <text class="confirm-note">标记后将无法修改申请信息</text>
                </view>

                <view class="modal-footer">
                    <button class="modal-btn secondary" @tap="closeCompleteModal">取消</button>
                    <button class="modal-btn primary" @tap="confirmComplete">确认</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
import { getMyReservations, completeReservation, getReservationDetail } from '@/api/student';
// pages/student-completed-process/student-completed-process.ts

// 筛选状态
const selectedStatus = ref('all');
const statusMap = ref({
    all: '全部',
    teacher_approved: '待管理员审核',
    approved: '已通过',
    rejected: '已拒绝',
    cancelled: '已取消',
    completed: '已完成'
});
// 申请数据
const applications = ref<any[]>([]);
const filteredApplications = ref<any[]>([]);
// 状态统计
const statusCounts = ref({
    all: 0,
    approved: 0,
    rejected: 0,
    cancelled: 0,
    completed: 0
});
// 弹窗相关
const showDetailModal = ref(false);
const showCompleteModal = ref(false);
const selectedApplication = ref<any>(null);
const completeApplicationId = ref('');

// 格式化日期时间
const formatDateTime = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
};

// 获取状态文本
const getStatusText = (status: string, isCompleted: boolean): string => {
    if (status === 'approved' && isCompleted) {
        return '已完成';
    }
    const statusTextMap: {
        [key: string]: string;
    } = {
        approved: '已通过',
        rejected: '已拒绝',
        cancelled: '已取消',
        completed: '已完成'
    };
    return statusTextMap[status] || status;
};

// 筛选申请：状态以后端真值为准（completed 为真实状态，不再依赖前端伪造的 isCompleted）
const filterApplications = () => {
    let filtered = applications.value;
    if (selectedStatus.value !== 'all') {
        filtered = applications.value.filter((app: any) => app.status === selectedStatus.value);
    }
    filteredApplications.value = filtered;
};

// 计算状态统计
const calculateStatusCounts = () => {
    const counts = {
        all: applications.value.length,
        approved: 0,
        rejected: 0,
        cancelled: 0,
        completed: 0
    };
    applications.value.forEach((app: any) => {
        if (counts.hasOwnProperty(app.status)) {
            counts[app.status as keyof typeof counts]++;
        }
    });
    statusCounts.value = counts;
};

// 加载申请数据
const loadApplications = async () => {
    try {
        // 从后端获取我的预约列表
        const list = (await getMyReservations()) || [];

        // 只显示已办流程（非待审核状态）
        const myCompletedApplications = (Array.isArray(list) ? list : []).filter((app: any) => {
            return app.status !== 'pending';
        });

        // 处理申请数据，添加显示用的字段
        const processedApplications = myCompletedApplications.map((rawApp: any) => {
            // 字段对齐：后端别名 -> 模板/处理逻辑所需字段
            const app: any = {
                ...rawApp,
                reviewComment: rawApp.reviewComment || rawApp.adminReviewComment || rawApp.rejectReason || rawApp.teacherReviewReason || '',
                reviewer: rawApp.reviewer || rawApp.adminReviewerName || rawApp.teacherReviewerName || '',
                reviewTime: rawApp.reviewTime || rawApp.adminReviewTime || rawApp.approvalTime || rawApp.teacherReviewTime || '',
                teacherReviewTime: rawApp.teacherReviewTime || '',
                teacherReviewerName: rawApp.teacherReviewerName || ''
            };
            const submitTime = app.submitTime ? new Date(app.submitTime) : new Date(app.applyTime || app.createTime || Date.now());
            const reviewTime = app.reviewTime ? new Date(app.reviewTime) : null;
            const teacherReviewTime = app.teacherReviewTime ? new Date(app.teacherReviewTime) : null;

            // 兼容字段
            const labName = app.labName || (app.lab && app.lab.name) || app.laboratory || '';
            const studentCount = app.studentCount || app.peopleCount || '';
            const title = app.title || app.subject || '';
            let timeText = app.timeText || app.timeSlot;
            if (!timeText) {
                if (app.startTime && app.endTime) {
                    timeText = `${app.startTime}-${app.endTime}`;
                } else {
                    timeText = '未选择时间段';
                }
            }

            // 判断是否已过期（仅用于展示，不据此伪造状态）
            const isExpired = new Date(app.date).getTime() < Date.now();
            // 状态与是否完成一律以后端真值为准，不在前端臆造 completed/isCompleted，
            // 否则过期但未完成的 approved 预约会被永久屏蔽「标记完成」按钮、无法真正落库。
            const finalStatus = app.status;
            const isCompleted = app.isCompleted;

            // 根据状态设置显示文本和样式
            let statusText = '';
            let statusClass = '';
            let progressText = '';
            let reviewTimeText = '';
            switch (finalStatus) {
                case 'teacher_approved':
                    statusText = '待管理员审核';
                    statusClass = 'status-teacher-approved';
                    progressText = '教师已审核通过，等待管理员最终审核';
                    reviewTimeText = teacherReviewTime ? formatDateTime(teacherReviewTime) : '';
                    break;
                case 'approved':
                    if (isCompleted) {
                        statusText = '已完成';
                        statusClass = 'status-completed';
                        progressText = '申请已完成使用';
                    } else {
                        statusText = '已通过';
                        statusClass = 'status-approved';
                        progressText = '审核通过，可正常使用';
                    }
                    reviewTimeText = reviewTime ? formatDateTime(reviewTime) : '';
                    break;
                case 'rejected':
                    statusText = '已拒绝';
                    statusClass = 'status-rejected';
                    progressText = '申请被拒绝';
                    reviewTimeText = reviewTime ? formatDateTime(reviewTime) : '';
                    break;
                case 'cancelled':
                    statusText = '已取消';
                    statusClass = 'status-cancelled';
                    progressText = '申请已取消';
                    reviewTimeText = '';
                    break;
                case 'completed':
                    statusText = '已完成';
                    statusClass = 'status-completed';
                    progressText = '申请已完成使用';
                    reviewTimeText = reviewTime ? formatDateTime(reviewTime) : '';
                    break;
                default:
                    statusText = '未知状态';
                    statusClass = 'status-unknown';
                    progressText = '状态未知';
                    reviewTimeText = reviewTime ? formatDateTime(reviewTime) : '';
            }

            // 判断是否可以修改（已通过且未完成且距离使用时间大于24小时）
            const canModify = finalStatus === 'approved' && !isCompleted && new Date(app.date).getTime() > Date.now() + 86400 * 1000;
            return {
                ...app,
                labName,
                studentCount,
                title,
                timeText,
                status: finalStatus,
                isCompleted,
                canModify,
                isExpired,
                statusText,
                statusClass,
                progressText,
                submitTimeText: formatDateTime(submitTime),
                reviewTimeText,
                teacherReviewTimeText: teacherReviewTime ? formatDateTime(teacherReviewTime) : '',
                teacherReviewerName: app.teacherReviewerName || '',
                showTeacherReview: finalStatus === 'teacher_approved' || finalStatus === 'approved'
            };
        });

        // 按审核时间倒序排列
        processedApplications.sort((a: any, b: any) => {
            const timeA = a.reviewTime || a.submitTime;
            const timeB = b.reviewTime || b.submitTime;
            return new Date(timeB).getTime() - new Date(timeA).getTime();
        });
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

// 按状态筛选
const filterByStatus = (e: any) => {
    const status = e.currentTarget.dataset.status;
    selectedStatus.value = status;
    filterApplications();
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
        // 真实审核人/时间别名：adminReviewerName/teacherReviewerName、adminReviewTime/teacherReviewTime
        const reviewer = detail.adminReviewerName || detail.teacherReviewerName || '';
        const reviewTime = detail.adminReviewTime || detail.teacherReviewTime || '';
        const reviewComment = detail.adminReviewComment || detail.rejectReason || detail.teacherReviewReason || '';
        selectedApplication.value = {
            ...detail,
            labName: detail.labName || (detail.lab && detail.lab.name) || '',
            timeText: detail.timeText || (detail.startTime && detail.endTime ? `${detail.startTime}-${detail.endTime}` : '未选择时间段'),
            submitTimeText: detail.submitTime ? formatDateTime(new Date(detail.submitTime)) : '',
            reviewer,
            reviewComment,
            reviewTimeText: reviewTime ? formatDateTime(new Date(reviewTime)) : '',
            canModify: detail.status === 'approved' && !detail.isCompleted && new Date(detail.date).getTime() > Date.now() + 86400 * 1000,
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

// 标记为完成
const markAsCompleted = (e: any) => {
    const application = e.currentTarget.dataset.application;
    completeApplicationId.value = application.id;
    showCompleteModal.value = true;
};

// 从详情页标记为完成
const markAsCompletedFromDetail = () => {
    if (selectedApplication.value) {
        completeApplicationId.value = selectedApplication.value.id;
        showCompleteModal.value = true;
        showDetailModal.value = false;
    }
};

// 关闭完成确认弹窗
const closeCompleteModal = () => {
    showCompleteModal.value = false;
    completeApplicationId.value = '';
};

// 确认完成
const confirmComplete = async () => {
    if (!completeApplicationId.value) {
        return;
    }
    uni.showLoading({
        title: '处理中...'
    });
    try {
        await completeReservation(completeApplicationId.value);
        uni.hideLoading();
        uni.showToast({
            title: '已标记完成',
            icon: 'success'
        });
        showCompleteModal.value = false;
        completeApplicationId.value = '';
        await loadApplications();
    } catch (error: any) {
        uni.hideLoading();
        console.error('标记完成失败:', error);
        uni.showToast({
            title: error?.data?.message || '操作失败，请重试',
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
@import './student-completed-process.less';
</style>
