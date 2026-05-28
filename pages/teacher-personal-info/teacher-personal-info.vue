<template>
    <view>
        <!-- pages/teacher-personal-info/teacher-personal-info.wxml -->
        <navigation-bar title="个人信息" :back="true" color="white" background="#10B981"></navigation-bar>

        <view class="container">
            <!-- 头像区域 -->
            <view class="avatar-section">
                <view class="avatar-container">
                    <image class="avatar" src="/static/images/icons/teacher-avatar.svg" mode="aspectFit"></image>
                    <view class="edit-avatar" @tap="editAvatar">
                        <image src="/static/images/icons/edit-icon.png" mode="aspectFit"></image>
                    </view>
                </view>
                <text class="username">{{ teacherInfo.name }}</text>
            </view>

            <!-- 信息列表 -->
            <view class="info-list">
                <view class="info-item">
                    <view class="info-label">姓名</view>
                    <view class="info-content">
                        <text class="info-value">{{ teacherInfo.name }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editName" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">工号</view>
                    <view class="info-content">
                        <text class="info-value">{{ teacherInfo.teacherId }}</text>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">性别</view>
                    <view class="info-content">
                        <text class="info-value">{{ teacherInfo.gender === 'male' ? '男' : '女' }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editGender" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">所属学院</view>
                    <view class="info-content">
                        <text class="info-value">{{ teacherInfo.college }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editCollege" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">部门</view>
                    <view class="info-content">
                        <text class="info-value">{{ teacherInfo.department }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editDepartment" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">手机号</view>
                    <view class="info-content">
                        <text class="info-value">{{ teacherInfo.phone }}</text>
                        <image class="edit-icon" src="/static/images/icons/edit-icon.png" @tap="editPhone" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="info-item">
                    <view class="info-label">注册时间</view>
                    <view class="info-content">
                        <text class="info-value">{{ teacherInfo.registerTime || '2024-01-01' }}</text>
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
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
// pages/teacher-personal-info/teacher-personal-info.ts

const teacherInfo = ref<any>({
    name: '',
    teacherId: '',
    gender: '',
    college: '',
    department: '',
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
const pickerOptions = ref<string[]>([]);
const pickerIndex = ref(-1);
const colleges = ref<string[]>([
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

/**
 * 生命周期函数--监听页面加载
 */
onLoad(() => {
    loadTeacherInfo();
});

/**
 * 页面返回时检查是否有未保存的修改
 */
onUnload(() => {
    const hasChanges = JSON.stringify(teacherInfo.value) !== JSON.stringify(originalInfo.value);
    if (hasChanges) {
        // 这里可以提示用户是否保存修改
        console.log('有未保存的修改');
    }
});

/**
 * 加载教师信息
 */
const loadTeacherInfo = () => {
    try {
        let _teacherInfo = uni.getStorageSync('teacherInfo');
        if (_teacherInfo) {
            // 尝试补全 password 字段
            if (!_teacherInfo.password && _teacherInfo.teacherId) {
                const registeredTeachers = uni.getStorageSync('registeredTeachers') || [];
                const found = registeredTeachers.find((teacher: any) => teacher.teacherId === _teacherInfo.teacherId);
                if (found && found.password) {
                    _teacherInfo.password = found.password;
                }
            }
            teacherInfo.value = _teacherInfo;
            originalInfo.value = JSON.parse(JSON.stringify(_teacherInfo));
        }
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载教师信息失败:', error);
        uni.showToast({
            title: '加载信息失败',
            icon: 'error'
        });
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
            // 这里可以上传图片到服务器，目前只是模拟
            uni.showToast({
                title: '头像更新成功',
                icon: 'success'
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
    openEditModal('姓名', 'input', teacherInfo.value.name);
};

/**
 * 编辑性别
 */
const editGender = () => {
    openEditModal('性别', 'gender', teacherInfo.value.gender);
};

/**
 * 编辑学院
 */
const editCollege = () => {
    const currentIndex = colleges.value.indexOf(teacherInfo.value.college);
    pickerOptions.value = colleges.value;
    pickerIndex.value = currentIndex;
    openEditModal('所属学院', 'picker', teacherInfo.value.college);
};

/**
 * 编辑部门
 */
const editDepartment = () => {
    openEditModal('部门', 'input', teacherInfo.value.department);
};

/**
 * 编辑手机号
 */
const editPhone = () => {
    openEditModal('手机号', 'input', teacherInfo.value.phone);
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
            teacherInfo.value.name = _editValue;
            break;
        case '性别':
            teacherInfo.value.gender = _editValue;
            break;
        case '所属学院':
            teacherInfo.value.college = _editValue;
            break;
        case '部门':
            teacherInfo.value.department = _editValue;
            break;
        case '手机号':
            teacherInfo.value.phone = _editValue;
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
const saveInfo = () => {
    try {
        // 保存到本地存储
        uni.setStorageSync('teacherInfo', teacherInfo.value);

        // 更新注册教师列表中的信息
        const registeredTeachers = uni.getStorageSync('registeredTeachers') || [];
        const teacherIndex = registeredTeachers.findIndex((teacher: any) => teacher.teacherId === teacherInfo.value.teacherId);
        if (teacherIndex !== -1) {
            registeredTeachers[teacherIndex] = {
                ...registeredTeachers[teacherIndex],
                ...teacherInfo.value
            };
            uni.setStorageSync('registeredTeachers', registeredTeachers);
        }
        uni.showToast({
            title: '保存成功',
            icon: 'success'
        });

        // 更新原始信息
        originalInfo.value = JSON.parse(JSON.stringify(teacherInfo.value));
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('保存信息失败:', error);
        uni.showToast({
            title: '保存失败',
            icon: 'error'
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

const confirmPasswordChange = () => {
    const _oldPassword = oldPassword.value;
    const _newPassword = newPassword.value;
    const _confirmNewPassword = confirmNewPassword.value;
    if (!_oldPassword || !_newPassword || !_confirmNewPassword) {
        uni.showToast({
            title: '请填写完整',
            icon: 'none'
        });
        return;
    }
    if (_oldPassword !== teacherInfo.value.password) {
        uni.showToast({
            title: '原密码错误',
            icon: 'none'
        });
        return;
    }
    if (_newPassword.length < 6) {
        uni.showToast({
            title: '新密码至少6位',
            icon: 'none'
        });
        return;
    }
    if (_newPassword !== _confirmNewPassword) {
        uni.showToast({
            title: '两次新密码不一致',
            icon: 'none'
        });
        return;
    }
    // 修改密码
    teacherInfo.value.password = _newPassword;
    showPasswordModal.value = false;
    // 同步到本地存储
    uni.setStorageSync('teacherInfo', teacherInfo.value);
    uni.showToast({
        title: '密码修改成功',
        icon: 'success'
    });
};
</script>
<style lang="less">
@import './teacher-personal-info.less';
</style>
