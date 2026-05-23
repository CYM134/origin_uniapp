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

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
// system-management.ts
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            activeTab: 0,

            // 0: 系统设置, 1: 数据备份, 2: 用户管理
            settings: {
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
            },

            backupRecords: [
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
            ],

            backupProgress: 0,

            backupInfo: {
                type: ''
            },

            showBackupProgressModal: false,
            showRestoreConfirmModal: false,
            showDeleteConfirmModal: false,
            currentBackupId: '',

            // 用户管理
            searchValue: '',

            userType: 'all',

            users: [
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
            ],

            filteredUsers: [] as any[],
            showUserModal: false,
            isEditingUser: false,

            userForm: {
                id: '',
                name: '',
                role: '',
                password: '',
                status: 'active'
            },

            roles: [
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
            ],

            roleIndex: 0,

            // 重置密码
            showResetPasswordModal: false,

            currentUserId: '',
            newPassword: '',
            confirmPassword: '',
            showDeleteUserConfirmModal: false,
            deleteUserId: '',
            name: ''
        };
    },
    onLoad() {
        this.filterUsers();
    },
    methods: {
        // 切换选项卡
        switchTab(e: any) {
            const activeTab = Number(e.currentTarget.dataset.index); // 修复：强制转为数字
            this.setData({
                activeTab
            });
        },

        // 系统设置相关方法
        onSystemNameInput(e: any) {
            this.setData({
                'settings.systemName': e.detail.value
            });
        },

        onSchoolNameInput(e: any) {
            this.setData({
                'settings.schoolName': e.detail.value
            });
        },

        uploadLogo() {
            uni.chooseImage({
                count: 1,
                sizeType: ['compressed'],
                sourceType: ['album', 'camera'],
                success: (res) => {
                    // 上传图片到服务器的逻辑在这里实现
                    // 这里仅做本地预览
                    this.setData({
                        'settings.logoUrl': res.tempFilePaths[0]
                    });
                }
            });
        },

        onStartTimeChange(e: any) {
            this.setData({
                'settings.reservationStartTime': e.detail.value
            });
        },

        onEndTimeChange(e: any) {
            this.setData({
                'settings.reservationEndTime': e.detail.value
            });
        },

        onAdvanceDaysInput(e: any) {
            this.setData({
                'settings.advanceDays': e.detail.value
            });
        },

        onAutoApprovalChange(e: any) {
            this.setData({
                'settings.autoApproval': e.detail.value
            });
        },

        onReservationNotificationChange(e: any) {
            this.setData({
                'settings.reservationNotification': e.detail.value
            });
        },

        onApprovalNotificationChange(e: any) {
            this.setData({
                'settings.approvalNotification': e.detail.value
            });
        },

        onReminderNotificationChange(e: any) {
            this.setData({
                'settings.reminderNotification': e.detail.value
            });
        },

        saveSettings() {
            // 保存设置到服务器的逻辑在这里实现
            uni.showToast({
                title: '设置已保存',
                icon: 'success'
            });
        },

        // 数据备份相关方法
        backupLabs() {
            this.startBackup('实验室');
        },

        backupSchedules() {
            this.startBackup('课表');
        },

        backupReservations() {
            this.startBackup('预约');
        },

        backupUsers() {
            this.startBackup('用户');
        },

        backupAll() {
            this.startBackup('全部');
        },

        startBackup(type: string) {
            this.setData({
                showBackupProgressModal: true,
                backupProgress: 0,
                'backupInfo.type': type
            });

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
                        time: this.formatDateTime(now),
                        size: (Math.random() * 5 + 1).toFixed(1) + 'MB'
                    };
                    const backupRecords = [newBackup, ...this.backupRecords];
                    this.setData({
                        backupProgress: progress,
                        backupRecords
                    });
                    setTimeout(() => {
                        this.setData({
                            showBackupProgressModal: false
                        });
                        uni.showToast({
                            title: '备份完成',
                            icon: 'success'
                        });
                    }, 500);
                } else {
                    this.setData({
                        backupProgress: progress
                    });
                }
            }, 300);
        },

        restoreBackup(e: any) {
            const backupId = e.currentTarget.dataset.id;
            this.setData({
                currentBackupId: backupId,
                showRestoreConfirmModal: true
            });
        },

        closeRestoreConfirmModal() {
            this.setData({
                showRestoreConfirmModal: false
            });
        },

        confirmRestore() {
            // 恢复备份的逻辑在这里实现
            const backupId = this.currentBackupId;
            const backup = this.backupRecords.find((item) => item.id === backupId);
            if (backup) {
                this.setData({
                    showRestoreConfirmModal: false
                });
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
        },

        downloadBackup(e: any) {
            const backupId = e.currentTarget.dataset.id;
            const backup = this.backupRecords.find((item) => item.id === backupId);
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
        },

        deleteBackup(e: any) {
            const backupId = e.currentTarget.dataset.id;
            this.setData({
                currentBackupId: backupId,
                showDeleteConfirmModal: true
            });
        },

        closeDeleteConfirmModal() {
            this.setData({
                showDeleteConfirmModal: false
            });
        },

        confirmDelete() {
            const backupId = this.currentBackupId;
            const backupRecords = this.backupRecords.filter((item) => item.id !== backupId);
            this.setData({
                backupRecords,
                showDeleteConfirmModal: false
            });
            uni.showToast({
                title: '已删除',
                icon: 'success'
            });
        },

        // 用户管理相关方法
        onSearchInput(e: any) {
            this.setData({
                searchValue: e.detail.value
            });
        },

        searchUsers() {
            this.filterUsers();
        },

        filterUsers() {
            const { users, searchValue, userType } = this;
            let filtered = users;

            // 按角色筛选
            if (userType !== 'all') {
                filtered = filtered.filter((user) => user.role === userType);
            }

            // 按搜索值筛选
            if (searchValue) {
                filtered = filtered.filter((user) => user.name.includes(searchValue) || user.id.includes(searchValue) || user.roleName.includes(searchValue));
            }
            this.setData({
                filteredUsers: filtered
            });
        },

        filterUsersByType(e: any) {
            const userType = e.currentTarget.dataset.type;
            this.setData(
                {
                    userType
                },
                () => {
                    this.filterUsers();
                }
            );
        },

        showAddUserModal() {
            this.setData({
                showUserModal: true,
                isEditingUser: false,
                userForm: {
                    id: '',
                    name: '',
                    role: 'student',
                    password: '',
                    status: 'active'
                },
                roleIndex: 2 // 默认为学生
            });
        },

        editUser(e: any) {
            const userId = e.currentTarget.dataset.id;
            const user = this.users.find((u) => u.id === userId);
            if (user) {
                const roleIndex = this.roles.findIndex((r) => r.id === user.role);
                this.setData({
                    showUserModal: true,
                    isEditingUser: true,
                    userForm: {
                        id: user.id,
                        name: user.name,
                        role: user.role,
                        password: '',
                        status: user.status
                    },
                    roleIndex: roleIndex !== -1 ? roleIndex : 0
                });
            }
        },

        closeUserModal() {
            this.setData({
                showUserModal: false
            });
        },

        onUserNameInput(e: any) {
            this.setData({
                'userForm.name': e.detail.value
            });
        },

        onUserIdInput(e: any) {
            this.setData({
                'userForm.id': e.detail.value
            });
        },

        onRoleChange(e: any) {
            const roleIndex = e.detail.value;
            const role = this.roles[roleIndex].id;
            this.setData({
                roleIndex,
                'userForm.role': role
            });
        },

        onPasswordInput(e: any) {
            this.setData({
                'userForm.password': e.detail.value
            });
        },

        onStatusChange(e: any) {
            this.setData({
                'userForm.status': e.detail.value
            });
        },

        saveUser() {
            const { userForm, isEditingUser, users } = this;

            // 表单验证
            if (!userForm.name) {
                uni.showToast({
                    title: '请输入用户名',
                    icon: 'none'
                });
                return;
            }
            if (!userForm.id) {
                uni.showToast({
                    title: '请输入用户ID',
                    icon: 'none'
                });
                return;
            }
            if (!isEditingUser && !userForm.password) {
                uni.showToast({
                    title: '请输入初始密码',
                    icon: 'none'
                });
                return;
            }

            // 检查ID是否已存在（仅在添加新用户时检查）
            if (!isEditingUser && users.some((u) => u.id === userForm.id)) {
                uni.showToast({
                    title: '用户ID已存在',
                    icon: 'none'
                });
                return;
            }

            // 获取角色名称
            const role = this.roles.find((r) => r.id === userForm.role);
            const roleName = role ? role.name : '未知';

            // 新增：删除状态处理
            if (userForm.status === 'deleted') {
                this.setData({
                    showDeleteUserConfirmModal: true,
                    deleteUserId: userForm.id
                });
                return;
            }
            if (isEditingUser) {
                // 更新现有用户
                const updatedUsers = users.map((user) => {
                    if (user.id === userForm.id) {
                        return {
                            ...user,
                            name: userForm.name,
                            role: userForm.role,
                            roleName,
                            status: userForm.status
                        };
                    }
                    return user;
                });
                this.setData(
                    {
                        users: updatedUsers,
                        showUserModal: false
                    },
                    () => {
                        this.filterUsers();
                        uni.showToast({
                            title: '用户已更新',
                            icon: 'success'
                        });
                    }
                );
            } else {
                // 添加新用户
                const newUser = {
                    id: userForm.id,
                    name: userForm.name,
                    avatar: '/images/avatar/default.png',
                    role: userForm.role,
                    roleName,
                    status: userForm.status,
                    lastLogin: ''
                };
                const updatedUsers = [...users, newUser];
                this.setData(
                    {
                        users: updatedUsers,
                        showUserModal: false
                    },
                    () => {
                        this.filterUsers();
                        uni.showToast({
                            title: '用户已添加',
                            icon: 'success'
                        });
                    }
                );
            }
        },

        resetPassword(e: any) {
            const userId = e.currentTarget.dataset.id;
            this.setData({
                currentUserId: userId,
                showResetPasswordModal: true,
                newPassword: '',
                confirmPassword: ''
            });
        },

        closeResetPasswordModal() {
            this.setData({
                showResetPasswordModal: false
            });
        },

        onNewPasswordInput(e: any) {
            this.setData({
                newPassword: e.detail.value
            });
        },

        onConfirmPasswordInput(e: any) {
            this.setData({
                confirmPassword: e.detail.value
            });
        },

        confirmResetPassword() {
            const { newPassword, confirmPassword } = this;
            if (!newPassword) {
                uni.showToast({
                    title: '请输入新密码',
                    icon: 'none'
                });
                return;
            }
            if (newPassword !== confirmPassword) {
                uni.showToast({
                    title: '两次密码不一致',
                    icon: 'none'
                });
                return;
            }

            // 重置密码的逻辑在这里实现
            this.setData({
                showResetPasswordModal: false
            });
            uni.showToast({
                title: '密码已重置',
                icon: 'success'
            });
        },

        toggleUserStatus(e: any) {
            const userId = e.currentTarget.dataset.id;
            const users = this.users.map((user) => {
                if (user.id === userId) {
                    return {
                        ...user,
                        status: user.status === 'active' ? 'disabled' : 'active'
                    };
                }
                return user;
            });
            this.setData(
                {
                    users
                },
                () => {
                    this.filterUsers();
                    uni.showToast({
                        title: '状态已更新',
                        icon: 'success'
                    });
                }
            );
        },

        closeDeleteUserConfirmModal() {
            this.setData({
                showDeleteUserConfirmModal: false,
                deleteUserId: ''
            });
        },

        confirmDeleteUser() {
            const { deleteUserId, users } = this;
            const updatedUsers = users.filter((u) => u.id !== deleteUserId);
            this.setData(
                {
                    users: updatedUsers,
                    showDeleteUserConfirmModal: false,
                    deleteUserId: '',
                    showUserModal: false // 删除后关闭编辑弹窗
                },
                () => {
                    this.filterUsers();
                    uni.showToast({
                        title: '用户已删除',
                        icon: 'success'
                    });
                }
            );
        },

        // 辅助方法
        formatDateTime(date: Date) {
            const year = date.getFullYear();
            const month = (date.getMonth() + 1).toString().padStart(2, '0');
            const day = date.getDate().toString().padStart(2, '0');
            const hours = date.getHours().toString().padStart(2, '0');
            const minutes = date.getMinutes().toString().padStart(2, '0');
            const seconds = date.getSeconds().toString().padStart(2, '0');
            return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
        }
    }
});
</script>
<style lang="less">
@import './admin-system-management.less';
</style>
