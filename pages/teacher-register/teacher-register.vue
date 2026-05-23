<template>
    <view>
        <!-- pages/teacher-register/teacher-register.wxml -->
        <navigation-bar title="教师注册" :back="true" color="white" background="#32c072"></navigation-bar>
        <view class="container">
            <view class="logo-container">
                <image class="logo" src="/static/images/天空实验室.png" mode="aspectFit"></image>
                <text class="title">实验室预约管理系统</text>
                <text class="subtitle">教师注册</text>
            </view>

            <view class="register-form">
                <view class="input-group">
                    <view class="input-label">工号</view>
                    <input class="input" type="text" placeholder="请输入工号" @input="onTeacherIdInput" :value="teacherId" />
                </view>

                <view class="input-group">
                    <view class="input-label">姓名</view>
                    <input class="input" type="text" placeholder="请输入姓名" @input="onNameInput" :value="name" />
                </view>

                <view class="input-group">
                    <view class="input-label">密码</view>
                    <input v-if="!showPassword" class="input" password placeholder="请输入密码" @input="onPasswordInput" :value="password" />
                    <input v-else class="input" placeholder="请输入密码" @input="onPasswordInput" :value="password" />
                    <view class="password-icon" @tap="togglePasswordVisibility" hover-class="none">
                        <image :src="showPassword ? '/images/icons/eye-open.png' : '/images/icons/eye-close.png'" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="input-group">
                    <view class="input-label">确认密码</view>
                    <input v-if="!showConfirmPassword" class="input" password placeholder="请再次输入密码" @input="onConfirmPasswordInput" :value="confirmPassword" />
                    <input v-else class="input" placeholder="请再次输入密码" @input="onConfirmPasswordInput" :value="confirmPassword" />
                    <view class="password-icon" @tap="toggleConfirmPasswordVisibility" hover-class="none">
                        <image :src="showConfirmPassword ? '/images/icons/eye-open.png' : '/images/icons/eye-close.png'" mode="aspectFit"></image>
                    </view>
                </view>

                <!-- 性别选择 -->
                <view class="input-group">
                    <view class="input-label">性别</view>
                    <view class="gender-group">
                        <view :class="'gender-option ' + (gender === 'male' ? 'active' : '')" @tap="selectGender" data-gender="male">
                            <text>男</text>
                        </view>
                        <view :class="'gender-option ' + (gender === 'female' ? 'active' : '')" @tap="selectGender" data-gender="female">
                            <text>女</text>
                        </view>
                    </view>
                </view>

                <!-- 所属学院选择 -->
                <view class="input-group">
                    <view class="input-label">所属学院</view>
                    <view class="custom-picker" @tap="showCollegePicker">
                        <view class="picker-input">
                            <text :class="collegeIndex === -1 ? 'placeholder' : ''">{{ collegeIndex === -1 ? '请选择所属学院' : colleges[collegeIndex] }}</text>
                            <image class="arrow-icon" src="/static/images/icons/arrow-down.svg"></image>
                        </view>
                    </view>
                </view>
                <!-- 学院选择弹窗 -->
                <view class="modal custom-picker-modal" v-if="showCollegePickerModal">
                    <view class="modal-mask" @tap="hideCollegePicker"></view>
                    <view class="modal-content picker-modal-content">
                        <view class="modal-header">
                            <text>选择所属学院</text>
                        </view>
                        <view class="modal-body">
                            <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[tempCollegeIndex]" @change="onCollegePickerChange">
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in colleges" :key="index">{{ item }}</view>
                                </picker-view-column>
                            </picker-view>
                        </view>
                        <view class="modal-footer">
                            <button class="cancel-btn" @tap="hideCollegePicker">取消</button>
                            <button class="confirm-btn" @tap="confirmCollegePicker">确定</button>
                        </view>
                    </view>
                </view>

                <view class="input-group">
                    <view class="input-label">手机号</view>
                    <input class="input" type="number" placeholder="请输入手机号" @input="onPhoneInput" :value="phone" />
                </view>

                <view class="agreement-section">
                    <checkbox :checked="agreeTerms" @tap="toggleAgreement"></checkbox>
                    <text class="agreement-text">我已阅读并同意</text>
                    <text class="agreement-link" @tap="showTerms">《用户协议》</text>
                </view>

                <button class="register-btn" @tap="register">注册</button>

                <view class="login-section">
                    <text class="login-text">已有账号？</text>
                    <text class="login-link" @tap="goToLogin">立即登录</text>
                </view>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>
    </view>
</template>

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
// pages/teacher-register/teacher-register.ts
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            teacherId: '',
            name: '',
            password: '',
            confirmPassword: '',
            genderIndex: -1,
            genderOptions: ['男', '女'],
            college: '',
            phone: '',
            showPassword: false,
            showConfirmPassword: false,
            gender: '',
            colleges: [
                '计算机科学与技术学院',
                '数学学院',
                '物理学院',
                '化学学院',
                '生命科学学院',
                '地理科学学院',
                '心理学院',
                '教育学院',
                '外国语言文化学院',
                '文学院',
                '历史文化学院',
                '马克思主义学院',
                '经济与管理学院',
                '法学院',
                '公共管理学院',
                '体育科学学院',
                '音乐学院',
                '美术学院'
            ],
            collegeIndex: -1,
            showCollegePickerModal: false,
            tempCollegeIndex: 0,
            agreeTerms: false
        };
    },
    methods: {
        onTeacherIdInput(e: any) {
            this.setData({
                teacherId: e.detail.value
            });
        },

        onNameInput(e: any) {
            this.setData({
                name: e.detail.value
            });
        },

        onPasswordInput(e: any) {
            this.setData({
                password: e.detail.value
            });
        },

        onConfirmPasswordInput(e: any) {
            this.setData({
                confirmPassword: e.detail.value
            });
        },

        onGenderChange(e: any) {
            this.setData({
                genderIndex: e.detail.value
            });
        },

        onCollegeInput(e: any) {
            this.setData({
                college: e.detail.value
            });
        },

        onPhoneInput(e: any) {
            this.setData({
                phone: e.detail.value
            });
        },

        togglePasswordVisibility() {
            this.setData({
                showPassword: !this.showPassword
            });
        },

        toggleConfirmPasswordVisibility() {
            this.setData({
                showConfirmPassword: !this.showConfirmPassword
            });
        },

        selectGender(e: any) {
            const gender = e.currentTarget.dataset.gender;
            this.setData({
                gender: gender
            });
        },

        showCollegePicker() {
            this.setData({
                showCollegePickerModal: true,
                tempCollegeIndex: this.collegeIndex === -1 ? 0 : this.collegeIndex
            });
        },

        hideCollegePicker() {
            this.setData({
                showCollegePickerModal: false
            });
        },

        onCollegePickerChange(e: any) {
            this.setData({
                tempCollegeIndex: e.detail.value[0]
            });
        },

        confirmCollegePicker() {
            this.setData({
                collegeIndex: this.tempCollegeIndex,
                showCollegePickerModal: false
            });
        },

        toggleAgreement() {
            this.setData({
                agreeTerms: !this.agreeTerms
            });
        },

        showTerms() {
            uni.showModal({
                title: '用户协议',
                content:
                    '1. 请确保提供的个人信息真实有效\n2. 账号仅限本人使用，不得转借他人\n3. 遵守实验室相关规定和预约制度\n4. 如发现违规行为，将取消使用资格\n5. 个人信息仅用于实验室管理，不会泄露给第三方\n6. 教师有权审核学生的实验室预约申请',
                showCancel: false,
                confirmText: '我知道了'
            });
        },

        register() {
            const { teacherId, name, password, confirmPassword, gender, collegeIndex, phone } = this;

            // 表单验证
            if (!teacherId.trim()) {
                uni.showToast({
                    title: '请输入工号',
                    icon: 'none'
                });
                return;
            }

            // 工号格式验证
            if (!/^[a-zA-Z0-9]+$/.test(teacherId)) {
                uni.showToast({
                    title: '工号格式不正确',
                    icon: 'none'
                });
                return;
            }
            if (!name.trim()) {
                uni.showToast({
                    title: '请输入姓名',
                    icon: 'none'
                });
                return;
            }
            if (!password.trim()) {
                uni.showToast({
                    title: '请输入密码',
                    icon: 'none'
                });
                return;
            }
            if (password.length < 6) {
                uni.showToast({
                    title: '密码长度至少6位',
                    icon: 'none'
                });
                return;
            }
            if (password !== confirmPassword) {
                uni.showToast({
                    title: '两次密码输入不一致',
                    icon: 'none'
                });
                return;
            }
            if (!gender) {
                uni.showToast({
                    title: '请选择性别',
                    icon: 'none'
                });
                return;
            }
            if (collegeIndex === -1) {
                uni.showToast({
                    title: '请选择所属学院',
                    icon: 'none'
                });
                return;
            }
            if (!phone.trim()) {
                uni.showToast({
                    title: '请输入手机号',
                    icon: 'none'
                });
                return;
            }

            // 手机号格式验证
            if (!/^1[3-9]\d{9}$/.test(phone)) {
                uni.showToast({
                    title: '手机号格式不正确',
                    icon: 'none'
                });
                return;
            }
            if (!this.agreeTerms) {
                uni.showToast({
                    title: '请同意用户协议',
                    icon: 'none'
                });
                return;
            }
            uni.showLoading({
                title: '注册中...',
                mask: true
            });

            // 模拟注册过程
            setTimeout(() => {
                uni.hideLoading();

                // 检查工号是否已存在
                const registeredTeachers = uni.getStorageSync('registeredTeachers') || [];
                const existingTeacher = registeredTeachers.find((t: any) => t.teacherId === teacherId);
                if (existingTeacher) {
                    uni.showToast({
                        title: '该工号已被注册',
                        icon: 'none'
                    });
                    return;
                }

                // 保存新教师信息
                const newTeacher = {
                    teacherId,
                    name,
                    password,
                    gender: gender === 'male' ? '男' : '女',
                    college: this.colleges[collegeIndex],
                    phone,
                    registerTime: new Date().getTime()
                };
                registeredTeachers.push(newTeacher);
                uni.setStorageSync('registeredTeachers', registeredTeachers);
                uni.showModal({
                    title: '注册成功',
                    content: '您的账号已成功注册，现在可以使用工号和密码登录了。',
                    showCancel: false,
                    confirmText: '去登录',
                    success: () => {
                        uni.navigateBack();
                    }
                });
            }, 1500);
        },

        goToLogin() {
            uni.navigateBack();
        }
    }
});
</script>
<style>
@import './teacher-register.css';
</style>
