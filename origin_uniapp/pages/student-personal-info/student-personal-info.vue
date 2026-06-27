<template>
    <view class="page-wrapper">
        <!-- pages/student-personal-info/student-personal-info.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="个人信息" color="white" background="#3B82F6" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <app-navigation-bar title="个人信息" :back="true" color="white" background="#3B82F6"></app-navigation-bar>
        <!-- #endif -->

        <view class="container">
            <!-- 头像区域 -->
            <view class="avatar-section">
                <view class="avatar-container">
                    <image class="avatar" src="/static/images/icons/user-avatar.svg" mode="aspectFit"></image>
                    <view class="edit-avatar" @tap="editAvatar">
                        <image src="/static/images/icons/edit-icon.png" mode="aspectFit"></image>
                    </view>
                </view>
                <text class="username">{{ studentInfo.name }}</text>
            </view>

            <!-- 信息列表 -->
            <view class="info-list">
                <view class="info-item">
                    <view class="info-label">姓名</view>
                    <view class="info-content">
                        <text class="info-value">{{ studentInfo.name }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editName" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">学号</view>
                    <view class="info-content">
                        <text class="info-value">{{ studentInfo.studentId }}</text>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">性别</view>
                    <view class="info-content">
                        <text class="info-value">{{ studentInfo.gender === 'male' ? '男' : studentInfo.gender === 'female' ? '女' : '未设置' }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editGender" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">所属学院</view>
                    <view class="info-content">
                        <text class="info-value">{{ studentInfo.college }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editCollege" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">年级专业</view>
                    <view class="info-content">
                        <text class="info-value">{{ studentInfo.major }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editMajor" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">手机号</view>
                    <view class="info-content">
                        <text class="info-value">{{ studentInfo.phone }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editPhone" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">注册时间</view>
                    <view class="info-content">
                        <text class="info-value">{{ formattedRegisterTime }}</text>
                    </view>
                </view>
            </view>

            <!-- 操作按钮 -->
            <view class="action-buttons">
                <button class="save-btn" @tap="saveInfo">保存修改</button>
                <button class="reset-btn" @tap="resetPassword">修改密码</button>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>

        <!-- 编辑弹窗 -->
        <view v-if="showEditModal" class="modal-overlay" @tap="closeEditModal">
            <view class="modal-content" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">编辑{{ editField }}</text>
                    <view class="close-btn" @tap="closeEditModal">×</view>
                </view>
                <view class="modal-body">
                    <input v-if="editType === 'input'" class="edit-input" :value="editValue" @input="onEditInput" :placeholder="'请输入' + editField" />
                    <picker v-if="editType === 'picker'" mode="selector" :range="pickerOptions" @change="onPickerChange" :value="pickerIndex">
                        <view class="picker-display">{{ editValue || '请选择' + editField }}</view>
                    </picker>
                    <view v-if="editType === 'gender'" class="gender-options">
                        <view :class="'gender-option ' + (editValue === 'male' ? 'active' : '')" @tap="selectGender" data-gender="male">
                            <text>男</text>
                        </view>
                        <view :class="'gender-option ' + (editValue === 'female' ? 'active' : '')" @tap="selectGender" data-gender="female">
                            <text>女</text>
                        </view>
                    </view>
                </view>
                <view class="modal-footer">
                    <button class="cancel-btn" @tap="closeEditModal">取消</button>
                    <button class="confirm-btn" @tap="confirmEdit">确认</button>
                </view>
            </view>
        </view>

        <!-- 修改密码弹窗 -->
        <view v-if="showPasswordModal" class="modal-overlay" @tap="closePasswordModal">
            <view class="modal-content" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">修改密码</text>
                    <view class="close-btn" @tap="closePasswordModal">×</view>
                </view>
                <view class="modal-body">
                    <input class="edit-input" password placeholder="请输入原密码" :value="oldPassword" @input="onOldPasswordInput" />
                    <input class="edit-input" password placeholder="请输入新密码（至少6位）" :value="newPassword" @input="onNewPasswordInput" style="margin-top: 20rpx" />
                    <input class="edit-input" password placeholder="请再次输入新密码" :value="confirmNewPassword" @input="onConfirmNewPasswordInput" style="margin-top: 20rpx" />
                </view>
                <view class="modal-footer">
                    <button class="cancel-btn" @tap="closePasswordModal">取消</button>
                    <button class="confirm-btn" @tap="confirmPasswordChange">确认</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onUnload } from '@dcloudio/uni-app';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
import { fetchCurrentUser } from '@/api/auth';
import { getStudentProfile, updateStudentProfile, changePassword } from '@/api/student';
import { getAccessToken, getStoredRole, getStoredUser, hasCompleteUserProfile, saveAuthSession } from '@/api/storage';
// pages/student-personal-info/student-personal-info.ts

const studentInfo = ref<any>({
    name: '',
    studentId: '',
    gender: '',
    college: '',
    major: '',
    phone: '',
    registerTime: '',
    password: ''
});
const originalInfo = ref<any>({});
// 保存原始信息，用于取消编辑时恢复
const showEditModal = ref(false);
const editField = ref('');
const editType = ref('');
// 'input', 'picker', 'gender'
const editValue = ref('');
const pickerOptions = ref<any[]>([]);
const pickerIndex = ref(-1);
const colleges = ref<any[]>([
    '计算机学院',
    '软件学院',
    '信息工程学院',
    '电子信息学院',
    '数学科学学院',
    '物理与电信工程学院',
    '化学学院',
    '生命科学学院',
    '地理科学学院',
    '心理学院',
    '教育科学学院',
    '外国语言文化学院',
    '文学院',
    '历史文化学院',
    '马克思主义学院',
    '法学院',
    '经济与管理学院',
    '公共管理学院',
    '旅游管理学院',
    '体育科学学院',
    '音乐学院',
    '美术学院'
]);
const showPasswordModal = ref(false);
const oldPassword = ref('');
const newPassword = ref('');
const confirmNewPassword = ref('');
const formattedRegisterTime = ref('2024-01-01');

/**
 * 生命周期函数--监听页面加载
 */
onLoad(() => {
    loadStudentInfo();
    setTimeout(() => {
        syncCurrentStudent();
    }, 0);
});

/**
 * 把后端返回的 profile 适配成模板使用的形状
 */
const normalizeProfile = (profile: any) => ({
    name: profile.name || profile.realName || '',
    studentId: profile.studentId || profile.accountNo || '',
    gender: profile.gender || '',
    college: profile.college || '',
    major: profile.major || '',
    phone: profile.phone || '',
    email: profile.email || '',
    registerTime: profile.registerTime || profile.createTime || '',
    password: profile.password || ''
});

/**
 * 页面返回时检查是否有未保存的修改
 */
onUnload(() => {
    const hasChanges = JSON.stringify(studentInfo.value) !== JSON.stringify(originalInfo.value);
    if (hasChanges) {
        // 这里可以提示用户是否保存修改
        console.log('有未保存的修改');
    }
});

/**
 * 格式化注册时间
 */
const formatRegisterTime = (registerTime: string) => {
    if (!registerTime) {
        return '2024-01-01';
    }
    try {
        const date = new Date(registerTime);
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('格式化注册时间失败:', error);
        return '2024-01-01';
    }
};

/**
 * 加载学生信息
 */
const loadStudentInfo = async () => {
    try {
        const profile: any = await getStudentProfile();
        const normalizedInfo = normalizeProfile(profile || {});

        studentInfo.value = normalizedInfo;
        originalInfo.value = JSON.parse(JSON.stringify(normalizedInfo));
        formattedRegisterTime.value = formatRegisterTime(normalizedInfo.registerTime);
    } catch (error: any) {
        console.error('加载学生信息失败:', error);
        formattedRegisterTime.value = '2024-01-01';
        uni.showToast({
            title: error?.data?.message || '加载失败',
            icon: 'none'
        });
    }
};

const syncCurrentStudent = async () => {
    const storedRole = getStoredRole();
    const storedUser = getStoredUser();
    if (storedRole && storedRole !== 'student') {
        uni.reLaunch({ url: '/pages/login-select/login-select' });
        return;
    }

    if (hasCompleteUserProfile(storedUser)) {
        return;
    }

    try {
        const user: any = await fetchCurrentUser();
        const latestInfo = {
            ...studentInfo.value,
            name: user.realName || '',
            studentId: user.accountNo || '',
            gender: user.gender || '',
            phone: user.phone || '',
            email: user.email || '',
            college: user.college || '',
            major: user.major || '',
            role: user.role,
            status: user.status
        };

        studentInfo.value = latestInfo;
        originalInfo.value = JSON.parse(JSON.stringify(latestInfo));
        const accessToken = getAccessToken();
        if (accessToken) {
            saveAuthSession(accessToken, {
                ...user,
                registerTime: latestInfo.registerTime
            });
        }
    } catch (error) {
        console.error('同步学生信息失败:', error);
    }
};

/**
 * 编辑头像
 */
const editAvatar = () => {
    uni.showActionSheet({
        itemList: ['拍照', '从相册选择'],
        success: (res) => {
            if (res.tapIndex === 0) {
                chooseImage('camera');
            } else if (res.tapIndex === 1) {
                chooseImage('album');
            }
        }
    });
};

/**
 * 选择图片
 */
const chooseImage = (sourceType: 'camera' | 'album') => {
    uni.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: [sourceType],
        success: (res) => {
            // 暂无对象存储，头像不做真实上传/持久化，避免误导性的“成功”提示
            uni.showToast({
                title: '当前为演示，头像暂不保存',
                icon: 'none'
            });
        },
        fail: (error) => {
            console.error('选择图片失败:', error);
        }
    });
};

/**
 * 编辑姓名
 */
const editName = () => {
    openEditModal('姓名', 'input', studentInfo.value.name);
};

/**
 * 编辑性别
 */
const editGender = () => {
    openEditModal('性别', 'gender', studentInfo.value.gender);
};

/**
 * 编辑学院
 */
const editCollege = () => {
    const currentIndex = colleges.value.indexOf(studentInfo.value.college);
    pickerOptions.value = colleges.value;
    pickerIndex.value = currentIndex;
    openEditModal('所属学院', 'picker', studentInfo.value.college);
};

/**
 * 编辑专业
 */
const editMajor = () => {
    openEditModal('年级专业', 'input', studentInfo.value.major);
};

/**
 * 编辑手机号
 */
const editPhone = () => {
    openEditModal('手机号', 'input', studentInfo.value.phone);
};

/**
 * 打开编辑弹窗
 */
const openEditModal = (field: string, type: string, value: string) => {
    showEditModal.value = true;
    editField.value = field;
    editType.value = type;
    editValue.value = value;
};

/**
 * 关闭编辑弹窗
 */
const closeEditModal = () => {
    showEditModal.value = false;
    editField.value = '';
    editType.value = '';
    editValue.value = '';
};

/**
 * 阻止事件冒泡
 */
const stopPropagation = () => {
    // 阻止点击弹窗内容时关闭弹窗
};

/**
 * 编辑输入事件
 */
const onEditInput = (e: any) => {
    editValue.value = e.detail.value;
};

/**
 * 选择器变化事件
 */
const onPickerChange = (e: any) => {
    const index = e.detail.value;
    pickerIndex.value = index;
    editValue.value = pickerOptions.value[index];
};

/**
 * 选择性别
 */
const selectGender = (e: any) => {
    const gender = e.currentTarget.dataset.gender;
    editValue.value = gender;
};

/**
 * 确认编辑
 */
const confirmEdit = () => {
    const _editField = editField.value;
    const _editValue = editValue.value;
    if (!_editValue.trim()) {
        uni.showToast({
            title: `请输入${_editField}`,
            icon: 'error'
        });
        return;
    }

    // 验证手机号格式
    if (_editField === '手机号') {
        const phoneRegex = /^1[3-9]\d{9}$/;
        if (!phoneRegex.test(_editValue)) {
            uni.showToast({
                title: '手机号格式不正确',
                icon: 'error'
            });
            return;
        }
    }

    // 更新对应字段
    switch (_editField) {
        case '姓名':
            studentInfo.value.name = _editValue;
            break;
        case '性别':
            studentInfo.value.gender = _editValue;
            break;
        case '所属学院':
            studentInfo.value.college = _editValue;
            break;
        case '年级专业':
            studentInfo.value.major = _editValue;
            break;
        case '手机号':
            studentInfo.value.phone = _editValue;
            break;
    }
    closeEditModal();
    uni.showToast({
        title: '修改成功',
        icon: 'success'
    });
};

/**
 * 保存信息
 */
const saveInfo = async () => {
    try {
        const payload = {
            name: studentInfo.value.name,
            gender: studentInfo.value.gender,
            college: studentInfo.value.college,
            major: studentInfo.value.major,
            phone: studentInfo.value.phone,
            email: studentInfo.value.email || ''
        };
        const updated: any = await updateStudentProfile(payload);
        if (updated && typeof updated === 'object') {
            studentInfo.value = normalizeProfile({ ...studentInfo.value, ...updated });
            formattedRegisterTime.value = formatRegisterTime(studentInfo.value.registerTime);
        }

        // 同步会话缓存，保持鉴权信息可用
        const accessToken = getAccessToken();
        const storedUser = getStoredUser() || {};
        const mergedUser = {
            ...storedUser,
            accountNo: studentInfo.value.studentId,
            realName: studentInfo.value.name,
            phone: studentInfo.value.phone,
            email: studentInfo.value.email || storedUser.email || '',
            gender: studentInfo.value.gender,
            college: studentInfo.value.college,
            major: studentInfo.value.major,
            registerTime: studentInfo.value.registerTime
        };
        if (accessToken) {
            saveAuthSession(accessToken, mergedUser);
        }

        uni.showToast({
            title: '保存成功',
            icon: 'success'
        });

        // 更新原始信息
        originalInfo.value = JSON.parse(JSON.stringify(studentInfo.value));
    } catch (error: any) {
        console.error('保存信息失败:', error);
        uni.showToast({
            title: error?.data?.message || '加载失败',
            icon: 'none'
        });
    }
};

/**
 * 修改密码
 */
const resetPassword = () => {
    showPasswordModal.value = true;
    oldPassword.value = '';
    newPassword.value = '';
    confirmNewPassword.value = '';
};

const closePasswordModal = () => {
    showPasswordModal.value = false;
    oldPassword.value = '';
    newPassword.value = '';
    confirmNewPassword.value = '';
};

const onOldPasswordInput = (e: any) => {
    oldPassword.value = e.detail.value;
};

const onNewPasswordInput = (e: any) => {
    newPassword.value = e.detail.value;
};

const onConfirmNewPasswordInput = (e: any) => {
    confirmNewPassword.value = e.detail.value;
};

const confirmPasswordChange = async () => {
    if (!oldPassword.value) {
        uni.showToast({ title: '请输入原密码', icon: 'none' });
        return;
    }
    if (!newPassword.value || newPassword.value.length < 6) {
        uni.showToast({ title: '新密码至少6位', icon: 'none' });
        return;
    }
    if (newPassword.value !== confirmNewPassword.value) {
        uni.showToast({ title: '两次输入的新密码不一致', icon: 'none' });
        return;
    }

    try {
        await changePassword({
            oldPassword: oldPassword.value,
            newPassword: newPassword.value
        });
        closePasswordModal();
        uni.showToast({
            title: '密码修改成功',
            icon: 'success'
        });
    } catch (error: any) {
        console.error('修改密码失败:', error);
        uni.showToast({
            title: error?.data?.message || '加载失败',
            icon: 'none'
        });
    }
};
</script>
<style lang="less">
@import './student-personal-info.less';
</style>
