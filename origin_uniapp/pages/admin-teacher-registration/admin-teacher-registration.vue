<template>
    <view class="page-wrapper">
        <!-- teacher-registration.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="教师注册审核" color="white" background="#F5A623" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <navigation-bar title="教师注册审核" :back="true" color="white" background="#F5A623"></navigation-bar>
        <!-- #endif -->

        <view class="container">
            <!-- 选项卡 -->
            <view class="tabs">
                <view :class="'tab ' + (activeTab === 'pending' ? 'active' : '')" @tap="switchTab" data-tab="pending">
                    待审核
                    <view class="badge" v-if="pendingCount > 0">{{ pendingCount }}</view>
                </view>
                <view :class="'tab ' + (activeTab === 'approved' ? 'active' : '')" @tap="switchTab" data-tab="approved">已通过</view>
                <view :class="'tab ' + (activeTab === 'rejected' ? 'active' : '')" @tap="switchTab" data-tab="rejected">已拒绝</view>
            </view>

            <!-- 搜索区域 -->
            <view class="search-section">
                <view class="search-box">
                    <input type="text" placeholder="搜索姓名或工号" @input="onSearchInput" :value="searchKeyword" confirm-type="search" />
                    <view class="search-icon">
                        <image src="/static/images/icons/search-icon.png"></image>
                    </view>
                </view>
            </view>

            <!-- 教师列表 -->
            <view class="teacher-list">
                <block v-if="filteredTeachers.length > 0">
                    <view :class="'teacher-item status-' + item.status" @tap="showTeacherDetail" :data-id="item.id" v-for="(item, index) in filteredTeachers" :key="index">
                        <view class="teacher-avatar">
                            <image :src="item.avatar || '/static/images/icons/teacher-icon.png'"></image>
                        </view>

                        <view class="teacher-info">
                            <view class="teacher-name">{{ item.name }}</view>
                            <view class="teacher-id">工号：{{ item.id }}</view>
                            <view class="teacher-department">院系：{{ item.department }}</view>
                            <view class="teacher-time">申请时间：{{ item.registerTime }}</view>
                        </view>

                        <view class="teacher-status" v-if="activeTab !== 'pending'">
                            <view :class="'status-tag ' + (activeTab === 'approved' ? 'approved' : 'rejected')">
                                {{ activeTab === 'approved' ? '已通过' : '已拒绝' }}
                            </view>
                            <view class="status-time">{{ item.approvalTime }}</view>
                        </view>

                        <view class="teacher-actions" v-if="activeTab === 'pending'" @tap.stop.prevent="stopPropagation">
                            <button class="action-btn reject-btn" @tap.stop.prevent="showRejectModalFun" :data-id="item.id">拒绝</button>
                            <button class="action-btn approve-btn" @tap.stop.prevent="approveTeacher" :data-id="item.id">通过</button>
                        </view>
                    </view>
                </block>

                <view class="empty-list" v-else>
                    <image src="/static/images/icons/empty.png"></image>
                    <text>暂无{{ activeTab === 'pending' ? '待审核' : activeTab === 'approved' ? '已通过' : '已拒绝' }}的教师注册申请</text>
                </view>
            </view>

            <!-- 教师详情弹窗 -->
            <view class="modal" v-if="showDetailModal">
                <view class="modal-mask" @tap="hideDetailModal"></view>
                <view class="modal-content">
                    <view class="modal-header">
                        <text>教师详情</text>
                        <view class="close-btn" @tap="hideDetailModal">×</view>
                    </view>

                    <view class="modal-body">
                        <view class="detail-avatar">
                            <image :src="currentTeacher.avatar || '/static/images/icons/empty.png'"></image>
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">姓名</view>
                                <view class="detail-value">{{ currentTeacher.name }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">工号</view>
                                <view class="detail-value">{{ currentTeacher.id }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">职位</view>
                                <view class="detail-value">{{ currentTeacher.position }}</view>
                            </view>
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">院系</view>
                                <view class="detail-value">{{ currentTeacher.department }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">职称</view>
                                <view class="detail-value">{{ currentTeacher.position }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">联系电话</view>
                                <view class="detail-value">{{ currentTeacher.phone }}</view>
                            </view>

                            <view class="detail-item">
                                <view class="detail-label">邮箱</view>
                                <view class="detail-value">{{ currentTeacher.email }}</view>
                            </view>
                        </view>

                        <view class="detail-section">
                            <view class="detail-item">
                                <view class="detail-label">申请时间</view>
                                <view class="detail-value">{{ currentTeacher.registerTime }}</view>
                            </view>

                            <view class="detail-item" v-if="currentTeacher.status !== 'pending'">
                                <view class="detail-label">审核时间</view>
                                <view class="detail-value">{{ currentTeacher.approvalTime }}</view>
                            </view>

                            <view class="detail-item" v-if="currentTeacher.status === 'rejected'">
                                <view class="detail-label">拒绝原因</view>
                                <view class="detail-value reason-box">{{ currentTeacher.rejectReason }}</view>
                            </view>
                        </view>

                        <view class="detail-section" v-if="currentTeacher.teacherCardImage">
                            <view class="detail-label">教工卡照片</view>
                            <view class="id-card-image" @tap="previewImage" :data-url="currentTeacher.teacherCardImage">
                                <image :src="currentTeacher.teacherCardImage"></image>
                            </view>
                        </view>

                        <view class="detail-section" v-if="currentTeacher.idCardFront">
                            <view class="detail-label">身份证正面</view>
                            <view class="id-card-image" @tap="previewImage" :data-url="currentTeacher.idCardFront">
                                <image :src="currentTeacher.idCardFront"></image>
                            </view>
                        </view>

                        <view class="detail-section" v-if="currentTeacher.idCardBack">
                            <view class="detail-label">身份证背面</view>
                            <view class="id-card-image" @tap="previewImage" :data-url="currentTeacher.idCardBack">
                                <image :src="currentTeacher.idCardBack"></image>
                            </view>
                        </view>
                    </view>

                    <view class="modal-footer" v-if="currentTeacher.status === 'pending'">
                        <button class="footer-btn reject-btn" @tap="showRejectModalFun" :data-id="currentTeacher.id">拒绝</button>
                        <button class="footer-btn approve-btn" @tap="approveTeacher" :data-id="currentTeacher.id">通过</button>
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
                        <button class="footer-btn confirm-btn" @tap="rejectTeacher">确定</button>
                    </view>
                </view>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
// teacher-registration.ts

const activeTab = ref('pending');
// 'pending': 待审核, 'approved': 已通过, 'rejected': 已拒绝
const searchValue = ref('');
const pendingTeachers = ref<any[]>([]);
const approvedTeachers = ref<any[]>([]);
const rejectedTeachers = ref<any[]>([]);
const filteredTeachers = ref<any[]>([]);
const pendingCount = ref(0);
const searchKeyword = ref('');
const showDetailModal = ref(false);
const showRejectModal = ref(false);
const currentTeacher = ref<any>(null);
const rejectReason = ref('');
const reasonTemplates = ref<any[]>([
    '证件照片不清晰，请重新上传',
    '工号信息有误，请核对后重新提交',
    '所属院系信息有误，请核对后重新提交',
    '提交的信息与学校记录不符',
    '请提供有效的教师证明材料'
]);
// 示例数据
const mockTeachers = ref<any[]>([
    {
        id: 'T20230001',
        name: '张教授',
        // avatar: '/static/images/avatar/teacher1.png',  //这里放老师的照片，因为没有所以注释
        department: '计算机科学与技术学院',
        position: '教授',
        phone: '13800138001',
        email: 'zhang@university.edu',
        idCardFront: '/static/images/icons/id-card-sample.png',
        idCardBack: '/static/images/icons/id-card-sample.png',
        teacherCardImage: '/static/images/teacher-card-sample.png',
        registerTime: '2023-09-01 10:23:45',
        status: 'pending',
        approvalTime: '',
        rejectReason: ''
    },
    {
        id: 'T20230002',
        name: '李副教授',
        //avatar: '/static/images/avatar/teacher2.png',
        department: '物理学院',
        position: '副教授',
        phone: '13900139002',
        email: 'li@university.edu',
        idCardFront: '/static/images/id-card-sample.png',
        idCardBack: '/static/images/id-card-sample.png',
        teacherCardImage: '/static/images/teacher-card-sample.png',
        registerTime: '2023-09-02 14:35:22',
        status: 'approved',
        approvalTime: '2023-09-03 09:15:30',
        rejectReason: ''
    },
    {
        id: 'T20230003',
        name: '王讲师',
        //avatar: '/static/images/avatar/teacher3.png',
        department: '化学学院',
        position: '讲师',
        phone: '13700137003',
        email: 'wang@university.edu',
        idCardFront: '/static/images/id-card-sample.png',
        idCardBack: '/static/images/id-card-sample.png',
        teacherCardImage: '/static/images/teacher-card-sample.png',
        registerTime: '2023-09-03 16:42:18',
        status: 'rejected',
        approvalTime: '2023-09-04 11:20:45',
        rejectReason: '证件照片不清晰，请重新上传'
    },
    {
        id: 'T20230004',
        name: '刘教授',
        // avatar: '/static/images/avatar/teacher4.png',
        department: '数学学院',
        position: '教授',
        phone: '13600136004',
        email: 'liu@university.edu',
        idCardFront: '/static/images/id-card-sample.png',
        idCardBack: '/static/images/id-card-sample.png',
        teacherCardImage: '/static/images/teacher-card-sample.png',
        registerTime: '2023-09-04 09:18:33',
        status: 'pending',
        approvalTime: '',
        rejectReason: ''
    },
    {
        id: 'T20230005',
        name: '陈副教授',
        //avatar: '/static/images/avatar/teacher5.png',
        department: '外国语学院',
        position: '副教授',
        phone: '13500135005',
        email: 'chen@university.edu',
        idCardFront: '/static/images/id-card-sample.png',
        idCardBack: '/static/images/id-card-sample.png',
        teacherCardImage: '/static/images/teacher-card-sample.png',
        registerTime: '2023-09-05 15:27:56',
        status: 'approved',
        approvalTime: '2023-09-06 10:05:12',
        rejectReason: ''
    }
]);

onLoad(() => {
    filterTeachers();
    // 计算待审核数量
    pendingCount.value = pendingTeachers.value.length;
});

// 过滤教师列表
const filterTeachers = () => {
    const _mockTeachers = mockTeachers.value;
    const _searchKeyword = searchKeyword.value;
    const _activeTab = activeTab.value;

    // 根据搜索值过滤
    const allFiltered = _searchKeyword
        ? _mockTeachers.filter((teacher) => teacher.name.includes(_searchKeyword) || teacher.id.includes(_searchKeyword) || teacher.department.includes(_searchKeyword))
        : _mockTeachers;

    // 按状态分类
    const _pendingTeachers = allFiltered.filter((t) => t.status === 'pending');
    const _approvedTeachers = allFiltered.filter((t) => t.status === 'approved');
    const _rejectedTeachers = allFiltered.filter((t) => t.status === 'rejected');

    // 根据当前选中的tab筛选要显示的教师列表
    let _filteredTeachers: any[] = [];
    if (_activeTab === 'pending') {
        _filteredTeachers = _pendingTeachers;
    } else if (_activeTab === 'approved') {
        _filteredTeachers = _approvedTeachers;
    } else if (_activeTab === 'rejected') {
        _filteredTeachers = _rejectedTeachers;
    }
    pendingTeachers.value = _pendingTeachers;
    approvedTeachers.value = _approvedTeachers;
    rejectedTeachers.value = _rejectedTeachers;
    filteredTeachers.value = _filteredTeachers;
    pendingCount.value = _pendingTeachers.length;
};

// 切换选项卡
const switchTab = (e: any) => {
    activeTab.value = e.currentTarget.dataset.tab;
    filterTeachers();
};

// 搜索
const onSearchInput = (e: any) => {
    searchKeyword.value = e.detail.value;
    filterTeachers();
};

// 清空搜索
const clearSearch = () => {
    searchKeyword.value = '';
    filterTeachers();
};

// 显示教师详情
const showTeacherDetail = (e: any) => {
    const teacherId = e.currentTarget.dataset.id;
    console.log('点击教师卡片，ID:', teacherId);

    // 先从当前显示的教师列表中查找
    let teacher = null;
    const _activeTab = activeTab.value;
    if (_activeTab === 'pending') {
        teacher = pendingTeachers.value.find((t) => t.id === teacherId);
    } else if (_activeTab === 'approved') {
        teacher = approvedTeachers.value.find((t) => t.id === teacherId);
    } else if (_activeTab === 'rejected') {
        teacher = rejectedTeachers.value.find((t) => t.id === teacherId);
    }

    // 如果在当前列表中没找到，再从所有教师中查找
    if (!teacher) {
        teacher = mockTeachers.value.find((t) => t.id === teacherId);
    }
    if (teacher) {
        console.log('找到教师信息:', teacher);
        currentTeacher.value = teacher;
        showDetailModal.value = true;
    } else {
        console.log('未找到教师信息');
        uni.showToast({
            title: '未找到教师信息',
            icon: 'none'
        });
    }
};

// 阻止事件冒泡
const stopPropagation = () => {
    // 仅用于阻止事件冒泡，无需实际逻辑
};

// 关闭详情弹窗
const hideDetailModal = () => {
    showDetailModal.value = false;
};

// 显示拒绝弹窗
const showRejectModalFun = (e: any) => {
    const teacherId = e.currentTarget.dataset.id;
    const teacher = mockTeachers.value.find((t) => t.id === teacherId);
    if (teacher) {
        currentTeacher.value = teacher;
        showRejectModal.value = true;
        rejectReason.value = '';
    }
};

// 关闭拒绝弹窗
const hideRejectModal = () => {
    showRejectModal.value = false;
};

// 输入拒绝原因
const onReasonInput = (e: any) => {
    rejectReason.value = e.detail.value;
};

// 选择拒绝原因模板
const selectReasonTemplate = (e: any) => {
    const reason = e.currentTarget.dataset.reason;
    rejectReason.value = reason;
};

// 预览证件图片
const previewImage = (e: any) => {
    const url = e.currentTarget.dataset.url;
    uni.previewImage({
        urls: [url],
        current: url
    });
};

// 通过审核
const approveTeacher = (e: any) => {
    const teacherId = e.currentTarget.dataset.id || (currentTeacher.value ? currentTeacher.value.id : '');
    if (!teacherId) {
        return;
    }

    // 更新教师状态
    mockTeachers.value = mockTeachers.value.map((teacher) => {
        if (teacher.id === teacherId) {
            return {
                ...teacher,
                status: 'approved',
                approvalTime: formatDate(new Date())
            };
        }
        return teacher;
    });
    showDetailModal.value = false;
    filterTeachers();
    uni.showToast({
        title: '已通过审核',
        icon: 'success'
    });
};

// 拒绝审核
const rejectTeacher = () => {
    const _currentTeacher = currentTeacher.value;
    const _rejectReason = rejectReason.value;
    if (!_currentTeacher || !_rejectReason.trim()) {
        uni.showToast({
            title: '请填写拒绝原因',
            icon: 'none'
        });
        return;
    }

    // 更新教师状态
    mockTeachers.value = mockTeachers.value.map((teacher) => {
        if (teacher.id === _currentTeacher.id) {
            return {
                ...teacher,
                status: 'rejected',
                approvalTime: formatDate(new Date()),
                rejectReason: _rejectReason.trim()
            };
        }
        return teacher;
    });
    showRejectModal.value = false;
    filterTeachers();
    uni.showToast({
        title: '已拒绝申请',
        icon: 'success'
    });
};

// 格式化日期
const formatDate = (date: Date) => {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};
</script>
<style lang="less">
@import './admin-teacher-registration.less';
</style>
