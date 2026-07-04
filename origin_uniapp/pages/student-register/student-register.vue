<template>
    <view class="page-wrapper">
        <!-- pages/student-register/student-register.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="学生注册" color="white" background="#3B82F6" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <app-navigation-bar title="学生注册" :back="true" color="white" background=" #3B82F6"></app-navigation-bar>
        <!-- #endif -->
        <view class="container">
            <view class="logo-container">
                <image class="logo" src="/static/images/天空实验室.png" mode="aspectFit"></image>
                <text class="title">实验室空间预约与协同管理系统</text>
                <text class="subtitle">学生注册</text>
            </view>

            <view class="register-form">
                <view class="input-group">
                    <view class="input-label">学号</view>
                    <input class="input" type="text" placeholder="请输入学号" @input="onStudentIdInput" :value="studentId" />
                </view>

                <view class="input-group">
                    <view class="input-label">姓名</view>
                    <input class="input" type="text" placeholder="请输入真实姓名" @input="onNameInput" :value="name" />
                </view>

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
                    <view class="input-label">年级专业</view>
                    <input class="input" type="text" placeholder="如：2023级计算机科学与技术" @input="onMajorInput" :value="major" />
                </view>

                <view class="input-group">
                    <view class="input-label">手机号</view>
                    <input class="input" type="number" placeholder="请输入手机号" @input="onPhoneInput" :value="phone" />
                </view>

                <view class="input-group">
                    <view class="input-label">密码</view>
                    <input v-if="!showPassword" class="input" password placeholder="请设置登录密码" @input="onPasswordInput" :value="password" />
                    <input v-else class="input" placeholder="请设置登录密码" @input="onPasswordInput" :value="password" />
                    <view class="password-icon" @tap="togglePasswordVisibility" hover-class="none">
                        <image :src="showPassword ? '/static/images/icons/eye-open.png' : '/static/images/icons/eye-close.png'" mode="aspectFit"></image>
                    </view>
                </view>

                <view class="input-group">
                    <view class="input-label">确认密码</view>
                    <input v-if="!showConfirmPassword" class="input" password placeholder="请再次输入密码" @input="onConfirmPasswordInput" :value="confirmPassword" />
                    <input v-else class="input" placeholder="请再次输入密码" @input="onConfirmPasswordInput" :value="confirmPassword" />
                    <view class="password-icon" @tap="toggleConfirmPasswordVisibility" hover-class="none">
                        <image :src="showConfirmPassword ? '/static/images/icons/eye-open.png' : '/static/images/icons/eye-close.png'" mode="aspectFit"></image>
                    </view>
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
                <text>© SCNU IBC 实验室空间预约与协同管理系统</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
import { registerStudent, checkStudentExists } from '@/api/student';
// pages/student-register/student-register.ts

const studentId = ref<string>('');
const name = ref<string>('');
const gender = ref<string>('');
const colleges = ref<string[]>([
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
]);
const collegeIndex = ref<number>(-1);
const major = ref<string>('');
const phone = ref<string>('');
const password = ref<string>('');
const confirmPassword = ref<string>('');
const showPassword = ref<boolean>(false);
const showConfirmPassword = ref<boolean>(false);
const agreeTerms = ref<boolean>(false);
const showCollegePickerModal = ref<boolean>(false);
const tempCollegeIndex = ref<number>(0);

const onStudentIdInput = (e: any) => {
    studentId.value = e.detail.value;
};

const onNameInput = (e: any) => {
    name.value = e.detail.value;
};

const selectGender = (e: any) => {
    const genderVal = e.currentTarget.dataset.gender;
    gender.value = genderVal;
};

const onCollegeChange = (e: any) => {
    collegeIndex.value = e.detail.value;
};

const onMajorInput = (e: any) => {
    major.value = e.detail.value;
};

const onPhoneInput = (e: any) => {
    phone.value = e.detail.value;
};

const onPasswordInput = (e: any) => {
    password.value = e.detail.value;
};

const onConfirmPasswordInput = (e: any) => {
    confirmPassword.value = e.detail.value;
};

const togglePasswordVisibility = () => {
    showPassword.value = !showPassword.value;
};

const toggleConfirmPasswordVisibility = () => {
    showConfirmPassword.value = !showConfirmPassword.value;
};

const toggleAgreement = () => {
    agreeTerms.value = !agreeTerms.value;
};

const showTerms = () => {
    uni.showModal({
        title: '用户协议',
        content:
            '1. 请确保提供的个人信息真实有效\n2. 账号仅限本人使用，不得转借他人\n3. 遵守实验室相关规定和预约制度\n4. 如发现违规行为，将取消使用资格\n5. 个人信息仅用于实验室管理，不会泄露给第三方',
        showCancel: false,
        confirmText: '我知道了'
    });
};

const validateForm = () => {
    const studentIdVal = studentId.value;
    const nameVal = name.value;
    const genderVal = gender.value;
    const collegeIndexVal = collegeIndex.value;
    const majorVal = major.value;
    const phoneVal = phone.value;
    const passwordVal = password.value;
    const confirmPasswordVal = confirmPassword.value;
    const agreeTermsVal = agreeTerms.value;
    if (!studentIdVal.trim()) {
        uni.showToast({
            title: '请输入学号',
            icon: 'none'
        });
        return false;
    }
    if (!/^\d+$/.test(studentIdVal)) {
        uni.showToast({
            title: '学号格式不正确',
            icon: 'none'
        });
        return false;
    }
    if (!nameVal.trim()) {
        uni.showToast({
            title: '请输入姓名',
            icon: 'none'
        });
        return false;
    }
    if (!/^[一-龥]{2,10}$/.test(nameVal)) {
        uni.showToast({
            title: '请输入正确的中文姓名',
            icon: 'none'
        });
        return false;
    }
    if (!genderVal) {
        uni.showToast({
            title: '请选择性别',
            icon: 'none'
        });
        return false;
    }
    if (collegeIndexVal === -1) {
        uni.showToast({
            title: '请选择所属学院',
            icon: 'none'
        });
        return false;
    }
    if (!majorVal.trim()) {
        uni.showToast({
            title: '请输入年级专业',
            icon: 'none'
        });
        return false;
    }
    if (!phoneVal.trim()) {
        uni.showToast({
            title: '请输入手机号',
            icon: 'none'
        });
        return false;
    }
    if (!/^1[3-9]\d{9}$/.test(phoneVal)) {
        uni.showToast({
            title: '手机号格式不正确',
            icon: 'none'
        });
        return false;
    }
    if (!passwordVal.trim()) {
        uni.showToast({
            title: '请设置密码',
            icon: 'none'
        });
        return false;
    }
    if (passwordVal.length < 6) {
        uni.showToast({
            title: '密码至少6位',
            icon: 'none'
        });
        return false;
    }
    if (passwordVal !== confirmPasswordVal) {
        uni.showToast({
            title: '两次密码不一致',
            icon: 'none'
        });
        return false;
    }
    if (!agreeTermsVal) {
        uni.showToast({
            title: '请同意用户协议',
            icon: 'none'
        });
        return false;
    }
    return true;
};

const checkStudentIdExists = async (studentIdArg: string) => {
    // 调用后端 API 检查学号是否已存在
    const res: any = await checkStudentExists(studentIdArg);
    return !!(res && res.exists);
};

const register = async () => {
    if (!validateForm()) {
        return;
    }
    const studentIdVal = studentId.value;
    const nameVal = name.value;
    const genderVal = gender.value;
    const collegeIndexVal = collegeIndex.value;
    const collegesVal = colleges.value;
    const majorVal = major.value;
    const phoneVal = phone.value;
    const passwordVal = password.value;

    uni.showLoading({
        title: '注册中...',
        mask: true
    });

    try {
        // 检查学号是否已存在
        const exists = await checkStudentIdExists(studentIdVal);
        if (exists) {
            uni.hideLoading();
            uni.showToast({
                title: '该学号已注册',
                icon: 'none'
            });
            return;
        }

        // 提交注册
        await registerStudent({
            studentId: studentIdVal,
            name: nameVal,
            gender: genderVal,
            college: collegesVal[collegeIndexVal],
            major: majorVal,
            phone: phoneVal,
            password: passwordVal
        });

        uni.hideLoading();
        uni.showToast({
            title: '注册成功',
            icon: 'success'
        });
        setTimeout(() => {
            uni.navigateBack();
        }, 1500);
    } catch (err: any) {
        uni.hideLoading();
        uni.showToast({
            title: err?.data?.message || '注册失败',
            icon: 'none'
        });
    }
};

const goToLogin = () => {
    uni.navigateBack();
};

const showCollegePicker = () => {
    showCollegePickerModal.value = true;
    tempCollegeIndex.value = collegeIndex.value === -1 ? 0 : collegeIndex.value;
};

const hideCollegePicker = () => {
    showCollegePickerModal.value = false;
};

const onCollegePickerChange = (e: any) => {
    tempCollegeIndex.value = e.detail.value[0];
};

const confirmCollegePicker = () => {
    collegeIndex.value = tempCollegeIndex.value;
    showCollegePickerModal.value = false;
};
</script>
<style lang="less">
@import './student-register.less';
</style>
