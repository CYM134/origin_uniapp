<template>
    <view class="page-wrapper">
        <!-- admin-schedule-management.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="课表管理" color="white" background="#F5A623" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <navigation-bar title="课表管理" :back="true" color="white" background="#F5A623"></navigation-bar>
        <!-- #endif -->

        <view class="container">
            <!-- 导入导出选项卡 -->
            <view class="tabs">
                <view :class="'tab ' + (activeTab === 'import' ? 'active' : '')" @tap="switchTab" data-tab="import">导入课表</view>
                <view :class="'tab ' + (activeTab === 'export' ? 'active' : '')" @tap="switchTab" data-tab="export">导出课表</view>
            </view>

            <!-- 导入课表内容 -->
            <view class="tab-content" v-if="activeTab === 'import'">
                <view class="section">
                    <view class="section-title">选择学期</view>
                    <view class="custom-picker" @tap="showSemesterPicker">
                        <view class="picker">
                            <text class="picker-text">{{ semesters[semesterIndex] }}</text>
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
            <view class="tab-content" v-if="activeTab === 'export'">
                <view class="section">
                    <view class="section-title">选择学期</view>
                    <view class="custom-picker" @tap="showExportSemesterPicker">
                        <view class="picker">
                            <text class="picker-text">{{ semesters[exportSemesterIndex] }}</text>
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
                        <label class="format-item">
                            <radio value="pdf" :checked="exportFormat === 'pdf'" />
                            <text>PDF格式(.pdf)</text>
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
                            <text>包含学生名单</text>
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
                        <view class="result-details" v-if="!importSuccess">
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

            <view class="footer">
                <text>© SCNU IBC实验室预约管理系统</text>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import navigationBar from "@/components/navigation-bar/navigation-bar.vue";
import { getSemesters, createImportBatch, getImportBatch, createExportTask } from "@/api/admin";
const activeTab = ref("import");
const semesters = ref([]);
const semesterList = ref([]);
const loadSemesters = async () => {
    try {
        const list = await getSemesters();
        const arr = Array.isArray(list) ? list : [];
        semesterList.value = arr;
        semesters.value = arr.map((item) => item.semesterName);
        if (semesterIndex.value >= semesters.value.length) semesterIndex.value = 0;
        if (exportSemesterIndex.value >= semesters.value.length) exportSemesterIndex.value = 0;
    } catch (err) {
        semesterList.value = [];
        semesters.value = [];
        uni.showToast({ title: err?.data?.message || "加载失败", icon: "none" });
    }
};
onMounted(() => { loadSemesters(); });
const semesterIndex = ref(0);
const exportSemesterIndex = ref(0);
const showSemesterPickerModal = ref(false);
const showExportSemesterPickerModal = ref(false);
const tempSemesterIndex = ref(0);
const tempExportSemesterIndex = ref(0);
const importFile = ref(null);
const showProgressModal = ref(false);
const importProgress = ref(0);
const progressStatus = ref("准备导入...");
const showResultModal = ref(false);
const importSuccess = ref(false);
const resultMessage = ref("");
const errorDetails = ref("");
const exportFormat = ref("excel");
const exportOptions = reactive({ includeRooms: true, includeTeachers: true, includeStudents: false });
const switchTab = (e) => { activeTab.value = e.currentTarget.dataset.tab; };
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
const chooseFile = () => { uni.showToast({ title: "选择文件功能开发中", icon: "none" }); setTimeout(() => { importFile.value = { name: "2023-2024-1学期课表.xlsx", size: "256KB", path: "temp/2023-2024-1.xlsx" }; }, 1000); };
const previewTemplate = () => { uni.showToast({ title: "模板下载功能开发中", icon: "none" }); };
const importSchedule = async () => { if (!importFile.value) { uni.showToast({ title: "请先选择文件", icon: "none" }); return; } showProgressModal.value = true; importProgress.value = 0; progressStatus.value = "准备导入..."; try { const semester = semesterList.value[semesterIndex.value]; const batch = await createImportBatch({ semesterId: semester?.id, fileName: importFile.value.name }); importProgress.value = 100; progressStatus.value = "完成导入..."; const success = batch?.status ? batch.status === "success" || batch.status === "completed" : true; setTimeout(() => { showProgressModal.value = false; showImportResult(success, batch); }, 500); } catch (err) { showProgressModal.value = false; showImportResult(false, { errorDetail: err?.data?.message || "加载失败" }); } };
const showImportResult = (success, batch) => { let message = ""; let errDetails = ""; if (success) { message = (batch && batch.message) || "课表导入成功！"; } else { message = (batch && batch.message) || "课表导入失败"; errDetails = (batch && (batch.errorDetail || batch.errorDetails || batch.message)) || "导入失败，请检查Excel文件格式是否正确，并确保所有必填字段已填写。"; } showResultModal.value = true; importSuccess.value = success; resultMessage.value = message; errorDetails.value = errDetails; };
const hideResultModal = () => { showResultModal.value = false; if (importSuccess.value) { importFile.value = null; } };
const exportSchedule = () => { let optionsText = []; if (exportOptions.includeRooms) { optionsText.push("教室信息"); } if (exportOptions.includeTeachers) { optionsText.push("教师信息"); } if (exportOptions.includeStudents) { optionsText.push("学生名单"); } const formatText = exportFormat.value === "excel" ? "Excel格式" : "PDF格式"; const optionsString = optionsText.length > 0 ? "，包含" + optionsText.join("、") : ""; uni.showModal({ title: "确认导出", content: "您将导出" + semesters.value[exportSemesterIndex.value] + "的课表，" + formatText + optionsString + "。确定继续吗？", confirmColor: "#3a7bd5", success: async (res) => { if (res.confirm) { uni.showLoading({ title: "导出中..." }); try { const semester = semesterList.value[exportSemesterIndex.value]; await createExportTask({ semesterId: semester?.id, exportFormat: exportFormat.value, includeRooms: exportOptions.includeRooms, includeTeachers: exportOptions.includeTeachers, includeStudents: exportOptions.includeStudents }); uni.hideLoading(); uni.showToast({ title: "导出成功", icon: "success" }); } catch (err) { uni.hideLoading(); uni.showToast({ title: err?.data?.message || "加载失败", icon: "none" }); } } } }); };
</script>
<style lang="less">
@import './admin-schedule-management.less';
</style>
