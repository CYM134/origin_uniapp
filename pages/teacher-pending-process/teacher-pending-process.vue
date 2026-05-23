<template>
    <view>
        <!-- pages/teacher-pending-process/teacher-pending-process.wxml -->
        <navigation-bar title="待办流程" :back="true" color="white" background="#3a7bd5"></navigation-bar>

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

            <!-- 申请列表 -->
            <view class="application-list">
                <view v-if="applications.length === 0" class="empty-state">
                    <image src="/static/images/icons/empty-box.svg" mode="aspectFit"></image>
                    <text class="empty-text">暂无待办事项</text>
                    <text class="empty-desc">当有学生申请需要您审核时，会在这里显示</text>
                </view>

                <view class="application-item" v-for="(item, index) in applications" :key="index">
                    <view class="item-header">
                        <view :class="'status-badge ' + item.status">
                            <text>{{ item.statusText }}</text>
                        </view>
                        <view class="submit-time">{{ item.submitTimeText }}</view>
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
                        <view v-if="item.remark" class="content-row">
                            <text class="label">备注：</text>
                            <text class="value remark">{{ item.remark }}</text>
                        </view>
                    </view>

                    <view class="item-actions">
                        <button v-if="item.status === 'pending'" class="action-btn reject" @tap="rejectApplication" :data-id="item.id">驳回</button>
                        <button v-if="item.status === 'pending'" class="action-btn approve" @tap="approveApplication" :data-id="item.id">通过</button>
                        <button class="action-btn detail" @tap="viewDetail" :data-item="item">详情</button>
                    </view>
                </view>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>

        <!-- 审核弹窗 -->
        <view v-if="showReviewModal" class="modal-overlay" @tap="closeReviewModal">
            <view class="modal-content" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">{{ reviewType === 'approve' ? '审核通过' : '驳回申请' }}</text>
                    <view class="close-btn" @tap="closeReviewModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="review-info">
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
                    </view>
                    <view class="review-reason">
                        <text class="reason-label">{{ reviewType === 'approve' ? '审核意见' : '驳回原因' }}：</text>
                        <textarea
                            class="reason-input"
                            :placeholder="reviewType === 'approve' ? '请输入审核意见（选填）' : '请输入驳回原因'"
                            :value="reviewReason"
                            @input="onReasonInput"
                            maxlength="200"
                        ></textarea>
                        <view class="char-count">{{ reviewReason.length }}/200</view>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="cancel-btn" @tap="closeReviewModal">取消</button>
                    <button :class="'confirm-btn ' + reviewType" @tap="confirmReview">
                        {{ reviewType === 'approve' ? '确认通过' : '确认驳回' }}
                    </button>
                </view>
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

                    <view v-if="selectedApplication.reviewReason" class="detail-section">
                        <view class="section-title">审核信息</view>
                        <view class="detail-item">
                            <text class="detail-label">审核状态：</text>
                            <text :class="'detail-value status ' + selectedApplication.status">{{ selectedApplication.statusText }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">审核意见：</text>
                            <text class="detail-value">{{ selectedApplication.reviewReason }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">审核时间：</text>
                            <text class="detail-value">{{ selectedApplication.reviewTimeText }}</text>
                        </view>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="confirm-btn" @tap="closeDetailModal">确定</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
// pages/teacher-pending-process/teacher-pending-process.ts

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
    applicant?: string;
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
    purpose?: string;
    status: string;
    submitTime: string;
    reviewTime?: string;
    reviewReason?: string;
    teacherReviewReason?: string;
    teacherReviewTime?: string;
    teacherReviewerId?: string;
    teacherReviewerName?: string;
    type?: string;
    statusText?: string;
    submitTimeText?: string;
    reviewTimeText?: string;
    lab?: {
        id: string;
        name: string;
    };
}
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            teacherInfo: {} as TeacherInfo,
            filterTabs: [
                {
                    name: '全部',
                    key: 'all',
                    active: true,
                    count: 0
                },
                {
                    name: '待审核',
                    key: 'pending',
                    active: false,
                    count: 0
                },
                {
                    name: '已通过',
                    key: 'approved',
                    active: false,
                    count: 0
                },
                {
                    name: '已驳回',
                    key: 'rejected',
                    active: false,
                    count: 0
                }
            ],
            currentFilter: 'all',
            allApplications: [] as Application[],
            applications: [] as Application[],
            showReviewModal: false,
            showDetailModal: false,
            reviewType: '',
            // 'approve' | 'reject'
            reviewReason: '',
            currentApplication: null as Application | null,
            selectedApplication: null as Application | null
        };
    },
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad() {
        this.loadTeacherInfo();
        this.loadApplications();
    },
    /**
     * 生命周期函数--监听页面显示
     */
    onShow() {
        this.loadApplications();
    },
    /**
     * 页面相关事件处理函数--监听用户下拉动作
     */
    onPullDownRefresh() {
        this.loadApplications();

        // 停止下拉刷新
        setTimeout(() => {
            uni.stopPullDownRefresh();
        }, 1000);
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
         * 加载申请列表
         */
        loadApplications() {
            try {
                // 获取所有学生申请
                const allApplications = uni.getStorageSync('studentApplications') || [];

                // 只过滤出学生发送的预约申请，排除教师申请和已取消的申请
                const studentApplications = allApplications.filter((app: any) => {
                    // 排除教师申请（type为'teacher'的申请）和已取消的申请
                    // 显示所有非教师申请且非已取消状态的学生申请
                    return app.type !== 'teacher' && app.status !== 'cancelled';
                });

                // 格式化申请数据
                const formattedApplications = studentApplications.map((app: any) => {
                    return {
                        ...app,
                        // 映射学生申请的数据结构到教师端显示格式
                        studentName: app.applicant || app.studentName || '未知学生',
                        studentId: app.studentId || '未知学号',
                        phone: app.contact || app.phone || '未提供',
                        labName: (app.lab && app.lab.name) || app.labName || '未知实验室',
                        labId: (app.lab && app.lab.id) || app.labId || '',
                        courseName: app.title || app.courseName || '未知课程',
                        courseType: (app.type && app.type.name) || app.courseType || '未知类型',
                        studentCount: app.studentCount || 0,
                        date: app.date || '',
                        timeSlot: app.startTime && app.endTime ? `${app.startTime}-${app.endTime}` : '',
                        remark: app.purpose || app.remark || '',
                        statusText: this.getStatusText(app.status),
                        submitTimeText: this.formatTime(app.submitTime),
                        reviewTimeText: app.reviewTime ? this.formatTime(app.reviewTime) : ''
                    };
                });

                // 按提交时间倒序排列
                formattedApplications.sort((a: any, b: any) => {
                    return new Date(b.submitTime).getTime() - new Date(a.submitTime).getTime();
                });
                this.setData({
                    allApplications: formattedApplications
                });
                this.updateFilterCounts();
                this.filterApplications();
            } catch (error) {
                console.log('CatchClause', error);
                console.log('CatchClause', error);
                console.error('加载申请列表失败:', error);
                uni.showToast({
                    title: '加载失败',
                    icon: 'error'
                });
            }
        },

        /**
         * 获取状态文本
         */
        getStatusText(status: string) {
            const statusMap: any = {
                pending: '待教师审核',
                teacher_approved: '待管理员审核',
                approved: '已通过',
                rejected: '已驳回'
            };
            return statusMap[status] || '未知状态';
        },

        /**
         * 格式化时间
         */
        formatTime(timeString: string) {
            const date = new Date(timeString);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            return `${year}-${month}-${day} ${hours}:${minutes}`;
        },

        /**
         * 更新筛选标签计数
         */
        updateFilterCounts() {
            const { allApplications } = this;
            const filterTabs = this.filterTabs.map((tab) => {
                let count = 0;
                if (tab.key === 'all') {
                    count = allApplications.length;
                } else {
                    count = allApplications.filter((app: any) => app.status === tab.key).length;
                }
                return {
                    ...tab,
                    count
                };
            });
            this.setData({
                filterTabs: filterTabs
            });
        },

        /**
         * 切换筛选器
         */
        switchFilter(e: any) {
            const index = e.currentTarget.dataset.index;
            const filterTabs = this.filterTabs.map((tab, i) => ({
                ...tab,
                active: i === index
            }));
            const currentFilter = filterTabs[index].key;
            this.setData({
                filterTabs: filterTabs,
                currentFilter: currentFilter
            });
            this.filterApplications();
        },

        /**
         * 过滤申请列表
         */
        filterApplications() {
            const { allApplications, currentFilter } = this;
            let filteredApplications = allApplications;
            if (currentFilter !== 'all') {
                filteredApplications = allApplications.filter((app: any) => app.status === currentFilter);
            }
            this.setData({
                applications: filteredApplications
            });
        },

        /**
         * 通过申请
         */
        approveApplication(e: any) {
            const id = e.currentTarget.dataset.id;
            const application = this.allApplications.find((app: any) => app.id === id);
            if (application) {
                this.setData({
                    showReviewModal: true,
                    reviewType: 'approve',
                    currentApplication: application,
                    reviewReason: ''
                });
            }
        },

        /**
         * 驳回申请
         */
        rejectApplication(e: any) {
            const id = e.currentTarget.dataset.id;
            const application = this.allApplications.find((app: any) => app.id === id);
            if (application) {
                this.setData({
                    showReviewModal: true,
                    reviewType: 'reject',
                    currentApplication: application,
                    reviewReason: ''
                });
            }
        },

        /**
         * 查看详情
         */
        viewDetail(e: any) {
            const item = e.currentTarget.dataset.item;
            this.setData({
                showDetailModal: true,
                selectedApplication: item
            });
        },

        /**
         * 关闭审核弹窗
         */
        closeReviewModal() {
            this.setData({
                showReviewModal: false,
                reviewType: '',
                currentApplication: null,
                reviewReason: ''
            });
        },

        /**
         * 关闭详情弹窗
         */
        closeDetailModal() {
            this.setData({
                showDetailModal: false,
                selectedApplication: null
            });
        },

        /**
         * 阻止事件冒泡
         */
        stopPropagation() {
            // 阻止点击弹窗内容时关闭弹窗
        },

        /**
         * 审核原因输入
         */
        onReasonInput(e: any) {
            this.setData({
                reviewReason: e.detail.value
            });
        },

        /**
         * 确认审核
         */
        confirmReview() {
            const { reviewType, reviewReason, currentApplication } = this;

            // 驳回时必须填写原因
            if (reviewType === 'reject' && !reviewReason.trim()) {
                uni.showToast({
                    title: '请填写驳回原因',
                    icon: 'none'
                });
                return;
            }
            if (!currentApplication || !currentApplication.id) {
                uni.showToast({
                    title: '申请信息错误',
                    icon: 'none'
                });
                return;
            }
            try {
                // 更新申请状态
                const allApplications = uni.getStorageSync('studentApplications') || [];
                const applicationIndex = allApplications.findIndex((app: any) => app.id === currentApplication.id);
                if (applicationIndex !== -1) {
                    const teacherInfo = this.teacherInfo as any;
                    allApplications[applicationIndex] = {
                        ...allApplications[applicationIndex],
                        status: reviewType === 'approve' ? 'teacher_approved' : 'rejected',
                        teacherReviewReason: reviewReason,
                        teacherReviewTime: new Date().toISOString(),
                        teacherReviewerId: teacherInfo.teacherId || '',
                        teacherReviewerName: teacherInfo.name || '未知教师'
                    };
                    uni.setStorageSync('studentApplications', allApplications);

                    // 发送通知给学生（模拟）
                    this.sendNotificationToStudent(allApplications[applicationIndex]);
                    uni.showToast({
                        title: reviewType === 'approve' ? '已提交管理员审核' : '已驳回申请',
                        icon: 'success'
                    });
                    this.closeReviewModal();
                    this.loadApplications();
                }
            } catch (error) {
                console.log('CatchClause', error);
                console.log('CatchClause', error);
                console.error('审核失败:', error);
                uni.showToast({
                    title: '审核失败',
                    icon: 'error'
                });
            }
        },

        /**
         * 发送通知给学生
         */
        sendNotificationToStudent(application: any) {
            try {
                const notifications = uni.getStorageSync('notifications') || [];
                const notification = {
                    id: 'notif_' + Date.now(),
                    title: application.status === 'approved' ? '申请审核通过' : '申请被驳回',
                    content: `您的实验室预约申请（${application.labName} ${application.date} ${application.timeSlot}）${
                        application.status === 'approved' ? '已通过审核' : '被驳回'
                    }`,
                    type: application.status === 'approved' ? 'success' : 'warning',
                    read: false,
                    createTime: new Date().toISOString(),
                    studentId: application.studentId
                };
                notifications.unshift(notification);
                uni.setStorageSync('notifications', notifications);
            } catch (error) {
                console.log('CatchClause', error);
                console.log('CatchClause', error);
                console.error('发送通知失败:', error);
            }
        }
    }
});
</script>
<style>
@import './teacher-pending-process.css';
</style>
