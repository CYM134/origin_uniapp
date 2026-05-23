<template>
    <view>
        <!-- pages/teacher-personal-info/teacher-personal-info.wxml -->
        <navigation-bar title="个人信息" :back="true" color="white" background="#3a7bd5"></navigation-bar>

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

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
// pages/teacher-personal-info/teacher-personal-info.ts
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            teacherInfo: {
                name: '',
                teacherId: '',
                gender: '',
                college: '',
                department: '',
                phone: '',
                registerTime: '',
                password: ''
            },
            originalInfo: {},
            // 保存原始信息，用于取消编辑时恢复
            showEditModal: false,
            editField: '',
            editType: '',
            // 'input', 'picker', 'gender'
            editValue: '',
            pickerOptions: [] as string[],
            pickerIndex: -1,
            colleges: [
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
            ],
            showPasswordModal: false,
            oldPassword: '',
            newPassword: '',
            confirmNewPassword: ''
        };
    },
    /**
     * 生命周期函数--监听页面加载
     */
    onLoad() {
        this.loadTeacherInfo();
    },
    /**
     * 页面返回时检查是否有未保存的修改
     */
    onUnload() {
        const hasChanges = JSON.stringify(this.teacherInfo) !== JSON.stringify(this.originalInfo);
        if (hasChanges) {
            // 这里可以提示用户是否保存修改
            console.log('有未保存的修改');
        }
    },
    methods: {
        /**
         * 加载教师信息
         */
        loadTeacherInfo() {
            try {
                let teacherInfo = uni.getStorageSync('teacherInfo');
                if (teacherInfo) {
                    // 尝试补全 password 字段
                    if (!teacherInfo.password && teacherInfo.teacherId) {
                        const registeredTeachers = uni.getStorageSync('registeredTeachers') || [];
                        const found = registeredTeachers.find((teacher: any) => teacher.teacherId === teacherInfo.teacherId);
                        if (found && found.password) {
                            teacherInfo.password = found.password;
                        }
                    }
                    this.setData({
                        teacherInfo: teacherInfo,
                        originalInfo: JSON.parse(JSON.stringify(teacherInfo))
                    });
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
        },

        /**
         * 编辑头像
         */
        editAvatar() {
            uni.showActionSheet({
                itemList: ['拍照', '从相册选择'],
                success: (res) => {
                    if (res.tapIndex === 0) {
                        this.chooseImage('camera');
                    } else if (res.tapIndex === 1) {
                        this.chooseImage('album');
                    }
                }
            });
        },

        /**
         * 选择图片
         */
        chooseImage(sourceType: 'camera' | 'album') {
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
        },

        /**
         * 编辑姓名
         */
        editName() {
            this.openEditModal('姓名', 'input', this.teacherInfo.name);
        },

        /**
         * 编辑性别
         */
        editGender() {
            this.openEditModal('性别', 'gender', this.teacherInfo.gender);
        },

        /**
         * 编辑学院
         */
        editCollege() {
            const currentIndex = this.colleges.indexOf(this.teacherInfo.college);
            this.setData({
                pickerOptions: this.colleges,
                pickerIndex: currentIndex
            });
            this.openEditModal('所属学院', 'picker', this.teacherInfo.college);
        },

        /**
         * 编辑部门
         */
        editDepartment() {
            this.openEditModal('部门', 'input', this.teacherInfo.department);
        },

        /**
         * 编辑手机号
         */
        editPhone() {
            this.openEditModal('手机号', 'input', this.teacherInfo.phone);
        },

        /**
         * 打开编辑弹窗
         */
        openEditModal(field: string, type: string, value: string) {
            this.setData({
                showEditModal: true,
                editField: field,
                editType: type,
                editValue: value
            });
        },

        /**
         * 关闭编辑弹窗
         */
        closeEditModal() {
            this.setData({
                showEditModal: false,
                editField: '',
                editType: '',
                editValue: ''
            });
        },

        /**
         * 阻止事件冒泡
         */
        stopPropagation() {
            // 阻止点击弹窗内容时关闭弹窗
        },

        /**
         * 编辑输入事件
         */
        onEditInput(e: any) {
            this.setData({
                editValue: e.detail.value
            });
        },

        /**
         * 选择器变化事件
         */
        onPickerChange(e: any) {
            const index = e.detail.value;
            this.setData({
                pickerIndex: index,
                editValue: this.pickerOptions[index]
            });
        },

        /**
         * 选择性别
         */
        selectGender(e: any) {
            const gender = e.currentTarget.dataset.gender;
            this.setData({
                editValue: gender
            });
        },

        /**
         * 确认编辑
         */
        confirmEdit() {
            const { editField, editValue } = this;
            if (!editValue.trim()) {
                uni.showToast({
                    title: `请输入${editField}`,
                    icon: 'error'
                });
                return;
            }

            // 验证手机号格式
            if (editField === '手机号') {
                const phoneRegex = /^1[3-9]\d{9}$/;
                if (!phoneRegex.test(editValue)) {
                    uni.showToast({
                        title: '手机号格式不正确',
                        icon: 'error'
                    });
                    return;
                }
            }

            // 更新对应字段
            const updateData: any = {};
            switch (editField) {
                case '姓名':
                    updateData['teacherInfo.name'] = editValue;
                    break;
                case '性别':
                    updateData['teacherInfo.gender'] = editValue;
                    break;
                case '所属学院':
                    updateData['teacherInfo.college'] = editValue;
                    break;
                case '部门':
                    updateData['teacherInfo.department'] = editValue;
                    break;
                case '手机号':
                    updateData['teacherInfo.phone'] = editValue;
                    break;
            }
            this.setData(updateData);
            this.closeEditModal();
            uni.showToast({
                title: '修改成功',
                icon: 'success'
            });
        },

        /**
         * 保存信息
         */
        saveInfo() {
            try {
                // 保存到本地存储
                uni.setStorageSync('teacherInfo', this.teacherInfo);

                // 更新注册教师列表中的信息
                const registeredTeachers = uni.getStorageSync('registeredTeachers') || [];
                const teacherIndex = registeredTeachers.findIndex((teacher: any) => teacher.teacherId === this.teacherInfo.teacherId);
                if (teacherIndex !== -1) {
                    registeredTeachers[teacherIndex] = {
                        ...registeredTeachers[teacherIndex],
                        ...this.teacherInfo
                    };
                    uni.setStorageSync('registeredTeachers', registeredTeachers);
                }
                uni.showToast({
                    title: '保存成功',
                    icon: 'success'
                });

                // 更新原始信息
                this.setData({
                    originalInfo: JSON.parse(JSON.stringify(this.teacherInfo))
                });
            } catch (error) {
                console.log('CatchClause', error);
                console.log('CatchClause', error);
                console.error('保存信息失败:', error);
                uni.showToast({
                    title: '保存失败',
                    icon: 'error'
                });
            }
        },

        /**
         * 修改密码
         */
        resetPassword() {
            this.setData({
                showPasswordModal: true,
                oldPassword: '',
                newPassword: '',
                confirmNewPassword: ''
            });
        },

        closePasswordModal() {
            this.setData({
                showPasswordModal: false,
                oldPassword: '',
                newPassword: '',
                confirmNewPassword: ''
            });
        },

        onOldPasswordInput(e: any) {
            this.setData({
                oldPassword: e.detail.value
            });
        },

        onNewPasswordInput(e: any) {
            this.setData({
                newPassword: e.detail.value
            });
        },

        onConfirmNewPasswordInput(e: any) {
            this.setData({
                confirmNewPassword: e.detail.value
            });
        },

        confirmPasswordChange() {
            const { oldPassword, newPassword, confirmNewPassword, teacherInfo } = this;
            if (!oldPassword || !newPassword || !confirmNewPassword) {
                uni.showToast({
                    title: '请填写完整',
                    icon: 'none'
                });
                return;
            }
            if (oldPassword !== teacherInfo.password) {
                uni.showToast({
                    title: '原密码错误',
                    icon: 'none'
                });
                return;
            }
            if (newPassword.length < 6) {
                uni.showToast({
                    title: '新密码至少6位',
                    icon: 'none'
                });
                return;
            }
            if (newPassword !== confirmNewPassword) {
                uni.showToast({
                    title: '两次新密码不一致',
                    icon: 'none'
                });
                return;
            }
            // 修改密码
            this.setData({
                'teacherInfo.password': newPassword,
                showPasswordModal: false
            });
            // 同步到本地存储
            uni.setStorageSync('teacherInfo', this.teacherInfo);
            uni.showToast({
                title: '密码修改成功',
                icon: 'success'
            });
        }
    }
});
</script>
<style lang="less">
@import './teacher-personal-info.less';
</style>
