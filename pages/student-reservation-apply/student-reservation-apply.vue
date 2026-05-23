<template>
    <view>
        <!-- pages/student-reservation-apply/student-reservation-apply.wxml -->
        <navigation-bar title="预约申请" :back="true" color="white" background="#3a7bd5"></navigation-bar>

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

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
// pages/student-reservation-apply/student-reservation-apply.ts
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            // 表单数据
            selectedLab: null as any,
            labIndex: -1,
            selectedDate: '',
            studentCount: 1,
            selectedType: null as any,
            typeIndex: -1,
            title: '',
            purpose: '',
            teacher: '',
            contact: '',
            requirements: '',
            emergencyContact: '',
            emergencyPhone: '',
            // 选项数据
            labs: [
                {
                    id: 'lab1',
                    name: '国际课程实验室',
                    maxStudents: 30
                },
                {
                    id: 'lab2',
                    name: '新商科实验室',
                    maxStudents: 25
                },
                {
                    id: 'lab3',
                    name: 'VR实验室',
                    maxStudents: 20
                },
                {
                    id: 'lab4',
                    name: '法语实验室',
                    maxStudents: 35
                },
                {
                    id: 'lab5',
                    name: '402实验室',
                    maxStudents: 28
                }
            ],
            applicationTypes: [
                {
                    id: 'course',
                    name: '课程实验'
                },
                {
                    id: 'research',
                    name: '科研项目'
                },
                {
                    id: 'competition',
                    name: '竞赛培训'
                },
                {
                    id: 'activity',
                    name: '学术活动'
                },
                {
                    id: 'other',
                    name: '其他'
                }
            ],
            // 日期限制,人数限制
            minDate: '',
            maxDate: '',
            maxStudents: 80,
            // 协议相关
            agreed: false,
            showAgreement: false,
            // 表单验证
            canSubmit: false,
            showLabPickerModal: false,
            labPickerOptions: [] as any[],
            tempLabIndex: 0,
            showDateModal: false,
            years: [] as number[],
            months: [] as number[],
            days: [] as number[],
            tempYearIndex: 0,
            tempMonthIndex: 0,
            tempDayIndex: 0,
            showStartTimePicker: false,
            showEndTimePicker: false,
            hours: Array.from(
                {
                    length: 24
                },
                (_, i) => i
            ),
            minutes: Array.from(
                {
                    length: 60
                },
                (_, i) => i
            ),
            startHourIndex: 8,
            startMinuteIndex: 0,
            endHourIndex: 10,
            endMinuteIndex: 0,
            startTime: '',
            endTime: '',
            showTypePickerModal: false,
            typePickerOptions: [] as any[],
            tempTypeIndex: 0,
            // 错误提示
            errorLab: '',
            errorDate: '',
            errorTime: '',
            errorType: '',
            errorTitle: '',
            errorPurpose: '',
            errorContact: '',
            errorAgreement: ''
        };
    },
    onLoad() {
        this.initializeDates();
        this.loadUserInfo();
        // 默认时间，避免未选时间导致无法提交
        this.setData({
            startTime: '08:00',
            endTime: '09:50'
        });
    },
    onShow() {
        this.validateForm();
        // 尝试加载草稿
        this.loadDraft();
    },
    methods: {
        /**
         * 加载草稿
         */
        loadDraft() {
            try {
                const draft = uni.getStorageSync('studentApplicationDraft');
                if (draft) {
                    // 询问是否加载草稿
                    uni.showModal({
                        title: '发现草稿',
                        content: '是否加载之前保存的草稿？',
                        success: (res) => {
                            if (res.confirm) {
                                this.applyDraft(draft);
                            }
                        }
                    });
                }
            } catch (error) {
                console.log('CatchClause', error);
                console.log('CatchClause', error);
                console.error('加载草稿失败:', error);
            }
        },

        /**
         * 应用草稿数据
         */
        applyDraft(draft: any) {
            // 设置实验室
            const labIndex = this.labs.findIndex((lab) => lab.id === draft.labId);
            if (labIndex !== -1) {
                this.setData({
                    labIndex: labIndex,
                    selectedLab: this.labs[labIndex],
                    maxStudents: this.labs[labIndex].maxStudents
                });
            }

            // 设置申请类型
            const typeIndex = this.applicationTypes.findIndex((type) => type.id === draft.typeId);
            if (typeIndex !== -1) {
                this.setData({
                    typeIndex: typeIndex,
                    selectedType: this.applicationTypes[typeIndex]
                });
            }

            // 设置其他数据
            this.setData({
                selectedDate: draft.date || '',
                startTime: draft.startTime || '',
                endTime: draft.endTime || '',
                studentCount: draft.studentCount || 1,
                title: draft.title || '',
                purpose: draft.purpose || '',
                teacher: draft.teacher || '',
                contact: draft.contact || '',
                requirements: draft.requirements || '',
                emergencyContact: draft.emergencyContact || '',
                emergencyPhone: draft.emergencyPhone || ''
            });
            this.validateForm();
            uni.showToast({
                title: '草稿加载成功',
                icon: 'success'
            });
        },

        /**
         * 保存草稿
         */
        saveDraft() {
            try {
                // 检查是否有必要的数据可以保存
                if (!this.selectedLab && !this.selectedDate && !this.title && !this.purpose) {
                    uni.showToast({
                        title: '表单为空，无需保存',
                        icon: 'none'
                    });
                    return;
                }
                const draft = {
                    labId: (this.selectedLab && this.selectedLab.id) || '',
                    date: this.selectedDate,
                    startTime: this.startTime,
                    endTime: this.endTime,
                    studentCount: this.studentCount,
                    typeId: (this.selectedType && this.selectedType.id) || '',
                    title: this.title,
                    purpose: this.purpose,
                    teacher: this.teacher,
                    contact: this.contact,
                    requirements: this.requirements,
                    emergencyContact: this.emergencyContact,
                    emergencyPhone: this.emergencyPhone,
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
        },

        // 初始化日期范围
        initializeDates() {
            const today = new Date();
            const tomorrow = new Date(today);
            tomorrow.setDate(today.getDate() + 1);
            const maxDate = new Date(today);
            maxDate.setDate(today.getDate() + 30); // 最多提前30天预约

            this.setData({
                minDate: this.formatDate(tomorrow),
                maxDate: this.formatDate(maxDate)
            });
        },

        // 格式化日期
        formatDate(date: Date): string {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}`;
        },

        // 加载用户信息
        loadUserInfo() {
            try {
                const userInfo = uni.getStorageSync('studentInfo');
                if (userInfo && userInfo.phone) {
                    this.setData({
                        contact: userInfo.phone
                    });
                }
            } catch (error) {
                console.log('CatchClause', error);
                console.log('CatchClause', error);
                console.error('加载用户信息失败:', error);
            }
        },

        // 实验室选择
        onLabChange(e: any) {
            const index = e.detail.value;
            const selectedLab = this.labs[index];
            this.setData({
                labIndex: index,
                selectedLab,
                maxStudents: selectedLab.maxStudents,
                studentCount: Math.min(this.studentCount, selectedLab.maxStudents)
            });
            this.validateForm();
        },

        // 日期选择
        onDateChange(e: any) {
            this.setData({
                selectedDate: e.detail.value
            });
            this.validateForm();
        },

        // 人数调整
        decreaseCount() {
            const count = Math.max(1, this.studentCount - 1);
            this.setData({
                studentCount: count
            });
            this.validateForm();
        },

        increaseCount() {
            const count = Math.min(this.maxStudents, this.studentCount + 1);
            this.setData({
                studentCount: count
            });
            this.validateForm();
        },

        onCountInput(e: any) {
            const value = parseInt(e.detail.value) || 1;
            const count = Math.max(1, Math.min(this.maxStudents, value));
            this.setData({
                studentCount: count
            });
            this.validateForm();
        },

        // 申请类型选择
        onTypeChange(e: any) {
            const index = e.detail.value;
            this.setData({
                typeIndex: index,
                selectedType: this.applicationTypes[index]
            });
            this.validateForm();
        },

        // 输入处理
        onTitleInput(e: any) {
            this.setData({
                title: e.detail.value
            });
            this.validateForm();
        },

        onPurposeInput(e: any) {
            this.setData({
                purpose: e.detail.value
            });
            this.validateForm();
        },

        onTeacherInput(e: any) {
            this.setData({
                teacher: e.detail.value
            });
        },

        onContactInput(e: any) {
            this.setData({
                contact: e.detail.value
            });
            this.validateForm();
        },

        onRequirementsInput(e: any) {
            this.setData({
                requirements: e.detail.value
            });
        },

        onEmergencyContactInput(e: any) {
            this.setData({
                emergencyContact: e.detail.value
            });
        },

        onEmergencyPhoneInput(e: any) {
            this.setData({
                emergencyPhone: e.detail.value
            });
        },

        // 协议相关
        onAgreementChange(e: any) {
            this.setData({
                agreed: e.detail.value.includes('agree')
            });
            this.validateForm();
        },

        viewAgreement() {
            this.setData({
                showAgreement: true
            });
        },

        closeAgreement() {
            this.setData({
                showAgreement: false
            });
        },

        // 表单验证
        validateForm() {
            const { selectedLab, selectedDate, startTime, endTime, selectedType, title, purpose, contact, agreed } = this;
            let errorLab = '';
            let errorDate = '';
            let errorTime = '';
            let errorType = '';
            let errorTitle = '';
            let errorPurpose = '';
            let errorContact = '';
            let errorAgreement = '';
            const isPhoneValid = /^1[3-9]\d{9}$/.test(contact);
            if (!selectedLab) {
                errorLab = '请选择实验室';
            }
            if (!selectedDate) {
                errorDate = '请选择预约日期';
            }
            if (!startTime || !endTime) {
                errorTime = '请选择时间段';
            } else {
                const [sh, sm] = startTime.split(':').map(Number);
                const [eh, em] = endTime.split(':').map(Number);
                if (eh * 60 + em <= sh * 60 + sm) {
                    errorTime = '结束时间必须大于开始时间';
                }
            }
            if (!selectedType) {
                errorType = '请选择申请类型';
            }
            if (!title.trim()) {
                errorTitle = '请输入申请主题';
            }
            if (!purpose.trim()) {
                errorPurpose = '请输入使用目的';
            }
            if (!contact) errorContact = '请输入手机号';
            else if (!isPhoneValid) {
                errorContact = '请输入有效的手机号';
            }
            if (!agreed) {
                errorAgreement = '请阅读并同意实验室使用协议';
            }
            const canSubmit = !errorLab && !errorDate && !errorTime && !errorType && !errorTitle && !errorPurpose && !errorContact && !errorAgreement;
            this.setData({
                canSubmit,
                errorLab,
                errorDate,
                errorTime,
                errorType,
                errorTitle,
                errorPurpose,
                errorContact,
                errorAgreement
            });
        },

        // 提交申请
        submitApplication() {
            if (!this.canSubmit) {
                uni.showToast({
                    title: '请完善申请信息',
                    icon: 'none'
                });
                // 自动滚动到第一个未填写项
                const { selectedLab, selectedDate, startTime, endTime, selectedType, title, purpose, contact, agreed } = this;
                let targetId = '';
                if (!selectedLab) targetId = 'labItem';
                else if (!selectedDate) targetId = 'dateItem';
                else if (!startTime || !endTime) targetId = 'timeItem';
                else if (!selectedType) targetId = 'typeItem';
                else if (!title.trim()) targetId = 'titleItem';
                else if (!purpose.trim()) targetId = 'purposeItem';
                else if (!/^1[3-9]\d{9}$/.test(contact)) targetId = 'contactItem';
                else if (!agreed) {
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

            // 获取当前登录学生信息
            const currentStudent = uni.getStorageSync('studentInfo');
            const applicationData = {
                id: 'app_' + Date.now(),
                lab: this.selectedLab,
                date: this.selectedDate,
                startTime: this.startTime,
                endTime: this.endTime,
                studentCount: this.studentCount,
                type: this.selectedType,
                title: this.title,
                purpose: this.purpose,
                teacher: this.teacher,
                contact: this.contact,
                requirements: this.requirements,
                emergencyContact: this.emergencyContact,
                emergencyPhone: this.emergencyPhone,
                status: 'pending',
                statusText: '待审核',
                submitTime: new Date().toISOString(),
                applicant: (currentStudent && currentStudent.name) || '未知',
                studentId: (currentStudent && currentStudent.studentId) || '',
                // 添加学生ID
                studentName: (currentStudent && currentStudent.name) || '未知' // 添加学生姓名
            };
            uni.showLoading({
                title: '提交中...'
            });

            // 模拟提交请求
            setTimeout(() => {
                uni.hideLoading();

                // 保存到本地存储
                try {
                    const applications = uni.getStorageSync('studentApplications') || [];
                    applications.unshift(applicationData);
                    uni.setStorageSync('studentApplications', applications);

                    // 清除保存的草稿
                    try {
                        uni.removeStorageSync('studentApplicationDraft');
                        console.log('草稿已清除');
                    } catch (e) {
                        console.log('CatchClause', e);
                        console.log('CatchClause', e);
                        console.error('清除草稿失败:', e);
                    }
                    uni.showModal({
                        title: '申请提交成功',
                        content: '您的预约申请已提交，请等待审核结果。您可以在"待办流程"中查看申请状态。',
                        showCancel: false,
                        success: () => {
                            uni.navigateBack();
                        }
                    });
                } catch (error) {
                    console.log('CatchClause', error);
                    console.log('CatchClause', error);
                    uni.showToast({
                        title: '提交失败，请重试',
                        icon: 'none'
                    });
                }
            }, 2000);
        },

        // 阻止事件冒泡
        stopPropagation() {
            // 阻止事件冒泡
        },

        showLabPicker() {
            this.setData({
                labPickerOptions: this.labs,
                tempLabIndex: this.labIndex === -1 ? 0 : this.labIndex,
                showLabPickerModal: true
            });
        },

        hideLabPicker() {
            this.setData({
                showLabPickerModal: false
            });
        },

        onLabPickerChange(e: any) {
            const index = e.detail.value[0];
            this.setData({
                tempLabIndex: index
            });
        },

        confirmLabSelection() {
            const lab = this.labPickerOptions[this.tempLabIndex];
            const maxStudents = Math.min(lab.maxStudents || 80, 80);
            this.setData(
                {
                    labIndex: this.tempLabIndex,
                    selectedLab: lab,
                    maxStudents,
                    studentCount: Math.min(this.studentCount, maxStudents),
                    showLabPickerModal: false
                },
                () => {
                    this.validateForm();
                }
            );
        },

        showDatePicker() {
            this.initDatePickerData(this.selectedDate);
            this.setData({
                showDateModal: true
            });
        },

        hideDatePicker() {
            this.setData({
                showDateModal: false
            });
        },

        initDatePickerData(selectedDate: any = '') {
            const today = new Date();
            const startYear = today.getFullYear() - 2;
            const years = [];
            for (let i = 0; i <= 2026 - startYear; i++) {
                years.push(startYear + i);
            }
            let year;
            let month;
            let day;
            if (selectedDate) {
                [year, month, day] = selectedDate.split('-').map(Number);
            } else {
                year = today.getFullYear();
                month = today.getMonth() + 1;
                day = today.getDate();
            }
            let months;
            if (year === 2026) {
                months = Array.from(
                    {
                        length: 9
                    },
                    (_, i) => i + 1
                );
                if (month > 9) {
                    month = 9;
                }
            } else {
                months = Array.from(
                    {
                        length: 12
                    },
                    (_, i) => i + 1
                );
            }
            const days = Array.from(
                {
                    length: new Date(year, month, 0).getDate()
                },
                (_, i) => i + 1
            );
            const tempYearIndex = years.indexOf(year);
            const tempMonthIndex = months.indexOf(month);
            const tempDayIndex = days.indexOf(day);
            this.setData({
                years,
                months,
                days,
                tempYearIndex,
                tempMonthIndex,
                tempDayIndex
            });
        },

        onDatePickerChange(e: any) {
            const [yearIdx, monthIdx, dayIdx] = e.detail.value;
            const year = this.years[yearIdx];
            let months;
            if (year === 2026) {
                months = Array.from(
                    {
                        length: 9
                    },
                    (_, i) => i + 1
                );
            } else {
                months = Array.from(
                    {
                        length: 12
                    },
                    (_, i) => i + 1
                );
            }
            let month = months[monthIdx];
            const days = Array.from(
                {
                    length: new Date(year, month, 0).getDate()
                },
                (_, i) => i + 1
            );
            let tempDayIndex = dayIdx;
            if (tempDayIndex >= days.length) {
                tempDayIndex = days.length - 1;
            }
            this.setData({
                tempYearIndex: yearIdx,
                months,
                tempMonthIndex: monthIdx,
                days,
                tempDayIndex
            });
        },

        confirmDatePicker() {
            const year = this.years[this.tempYearIndex];
            const month = this.months[this.tempMonthIndex];
            const day = this.days[this.tempDayIndex];
            const selectedDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            this.setData(
                {
                    selectedDate,
                    showDateModal: false
                },
                () => {
                    this.validateForm();
                }
            );
        },

        showStartTimePickerFun() {
            this.setData({
                showStartTimePicker: true
            });
        },

        hideStartTimePicker() {
            this.setData({
                showStartTimePicker: false
            });
        },

        onStartTimeChange(e: any) {
            const [hourIdx, minIdx] = e.detail.value;
            this.setData({
                startHourIndex: hourIdx,
                startMinuteIndex: minIdx
            });
        },

        confirmStartTime() {
            const hour = this.hours[this.startHourIndex];
            const min = this.minutes[this.startMinuteIndex];
            const startTime = `${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
            this.setData(
                {
                    startTime,
                    showStartTimePicker: false
                },
                () => {
                    this.validateTimeRange();
                }
            );
        },

        showEndTimePickerFun() {
            this.setData({
                showEndTimePicker: true
            });
        },

        hideEndTimePicker() {
            this.setData({
                showEndTimePicker: false
            });
        },

        onEndTimeChange(e: any) {
            const [hourIdx, minIdx] = e.detail.value;
            this.setData({
                endHourIndex: hourIdx,
                endMinuteIndex: minIdx
            });
        },

        confirmEndTime() {
            const hour = this.hours[this.endHourIndex];
            const min = this.minutes[this.endMinuteIndex];
            const endTime = `${String(hour).padStart(2, '0')}:${String(min).padStart(2, '0')}`;
            this.setData(
                {
                    endTime,
                    showEndTimePicker: false
                },
                () => {
                    this.validateTimeRange();
                }
            );
        },

        validateTimeRange() {
            const { startTime, endTime } = this;
            if (startTime && endTime) {
                const [sh, sm] = startTime.split(':').map(Number);
                const [eh, em] = endTime.split(':').map(Number);
                const start = sh * 60 + sm;
                const end = eh * 60 + em;
                if (end <= start) {
                    uni.showToast({
                        title: '结束时间必须大于开始时间',
                        icon: 'none'
                    });
                    this.setData(
                        {
                            endTime: ''
                        },
                        () => {
                            this.validateForm();
                        }
                    );
                } else {
                    this.validateForm();
                }
            } else {
                this.validateForm();
            }
        },

        showTypePicker() {
            this.setData({
                typePickerOptions: this.applicationTypes,
                tempTypeIndex: this.typeIndex === -1 ? 0 : this.typeIndex,
                showTypePickerModal: true
            });
        },

        hideTypePicker() {
            this.setData({
                showTypePickerModal: false
            });
        },

        onTypePickerChange(e: any) {
            const index = e.detail.value[0];
            this.setData({
                tempTypeIndex: index
            });
        },

        confirmTypePicker() {
            const type = this.typePickerOptions[this.tempTypeIndex];
            this.setData(
                {
                    typeIndex: this.tempTypeIndex,
                    selectedType: type,
                    showTypePickerModal: false
                },
                () => {
                    this.validateForm();
                }
            );
        }
    }
});
</script>
<style>
@import './student-reservation-apply.css';
</style>
