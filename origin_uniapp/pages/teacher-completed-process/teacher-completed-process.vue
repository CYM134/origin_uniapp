<template>
    <view class="page-wrapper">
        <!-- pages/teacher-completed-process/teacher-completed-process.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="已办流程" color="white" background="#10B981" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <app-navigation-bar title="已办流程" :back="true" color="white" background="#10B981"></app-navigation-bar>
        <!-- #endif -->

        <view class="container">
            <!-- 筛选器 -->
            <view class="filter-section">
                <view class="filter-tabs">
                    <view :class="'filter-tab ' + (item.active ? 'active' : '')" @tap="switchFilter" :data-index="index" v-for="(item, index) in filterTabs" :key="index">
                        <text>{{ item.name }}</text>

                        <view v-if="item.count > 0" class="tab-badge">{{ item.count }}</view>
                    </view>
                </view>
            </view>

            <!-- 统计信息 -->
            <view class="statistics">
                <view class="stat-item">
                    <view class="stat-number">{{ statistics.total }}</view>
                    <view class="stat-label">总申请</view>
                </view>
                <view class="stat-item">
                    <view class="stat-number approved">{{ statistics.approved }}</view>
                    <view class="stat-label">已通过</view>
                </view>
                <view class="stat-item">
                    <view class="stat-number rejected">{{ statistics.rejected }}</view>
                    <view class="stat-label">已驳回</view>
                </view>
            </view>

            <!-- 申请列表 -->
            <view class="application-list">
                <view v-if="applications.length === 0" class="empty-state">
                    <image src="/static/images/icons/empty-box.svg" mode="aspectFit"></image>
                    <text class="empty-text">暂无已办事项</text>
                    <text class="empty-desc">您审核过的申请会在这里显示</text>
                </view>

                <view class="application-item" v-for="(item, index) in applications" :key="index">
                    <view class="item-header">
                        <view :class="'status-badge ' + item.status">
                            <text>{{ item.statusText }}</text>
                        </view>
                        <view class="review-time">{{ item.reviewTimeText }}</view>
                    </view>

                    <view class="item-content">
                        <view class="content-row">
                            <text class="label">申请人：</text>
                            <text class="value">{{ item.studentName }} ({{ item.studentId }})</text>
                        </view>
                        <view class="content-row">
                            <text class="label">实验室：</text>
                            <text class="value">{{ item.labName }}</text>
                        </view>
                        <view class="content-row">
                            <text class="label">时间：</text>
                            <text class="value">{{ item.date }} {{ item.timeSlot }}</text>
                        </view>
                        <view class="content-row">
                            <text class="label">课程：</text>
                            <text class="value">{{ item.courseName }}</text>
                        </view>
                        <view class="content-row">
                            <text class="label">人数：</text>
                            <text class="value">{{ item.studentCount }}人</text>
                        </view>
                        <view v-if="item.reviewReason" class="content-row">
                            <text class="label">审核意见：</text>
                            <text class="value review-reason">{{ item.reviewReason }}</text>
                        </view>
                    </view>

                    <view class="item-actions">
                        <button class="action-btn detail" @tap="viewDetail" :data-item="item">详情</button>
                        <button v-if="item.status === 'rejected'" class="action-btn reprocess" @tap="reprocessApplication" :data-id="item.id">重新审核</button>
                    </view>
                </view>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>

        <!-- 详情弹窗 -->
        <view v-if="showDetailModal" class="modal-overlay" @tap="closeDetailModal">
            <view class="modal-content" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">申请详情</text>
                    <view class="close-btn" @tap="closeDetailModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="detail-section">
                        <view class="section-title">申请信息</view>
                        <view class="detail-item">
                            <text class="detail-label">申请编号：</text>
                            <text class="detail-value">{{ selectedApplication.id }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">申请人：</text>
                            <text class="detail-value">{{ selectedApplication.studentName }} ({{ selectedApplication.studentId }})</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">联系方式：</text>
                            <text class="detail-value">{{ selectedApplication.phone || '未提供' }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">实验室：</text>
                            <text class="detail-value">{{ selectedApplication.labName }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">预约时间：</text>
                            <text class="detail-value">{{ selectedApplication.date }} {{ selectedApplication.timeSlot }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">课程名称：</text>
                            <text class="detail-value">{{ selectedApplication.courseName }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">课程类型：</text>
                            <text class="detail-value">{{ selectedApplication.courseType }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">学生人数：</text>
                            <text class="detail-value">{{ selectedApplication.studentCount }}人</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">申请时间：</text>
                            <text class="detail-value">{{ selectedApplication.submitTimeText }}</text>
                        </view>
                        <view v-if="selectedApplication.remark" class="detail-item">
                            <text class="detail-label">备注说明：</text>
                            <text class="detail-value">{{ selectedApplication.remark }}</text>
                        </view>
                    </view>

                    <view class="detail-section">
                        <view class="section-title">审核信息</view>
                        <view class="detail-item">
                            <text class="detail-label">审核状态：</text>
                            <text :class="'detail-value status ' + selectedApplication.status">{{ selectedApplication.statusText }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">审核人：</text>
                            <text class="detail-value">{{ selectedApplication.reviewerName }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">审核时间：</text>
                            <text class="detail-value">{{ selectedApplication.reviewTimeText }}</text>
                        </view>
                        <view v-if="selectedApplication.reviewReason" class="detail-item">
                            <text class="detail-label">审核意见：</text>
                            <text class="detail-value">{{ selectedApplication.reviewReason }}</text>
                        </view>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="confirm-btn" @tap="closeDetailModal">确定</button>
                </view>
            </view>
        </view>

        <!-- 重新审核弹窗 -->
        <view v-if="showReprocessModal" class="modal-overlay" @tap="closeReprocessModal">
            <view class="modal-content" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">重新审核</text>
                    <view class="close-btn" @tap="closeReprocessModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="reprocess-info">
                        <view class="info-item">
                            <text class="info-label">申请人：</text>
                            <text class="info-value">{{ currentApplication.studentName }}</text>
                        </view>
                        <view class="info-item">
                            <text class="info-label">实验室：</text>
                            <text class="info-value">{{ currentApplication.labName }}</text>
                        </view>
                        <view class="info-item">
                            <text class="info-label">时间：</text>
                            <text class="info-value">{{ currentApplication.date }} {{ currentApplication.timeSlot }}</text>
                        </view>
                        <view class="info-item">
                            <text class="info-label">原审核意见：</text>
                            <text class="info-value">{{ currentApplication.reviewReason }}</text>
                        </view>
                    </view>
                    <view class="reprocess-reason">
                        <text class="reason-label">新的审核意见：</text>
                        <textarea class="reason-input" placeholder="请输入新的审核意见" :value="newReviewReason" @input="onNewReasonInput" maxlength="200"></textarea>
                        <view class="char-count">{{ newReviewReason.length }}/200</view>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="cancel-btn" @tap="closeReprocessModal">取消</button>
                    <button class="confirm-btn approve" @tap="confirmReprocess">确认通过</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
import { getMyReservations, getReviewedApplications, reapproveReservation } from '@/api/teacher';
// pages/teacher-completed-process/teacher-completed-process.ts

interface TeacherInfo {
    teacherId: string;
    name: string;
    phone?: string;
    email?: string;
    department?: string;
}
interface Application {
    id: string;
    teacherId?: string;
    teacherName?: string;
    studentId?: string;
    studentName?: string;
    phone?: string;
    contact?: string;
    labId: string;
    labName: string;
    date: string;
    startTime: string;
    endTime: string;
    timeSlot: string;
    courseName?: string;
    title?: string;
    courseType?: string;
    studentCount: number;
    remark?: string;
    status: string;
    submitTime: string;
    reviewTime?: string;
    reviewReason?: string;
    reviewerId?: string;
    reviewerName?: string;
    type?: string;
    isMyApplication?: boolean;
    isStudentApplication?: boolean;
    statusText?: string;
    submitTimeText?: string;
    reviewTimeText?: string;
    applicantType?: string;
}

const teacherInfo = ref<TeacherInfo>({} as TeacherInfo);
const filterTabs = ref([
    {
        name: '我的',
        key: 'mine',
        active: true,
        count: 0
    },
    {
        name: '学生：已通过',
        key: 'student_approved',
        active: false,
        count: 0
    },
    {
        name: '学生：已驳回',
        key: 'student_rejected',
        active: false,
        count: 0
    }
]);
const currentFilter = ref('mine');
const allApplications = ref<Application[]>([]);
const applications = ref<Application[]>([]);
const statistics = ref({
    total: 0,
    approved: 0,
    rejected: 0,
    approvalRate: 0
});
const showDetailModal = ref(false);
const showReprocessModal = ref(false);
const selectedApplication = ref<Application | null>(null);
const currentApplication = ref<Application | null>(null);
const newReviewReason = ref('');

/**
 * 获取状态文本
 */
const getStatusText = (status: string) => {
    const statusMap: any = {
        approved: '已通过',
        rejected: '已驳回',
        teacher_approved: '待管理员审核'
    };
    return statusMap[status] || '未知状态';
};

/**
 * 格式化时间
 */
const formatTime = (timeString: string) => {
    const date = new Date(timeString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
};

/**
 * 过滤申请列表
 */
const filterApplications = () => {
    let filteredApplications = allApplications.value;
    if (currentFilter.value === 'mine') {
        // 我的：显示当前教师的所有申请（包括所有状态），仅按 id 去重（在 loadApplications 中已完成），
        // 不再按 日期+时间段+实验室 二次去重，避免吞掉本人合法的多次申请
        filteredApplications = allApplications.value.filter((app: any) => app.isMyApplication);
    } else if (currentFilter.value === 'student_approved') {
        // 学生：已通过 - 显示已通过和待管理员审核的记录
        filteredApplications = allApplications.value.filter((app: any) => app.isStudentApplication && (app.status === 'approved' || app.status === 'teacher_approved'));
    } else if (currentFilter.value === 'student_rejected') {
        // 学生：已驳回 - 显示所有被驳回的记录（包括教师驳回和管理员驳回）
        filteredApplications = allApplications.value.filter((app: any) => app.isStudentApplication && app.status === 'rejected');
    }
    applications.value = filteredApplications;
};

/**
 * 更新统计信息
 */
const updateStatistics = () => {
    // 只统计学生申请，不包括教师自己的申请
    const studentApplications = allApplications.value.filter((app: any) => !app.isMyApplication);
    const total = studentApplications.length;
    const approved = studentApplications.filter((app: any) => app.status === 'approved').length;
    const rejected = studentApplications.filter((app: any) => app.status === 'rejected').length;
    const approvalRate = total > 0 ? Math.round((approved / total) * 100) : 0;
    statistics.value = {
        total,
        approved,
        rejected,
        approvalRate
    };
};

/**
 * 更新筛选标签计数
 */
const updateFilterCounts = () => {
    filterTabs.value = filterTabs.value.map((tab) => {
        let count = 0;
        if (tab.key === 'mine') {
            // 我的：显示当前教师的所有申请
            count = allApplications.value.filter((app: any) => app.isMyApplication).length;
        } else if (tab.key === 'student_approved') {
            // 学生：已通过 - 包括已通过和待管理员审核的记录
            count = allApplications.value.filter((app: any) => app.isStudentApplication && (app.status === 'approved' || app.status === 'teacher_approved')).length;
        } else if (tab.key === 'student_rejected') {
            // 学生：已驳回 - 显示所有被驳回的记录
            count = allApplications.value.filter((app: any) => app.isStudentApplication && app.status === 'rejected').length;
        }
        return {
            ...tab,
            count
        };
    });
};

/**
 * 将后端预约对象映射成模板需要的形状
 */
const mapApplication = (app: any, isMyApplication: boolean) => {
    const status = app.status || '';
    const reviewTime = app.teacherReviewTime || app.adminReviewTime || app.approvalTime || app.reviewTime || '';
    const submitTime = app.submitTime || app.applyTime || app.createTime || '';
    const reviewReason = app.teacherReviewReason || app.adminReviewComment || app.rejectReason || app.reviewReason || '';
    const reviewerName = app.teacherReviewerName || app.adminReviewerName || app.reviewerName || '';
    const formattedApp: any = {
        ...app,
        isMyApplication,
        isStudentApplication: !isMyApplication,
        status,
        labName: app.labName || '未知实验室',
        labId: app.labId || '',
        courseName: app.courseName || app.title || '未知课程',
        courseType: app.courseType || '未知类型',
        studentCount: app.studentCount || 0,
        date: app.date || '',
        timeSlot: app.timeSlot || (app.startTime && app.endTime ? `${app.startTime}-${app.endTime}` : ''),
        remark: app.remark || app.purpose || '',
        statusText: app.statusText || getStatusText(status),
        submitTimeText: submitTime ? formatTime(submitTime) : '',
        reviewTimeText: reviewTime ? formatTime(reviewTime) : '',
        reviewTime,
        submitTime,
        reviewReason,
        reviewerName,
        applicantType: isMyApplication ? '教师申请' : '学生申请'
    };
    if (isMyApplication) {
        // 教师自己的申请：申请人显示为教师本人
        formattedApp.studentName = app.applicantName || app.applicant || teacherInfo.value.name || '';
        formattedApp.studentId = app.applicantUserId || teacherInfo.value.teacherId || '';
        formattedApp.phone = app.applicantPhone || app.contact || app.phone || teacherInfo.value.phone || '';
    } else {
        // 学生申请
        formattedApp.studentName = app.studentName || app.applicantName || app.applicant || '未知学生';
        formattedApp.studentId = app.studentId || app.applicantUserId || '未知学号';
        formattedApp.phone = app.applicantPhone || app.phone || app.contact || '未提供';
    }
    return formattedApp;
};

/**
 * 加载申请列表
 */
const loadApplications = async () => {
    try {
        // 并行拉取「我的申请」与「我审核过的学生申请」
        const [myRes, reviewedRes] = await Promise.all([getMyReservations(), getReviewedApplications()]);
        const myList = Array.isArray(myRes) ? myRes : [];
        const reviewedList = Array.isArray(reviewedRes) ? reviewedRes : [];

        // 合并为 allApplications，并按 id 去重
        const applicationMap = new Map();
        myList.forEach((app: any) => {
            applicationMap.set(app.id, mapApplication(app, true));
        });
        reviewedList.forEach((app: any) => {
            if (!applicationMap.has(app.id)) {
                applicationMap.set(app.id, mapApplication(app, false));
            }
        });
        const formattedApplications = Array.from(applicationMap.values());

        // 按审核时间倒序排列
        formattedApplications.sort((a: any, b: any) => {
            return new Date(b.reviewTime || b.submitTime).getTime() - new Date(a.reviewTime || a.submitTime).getTime();
        });
        allApplications.value = formattedApplications;
        updateStatistics();
        updateFilterCounts();
        filterApplications();
    } catch (error: any) {
        console.error('加载申请列表失败:', error);
        allApplications.value = [];
        updateStatistics();
        updateFilterCounts();
        filterApplications();
        uni.showToast({
            title: error?.data?.message || '加载失败',
            icon: 'none'
        });
    }
};

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
 * 生命周期函数--监听页面加载
 */
onLoad(() => {
    loadTeacherInfo();
    loadApplications();
});

/**
 * 生命周期函数--监听页面显示
 */
onShow(() => {
    loadApplications();
});

/**
 * 页面相关事件处理函数--监听用户下拉动作
 */
onPullDownRefresh(async () => {
    await loadApplications();
    uni.stopPullDownRefresh();
});

/**
 * 切换筛选器
 */
const switchFilter = (e: any) => {
    const index = e.currentTarget.dataset.index;
    filterTabs.value = filterTabs.value.map((tab, i) => ({
        ...tab,
        active: i === index
    }));
    currentFilter.value = filterTabs.value[index].key;
    filterApplications();
};

/**
 * 查看详情
 */
const viewDetail = (e: any) => {
    const item = e.currentTarget.dataset.item;
    showDetailModal.value = true;
    selectedApplication.value = item;
};

/**
 * 重新审核申请
 */
const reprocessApplication = (e: any) => {
    const id = e.currentTarget.dataset.id;
    const application = allApplications.value.find((app: any) => app.id === id);
    if (application) {
        showReprocessModal.value = true;
        currentApplication.value = application;
        newReviewReason.value = '';
    }
};

/**
 * 关闭详情弹窗
 */
const closeDetailModal = () => {
    showDetailModal.value = false;
    selectedApplication.value = null;
};

/**
 * 关闭重新审核弹窗
 */
const closeReprocessModal = () => {
    showReprocessModal.value = false;
    currentApplication.value = null;
    newReviewReason.value = '';
};

/**
 * 阻止事件冒泡
 */
const stopPropagation = () => {
    // 阻止点击弹窗内容时关闭弹窗
};

/**
 * 新审核意见输入
 */
const onNewReasonInput = (e: any) => {
    newReviewReason.value = e.detail.value;
};

/**
 * 确认重新审核
 */
const confirmReprocess = async () => {
    if (!newReviewReason.value.trim()) {
        uni.showToast({
            title: '请填写审核意见',
            icon: 'none'
        });
        return;
    }
    if (!currentApplication.value || !currentApplication.value.id) {
        uni.showToast({
            title: '申请信息错误',
            icon: 'none'
        });
        return;
    }
    try {
        // 调用后端重新审核接口，后端会自动通知学生
        await reapproveReservation(currentApplication.value.id, newReviewReason.value.trim());
        uni.showToast({
            title: '重新审核成功',
            icon: 'success'
        });
        closeReprocessModal();
        await loadApplications();
    } catch (error: any) {
        console.error('重新审核失败:', error);
        uni.showToast({
            title: error?.data?.message || '审核失败',
            icon: 'none'
        });
    }
};
</script>
<style lang="less">
@import './teacher-completed-process.less';
</style>
