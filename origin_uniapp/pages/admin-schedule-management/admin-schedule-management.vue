<template>
    <view class="page-wrapper">
        <!-- admin-schedule-management.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="课表管理" color="white" background="#F5A623" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <app-navigation-bar title="课表管理" :back="true" color="white" background="#F5A623"></app-navigation-bar>
        <!-- #endif -->

        <view class="container">
            <!-- 一级维护对象 -->
            <view class="tabs primary-tabs">
                <view :class="'tab ' + (activeMode === 'account' ? 'active' : '')" @tap="switchMode" data-mode="account">按师生账号编辑</view>
                <view :class="'tab ' + (activeMode === 'course' ? 'active' : '')" @tap="switchMode" data-mode="course">按课程编辑</view>
            </view>

            <!-- 二级功能选项 -->
            <view class="tabs sub-tabs" v-if="activeMode === 'account'">
                <view :class="'tab ' + (accountSubTab === 'student' ? 'active' : '')" @tap="switchAccountSubTab" data-tab="student">按学生查看</view>
                <view :class="'tab ' + (accountSubTab === 'teacher' ? 'active' : '')" @tap="switchAccountSubTab" data-tab="teacher">按教师查看</view>
                <view :class="'tab ' + (accountSubTab === 'import' ? 'active' : '')" @tap="switchAccountSubTab" data-tab="import">导入课表</view>
                <view :class="'tab ' + (accountSubTab === 'export' ? 'active' : '')" @tap="switchAccountSubTab" data-tab="export">导出课表</view>
            </view>
            <view class="tabs sub-tabs" v-else>
                <view :class="'tab ' + (courseSubTab === 'manage' ? 'active' : '')" @tap="switchCourseSubTab" data-tab="manage">课程维护</view>
                <view :class="'tab ' + (courseSubTab === 'import' ? 'active' : '')" @tap="switchCourseSubTab" data-tab="import">导入课表</view>
                <view :class="'tab ' + (courseSubTab === 'export' ? 'active' : '')" @tap="switchCourseSubTab" data-tab="export">导出课表</view>
            </view>

            <!-- 按账号维护内容 -->
            <view class="tab-content" v-if="activeMode === 'account' && isAccountScheduleTab">
                <view class="section">
                    <view class="section-title">{{ accountSectionTitle }}</view>
                    <view class="manage-toolbar">
                        <input
                            class="search-input"
                            v-model="accountKeyword"
                            :placeholder="accountSearchPlaceholder"
                            confirm-type="search"
                            @confirm="loadAccountUsers"
                        />
                        <button class="mini-btn" @tap="loadAccountUsers">搜索</button>
                    </view>
                </view>

                <view class="account-layout">
                    <view class="account-list">
                        <view v-if="accountUsers.length === 0" class="empty-card compact">
                            暂无账号数据，可输入账号或姓名搜索。
                        </view>
                        <view
                            v-for="user in accountUsers"
                            :key="user.id"
                            :class="'account-card ' + (selectedAccount && selectedAccount.id === user.id ? 'active' : '')"
                            @tap="selectAccount(user)"
                        >
                            <view class="account-name">{{ user.name || user.realName || '-' }}</view>
                            <view class="account-no">{{ user.accountNo }}</view>
                            <view class="account-meta">{{ user.status === 'active' ? '正常' : '停用' }}</view>
                        </view>
                    </view>

                    <view class="account-schedule-panel">
                        <view class="section">
                            <view class="panel-head">
                                <view>
                                    <view class="section-title no-margin">{{ selectedAccountLabel }}</view>
                                    <view class="muted">按账号查看并维护该账号关联的课表记录</view>
                                </view>
                                <button class="primary-btn panel-btn" @tap="openCreateForAccount" :disabled="!selectedAccount">新增课表</button>
                            </view>
                        </view>

                        <view v-if="!selectedAccount" class="empty-card">
                            请先选择左侧账号。
                        </view>
                        <view v-else-if="accountScheduleItems.length === 0" class="empty-card">
                            该账号暂无课表，可点击“新增课表”添加课程安排。
                        </view>

                        <view class="schedule-card" v-for="item in accountScheduleItems" :key="item.id">
                            <view class="schedule-card-head">
                                <view>
                                    <view class="schedule-title">{{ item.courseName }}</view>
                                    <view class="schedule-sub">{{ item.scheduleDate }} {{ item.timeSlot }}</view>
                                </view>
                                <view class="schedule-status">{{ statusLabel(item.status) }}</view>
                            </view>
                            <view class="schedule-line">实验室：{{ item.labName }}</view>
                            <view class="schedule-line">教师：{{ item.teacherName || '未绑定' }} {{ item.teacherAccountNo ? '(' + item.teacherAccountNo + ')' : '' }}</view>
                            <view class="schedule-line">学生：{{ item.studentNames || '未绑定学生' }}</view>
                            <view class="card-actions">
                                <button class="secondary-btn small" @tap="openEditSchedule(item)">编辑</button>
                                <button class="danger-btn small" @tap="removeSchedule(item)">删除</button>
                            </view>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 按课程维护内容 -->
            <view class="tab-content" v-if="activeMode === 'course' && courseSubTab === 'manage'">
                <view class="section">
                    <view class="section-title">按课程查看与编辑</view>
                    <view class="manage-toolbar">
                        <input
                            class="search-input"
                            v-model="scheduleKeyword"
                            placeholder="搜索课程名称"
                            confirm-type="search"
                            @confirm="loadScheduleItems"
                        />
                        <button class="mini-btn" @tap="loadScheduleItems">搜索</button>
                    </view>
                    <view class="button-group">
                        <button class="primary-btn" @tap="openCreateSchedule">新增课表</button>
                    </view>
                </view>

                <view v-if="scheduleItems.length === 0" class="empty-card">
                    暂无课表数据，可点击“新增课表”为教师和学生账号添加课程安排。
                </view>

                <view class="schedule-card" v-for="item in scheduleItems" :key="item.id">
                    <view class="schedule-card-head">
                        <view>
                            <view class="schedule-title">{{ item.courseName }}</view>
                            <view class="schedule-sub">{{ item.scheduleDate }} {{ item.timeSlot }}</view>
                        </view>
                        <view class="schedule-status">{{ statusLabel(item.status) }}</view>
                    </view>
                    <view class="schedule-line">实验室：{{ item.labName }}</view>
                    <view class="schedule-line">教师：{{ item.teacherName || '未绑定' }} {{ item.teacherAccountNo ? '(' + item.teacherAccountNo + ')' : '' }}</view>
                    <view class="schedule-line">学生：{{ item.studentNames || '未绑定学生' }}</view>
                    <view class="card-actions">
                        <button class="secondary-btn small" @tap="openEditSchedule(item)">编辑</button>
                        <button class="danger-btn small" @tap="removeSchedule(item)">删除</button>
                    </view>
                </view>
            </view>

            <!-- 导入课表内容 -->
            <view class="tab-content" v-if="isImportPaneVisible">
                <view class="section">
                    <view class="section-title">选择学期</view>
                    <view class="custom-picker" @tap="showSemesterPicker">
                        <view class="picker">
                            <text class="picker-text">{{ semesters[semesterIndex] || '请选择学期' }}</text>
                            <image class="arrow-icon" src="/static/images/icons/导入.png"></image>
                        </view>
                    </view>
                </view>

                <view class="section">
                    <view class="section-title">上传Excel文件</view>
                    <view class="upload-box" @tap="chooseFile">
                        <block v-if="!importFile">
                            <view class="upload-placeholder">
                                <image src="/static/images/icons/excel.png"></image>
                                <text>点击选择Excel文件</text>
                            </view>
                        </block>
                        <block v-else>
                            <view class="file-info">
                                <image src="/static/images/icons/excel.png"></image>
                                <text>{{ importFile.name }}</text>
                            </view>
                        </block>
                    </view>
                </view>

                <view class="section">
                    <view class="section-title">导入说明</view>
                    <view class="import-guide">
                        <text>1. 请确保Excel文件格式正确，包含必要的课程信息。</text>
                        <text>2. 支持的Excel格式：.xlsx, .xls</text>
                        <text>3. 导入将覆盖当前学期的课表数据。</text>
                        <text>4. 导入前请确认数据无误。</text>
                    </view>
                </view>

                <view class="button-group">
                    <button class="primary-btn" @tap="importSchedule" :disabled="!importFile">开始导入</button>
                    <button class="secondary-btn" @tap="previewTemplate">下载模板</button>
                </view>
            </view>

            <!-- 导出课表内容 -->
            <view class="tab-content" v-if="isExportPaneVisible">
                <view class="section">
                    <view class="section-title">选择学期</view>
                    <view class="custom-picker" @tap="showExportSemesterPicker">
                        <view class="picker">
                            <text class="picker-text">{{ semesters[exportSemesterIndex] || '请选择学期' }}</text>
                        </view>
                    </view>
                </view>

                <view class="section">
                    <view class="section-title">导出格式</view>
                    <radio-group class="format-group" @change="onFormatChange">
                        <label class="format-item">
                            <radio value="excel" :checked="exportFormat === 'excel'" />
                            <text>Excel格式(.xlsx)</text>
                        </label>
                    </radio-group>
                </view>

                <view class="section">
                    <view class="section-title">导出选项</view>
                    <checkbox-group @change="onExportOptionsChange">
                        <label class="option-item">
                            <checkbox value="includeRooms" :checked="exportOptions.includeRooms" />
                            <text>包含教室信息</text>
                        </label>
                        <label class="option-item">
                            <checkbox value="includeTeachers" :checked="exportOptions.includeTeachers" />
                            <text>包含教师信息</text>
                        </label>
                        <label class="option-item">
                            <checkbox value="includeStudents" :checked="exportOptions.includeStudents" />
                            <text>包含学生人数</text>
                        </label>
                    </checkbox-group>
                </view>

                <view class="button-group">
                    <button class="primary-btn" @tap="exportSchedule">开始导出</button>
                </view>
            </view>

            <!-- 导入进度弹窗 -->
            <view class="modal" v-if="showProgressModal">
                <view class="modal-mask"></view>
                <view class="modal-content">
                    <view class="modal-header">导入进度</view>
                    <view class="modal-body">
                        <view class="progress-container">
                            <progress :percent="importProgress" stroke-width="6" color="#3a7bd5" />
                            <text class="progress-text">{{ importProgress }}%</text>
                        </view>
                        <view class="progress-status">{{ progressStatus }}</view>
                    </view>
                </view>
            </view>

            <!-- 导入结果弹窗 -->
            <view class="modal" v-if="showResultModal">
                <view class="modal-mask" @tap="hideResultModal"></view>
                <view class="modal-content">
                    <view class="modal-header">导入结果</view>
                    <view class="modal-body">
                        <view :class="'result-icon ' + (importSuccess ? 'success' : 'error')">
                            <image :src="importSuccess ? '/static/images/icons/success.png' : '/static/images/icons/error.png'"></image>
                        </view>
                        <view class="result-message">{{ resultMessage }}</view>
                        <view class="result-details" v-if="errorDetails">
                            <text>错误详情：</text>
                            <text>{{ errorDetails }}</text>
                        </view>
                    </view>
                    <view class="modal-footer">
                        <button class="confirm-btn" @tap="hideResultModal">确定</button>
                    </view>
                </view>
            </view>

            <!-- 自定义学期选择器弹窗 -->
            <view class="modal custom-picker-modal" v-if="showSemesterPickerModal">
                <view class="modal-mask" @tap="hideSemesterPicker"></view>
                <view class="modal-content picker-modal-content">
                    <view class="modal-header">
                        <text>选择学期</text>
                    </view>
                    <view class="modal-body">
                        <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[semesterIndex]" @change="onSemesterPickerChange">
                            <picker-view-column>
                                <view class="picker-view-item" v-for="(item, index) in semesters" :key="index">{{ item }}</view>
                            </picker-view-column>
                        </picker-view>
                    </view>
                    <view class="modal-footer">
                        <button class="cancel-btn" @tap="hideSemesterPicker">取消</button>
                        <button class="confirm-btn" @tap="confirmSemesterPicker">确定</button>
                    </view>
                </view>
            </view>

            <!-- 自定义导出学期选择器弹窗 -->
            <view class="modal custom-picker-modal" v-if="showExportSemesterPickerModal">
                <view class="modal-mask" @tap="hideExportSemesterPicker"></view>
                <view class="modal-content picker-modal-content">
                    <view class="modal-header">
                        <text>选择学期</text>
                    </view>
                    <view class="modal-body">
                        <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[exportSemesterIndex]" @change="onExportSemesterPickerChange">
                            <picker-view-column>
                                <view class="picker-view-item" v-for="(item, index) in semesters" :key="index">{{ item }}</view>
                            </picker-view-column>
                        </picker-view>
                    </view>
                    <view class="modal-footer">
                        <button class="cancel-btn" @tap="hideExportSemesterPicker">取消</button>
                        <button class="confirm-btn" @tap="confirmExportSemesterPicker">确定</button>
                    </view>
                </view>
            </view>

            <!-- 新增/编辑课表弹窗 -->
            <view class="modal schedule-form-modal" v-if="showScheduleModal">
                <view class="modal-mask" @tap="closeScheduleModal"></view>
                <view class="modal-content form-modal-content">
                    <view class="modal-header">{{ editingScheduleId ? '编辑课表' : '新增课表' }}</view>
                    <scroll-view class="modal-body form-scroll" scroll-y>
                        <view class="form-item">
                            <text class="form-label">学期</text>
                            <picker :range="semesters" :value="formSemesterIndex" @change="onFormSemesterChange">
                                <view class="picker form-picker">{{ semesters[formSemesterIndex] || '请选择学期' }}</view>
                            </picker>
                        </view>
                        <view class="form-item">
                            <text class="form-label">课程名称</text>
                            <input class="form-input" v-model="scheduleForm.courseName" placeholder="例如：数据结构与算法" />
                        </view>
                        <view class="form-item">
                            <text class="form-label">课程类型</text>
                            <input class="form-input" v-model="scheduleForm.courseType" placeholder="理论课 / 实验课 / 实践课" />
                        </view>
                        <view class="form-item">
                            <text class="form-label">实验室</text>
                            <picker :range="labNames" :value="formLabIndex" @change="onFormLabChange">
                                <view class="picker form-picker">{{ labNames[formLabIndex] || '请选择实验室' }}</view>
                            </picker>
                        </view>
                        <view class="form-item">
                            <text class="form-label">教师账号</text>
                            <picker :range="teacherNames" :value="formTeacherIndex" @change="onFormTeacherChange">
                                <view class="picker form-picker">{{ teacherNames[formTeacherIndex] || '不指定教师' }}</view>
                            </picker>
                        </view>
                        <view class="form-item">
                            <text class="form-label">学生账号</text>
                            <textarea class="form-textarea" v-model="scheduleForm.studentAccountNos" placeholder="多个学生账号用逗号分隔，例如：S20230001,S20230002" />
                        </view>
                        <view class="form-row">
                            <view class="form-item half">
                                <text class="form-label">日期</text>
                                <input class="form-input" v-model="scheduleForm.scheduleDate" placeholder="yyyy-MM-dd" />
                            </view>
                            <view class="form-item half">
                                <text class="form-label">状态</text>
                                <picker :range="statusOptions" range-key="label" :value="formStatusIndex" @change="onFormStatusChange">
                                    <view class="picker form-picker">{{ statusOptions[formStatusIndex]?.label || '仅供查看' }}</view>
                                </picker>
                            </view>
                        </view>
                        <view class="form-row">
                            <view class="form-item half">
                                <text class="form-label">开始时间</text>
                                <input class="form-input" v-model="scheduleForm.startTime" placeholder="08:00" />
                            </view>
                            <view class="form-item half">
                                <text class="form-label">结束时间</text>
                                <input class="form-input" v-model="scheduleForm.endTime" placeholder="09:50" />
                            </view>
                        </view>
                        <view class="form-row">
                            <view class="form-item half">
                                <text class="form-label">计划人数</text>
                                <input class="form-input" v-model="scheduleForm.plannedStudentCount" type="number" />
                            </view>
                            <view class="form-item half">
                                <text class="form-label">最大人数</text>
                                <input class="form-input" v-model="scheduleForm.maxStudentCount" type="number" />
                            </view>
                        </view>
                        <view class="form-item">
                            <text class="form-label">说明</text>
                            <textarea class="form-textarea" v-model="scheduleForm.description" placeholder="课程说明，可选" />
                        </view>
                    </scroll-view>
                    <view class="modal-footer">
                        <button class="cancel-btn" @tap="closeScheduleModal">取消</button>
                        <button class="confirm-btn" @tap="saveSchedule">保存</button>
                    </view>
                </view>
            </view>

        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
import {
    getSemesters,
    getLabs,
    getUsers,
    getScheduleItems,
    createScheduleItem,
    updateScheduleItem,
    deleteScheduleItem,
    importScheduleExcel,
    downloadScheduleTemplate,
    downloadScheduleExcel
} from '@/api/admin';

const activeMode = ref('account');
const accountSubTab = ref('student');
const courseSubTab = ref('manage');
const isAccountScheduleTab = computed(() => accountSubTab.value === 'student' || accountSubTab.value === 'teacher');
const isImportPaneVisible = computed(() => activeMode.value === 'account' ? accountSubTab.value === 'import' : courseSubTab.value === 'import');
const isExportPaneVisible = computed(() => activeMode.value === 'account' ? accountSubTab.value === 'export' : courseSubTab.value === 'export');
const accountRoleName = computed(() => accountSubTab.value === 'teacher' ? '教师' : '学生');
const accountSectionTitle = computed(() => `按${accountRoleName.value}账号查看与编辑`);
const accountSearchPlaceholder = computed(() => `搜索${accountRoleName.value}账号或姓名`);
const selectedAccountLabel = computed(() => {
    if (!selectedAccount.value) return `未选择${accountRoleName.value}账号`;
    const name = selectedAccount.value.name || selectedAccount.value.realName || '-';
    return `${accountRoleName.value}：${name}（${selectedAccount.value.accountNo}）`;
});
const semesters = ref<string[]>([]);
const semesterList = ref<any[]>([]);
const loadSemesters = async () => {
    try {
        const list = await getSemesters();
        const arr = Array.isArray(list) ? list : [];
        semesterList.value = arr;
        semesters.value = arr.map((item: any) => item.semesterName);
        if (semesterIndex.value >= semesters.value.length) semesterIndex.value = 0;
        if (exportSemesterIndex.value >= semesters.value.length) exportSemesterIndex.value = 0;
        if (formSemesterIndex.value >= semesters.value.length) formSemesterIndex.value = 0;
        if (!scheduleForm.semesterId && arr[0]?.id) scheduleForm.semesterId = arr[0].id;
    } catch (err: any) {
        semesterList.value = [];
        semesters.value = [];
        uni.showToast({ title: err?.data?.message || "加载失败", icon: "none" });
    }
};
onMounted(async () => {
    await loadSemesters();
    await loadManageOptions();
    await loadAccountUsers();
    await loadScheduleItems();
});
const semesterIndex = ref(0);
const exportSemesterIndex = ref(0);
const formSemesterIndex = ref(0);
const showSemesterPickerModal = ref(false);
const showExportSemesterPickerModal = ref(false);
const tempSemesterIndex = ref(0);
const tempExportSemesterIndex = ref(0);
const importFile = ref<any>(null);
const showProgressModal = ref(false);
const importProgress = ref(0);
const progressStatus = ref("准备导入...");
const showResultModal = ref(false);
const importSuccess = ref(false);
const resultMessage = ref("");
const errorDetails = ref("");
const exportFormat = ref("excel");
const exportOptions = reactive({ includeRooms: true, includeTeachers: true, includeStudents: false });
const scheduleKeyword = ref('');
const scheduleItems = ref<any[]>([]);
const accountKeyword = ref('');
const accountUsers = ref<any[]>([]);
const selectedAccount = ref<any>(null);
const accountScheduleItems = ref<any[]>([]);
const labList = ref<any[]>([]);
const labNames = ref<string[]>([]);
const teacherList = ref<any[]>([]);
const teacherNames = ref<string[]>(['不指定教师']);
const formLabIndex = ref(0);
const formTeacherIndex = ref(0);
const formStatusIndex = ref(0);
const showScheduleModal = ref(false);
const editingScheduleId = ref<any>(null);
const statusOptions = [
    { label: '仅供查看', value: 'scheduled' },
    { label: '可预约', value: 'available' },
    { label: '已满员', value: 'full' },
    { label: '进行中', value: 'ongoing' },
    { label: '已取消', value: 'cancelled' }
];
const newScheduleForm = () => ({
    semesterId: semesterList.value[0]?.id || null,
    labId: labList.value[0]?.id || null,
    teacherUserId: null,
    courseName: '',
    courseType: '实验课',
    scheduleDate: getTodayDate(),
    startTime: '08:00',
    endTime: '09:50',
    plannedStudentCount: 1,
    maxStudentCount: 40,
    status: 'scheduled',
    canReserve: false,
    studentAccountNos: '',
    description: '',
    remark: ''
});
const scheduleForm = reactive<any>(newScheduleForm());
const switchMode = async (e) => {
    activeMode.value = e.currentTarget.dataset.mode;
    if (activeMode.value === 'account' && isAccountScheduleTab.value) {
        await loadAccountUsers();
        return;
    }
    if (activeMode.value === 'course' && courseSubTab.value === 'manage') {
        await loadScheduleItems();
    }
};

const switchAccountSubTab = async (e) => {
    const next = e.currentTarget.dataset.tab;
    const roleChanged = next === 'student' || next === 'teacher';
    accountSubTab.value = next;
    if (roleChanged) {
        selectedAccount.value = null;
        accountScheduleItems.value = [];
        await loadAccountUsers();
    }
};

const switchCourseSubTab = async (e) => {
    courseSubTab.value = e.currentTarget.dataset.tab;
    if (courseSubTab.value === 'manage') await loadScheduleItems();
};
const showSemesterPicker = () => { showSemesterPickerModal.value = true; tempSemesterIndex.value = semesterIndex.value; };
const hideSemesterPicker = () => { showSemesterPickerModal.value = false; };
const onSemesterPickerChange = (e) => { tempSemesterIndex.value = e.detail.value[0]; };
const confirmSemesterPicker = () => { semesterIndex.value = tempSemesterIndex.value; showSemesterPickerModal.value = false; };
const showExportSemesterPicker = () => { showExportSemesterPickerModal.value = true; tempExportSemesterIndex.value = exportSemesterIndex.value; };
const hideExportSemesterPicker = () => { showExportSemesterPickerModal.value = false; };
const onExportSemesterPickerChange = (e) => { tempExportSemesterIndex.value = e.detail.value[0]; };
const confirmExportSemesterPicker = () => { exportSemesterIndex.value = tempExportSemesterIndex.value; showExportSemesterPickerModal.value = false; };
const onFormatChange = (e) => { exportFormat.value = e.detail.value; };
const onExportOptionsChange = (e) => { const values = e.detail.value; exportOptions.includeRooms = values.includes("includeRooms"); exportOptions.includeTeachers = values.includes("includeTeachers"); exportOptions.includeStudents = values.includes("includeStudents"); };

const selectedImportSemester = () => semesterList.value[semesterIndex.value];
const selectedExportSemester = () => semesterList.value[exportSemesterIndex.value];

function getTodayDate() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

const loadManageOptions = async () => {
    try {
        const labs = await getLabs();
        labList.value = Array.isArray(labs) ? labs : [];
        labNames.value = labList.value.map((item: any) => item.name || item.labName || `实验室${item.id}`);
        if (!scheduleForm.labId && labList.value[0]?.id) scheduleForm.labId = labList.value[0].id;
    } catch (err: any) {
        labList.value = [];
        labNames.value = [];
        uni.showToast({ title: err?.data?.message || '实验室列表加载失败', icon: 'none' });
    }

    try {
        const teachers = await getUsers('', 'teacher');
        teacherList.value = Array.isArray(teachers) ? teachers : [];
        teacherNames.value = ['不指定教师', ...teacherList.value.map((item: any) => `${item.accountNo} ${item.name}`)];
    } catch (err: any) {
        teacherList.value = [];
        teacherNames.value = ['不指定教师'];
        uni.showToast({ title: err?.data?.message || '教师列表加载失败', icon: 'none' });
    }
};

const loadScheduleItems = async () => {
    try {
        const list = await getScheduleItems({ keyword: scheduleKeyword.value });
        scheduleItems.value = Array.isArray(list) ? list : [];
    } catch (err: any) {
        scheduleItems.value = [];
        uni.showToast({ title: err?.data?.message || '加载课表失败', icon: 'none' });
    }
};

const loadAccountUsers = async () => {
    if (!isAccountScheduleTab.value) return;
    try {
        const list = await getUsers(accountKeyword.value, accountSubTab.value);
        const users = Array.isArray(list) ? list : [];
        accountUsers.value = users;
        if (!users.length) {
            selectedAccount.value = null;
            accountScheduleItems.value = [];
            return;
        }
        const current = selectedAccount.value
            ? users.find((item: any) => item.id === selectedAccount.value.id)
            : null;
        selectedAccount.value = current || users[0];
        await loadAccountSchedules();
    } catch (err: any) {
        accountUsers.value = [];
        selectedAccount.value = null;
        accountScheduleItems.value = [];
        uni.showToast({ title: err?.data?.message || '账号列表加载失败', icon: 'none' });
    }
};

const selectAccount = async (user: any) => {
    selectedAccount.value = user;
    await loadAccountSchedules();
};

const loadAccountSchedules = async () => {
    if (!selectedAccount.value?.accountNo) {
        accountScheduleItems.value = [];
        return;
    }
    try {
        const accountNo = selectedAccount.value.accountNo;
        const list = await getScheduleItems({ keyword: accountNo });
        const rows = Array.isArray(list) ? list : [];
        accountScheduleItems.value = rows.filter((item: any) => scheduleBelongsToSelectedAccount(item));
    } catch (err: any) {
        accountScheduleItems.value = [];
        uni.showToast({ title: err?.data?.message || '账号课表加载失败', icon: 'none' });
    }
};

const scheduleBelongsToSelectedAccount = (item: any) => {
    const accountNo = selectedAccount.value?.accountNo;
    if (!accountNo) return false;
    const role = selectedAccount.value?.role || accountSubTab.value;
    if (role === 'teacher') {
        return item.teacherAccountNo === accountNo;
    }
    return String(item.studentAccountNos || '')
        .split(',')
        .map((value) => value.trim())
        .includes(accountNo);
};

const refreshScheduleViews = async () => {
    const tasks: Promise<any>[] = [];
    if (activeMode.value === 'course' && courseSubTab.value === 'manage') {
        tasks.push(loadScheduleItems());
    }
    if (selectedAccount.value) {
        tasks.push(loadAccountSchedules());
    }
    if (!tasks.length) {
        tasks.push(loadScheduleItems());
    }
    await Promise.all(tasks);
};

const resetScheduleForm = (item?: any) => {
    const source = item
        ? {
              semesterId: item.semesterId,
              labId: item.labId,
              teacherUserId: item.teacherUserId,
              courseName: item.courseName || '',
              courseType: item.courseType || '实验课',
              scheduleDate: item.scheduleDate || getTodayDate(),
              startTime: item.startTime || '08:00',
              endTime: item.endTime || '09:50',
              plannedStudentCount: item.plannedStudentCount || 1,
              maxStudentCount: item.maxStudentCount || 40,
              status: item.status || 'scheduled',
              canReserve: Number(item.canReserve) === 1 || item.canReserve === true,
              studentAccountNos: item.studentAccountNos || '',
              description: item.description || '',
              remark: item.remark || ''
          }
        : newScheduleForm();
    Object.assign(scheduleForm, source);
    formSemesterIndex.value = Math.max(0, semesterList.value.findIndex((s: any) => s.id === scheduleForm.semesterId));
    formLabIndex.value = Math.max(0, labList.value.findIndex((l: any) => l.id === scheduleForm.labId));
    const teacherIndex = teacherList.value.findIndex((t: any) => t.id === scheduleForm.teacherUserId);
    formTeacherIndex.value = teacherIndex >= 0 ? teacherIndex + 1 : 0;
    formStatusIndex.value = Math.max(0, statusOptions.findIndex((s) => s.value === scheduleForm.status));
};

const openCreateSchedule = async () => {
    if (labList.value.length === 0 || teacherNames.value.length === 1) await loadManageOptions();
    resetScheduleForm();
    editingScheduleId.value = null;
    showScheduleModal.value = true;
};

const openCreateForAccount = async () => {
    if (!selectedAccount.value) {
        uni.showToast({ title: '请先选择账号', icon: 'none' });
        return;
    }
    if (labList.value.length === 0 || teacherNames.value.length === 1) await loadManageOptions();
    resetScheduleForm();
    editingScheduleId.value = null;
    if (accountSubTab.value === 'teacher') {
        scheduleForm.teacherUserId = selectedAccount.value.id;
        scheduleForm.studentAccountNos = '';
        const teacherIndex = teacherList.value.findIndex((item: any) => item.id === scheduleForm.teacherUserId);
        formTeacherIndex.value = teacherIndex >= 0 ? teacherIndex + 1 : 0;
    } else {
        scheduleForm.teacherUserId = null;
        scheduleForm.studentAccountNos = selectedAccount.value.accountNo || '';
        formTeacherIndex.value = 0;
    }
    showScheduleModal.value = true;
};

const openEditSchedule = async (item: any) => {
    if (labList.value.length === 0 || teacherNames.value.length === 1) await loadManageOptions();
    resetScheduleForm(item);
    editingScheduleId.value = item.id;
    showScheduleModal.value = true;
};

const closeScheduleModal = () => {
    showScheduleModal.value = false;
};

const onFormSemesterChange = (e) => {
    formSemesterIndex.value = Number(e.detail.value) || 0;
    scheduleForm.semesterId = semesterList.value[formSemesterIndex.value]?.id || null;
};

const onFormLabChange = (e) => {
    formLabIndex.value = Number(e.detail.value) || 0;
    scheduleForm.labId = labList.value[formLabIndex.value]?.id || null;
};

const onFormTeacherChange = (e) => {
    formTeacherIndex.value = Number(e.detail.value) || 0;
    scheduleForm.teacherUserId = formTeacherIndex.value > 0 ? teacherList.value[formTeacherIndex.value - 1]?.id : null;
};

const onFormStatusChange = (e) => {
    formStatusIndex.value = Number(e.detail.value) || 0;
    scheduleForm.status = statusOptions[formStatusIndex.value]?.value || 'scheduled';
    scheduleForm.canReserve = scheduleForm.status === 'available';
};

const statusLabel = (status: string) => {
    const item = statusOptions.find((option) => option.value === status);
    return item?.label || status || '';
};

const saveSchedule = async () => {
    if (!scheduleForm.semesterId || !scheduleForm.labId || !scheduleForm.courseName || !scheduleForm.scheduleDate) {
        uni.showToast({ title: '请填写学期、实验室、课程和日期', icon: 'none' });
        return;
    }
    try {
        const payload = {
            ...scheduleForm,
            plannedStudentCount: Number(scheduleForm.plannedStudentCount) || 0,
            maxStudentCount: Number(scheduleForm.maxStudentCount) || 0
        };
        if (editingScheduleId.value) {
            await updateScheduleItem(editingScheduleId.value, payload);
        } else {
            await createScheduleItem(payload);
        }
        showScheduleModal.value = false;
        await refreshScheduleViews();
        uni.showToast({ title: '保存成功', icon: 'success' });
    } catch (err: any) {
        uni.showToast({ title: err?.data?.message || '保存失败', icon: 'none' });
    }
};

const removeSchedule = (item: any) => {
    uni.showModal({
        title: '确认删除',
        content: `确定删除「${item.courseName}」这条课表吗？`,
        confirmColor: '#EF4444',
        success: async (res) => {
            if (!res.confirm) return;
            try {
                await deleteScheduleItem(item.id);
                await refreshScheduleViews();
                uni.showToast({ title: '已删除', icon: 'success' });
            } catch (err: any) {
                uni.showToast({ title: err?.data?.message || '删除失败', icon: 'none' });
            }
        }
    });
};

const formatFileSize = (size: any) => {
    const bytes = Number(size) || 0;
    if (!bytes) return '';
    if (bytes < 1024) return `${bytes}B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)}MB`;
};

const setPickedFile = (file: any) => {
    const path = file?.path || file?.tempFilePath;
    if (!path) {
        uni.showToast({ title: '无法读取文件路径', icon: 'none' });
        return;
    }
    const name = file?.name || path.split('/').pop() || 'schedule.xlsx';
    if (!/\.(xlsx|xls)$/i.test(name)) {
        uni.showToast({ title: '请选择 .xlsx 或 .xls 文件', icon: 'none' });
        return;
    }
    importFile.value = {
        name,
        size: file?.size,
        sizeText: formatFileSize(file?.size),
        path
    };
};

const chooseFile = () => {
    const chooseFileApi = (uni as any).chooseFile;
    const chooseMessageFileApi = (uni as any).chooseMessageFile;
    const onSuccess = (res: any) => {
        const file = (res.tempFiles && res.tempFiles[0]) || {
            path: res.tempFilePaths && res.tempFilePaths[0],
            name: res.tempFilePaths && String(res.tempFilePaths[0]).split('/').pop()
        };
        setPickedFile(file);
    };

    if (typeof chooseFileApi === 'function') {
        chooseFileApi({
            count: 1,
            type: 'file',
            extension: ['.xlsx', '.xls', 'xlsx', 'xls'],
            success: onSuccess
        });
        return;
    }

    if (typeof chooseMessageFileApi === 'function') {
        chooseMessageFileApi({
            count: 1,
            type: 'file',
            extension: ['xlsx', 'xls'],
            success: onSuccess
        });
        return;
    }

    uni.showToast({ title: '当前平台不支持选择文件', icon: 'none' });
};

const previewTemplate = async () => {
    try {
        uni.showLoading({ title: '下载中...' });
        await downloadScheduleTemplate();
        uni.hideLoading();
        uni.showToast({ title: '模板下载已开始', icon: 'success' });
    } catch (err: any) {
        uni.hideLoading();
        uni.showToast({ title: err?.data?.message || '模板下载失败', icon: 'none' });
    }
};

const importSchedule = async () => {
    if (!importFile.value) {
        uni.showToast({ title: "请先选择文件", icon: "none" });
        return;
    }
    const semester = selectedImportSemester();
    if (!semester?.id) {
        uni.showToast({ title: "请先选择学期", icon: "none" });
        return;
    }

    showProgressModal.value = true;
    importProgress.value = 20;
    progressStatus.value = "上传并解析Excel...";
    try {
        const batch: any = await importScheduleExcel({
            semesterId: semester.id,
            filePath: importFile.value.path,
            fileName: importFile.value.name
        });
        importProgress.value = 100;
        progressStatus.value = "完成导入...";
        const successCount = Number(batch?.successCount) || 0;
        const success = successCount > 0 && batch?.status !== 'failed';
        setTimeout(() => {
            showProgressModal.value = false;
            showImportResult(success, batch);
        }, 400);
    } catch (err: any) {
        showProgressModal.value = false;
        showImportResult(false, { errorDetail: err?.data?.message || "导入失败" });
    }
};

const showImportResult = (success, batch) => {
    const successCount = Number(batch?.successCount) || 0;
    const failureCount = Number(batch?.failureCount) || 0;
    let message = "";
    let errDetails = "";
    if (success) {
        message = batch?.message || `课表导入成功，共导入 ${successCount} 行`;
        if (failureCount > 0) {
            errDetails = batch?.errorDetails || "部分行导入失败，请检查Excel文件。";
        }
    } else {
        message = batch?.message || "课表导入失败";
        errDetails = batch?.errorDetail || batch?.errorDetails || "导入失败，请检查Excel文件格式是否正确，并确保所有必填字段已填写。";
    }
    showResultModal.value = true;
    importSuccess.value = success;
    resultMessage.value = message;
    errorDetails.value = errDetails;
};
const hideResultModal = async () => {
    showResultModal.value = false;
    if (importSuccess.value) {
        importFile.value = null;
        await refreshScheduleViews();
    }
};

const exportSchedule = () => {
    const semester = selectedExportSemester();
    if (!semester?.id) {
        uni.showToast({ title: "请先选择学期", icon: "none" });
        return;
    }
    let optionsText: string[] = [];
    if (exportOptions.includeRooms) optionsText.push("教室信息");
    if (exportOptions.includeTeachers) optionsText.push("教师信息");
    if (exportOptions.includeStudents) optionsText.push("学生人数");
    const optionsString = optionsText.length > 0 ? "，包含" + optionsText.join("、") : "";
    uni.showModal({
        title: "确认导出",
        content: "您将导出" + (semester.semesterName || semesters.value[exportSemesterIndex.value]) + "的课表，Excel格式" + optionsString + "。确定继续吗？",
        confirmColor: "#3a7bd5",
        success: async (res) => {
            if (!res.confirm) return;
            uni.showLoading({ title: "导出中..." });
            try {
                await downloadScheduleExcel({
                    semesterId: semester.id,
                    includeRooms: exportOptions.includeRooms,
                    includeTeachers: exportOptions.includeTeachers,
                    includeStudents: exportOptions.includeStudents,
                    fileName: `${semester.semesterName || '课表'}课表.xlsx`
                });
                uni.hideLoading();
                uni.showToast({ title: "下载已开始", icon: "success" });
            } catch (err: any) {
                uni.hideLoading();
                uni.showToast({ title: err?.data?.message || "导出失败", icon: "none" });
            }
        }
    });
};
</script>
<style lang="less">
@import './admin-schedule-management.less';
</style>
