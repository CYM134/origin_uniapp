<template>
    <view>
        <!-- system-management.wxml -->
        <navigation-bar title="系统管理" :back="true" color="white" background="#3a7bd5" />

        <view class="container">
            <!-- 选项卡 -->
            <view class="tabs">
                <view :class="'tab ' + (activeTab === 0 ? 'active' : '')" @tap="switchTab" data-index="0">系统设置</view>
                <view :class="'tab ' + (activeTab === 1 ? 'active' : '')" @tap="switchTab" data-index="1">数据备份</view>
                <view :class="'tab ' + (activeTab === 2 ? 'active' : '')" @tap="switchTab" data-index="2">用户管理</view>
            </view>

            <!-- 系统设置 -->
            <view class="tab-content" v-if="activeTab === 0">
                <view class="section-title">基本设置</view>

                <view class="setting-item">
                    <view class="setting-label">系统名称</view>
                    <view class="setting-input">
                        <input type="text" :value="settings.systemName" @input="onSystemNameInput" placeholder="请输入系统名称" />
                    </view>
                </view>

                <view class="setting-item">
                    <view class="setting-label">学校名称</view>
                    <view class="setting-input">
                        <input type="text" :value="settings.schoolName" @input="onSchoolNameInput" placeholder="请输入学校名称" />
                    </view>
                </view>

                <view class="setting-item">
                    <view class="setting-label">系统Logo</view>
                    <view class="logo-upload" @tap="uploadLogo">
                        <image v-if="settings.logoUrl" :src="settings.logoUrl" mode="aspectFit"></image>
                        <view v-else class="upload-placeholder">
                            <text class="upload-icon">+</text>
                            <text class="upload-text">上传Logo</text>
                        </view>
                    </view>
                </view>

                <view class="section-title">预约设置</view>

                <view class="setting-item">
                    <view class="setting-label">开放预约时间</view>
                    <view class="time-range">
                        <picker mode="time" :value="settings.reservationStartTime" start="00:00" end="23:59" @change="onStartTimeChange">
                            <view class="time-picker">{{ settings.reservationStartTime }}</view>
                        </picker>
                        <text class="time-separator">至</text>
                        <picker mode="time" :value="settings.reservationEndTime" start="00:00" end="23:59" @change="onEndTimeChange">
                            <view class="time-picker">{{ settings.reservationEndTime }}</view>
                        </picker>
                    </view>
                </view>

                <view class="setting-item">
                    <view class="setting-label">预约提前天数</view>
                    <view class="setting-input">
                        <input type="number" :value="settings.advanceDays" @input="onAdvanceDaysInput" placeholder="请输入可提前预约的天数" />
                    </view>
                </view>

                <view class="setting-item">
                    <view class="setting-label">自动审批</view>
                    <switch :checked="settings.autoApproval" @change="onAutoApprovalChange" color="#3a7bd5" />
                </view>

                <view class="section-title">通知设置</view>

                <view class="setting-item">
                    <view class="setting-label">预约通知</view>
                    <switch :checked="settings.reservationNotification" @change="onReservationNotificationChange" color="#3a7bd5" />
                </view>

                <view class="setting-item">
                    <view class="setting-label">审批通知</view>
                    <switch :checked="settings.approvalNotification" @change="onApprovalNotificationChange" color="#3a7bd5" />
                </view>

                <view class="setting-item">
                    <view class="setting-label">提醒通知</view>
                    <switch :checked="settings.reminderNotification" @change="onReminderNotificationChange" color="#3a7bd5" />
                </view>

                <view class="save-btn" @tap="saveSettings">保存设置</view>
            </view>

            <!-- 数据备份 -->
            <view class="tab-content" v-if="activeTab === 1">
                <view class="section-title">数据备份</view>

                <view class="backup-item">
                    <view class="backup-info">
                        <view class="backup-title">实验室数据</view>
                        <view class="backup-desc">包含所有实验室信息、设备信息等</view>
                    </view>
                    <view class="backup-btn" @tap="backupLabs">备份</view>
                </view>

                <view class="backup-item">
                    <view class="backup-info">
                        <view class="backup-title">课表数据</view>
                        <view class="backup-desc">包含所有课程安排、教室占用信息等</view>
                    </view>
                    <view class="backup-btn" @tap="backupSchedules">备份</view>
                </view>

                <view class="backup-item">
                    <view class="backup-info">
                        <view class="backup-title">预约数据</view>
                        <view class="backup-desc">包含所有预约记录、审批记录等</view>
                    </view>
                    <view class="backup-btn" @tap="backupReservations">备份</view>
                </view>

                <view class="backup-item">
                    <view class="backup-info">
                        <view class="backup-title">用户数据</view>
                        <view class="backup-desc">包含所有用户信息、权限设置等</view>
                    </view>
                    <view class="backup-btn" @tap="backupUsers">备份</view>
                </view>

                <view class="backup-item">
                    <view class="backup-info">
                        <view class="backup-title">全部数据</view>
                        <view class="backup-desc">包含系统所有数据</view>
                    </view>
                    <view class="backup-btn backup-all-btn" @tap="backupAll">备份全部</view>
                </view>

                <view class="section-title">备份记录</view>

                <view class="backup-records">
                    <view v-if="backupRecords.length === 0" class="empty-records">
                        <text>暂无备份记录</text>
                    </view>

                    <view class="record-item" v-for="(item, index) in backupRecords" :key="index">
                        <view class="record-info">
                            <view class="record-name">{{ item.name }}</view>
                            <view class="record-time">{{ item.time }}</view>
                            <view class="record-size">{{ item.size }}</view>
                        </view>

                        <view class="record-actions">
                            <view class="record-btn restore-btn" @tap="restoreBackup" :data-id="item.id">恢复</view>
                            <view class="record-btn download-btn" @tap="downloadBackup" :data-id="item.id">下载</view>
                            <view class="record-btn delete-btn" @tap="deleteBackup" :data-id="item.id">删除</view>
                        </view>
                    </view>
                </view>
            </view>

            <!-- 用户管理 -->
            <view class="tab-content" v-if="activeTab === 2">
                <view class="user-search">
                    <view class="search-box">
                        <input type="text" :value="searchValue" @input="onSearchInput" placeholder="搜索用户名、ID或角色" />
                        <view class="search-icon" @tap="searchUsers">
                            <image src="/static/images/icons/search-icon.png" mode="aspectFit"></image>
                        </view>
                    </view>
                </view>
                <view class="add-user-btn" @tap="showAddUserModal">
                    <text class="add-icon">+</text>
                    <text>添加用户</text>
                </view>

                <view class="user-filter">
                    <view :class="'filter-item ' + (userType === 'all' ? 'active' : '')" @tap="filterUsersByType" data-type="all">全部</view>
                    <view :class="'filter-item ' + (userType === 'admin' ? 'active' : '')" @tap="filterUsersByType" data-type="admin">管理员</view>
                    <view :class="'filter-item ' + (userType === 'teacher' ? 'active' : '')" @tap="filterUsersByType" data-type="teacher">教师</view>
                    <view :class="'filter-item ' + (userType === 'student' ? 'active' : '')" @tap="filterUsersByType" data-type="student">学生</view>
                </view>

                <view class="user-list">
                    <view v-if="filteredUsers.length === 0" class="empty-users">
                        <text>暂无用户数据</text>
                    </view>

                    <view class="user-item" v-for="(item, index) in filteredUsers" :key="index">
                        <view class="user-avatar">
                            <image :src="item.avatar || '/images/avatar/default.png'" mode="aspectFill"></image>
                        </view>

                        <view class="user-info">
                            <view class="user-name">{{ item.name }}</view>
                            <view class="user-id">ID: {{ item.id }}</view>
                            <view class="user-role">角色: {{ item.roleName }}</view>
                        </view>

                        <view class="user-actions">
                            <view class="user-btn edit-btn" @tap="editUser" :data-id="item.id">编辑</view>
                            <view class="user-btn reset-btn" @tap="resetPassword" :data-id="item.id">重置密码</view>
                            <view :class="'user-btn ' + (item.status === 'active' ? 'disable-btn' : 'enable-btn')" @tap="toggleUserStatus" :data-id="item.id">
                                {{ item.status === 'active' ? '禁用' : '启用' }}
                            </view>
                        </view>
                    </view>
                </view>
            </view>
        </view>

        <!-- 添加/编辑用户弹窗 -->
        <view class="modal" v-if="showUserModal">
            <view class="modal-mask" @tap="closeUserModal"></view>
            <view class="modal-content">
                <view class="modal-header">
                    {{ isEditingUser ? '编辑用户' : '添加用户' }}
                    <view class="close-btn" @tap="closeUserModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="form-item">
                        <view class="form-label">用户名</view>
                        <view class="form-input">
                            <input type="text" :value="userForm.name" @input="onUserNameInput" placeholder="请输入用户名" />
                        </view>
                    </view>

                    <view class="form-item">
                        <view class="form-label">用户ID</view>
                        <view class="form-input">
                            <input type="text" :value="userForm.id" @input="onUserIdInput" placeholder="请输入用户ID" :disabled="isEditingUser" />
                        </view>
                    </view>

                    <view class="form-item">
                        <view class="form-label">角色</view>
                        <view class="form-input">
                            <picker @change="onRoleChange" :value="roleIndex" :range="roles" range-key="name">
                                <view class="picker">
                                    {{ roles[roleIndex].name || '请选择角色' }}
                                </view>
                            </picker>
                        </view>
                    </view>

                    <view class="form-item" v-if="!isEditingUser">
                        <view class="form-label">初始密码</view>
                        <view class="form-input">
                            <input type="password" :value="userForm.password" @input="onPasswordInput" placeholder="请输入初始密码" />
                        </view>
                    </view>

                    <view class="form-item">
                        <view class="form-label">状态</view>
                        <view class="form-input">
                            <radio-group @change="onStatusChange">
                                <label class="radio">
                                    <radio value="active" :checked="userForm.status === 'active'" />
                                    启用
                                </label>
                                <label class="radio">
                                    <radio value="disabled" :checked="userForm.status === 'disabled'" />
                                    禁用
                                </label>
                                <label class="radio">
                                    <radio value="deleted" :checked="userForm.status === 'deleted'" />
                                    删除
                                </label>
                            </radio-group>
                        </view>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="footer-btn cancel-btn" @tap="closeUserModal">取消</button>
                    <button class="footer-btn confirm-btn" @tap="saveUser">保存</button>
                </view>
            </view>
        </view>

        <!-- 备份进度弹窗 -->
        <view class="modal" v-if="showBackupProgressModal">
            <view class="modal-mask"></view>
            <view class="modal-content progress-modal">
                <view class="modal-header">数据备份中</view>
                <view class="modal-body">
                    <view class="progress-info">正在备份{{ backupInfo.type }}数据...</view>
                    <view class="progress-bar-container">
                        <view class="progress-bar" :style="'width: ' + backupProgress + ';'"></view>
                    </view>
                    <view class="progress-percentage">{{ backupProgress }}%</view>
                </view>
            </view>
        </view>

        <!-- 恢复确认弹窗 -->
        <view class="modal" v-if="showRestoreConfirmModal">
            <view class="modal-mask" @tap="closeRestoreConfirmModal"></view>
            <view class="modal-content confirm-modal">
                <view class="modal-header">
                    恢复确认
                    <view class="close-btn" @tap="closeRestoreConfirmModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="confirm-message">确定要恢复此备份数据吗？恢复操作将覆盖当前数据，此操作不可撤销。</view>
                </view>
                <view class="modal-footer">
                    <button class="footer-btn cancel-btn" @tap="closeRestoreConfirmModal">取消</button>
                    <button class="footer-btn confirm-btn" @tap="confirmRestore">确定恢复</button>
                </view>
            </view>
        </view>

        <!-- 删除确认弹窗 -->
        <view class="modal" v-if="showDeleteConfirmModal">
            <view class="modal-mask" @tap="closeDeleteConfirmModal"></view>
            <view class="modal-content confirm-modal">
                <view class="modal-header">
                    删除确认
                    <view class="close-btn" @tap="closeDeleteConfirmModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="confirm-message">确定要删除此备份数据吗？删除后将无法恢复。</view>
                </view>
                <view class="modal-footer">
                    <button class="footer-btn cancel-btn" @tap="closeDeleteConfirmModal">取消</button>
                    <button class="footer-btn confirm-btn danger-btn" @tap="confirmDelete">确定删除</button>
                </view>
            </view>
        </view>

        <!-- 重置密码确认弹窗 -->
        <view class="modal" v-if="showResetPasswordModal">
            <view class="modal-mask" @tap="closeResetPasswordModal"></view>
            <view class="modal-content confirm-modal">
                <view class="modal-header">
                    重置密码
                    <view class="close-btn" @tap="closeResetPasswordModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="form-item">
                        <view class="form-label">新密码</view>
                        <view class="form-input">
                            <input type="password" :value="newPassword" @input="onNewPasswordInput" placeholder="请输入新密码" />
                        </view>
                    </view>
                    <view class="form-item">
                        <view class="form-label">确认密码</view>
                        <view class="form-input">
                            <input type="password" :value="confirmPassword" @input="onConfirmPasswordInput" placeholder="请再次输入新密码" />
                        </view>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="footer-btn cancel-btn" @tap="closeResetPasswordModal">取消</button>
                    <button class="footer-btn confirm-btn" @tap="confirmResetPassword">确定重置</button>
                </view>
            </view>
        </view>

        <view class="footer">
            <text>© SCNU IBC实验室预约管理系统</text>
        </view>

        <view class="modal" v-if="showDeleteUserConfirmModal">
            <view class="modal-mask" @tap="closeDeleteUserConfirmModal"></view>
            <view class="modal-content confirm-modal">
                <view class="modal-header">
                    删除用户
                    <view class="close-btn" @tap="closeDeleteUserConfirmModal">×</view>
                </view>
                <view class="modal-body">
                    <view class="confirm-message">确定要删除该用户吗？删除后不可恢复。</view>
                </view>
                <view class="modal-footer">
                    <button class="footer-btn cancel-btn" @tap="closeDeleteUserConfirmModal">取消</button>
                    <button class="footer-btn confirm-btn danger-btn" @tap="confirmDeleteUser">确认删除</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
// system-management.ts

const activeTab = ref<number>(0);

// 0: 系统设置, 1: 数据备份, 2: 用户管理
const settings = ref<any>({
    systemName: '实验室预约管理系统',
    schoolName: 'SCNU',
    logoUrl: '/static/images/天空实验室.png',
    reservationStartTime: '08:30',
    reservationEndTime: '22:00',
    advanceDays: 3,
    autoApproval: false,
    reservationNotification: true,
    approvalNotification: true,
    reminderNotification: true
});

const backupRecords = ref<any[]>([
    {
        id: 'backup001',
        name: '全部数据备份',
        time: '2023-09-15 15:30:45',
        size: '5.2MB'
    },
    {
        id: 'backup002',
        name: '实验室数据备份',
        time: '2023-09-10 09:15:22',
        size: '1.8MB'
    },
    {
        id: 'backup003',
        name: '课表数据备份',
        time: '2023-09-05 14:22:36',
        size: '2.3MB'
    }
]);

const backupProgress = ref<number>(0);

const backupInfo = ref<any>({
    type: ''
});

const showBackupProgressModal = ref<boolean>(false);
const showRestoreConfirmModal = ref<boolean>(false);
const showDeleteConfirmModal = ref<boolean>(false);
const currentBackupId = ref<string>('');

// 用户管理
const searchValue = ref<string>('');

const userType = ref<string>('all');

const users = ref<any[]>([
    {
        id: 'admin001',
        name: '系统管理员',
        //avatar: '/images/avatar/admin.png',
        avatar: '/static/images/icons/管理员_角色管理.png',
        role: 'admin',
        roleName: '管理员',
        status: 'active',
        lastLogin: '2023-09-15 10:30:22'
    },
    {
        id: 'T20230001',
        name: '张教授',
        //avatar: '/images/avatar/teacher1.png',
        avatar: '/static/images/icons/教师，领导中心.png',
        role: 'teacher',
        roleName: '教师',
        status: 'active',
        lastLogin: '2023-09-14 16:45:18'
    },
    {
        id: 'T20230002',
        name: '李副教授',
        avatar: '/static/images/icons/教师，领导中心.png',
        role: 'teacher',
        roleName: '教师',
        status: 'active',
        lastLogin: '2023-09-13 09:22:45'
    },
    {
        id: 'S20230001',
        name: '王同学',
        avatar: '/static/images/icons/学生.png',
        role: 'student',
        roleName: '学生',
        status: 'active',
        lastLogin: '2023-09-15 14:18:36'
    },
    {
        id: 'S20230002',
        name: '赵同学',
        //avatar: '/images/avatar/student2.png',
        avatar: '/static/images/icons/学生.png',
        role: 'student',
        roleName: '学生',
        status: 'disabled',
        lastLogin: '2023-09-10 11:05:52'
    }
]);

const filteredUsers = ref<any[]>([]);
const showUserModal = ref<boolean>(false);
const isEditingUser = ref<boolean>(false);

const userForm = ref<any>({
    id: '',
    name: '',
    role: '',
    password: '',
    status: 'active'
});

const roles = ref<any[]>([
    {
        id: 'admin',
        name: '管理员'
    },
    {
        id: 'teacher',
        name: '教师'
    },
    {
        id: 'student',
        name: '学生'
    }
]);

const roleIndex = ref<number>(0);

// 重置密码
const showResetPasswordModal = ref<boolean>(false);

const currentUserId = ref<string>('');
const newPassword = ref<string>('');
const confirmPassword = ref<string>('');
const showDeleteUserConfirmModal = ref<boolean>(false);
const deleteUserId = ref<string>('');
const name = ref<string>('');

onLoad(() => {
    filterUsers();
});

// 切换选项卡
const switchTab = (e: any) => {
    const activeTabVal = Number(e.currentTarget.dataset.index); // 修复：强制转为数字
    activeTab.value = activeTabVal;
};

// 系统设置相关方法
const onSystemNameInput = (e: any) => {
    settings.value.systemName = e.detail.value;
};

const onSchoolNameInput = (e: any) => {
    settings.value.schoolName = e.detail.value;
};

const uploadLogo = () => {
    uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
            // 上传图片到服务器的逻辑在这里实现
            // 这里仅做本地预览
            settings.value.logoUrl = res.tempFilePaths[0];
        }
    });
};

const onStartTimeChange = (e: any) => {
    settings.value.reservationStartTime = e.detail.value;
};

const onEndTimeChange = (e: any) => {
    settings.value.reservationEndTime = e.detail.value;
};

const onAdvanceDaysInput = (e: any) => {
    settings.value.advanceDays = e.detail.value;
};

const onAutoApprovalChange = (e: any) => {
    settings.value.autoApproval = e.detail.value;
};

const onReservationNotificationChange = (e: any) => {
    settings.value.reservationNotification = e.detail.value;
};

const onApprovalNotificationChange = (e: any) => {
    settings.value.approvalNotification = e.detail.value;
};

const onReminderNotificationChange = (e: any) => {
    settings.value.reminderNotification = e.detail.value;
};

const saveSettings = () => {
    // 保存设置到服务器的逻辑在这里实现
    uni.showToast({
        title: '设置已保存',
        icon: 'success'
    });
};

// 数据备份相关方法
const backupLabs = () => {
    startBackup('实验室');
};

const backupSchedules = () => {
    startBackup('课表');
};

const backupReservations = () => {
    startBackup('预约');
};

const backupUsers = () => {
    startBackup('用户');
};

const backupAll = () => {
    startBackup('全部');
};

const startBackup = (type: string) => {
    showBackupProgressModal.value = true;
    backupProgress.value = 0;
    backupInfo.value.type = type;

    // 模拟备份进度
    let progress = 0;
    const timer = setInterval(() => {
        progress += 10;
        if (progress > 100) {
            clearInterval(timer);
            progress = 100;

            // 模拟添加新备份记录
            const now = new Date();
            const newBackup = {
                id: 'backup' + now.getTime(),
                name: type + '数据备份',
                time: formatDateTime(now),
                size: (Math.random() * 5 + 1).toFixed(1) + 'MB'
            };
            backupProgress.value = progress;
            backupRecords.value = [newBackup, ...backupRecords.value];
            setTimeout(() => {
                showBackupProgressModal.value = false;
                uni.showToast({
                    title: '备份完成',
                    icon: 'success'
                });
            }, 500);
        } else {
            backupProgress.value = progress;
        }
    }, 300);
};

const restoreBackup = (e: any) => {
    const backupId = e.currentTarget.dataset.id;
    currentBackupId.value = backupId;
    showRestoreConfirmModal.value = true;
};

const closeRestoreConfirmModal = () => {
    showRestoreConfirmModal.value = false;
};

const confirmRestore = () => {
    // 恢复备份的逻辑在这里实现
    const backupId = currentBackupId.value;
    const backup = backupRecords.value.find((item) => item.id === backupId);
    if (backup) {
        showRestoreConfirmModal.value = false;
        uni.showLoading({
            title: '正在恢复数据'
        });

        // 模拟恢复过程
        setTimeout(() => {
            uni.hideLoading();
            uni.showToast({
                title: '恢复完成',
                icon: 'success'
            });
        }, 2000);
    }
};

const downloadBackup = (e: any) => {
    const backupId = e.currentTarget.dataset.id;
    const backup = backupRecords.value.find((item) => item.id === backupId);
    if (backup) {
        uni.showLoading({
            title: '准备下载'
        });

        // 模拟下载过程
        setTimeout(() => {
            uni.hideLoading();
            uni.showToast({
                title: '下载完成',
                icon: 'success'
            });
        }, 1500);
    }
};

const deleteBackup = (e: any) => {
    const backupId = e.currentTarget.dataset.id;
    currentBackupId.value = backupId;
    showDeleteConfirmModal.value = true;
};

const closeDeleteConfirmModal = () => {
    showDeleteConfirmModal.value = false;
};

const confirmDelete = () => {
    const backupId = currentBackupId.value;
    backupRecords.value = backupRecords.value.filter((item) => item.id !== backupId);
    showDeleteConfirmModal.value = false;
    uni.showToast({
        title: '已删除',
        icon: 'success'
    });
};

// 用户管理相关方法
const onSearchInput = (e: any) => {
    searchValue.value = e.detail.value;
};

const searchUsers = () => {
    filterUsers();
};

const filterUsers = () => {
    let filtered = users.value;

    // 按角色筛选
    if (userType.value !== 'all') {
        filtered = filtered.filter((user) => user.role === userType.value);
    }

    // 按搜索值筛选
    if (searchValue.value) {
        filtered = filtered.filter((user) => user.name.includes(searchValue.value) || user.id.includes(searchValue.value) || user.roleName.includes(searchValue.value));
    }
    filteredUsers.value = filtered;
};

const filterUsersByType = (e: any) => {
    userType.value = e.currentTarget.dataset.type;
    filterUsers();
};

const showAddUserModal = () => {
    showUserModal.value = true;
    isEditingUser.value = false;
    userForm.value = {
        id: '',
        name: '',
        role: 'student',
        password: '',
        status: 'active'
    };
    roleIndex.value = 2; // 默认为学生
};

const editUser = (e: any) => {
    const userId = e.currentTarget.dataset.id;
    const user = users.value.find((u) => u.id === userId);
    if (user) {
        const roleIndexVal = roles.value.findIndex((r) => r.id === user.role);
        showUserModal.value = true;
        isEditingUser.value = true;
        userForm.value = {
            id: user.id,
            name: user.name,
            role: user.role,
            password: '',
            status: user.status
        };
        roleIndex.value = roleIndexVal !== -1 ? roleIndexVal : 0;
    }
};

const closeUserModal = () => {
    showUserModal.value = false;
};

const onUserNameInput = (e: any) => {
    userForm.value.name = e.detail.value;
};

const onUserIdInput = (e: any) => {
    userForm.value.id = e.detail.value;
};

const onRoleChange = (e: any) => {
    const roleIndexVal = e.detail.value;
    const role = roles.value[roleIndexVal].id;
    roleIndex.value = roleIndexVal;
    userForm.value.role = role;
};

const onPasswordInput = (e: any) => {
    userForm.value.password = e.detail.value;
};

const onStatusChange = (e: any) => {
    userForm.value.status = e.detail.value;
};

const saveUser = () => {
    // 表单验证
    if (!userForm.value.name) {
        uni.showToast({
            title: '请输入用户名',
            icon: 'none'
        });
        return;
    }
    if (!userForm.value.id) {
        uni.showToast({
            title: '请输入用户ID',
            icon: 'none'
        });
        return;
    }
    if (!isEditingUser.value && !userForm.value.password) {
        uni.showToast({
            title: '请输入初始密码',
            icon: 'none'
        });
        return;
    }

    // 检查ID是否已存在（仅在添加新用户时检查）
    if (!isEditingUser.value && users.value.some((u) => u.id === userForm.value.id)) {
        uni.showToast({
            title: '用户ID已存在',
            icon: 'none'
        });
        return;
    }

    // 获取角色名称
    const role = roles.value.find((r) => r.id === userForm.value.role);
    const roleName = role ? role.name : '未知';

    // 新增：删除状态处理
    if (userForm.value.status === 'deleted') {
        showDeleteUserConfirmModal.value = true;
        deleteUserId.value = userForm.value.id;
        return;
    }
    if (isEditingUser.value) {
        // 更新现有用户
        const updatedUsers = users.value.map((user) => {
            if (user.id === userForm.value.id) {
                return {
                    ...user,
                    name: userForm.value.name,
                    role: userForm.value.role,
                    roleName,
                    status: userForm.value.status
                };
            }
            return user;
        });
        users.value = updatedUsers;
        showUserModal.value = false;
        filterUsers();
        uni.showToast({
            title: '用户已更新',
            icon: 'success'
        });
    } else {
        // 添加新用户
        const newUser = {
            id: userForm.value.id,
            name: userForm.value.name,
            avatar: '/images/avatar/default.png',
            role: userForm.value.role,
            roleName,
            status: userForm.value.status,
            lastLogin: ''
        };
        users.value = [...users.value, newUser];
        showUserModal.value = false;
        filterUsers();
        uni.showToast({
            title: '用户已添加',
            icon: 'success'
        });
    }
};

const resetPassword = (e: any) => {
    const userId = e.currentTarget.dataset.id;
    currentUserId.value = userId;
    showResetPasswordModal.value = true;
    newPassword.value = '';
    confirmPassword.value = '';
};

const closeResetPasswordModal = () => {
    showResetPasswordModal.value = false;
};

const onNewPasswordInput = (e: any) => {
    newPassword.value = e.detail.value;
};

const onConfirmPasswordInput = (e: any) => {
    confirmPassword.value = e.detail.value;
};

const confirmResetPassword = () => {
    if (!newPassword.value) {
        uni.showToast({
            title: '请输入新密码',
            icon: 'none'
        });
        return;
    }
    if (newPassword.value !== confirmPassword.value) {
        uni.showToast({
            title: '两次密码不一致',
            icon: 'none'
        });
        return;
    }

    // 重置密码的逻辑在这里实现
    showResetPasswordModal.value = false;
    uni.showToast({
        title: '密码已重置',
        icon: 'success'
    });
};

const toggleUserStatus = (e: any) => {
    const userId = e.currentTarget.dataset.id;
    users.value = users.value.map((user) => {
        if (user.id === userId) {
            return {
                ...user,
                status: user.status === 'active' ? 'disabled' : 'active'
            };
        }
        return user;
    });
    filterUsers();
    uni.showToast({
        title: '状态已更新',
        icon: 'success'
    });
};

const closeDeleteUserConfirmModal = () => {
    showDeleteUserConfirmModal.value = false;
    deleteUserId.value = '';
};

const confirmDeleteUser = () => {
    users.value = users.value.filter((u) => u.id !== deleteUserId.value);
    showDeleteUserConfirmModal.value = false;
    deleteUserId.value = '';
    showUserModal.value = false; // 删除后关闭编辑弹窗
    filterUsers();
    uni.showToast({
        title: '用户已删除',
        icon: 'success'
    });
};

// 辅助方法
const formatDateTime = (date: Date) => {
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
@import './admin-system-management.less';
</style>
