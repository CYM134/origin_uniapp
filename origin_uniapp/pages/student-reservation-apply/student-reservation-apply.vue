<template>
    <view class="page-wrapper">
        <!-- pages/student-reservation-apply/student-reservation-apply.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="预约申请" color="white" background="#3B82F6" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <app-navigation-bar title="预约申请" :back="true" color="white" background="#3B82F6"></app-navigation-bar>
        <!-- #endif -->

        <view class="container">
            <!-- 预约表单 -->
            <scroll-view class="form-container" :scroll-y="true" id="formScroll">
                <view class="form-section">
                    <view class="section-title">
                        <image src="/static/images/icons/calendar.svg" mode="aspectFit"></image>
                        <text>预约信息</text>
                    </view>

                    <!-- 实验室选择 -->
                    <view class="form-item" id="labItem">
                        <text class="label">
                            实验室
                            <text class="required">*</text>
                        </text>
                        <view :class="'picker-display ' + (!selectedLab ? 'placeholder' : '')" @tap="showLabPicker">
                            <text>{{ selectedLab ? selectedLab.name : '请选择实验室' }}</text>
                            <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                        </view>
                        <view v-if="errorLab" class="error-tip">{{ errorLab }}</view>
                    </view>
                    <!-- 实验室选择器弹窗 -->
                    <view v-if="showLabPickerModal" class="modal-overlay" @tap="hideLabPicker">
                        <view class="modal-content picker-modal-content custom-picker-modal" @tap.stop.prevent="stopPropagation">
                            <view class="modal-header">
                                <text>选择实验室</text>
                                <view class="close-btn" @tap="hideLabPicker">×</view>
                            </view>
                            <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[tempLabIndex]" @change="onLabPickerChange">
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in labPickerOptions" :key="index">{{ item.name }}</view>
                                </picker-view-column>
                            </picker-view>
                            <view class="modal-footer">
                                <button class="cancel-btn" @tap="hideLabPicker">取消</button>
                                <button class="confirm-btn" @tap="confirmLabSelection">确定</button>
                            </view>
                        </view>
                    </view>

                    <!-- 日期选择 -->
                    <view class="form-item" id="dateItem">
                        <text class="label">
                            预约日期
                            <text class="required">*</text>
                        </text>
                        <view :class="'picker-display ' + (!selectedDate ? 'placeholder' : '')" @tap="showDatePicker">
                            <text>{{ selectedDate || '请选择日期' }}</text>
                            <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                        </view>
                        <view v-if="errorDate" class="error-tip">{{ errorDate }}</view>
                    </view>
                    <!-- 日期选择器弹窗 -->
                    <view v-if="showDateModal" class="modal-overlay" @tap="hideDatePicker">
                        <view class="modal-content picker-modal-content custom-picker-modal" @tap.stop.prevent="stopPropagation">
                            <view class="modal-header">
                                <text>选择日期</text>
                                <view class="close-btn" @tap="hideDatePicker">×</view>
                            </view>
                            <picker-view
                                indicator-style="height: 100rpx;"
                                style="width: 100%; height: 400rpx"
                                :value="[tempYearIndex, tempMonthIndex, tempDayIndex]"
                                @change="onDatePickerChange"
                            >
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in years" :key="index">{{ item }}年</view>
                                </picker-view-column>
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in months" :key="index">{{ item }}月</view>
                                </picker-view-column>
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in days" :key="index">{{ item }}日</view>
                                </picker-view-column>
                            </picker-view>
                            <view class="modal-footer">
                                <button class="cancel-btn" @tap="hideDatePicker">取消</button>
                                <button class="confirm-btn" @tap="confirmDatePicker">确定</button>
                            </view>
                        </view>
                    </view>

                    <!-- 时间段选择 -->
                    <view class="form-item" id="timeItem">
                        <text class="label">
                            时间段
                            <text class="required">*</text>
                        </text>
                        <view class="time-picker-row">
                            <view class="picker-display" @tap="showStartTimePickerFun">
                                <text>{{ startTime || '开始时间' }}</text>
                                <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                            </view>
                            <text style="margin: 0 16rpx">-</text>
                            <view class="picker-display" @tap="showEndTimePickerFun">
                                <text>{{ endTime || '结束时间' }}</text>
                                <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                            </view>
                        </view>
                        <view v-if="errorTime" class="error-tip">{{ errorTime }}</view>
                    </view>
                    <!-- 开始时间picker-view弹窗 -->
                    <view v-if="showStartTimePicker" class="modal-overlay" @tap="hideStartTimePicker">
                        <view class="modal-content picker-modal-content custom-picker-modal" @tap.stop.prevent="stopPropagation">
                            <view class="modal-header">
                                <text>选择开始时间</text>
                                <view class="close-btn" @tap="hideStartTimePicker">×</view>
                            </view>
                            <picker-view
                                indicator-style="height: 100rpx;"
                                style="width: 100%; height: 400rpx"
                                :value="[startHourIndex, startMinuteIndex]"
                                @change="onStartTimeChange"
                            >
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in hours" :key="index">{{ item }}时</view>
                                </picker-view-column>
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in minutes" :key="index">{{ item }}分</view>
                                </picker-view-column>
                            </picker-view>
                            <view class="modal-footer">
                                <button class="cancel-btn" @tap="hideStartTimePicker">取消</button>
                                <button class="confirm-btn" @tap="confirmStartTime">确定</button>
                            </view>
                        </view>
                    </view>
                    <!-- 结束时间picker-view弹窗 -->
                    <view v-if="showEndTimePicker" class="modal-overlay" @tap="hideEndTimePicker">
                        <view class="modal-content picker-modal-content custom-picker-modal" @tap.stop.prevent="stopPropagation">
                            <view class="modal-header">
                                <text>选择结束时间</text>
                                <view class="close-btn" @tap="hideEndTimePicker">×</view>
                            </view>
                            <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[endHourIndex, endMinuteIndex]" @change="onEndTimeChange">
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in hours" :key="index">{{ item }}时</view>
                                </picker-view-column>
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in minutes" :key="index">{{ item }}分</view>
                                </picker-view-column>
                            </picker-view>
                            <view class="modal-footer">
                                <button class="cancel-btn" @tap="hideEndTimePicker">取消</button>
                                <button class="confirm-btn" @tap="confirmEndTime">确定</button>
                            </view>
                        </view>
                    </view>

                    <!-- 预约人数 -->
                    <view class="form-item" id="countItem">
                        <text class="label">
                            预约人数
                            <text class="required">*</text>
                        </text>
                        <view class="number-input">
                            <view :class="'number-btn ' + (studentCount <= 1 ? 'disabled' : '')" @tap="decreaseCount">
                                <text>-</text>
                            </view>
                            <input class="number-display" type="number" :value="studentCount" @input="onCountInput" maxlength="2" />
                            <view :class="'number-btn ' + (studentCount >= maxStudents ? 'disabled' : '')" @tap="increaseCount">
                                <text>+</text>
                            </view>
                        </view>
                        <text class="hint">最多{{ maxStudents }}人</text>
                    </view>
                </view>

                <view class="form-section">
                    <view class="section-title">
                        <image src="/static/images/icons/user.svg" mode="aspectFit"></image>
                        <text>申请信息</text>
                    </view>

                    <!-- 申请类型 -->
                    <view class="form-item" id="typeItem">
                        <text class="label">
                            申请类型
                            <text class="required">*</text>
                        </text>
                        <view :class="'picker-display ' + (!selectedType ? 'placeholder' : '')" @tap="showTypePicker">
                            <text>{{ selectedType ? selectedType.name : '请选择申请类型' }}</text>
                            <image class="arrow-icon" src="/static/images/icons/arrow-down.svg" mode="aspectFit"></image>
                        </view>
                        <view v-if="errorType" class="error-tip">{{ errorType }}</view>
                    </view>
                    <!-- 申请类型picker-view弹窗 -->
                    <view v-if="showTypePickerModal" class="modal-overlay" @tap="hideTypePicker">
                        <view class="modal-content picker-modal-content custom-picker-modal" @tap.stop.prevent="stopPropagation">
                            <view class="modal-header">
                                <text>选择申请类型</text>
                                <view class="close-btn" @tap="hideTypePicker">×</view>
                            </view>
                            <picker-view indicator-style="height: 100rpx;" style="width: 100%; height: 400rpx" :value="[tempTypeIndex]" @change="onTypePickerChange">
                                <picker-view-column>
                                    <view class="picker-view-item" v-for="(item, index) in typePickerOptions" :key="index">{{ item.name }}</view>
                                </picker-view-column>
                            </picker-view>
                            <view class="modal-footer">
                                <button class="cancel-btn" @tap="hideTypePicker">取消</button>
                                <button class="confirm-btn" @tap="confirmTypePicker">确定</button>
                            </view>
                        </view>
                    </view>

                    <!-- 申请主题 -->
                    <view class="form-item" id="titleItem">
                        <text class="label">
                            申请主题
                            <text class="required">*</text>
                        </text>
                        <textarea class="textarea-input subject-textarea" placeholder="请输入申请主题" :value="title" @input="onTitleInput" maxlength="50" rows="2"></textarea>
                        <text class="char-count">{{ title.length }}/50</text>
                        <view v-if="errorTitle" class="error-tip">{{ errorTitle }}</view>
                    </view>

                    <!-- 使用目的 -->
                    <view class="form-item" id="purposeItem">
                        <text class="label">
                            使用目的
                            <text class="required">*</text>
                        </text>
                        <textarea class="textarea-input" placeholder="请详细描述使用目的、活动内容等" :value="purpose" @input="onPurposeInput" maxlength="200"></textarea>
                        <text class="char-count">{{ purpose.length }}/200</text>
                        <view v-if="errorPurpose" class="error-tip">{{ errorPurpose }}</view>
                    </view>

                    <!-- 指导老师 -->
                    <view class="form-item">
                        <text class="label">指导老师</text>
                        <input class="text-input" placeholder="请输入指导老师姓名" :value="teacher" @input="onTeacherInput" maxlength="20" />
                    </view>

                    <!-- 联系方式 -->
                    <view class="form-item" id="contactItem">
                        <text class="label">
                            联系方式
                            <text class="required">*</text>
                        </text>
                        <input class="text-input" placeholder="请输入手机号" :value="contact" @input="onContactInput" type="number" maxlength="11" />
                        <view v-if="errorContact" class="error-tip">{{ errorContact }}</view>
                    </view>
                </view>

                <!-- 附加信息 -->
                <view class="form-section">
                    <view class="section-title">
                        <image src="/static/images/icons/info.svg" mode="aspectFit"></image>
                        <text>附加信息</text>
                    </view>

                    <!-- 特殊要求 -->
                    <view class="form-item">
                        <text class="label">特殊要求</text>
                        <textarea
                            class="textarea-input"
                            placeholder="如有特殊设备需求或其他要求，请在此说明"
                            :value="requirements"
                            @input="onRequirementsInput"
                            maxlength="100"
                        ></textarea>
                        <text class="char-count">{{ requirements.length }}/100</text>
                    </view>

                    <!-- 紧急联系人 -->
                    <view class="form-item">
                        <text class="label">紧急联系人</text>
                        <input class="text-input" placeholder="紧急情况联系人姓名" :value="emergencyContact" @input="onEmergencyContactInput" maxlength="20" />
                    </view>

                    <view class="form-item">
                        <text class="label">紧急联系电话</text>
                        <input class="text-input" placeholder="紧急联系人电话" :value="emergencyPhone" @input="onEmergencyPhoneInput" type="number" maxlength="11" />
                    </view>
                </view>
            </scroll-view>

            <!-- 预约须知 -->
            <view class="notice-section">
                <view class="notice-title">
                    <image src="/static/images/icons/warning.svg" mode="aspectFit"></image>
                    <text>预约须知</text>
                </view>
                <view class="notice-content">
                    <text class="notice-item">1. 请提前至少1天申请，紧急情况除外</text>
                    <text class="notice-item">2. 使用期间请爱护实验室设备和环境</text>
                    <text class="notice-item">3. 请勿在实验室内食用零食或餐饭</text>
                    <text class="notice-item">4. 使用完毕后请及时清理现场</text>
                    <text class="notice-item">5. 如需取消预约，请提前2小时通知</text>
                    <text class="notice-item">6. 违规使用将影响后续预约权限</text>
                </view>
            </view>

            <!-- 提交按钮 -->
            <view class="submit-section">
                <view class="agreement">
                    <checkbox-group @change="onAgreementChange">
                        <label class="agreement-item">
                            <checkbox value="agree" :checked="agreed" />
                            <text>我已阅读并同意</text>
                            <text class="link" @tap="viewAgreement">《实验室使用协议》</text>
                        </label>
                    </checkbox-group>
                    <view v-if="errorAgreement" class="error-tip">{{ errorAgreement }}</view>
                </view>
                <view class="button-group">
                    <button :class="'submit-btn ' + (canSubmit ? '' : 'disabled')" @tap="submitApplication" :disabled="!canSubmit">提交申请</button>
                    <button class="draft-btn" @tap="saveDraft">保存草稿</button>
                </view>
            </view>

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>

        <!-- 协议弹窗 -->
        <view v-if="showAgreement" class="modal-overlay" @tap="closeAgreement">
            <view class="modal-content agreement-modal" @tap.stop.prevent="stopPropagation">
                <view class="modal-header">
                    <text class="modal-title">实验室使用协议</text>
                    <view class="close-btn" @tap="closeAgreement">×</view>
                </view>
                <scroll-view class="modal-body" :scroll-y="true">
                    <view class="agreement-text">
                        <text class="article-title">第一条 总则</text>
                        <text class="article-content">为规范实验室使用管理，保障实验室设备安全和教学科研活动正常进行，特制定本协议。</text>

                        <text class="article-title">第二条 使用规范</text>
                        <text class="article-content">1. 使用者必须遵守实验室各项规章制度；</text>
                        <text class="article-content">2. 爱护实验室设备，按规范操作；</text>
                        <text class="article-content">3. 保持实验室环境整洁；</text>
                        <text class="article-content">4. 禁止在实验室内饮食、吸烟；</text>
                        <text class="article-content">5. 使用完毕后及时关闭设备电源。</text>

                        <text class="article-title">第三条 安全责任</text>
                        <text class="article-content">使用者对使用期间的人身安全和设备安全负责，如因违规操作造成损失，将承担相应责任。</text>

                        <text class="article-title">第四条 违约处理</text>
                        <text class="article-content">违反本协议的，将根据情节轻重给予警告、暂停使用权限等处理。</text>
                    </view>
                </scroll-view>
                <view class="modal-footer">
                    <button class="confirm-btn" @tap="closeAgreement">我已阅读</button>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
import { getLabs, getApplicationTypes, createStudentReservation } from '@/api/student';
// pages/student-reservation-apply/student-reservation-apply.ts

// 表单数据
const selectedLab = ref<any>(null);
const labIndex = ref<number>(-1);
const selectedDate = ref<string>('');
const studentCount = ref<number>(1);
const selectedType = ref<any>(null);
const typeIndex = ref<number>(-1);
const title = ref<string>('');
const purpose = ref<string>('');
const teacher = ref<string>('');
const contact = ref<string>('');
const requirements = ref<string>('');
const emergencyContact = ref<string>('');
const emergencyPhone = ref<string>('');
// 选项数据（由后端接口加载）
const labs = ref<any[]>([]);
const applicationTypes = ref<any[]>([]);
// 日期限制,人数限制
const minDate = ref<string>('');
const maxDate = ref<string>('');
const maxStudents = ref<number>(80);
// 协议相关
const agreed = ref<boolean>(false);
const showAgreement = ref<boolean>(false);
// 表单验证
const canSubmit = ref<boolean>(false);
const showLabPickerModal = ref<boolean>(false);
const labPickerOptions = ref<any[]>([]);
const tempLabIndex = ref<number>(0);
const showDateModal = ref<boolean>(false);
const years = ref<number[]>([]);
const months = ref<number[]>([]);
const days = ref<number[]>([]);
const tempYearIndex = ref<number>(0);
const tempMonthIndex = ref<number>(0);
const tempDayIndex = ref<number>(0);
const showStartTimePicker = ref<boolean>(false);
const showEndTimePicker = ref<boolean>(false);
const hours = ref<number[]>(Array.from(
    {
        length: 24
    },
    (_, i) => i
));
const minutes = ref<number[]>(Array.from(
    {
        length: 60
    },
    (_, i) => i
));
const startHourIndex = ref<number>(8);
const startMinuteIndex = ref<number>(0);
const endHourIndex = ref<number>(10);
const endMinuteIndex = ref<number>(0);
const startTime = ref<string>('');
const endTime = ref<string>('');
const showTypePickerModal = ref<boolean>(false);
const typePickerOptions = ref<any[]>([]);
const tempTypeIndex = ref<number>(0);
// 错误提示
const errorLab = ref<string>('');
const errorDate = ref<string>('');
const errorTime = ref<string>('');
const errorType = ref<string>('');
const errorTitle = ref<string>('');
const errorPurpose = ref<string>('');
const errorContact = ref<string>('');
const errorAgreement = ref<string>('');

onLoad(() => {
    initializeDates();
    loadUserInfo();
    loadOptions();
    // 默认时间，避免未选时间导致无法提交
    startTime.value = '08:00';
    endTime.value = '09:50';
});

/**
 * 加载实验室与申请类型选项
 */
const loadOptions = async () => {
    try {
        const [labList, typeList] = await Promise.all([getLabs(), getApplicationTypes()]);
        labs.value = Array.isArray(labList) ? labList : [];
        applicationTypes.value = Array.isArray(typeList) ? typeList : [];
    } catch (err: any) {
        labs.value = [];
        applicationTypes.value = [];
        uni.showToast({ title: err?.data?.message || '加载失败', icon: 'none' });
    }
};

onShow(() => {
    validateForm();
    // 尝试加载草稿
    loadDraft();
});

/**
 * 加载草稿
 */
const loadDraft = () => {
    try {
        const draft = uni.getStorageSync('studentApplicationDraft');
        if (draft) {
            // 询问是否加载草稿
            uni.showModal({
                title: '发现草稿',
                content: '是否加载之前保存的草稿？',
                success: (res) => {
                    if (res.confirm) {
                        applyDraft(draft);
                    }
                }
            });
        }
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载草稿失败:', error);
    }
};

/**
 * 应用草稿数据
 */
const applyDraft = (draft: any) => {
    // 设置实验室
    const _labIndex = labs.value.findIndex((lab) => lab.id === draft.labId);
    if (_labIndex !== -1) {
        labIndex.value = _labIndex;
        selectedLab.value = labs.value[_labIndex];
        maxStudents.value = labs.value[_labIndex].maxStudents;
    }

    // 设置申请类型
    const _typeIndex = applicationTypes.value.findIndex((type) => type.id === draft.typeId);
    if (_typeIndex !== -1) {
        typeIndex.value = _typeIndex;
        selectedType.value = applicationTypes.value[_typeIndex];
    }

    // 设置其他数据
    selectedDate.value = draft.date || '';
    startTime.value = draft.startTime || '';
    endTime.value = draft.endTime || '';
    studentCount.value = draft.studentCount || 1;
    title.value = draft.title || '';
    purpose.value = draft.purpose || '';
    teacher.value = draft.teacher || '';
    contact.value = draft.contact || '';
    requirements.value = draft.requirements || '';
    emergencyContact.value = draft.emergencyContact || '';
    emergencyPhone.value = draft.emergencyPhone || '';
    validateForm();
    uni.showToast({
        title: '草稿加载成功',
        icon: 'success'
    });
};

/**
 * 保存草稿
 */
const saveDraft = () => {
    try {
        // 检查是否有必要的数据可以保存
        if (!selectedLab.value && !selectedDate.value && !title.value && !purpose.value) {
            uni.showToast({
                title: '表单为空，无需保存',
                icon: 'none'
            });
            return;
        }
        const draft = {
            labId: (selectedLab.value && selectedLab.value.id) || '',
            date: selectedDate.value,
            startTime: startTime.value,
            endTime: endTime.value,
            studentCount: studentCount.value,
            typeId: (selectedType.value && selectedType.value.id) || '',
            title: title.value,
            purpose: purpose.value,
            teacher: teacher.value,
            contact: contact.value,
            requirements: requirements.value,
            emergencyContact: emergencyContact.value,
            emergencyPhone: emergencyPhone.value,
            saveTime: new Date().toISOString()
        };
        uni.setStorageSync('studentApplicationDraft', draft);
        uni.showToast({
            title: '草稿保存成功',
            icon: 'success'
        });
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('保存草稿失败:', error);
        uni.showToast({
            title: '保存失败',
            icon: 'error'
        });
    }
};

// 初始化日期范围
const initializeDates = () => {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(today.getDate() + 1);
    const _maxDate = new Date(today);
    _maxDate.setDate(today.getDate() + 30); // 最多提前30天预约

    minDate.value = formatDate(tomorrow);
    maxDate.value = formatDate(_maxDate);
};

// 格式化日期
const formatDate = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

// 加载用户信息
const loadUserInfo = () => {
    try {
        const userInfo = uni.getStorageSync('studentInfo');
        if (userInfo && userInfo.phone) {
            contact.value = userInfo.phone;
        }
    } catch (error) {
        console.log('CatchClause', error);
        console.log('CatchClause', error);
        console.error('加载用户信息失败:', error);
    }
};

// 实验室选择
const onLabChange = (e: any) => {
    const index = e.detail.value;
    const _selectedLab = labs.value[index];
    labIndex.value = index;
    selectedLab.value = _selectedLab;
    maxStudents.value = _selectedLab.maxStudents;
    studentCount.value = Math.min(studentCount.value, _selectedLab.maxStudents);
    validateForm();
};

// 日期选择
const onDateChange = (e: any) => {
    selectedDate.value = e.detail.value;
    validateForm();
};

// 人数调整
const decreaseCount = () => {
    const count = Math.max(1, studentCount.value - 1);
    studentCount.value = count;
    validateForm();
};

const increaseCount = () => {
    const count = Math.min(maxStudents.value, studentCount.value + 1);
    studentCount.value = count;
    validateForm();
};

const onCountInput = (e: any) => {
    const value = parseInt(e.detail.value) || 1;
    const count = Math.max(1, Math.min(maxStudents.value, value));
    studentCount.value = count;
    validateForm();
};

// 申请类型选择
const onTypeChange = (e: any) => {
    const index = e.detail.value;
    typeIndex.value = index;
    selectedType.value = applicationTypes.value[index];
    validateForm();
};

// 输入处理
const onTitleInput = (e: any) => {
    title.value = e.detail.value;
    validateForm();
};

const onPurposeInput = (e: any) => {
    purpose.value = e.detail.value;
    validateForm();
};

const onTeacherInput = (e: any) => {
    teacher.value = e.detail.value;
};

const onContactInput = (e: any) => {
    contact.value = e.detail.value;
    validateForm();
};

const onRequirementsInput = (e: any) => {
    requirements.value = e.detail.value;
};

const onEmergencyContactInput = (e: any) => {
    emergencyContact.value = e.detail.value;
};

const onEmergencyPhoneInput = (e: any) => {
    emergencyPhone.value = e.detail.value;
};

// 协议相关
const onAgreementChange = (e: any) => {
    agreed.value = e.detail.value.includes('agree');
    validateForm();
};

const viewAgreement = () => {
    showAgreement.value = true;
};

const closeAgreement = () => {
    showAgreement.value = false;
};

// 表单验证
const validateForm = () => {
    const _selectedLab = selectedLab.value;
    const _selectedDate = selectedDate.value;
    const _startTime = startTime.value;
    const _endTime = endTime.value;
    const _selectedType = selectedType.value;
    const _title = title.value;
    const _purpose = purpose.value;
    const _contact = contact.value;
    const _agreed = agreed.value;
    let _errorLab = '';
    let _errorDate = '';
    let _errorTime = '';
    let _errorType = '';
    let _errorTitle = '';
    let _errorPurpose = '';
    let _errorContact = '';
    let _errorAgreement = '';
    const isPhoneValid = /^1[3-9]\d{9}$/.test(_contact);
    if (!_selectedLab) {
        _errorLab = '请选择实验室';
    }
    if (!_selectedDate) {
        _errorDate = '请选择预约日期';
    }
    if (!_startTime || !_endTime) {
        _errorTime = '请选择时间段';
    } else {
        const [sh, sm] = _startTime.split(':').map(Number);
        const [eh, em] = _endTime.split(':').map(Number);
        if (eh * 60 + em <= sh * 60 + sm) {
            _errorTime = '结束时间必须大于开始时间';
        }
    }
    if (!_selectedType) {
        _errorType = '请选择申请类型';
    }
    if (!_title.trim()) {
        _errorTitle = '请输入申请主题';
    }
    if (!_purpose.trim()) {
        _errorPurpose = '请输入使用目的';
    }
    if (!_contact) _errorContact = '请输入手机号';
    else if (!isPhoneValid) {
        _errorContact = '请输入有效的手机号';
    }
    if (!_agreed) {
        _errorAgreement = '请阅读并同意实验室使用协议';
    }
    const _canSubmit = !_errorLab && !_errorDate && !_errorTime && !_errorType && !_errorTitle && !_errorPurpose && !_errorContact && !_errorAgreement;
    canSubmit.value = _canSubmit;
    errorLab.value = _errorLab;
    errorDate.value = _errorDate;
    errorTime.value = _errorTime;
    errorType.value = _errorType;
    errorTitle.value = _errorTitle;
    errorPurpose.value = _errorPurpose;
    errorContact.value = _errorContact;
    errorAgreement.value = _errorAgreement;
};

// 提交申请
const submitApplication = async () => {
    if (!canSubmit.value) {
        uni.showToast({
            title: '请完善申请信息',
            icon: 'none'
        });
        // 自动滚动到第一个未填写项
        const _selectedLab = selectedLab.value;
        const _selectedDate = selectedDate.value;
        const _startTime = startTime.value;
        const _endTime = endTime.value;
        const _selectedType = selectedType.value;
        const _title = title.value;
        const _purpose = purpose.value;
        const _contact = contact.value;
        const _agreed = agreed.value;
        let targetId = '';
        if (!_selectedLab) targetId = 'labItem';
        else if (!_selectedDate) targetId = 'dateItem';
        else if (!_startTime || !_endTime) targetId = 'timeItem';
        else if (!_selectedType) targetId = 'typeItem';
        else if (!_title.trim()) targetId = 'titleItem';
        else if (!_purpose.trim()) targetId = 'purposeItem';
        else if (!/^1[3-9]\d{9}$/.test(_contact)) targetId = 'contactItem';
        else if (!_agreed) {
            targetId = 'formScroll';
        }
        if (targetId) {
            uni.pageScrollTo({
                selector: `#${targetId}`,
                duration: 300
            });
        }
        return;
    }

    // 提交真实预约申请
    uni.showLoading({
        title: '提交中...'
    });
    try {
        await createStudentReservation({
            labId: (selectedLab.value && selectedLab.value.id) || '',
            date: selectedDate.value,
            startTime: startTime.value,
            endTime: endTime.value,
            studentCount: studentCount.value,
            applicationType: (selectedType.value && selectedType.value.id) || '',
            title: title.value,
            purpose: purpose.value,
            teacher: teacher.value,
            contact: contact.value,
            requirements: requirements.value,
            emergencyContact: emergencyContact.value,
            emergencyPhone: emergencyPhone.value
        });
        uni.hideLoading();

        // 清除保存的草稿
        try {
            uni.removeStorageSync('studentApplicationDraft');
            console.log('草稿已清除');
        } catch (e) {
            console.log('CatchClause', e);
            console.error('清除草稿失败:', e);
        }

        uni.showToast({
            title: '提交成功',
            icon: 'success'
        });
        // 跳转待办流程页
        setTimeout(() => {
            uni.redirectTo({
                url: '/pages/student-pending-process/student-pending-process'
            });
        }, 1200);
    } catch (err: any) {
        uni.hideLoading();
        uni.showToast({
            title: err?.data?.message || '提交失败，请重试',
            icon: 'none'
        });
    }
};

// 阻止事件冒泡
const stopPropagation = () => {
    // 阻止事件冒泡
};

const showLabPicker = () => {
    labPickerOptions.value = labs.value;
    tempLabIndex.value = labIndex.value === -1 ? 0 : labIndex.value;
    showLabPickerModal.value = true;
};

const hideLabPicker = () => {
    showLabPickerModal.value = false;
};

const onLabPickerChange = (e: any) => {
    const index = e.detail.value[0];
    tempLabIndex.value = index;
};

const confirmLabSelection = () => {
    const lab = labPickerOptions.value[tempLabIndex.value];
    const _maxStudents = Math.min(lab.maxStudents || 80, 80);
    labIndex.value = tempLabIndex.value;
    selectedLab.value = lab;
    maxStudents.value = _maxStudents;
    studentCount.value = Math.min(studentCount.value, _maxStudents);
    showLabPickerModal.value = false;
    validateForm();
};

const showDatePicker = () => {
    initDatePickerData(selectedDate.value);
    showDateModal.value = true;
};

const hideDatePicker = () => {
    showDateModal.value = false;
};

const initDatePickerData = (selDate: any = '') => {
    const today = new Date();
    const startYear = today.getFullYear() - 2;
    const _years = [];
    for (let i = 0; i <= 2026 - startYear; i++) {
        _years.push(startYear + i);
    }
    let year;
    let month;
    let day;
    if (selDate) {
        [year, month, day] = selDate.split('-').map(Number);
    } else {
        year = today.getFullYear();
        month = today.getMonth() + 1;
        day = today.getDate();
    }
    let _months;
    if (year === 2026) {
        _months = Array.from(
            {
                length: 9
            },
            (_, i) => i + 1
        );
        if (month > 9) {
            month = 9;
        }
    } else {
        _months = Array.from(
            {
                length: 12
            },
            (_, i) => i + 1
        );
    }
    const _days = Array.from(
        {
            length: new Date(year, month, 0).getDate()
        },
        (_, i) => i + 1
    );
    const _tempYearIndex = _years.indexOf(year);
    const _tempMonthIndex = _months.indexOf(month);
    const _tempDayIndex = _days.indexOf(day);
    years.value = _years;
    months.value = _months;
    days.value = _days;
    tempYearIndex.value = _tempYearIndex;
    tempMonthIndex.value = _tempMonthIndex;
    tempDayIndex.value = _tempDayIndex;
};

const onDatePickerChange = (e: any) => {
    const [yearIdx, monthIdx, dayIdx] = e.detail.value;
    const year = years.value[yearIdx];
    let _months;
    if (year === 2026) {
        _months = Array.from(
            {
                length: 9
            },
            (_, i) => i + 1
        );
    } else {
        _months = Array.from(
            {
                length: 12
            },
            (_, i) => i + 1
        );
    }
    let month = _months[monthIdx];
    const _days = Array.from(
        {
            length: new Date(year, month, 0).getDate()
        },
        (_, i) => i + 1
    );
    let _tempDayIndex = dayIdx;
    if (_tempDayIndex >= _days.length) {
        _tempDayIndex = _days.length - 1;
    }
    tempYearIndex.value = yearIdx;
    months.value = _months;
    tempMonthIndex.value = monthIdx;
    days.value = _days;
    tempDayIndex.value = _tempDayIndex;
};

const confirmDatePicker = () => {
    const year = years.value[tempYearIndex.value];
    const month = months.value[tempMonthIndex.value];
    const day = days.value[tempDayIndex.value];
    const _selectedDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    selectedDate.value = _selectedDate;
    showDateModal.value = false;
    validateForm();
};

const showStartTimePickerFun = () => {
    showStartTimePicker.value = true;
};

const hideStartTimePicker = () => {
    showStartTimePicker.value = false;
};

const onStartTimeChange = (e: any) => {
    const [hourIdx, minIdx] = e.detail.value;
    startHourIndex.value = hourIdx;
    startMinuteIndex.value = minIdx;
};

const confirmStartTime = () => {
    const hour = hours.value[startHourIndex.value];
    const min = minutes.value[startMinuteIndex.value];
    const _startTime = `${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
    startTime.value = _startTime;
    showStartTimePicker.value = false;
    validateTimeRange();
};

const showEndTimePickerFun = () => {
    showEndTimePicker.value = true;
};

const hideEndTimePicker = () => {
    showEndTimePicker.value = false;
};

const onEndTimeChange = (e: any) => {
    const [hourIdx, minIdx] = e.detail.value;
    endHourIndex.value = hourIdx;
    endMinuteIndex.value = minIdx;
};

const confirmEndTime = () => {
    const hour = hours.value[endHourIndex.value];
    const min = minutes.value[endMinuteIndex.value];
    const _endTime = `${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
    endTime.value = _endTime;
    showEndTimePicker.value = false;
    validateTimeRange();
};

const validateTimeRange = () => {
    const _startTime = startTime.value;
    const _endTime = endTime.value;
    if (_startTime && _endTime) {
        const [sh, sm] = _startTime.split(':').map(Number);
        const [eh, em] = _endTime.split(':').map(Number);
        const start = sh * 60 + sm;
        const end = eh * 60 + em;
        if (end <= start) {
            uni.showToast({
                title: '结束时间必须大于开始时间',
                icon: 'none'
            });
            endTime.value = '';
            validateForm();
        } else {
            validateForm();
        }
    } else {
        validateForm();
    }
};

const showTypePicker = () => {
    typePickerOptions.value = applicationTypes.value;
    tempTypeIndex.value = typeIndex.value === -1 ? 0 : typeIndex.value;
    showTypePickerModal.value = true;
};

const hideTypePicker = () => {
    showTypePickerModal.value = false;
};

const onTypePickerChange = (e: any) => {
    const index = e.detail.value[0];
    tempTypeIndex.value = index;
};

const confirmTypePicker = () => {
    const type = typePickerOptions.value[tempTypeIndex.value];
    typeIndex.value = tempTypeIndex.value;
    selectedType.value = type;
    showTypePickerModal.value = false;
    validateForm();
};
</script>
<style lang="less">
@import './student-reservation-apply.less';
</style>
