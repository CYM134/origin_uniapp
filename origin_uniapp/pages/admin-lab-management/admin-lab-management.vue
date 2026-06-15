<template>
    <view class="page-wrapper">
        <!-- admin-lab-management.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="实验室信息管理" color="white" background="#F5A623" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <navigation-bar title="实验室信息管理" :back="true" color="white" background="#F5A623"></navigation-bar>
        <!-- #endif -->
        <view class="container">
            <view class="header">
                <view class="search-box">
                    <input type="text" placeholder="搜索实验室" @input="onSearchInput" :value="searchKeyword" />
                    <view class="search-icon">
                        <image src="/static/images/icons/search-icon.png" mode="aspectFit"></image>
                    </view>
                </view>
                <button class="add-btn" @tap="showAddLabModal">添加实验室</button>
            </view>

            <!-- 实验室列表 -->
            <view class="lab-list">
                <block v-if="filteredLabs.length > 0">
                    <view class="lab-item" @tap="showLabDetail" :data-id="item.id" v-for="(item, index) in filteredLabs" :key="index">
                        <view class="lab-info">
                            <view class="lab-name">{{ item.name }}</view>
                            <view class="lab-location">位置：{{ item.location }}</view>
                            <view class="lab-equipment">设备：{{ item.equipment }}</view>
                        </view>

                        <view class="lab-actions">
                            <view class="action-btn edit" @tap.stop.prevent="showEditLabModal" :data-id="item.id">
                                <image src="/static/images/icons/edit-icon.png" mode="aspectFit"></image>
                            </view>
                            <view class="action-btn delete" @tap.stop.prevent="showDeleteConfirm" :data-id="item.id" :data-name="item.name">
                                <image src="/static/images/icons/delete-icon.png" mode="aspectFit"></image>
                            </view>
                        </view>
                    </view>
                </block>
                <view v-else class="empty-list">
                    <image src="/static/images/icons/empty.png" mode="aspectFit"></image>
                    <text>暂无实验室信息</text>
                </view>
            </view>

            <!-- 添加/编辑实验室弹窗 -->
            <view class="modal" v-if="showModal">
                <view class="modal-mask" @tap="hideModal"></view>
                <view class="modal-content">
                    <view class="modal-header">
                        <text>{{ isEditing ? '编辑实验室' : '添加实验室' }}</text>
                    </view>
                    <view class="modal-body">
                        <view class="form-item">
                            <text class="label">实验室名称</text>
                            <input
                                class="fixed-input"
                                type="text"
                                placeholder="请输入实验室名称"
                                :value="currentLab.name"
                                @input="onLabNameInput"
                                style="height: 88rpx; display: flex; align-items: center"
                            />
                        </view>
                        <view class="form-item">
                            <text class="label">位置</text>
                            <input
                                class="fixed-input"
                                type="text"
                                placeholder="请输入实验室位置"
                                :value="currentLab.location"
                                @input="onLabLocationInput"
                                style="height: 88rpx; display: flex; align-items: center"
                            />
                        </view>
                        <view class="form-item">
                            <text class="label">设备情况</text>
                            <textarea placeholder="请输入设备情况" :value="currentLab.equipment" @input="onLabEquipmentInput"></textarea>
                        </view>
                        <view class="form-item">
                            <text class="label">预览图片</text>
                            <view class="image-upload" @tap="chooseImage">
                                <image v-if="currentLab.image" :src="currentLab.image" mode="aspectFit"></image>
                                <view v-else class="upload-placeholder">
                                    <image src="/static/images/icons/upload-icon.png" mode="aspectFit"></image>
                                    <text>点击上传图片</text>
                                </view>
                            </view>
                        </view>
                    </view>
                    <view class="modal-footer">
                        <button class="cancel-btn" @tap="hideModal">取消</button>
                        <button class="confirm-btn" @tap="saveLabInfo">保存</button>
                    </view>
                </view>
            </view>

            <!-- 实验室详情弹窗 -->
            <view class="modal" v-if="showDetailModal">
                <view class="modal-mask" @tap="hideDetailModal"></view>
                <view class="modal-content detail-modal">
                    <view class="modal-header">
                        <text>实验室详情</text>
                    </view>
                    <view class="modal-body">
                        <view class="detail-image" v-if="detailLab.image">
                            <image :src="detailLab.image" mode="aspectFit"></image>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">实验室名称</text>
                            <text class="detail-value">{{ detailLab.name }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">位置</text>
                            <text class="detail-value">{{ detailLab.location }}</text>
                        </view>
                        <view class="detail-item">
                            <text class="detail-label">设备情况</text>
                            <text class="detail-value">{{ detailLab.equipment }}</text>
                        </view>
                    </view>
                    <view class="modal-footer">
                        <button class="confirm-btn" @tap="hideDetailModal">关闭</button>
                    </view>
                </view>
            </view>

            <!-- 删除确认密码弹窗 -->
            <view class="modal" v-if="showDeletePasswordModal">
                <view class="modal-mask" @tap="hideDeletePasswordModal"></view>
                <view class="modal-content password-modal">
                    <view class="modal-header">
                        <text>安全验证</text>
                    </view>
                    <view class="modal-body">
                        <view class="password-tip">删除实验室是敏感操作，请输入管理员密码确认：</view>
                        <view class="form-item">
                            <input
                                class="fixed-input"
                                type="password"
                                placeholder="请输入管理员密码"
                                :value="adminPassword"
                                @input="onAdminPasswordInput"
                                style="height: 88rpx; display: flex; align-items: center"
                            />
                        </view>
                    </view>
                    <view class="modal-footer">
                        <button class="cancel-btn" @tap="hideDeletePasswordModal">取消</button>
                        <button class="confirm-btn" @tap="confirmDeleteWithPassword">确认删除</button>
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
import { ref } from 'vue';
import { onMounted } from 'vue';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
import { getLabs, createLab, updateLab, deleteLab as deleteLabApi } from '@/api/admin';
// admin-lab-management.ts

// 实验室列表（数据来源改为后端接口）
const labs = ref<any[]>([]);
const filteredLabs = ref<any[]>([]);
const searchKeyword = ref<string>('');
// 弹窗控制
const showModal = ref<boolean>(false);
const isEditing = ref<boolean>(false);
const currentLab = ref<any>({
    id: '',
    name: '',
    location: '',
    equipment: '',
    image: ''
});
// 详情弹窗
const showDetailModal = ref<boolean>(false);
const detailLab = ref<any>({
    id: '',
    name: '',
    location: '',
    equipment: '',
    image: ''
});
// 删除密码弹窗
const showDeletePasswordModal = ref<boolean>(false);
const adminPassword = ref<string>('');
const labIdToDelete = ref<string>('');

// 从后端加载实验室列表
const loadLabs = async () => {
    try {
        const list = await getLabs(searchKeyword.value);
        const arr = Array.isArray(list) ? list : [];
        // 适配为模板需要的字段形状（保留模板字段名）
        labs.value = arr.map((item: any) => ({
            id: item.id,
            name: item.name,
            location: item.location,
            equipment: item.equipment,
            image: item.image,
            capacity: item.capacity ?? item.maxStudents
        }));
    } catch (err: any) {
        labs.value = [];
        uni.showToast({ title: err?.data?.message || '加载失败', icon: 'none' });
    }
    // 后端已按 keyword 过滤，直接同步到展示列表
    filteredLabs.value = labs.value;
};

onMounted(() => {
    // 处理小程序 attached 生命周期
    // 初始化实验室列表
    loadLabs();
});

// 搜索实验室（走后端关键词过滤）
const onSearchInput = (e: any) => {
    const keyword = e.detail.value.trim();
    searchKeyword.value = keyword;
    loadLabs();
};

// 根据关键词过滤实验室（本地兜底过滤，保留以兼容调用方）
const filterLabs = () => {
    const labsVal = labs.value;
    const keyword = searchKeyword.value;
    if (!keyword) {
        filteredLabs.value = labsVal;
        return;
    }
    const filtered = labsVal.filter((lab) => {
        return lab.name.includes(keyword) || lab.location.includes(keyword) || lab.equipment.includes(keyword);
    });
    filteredLabs.value = filtered;
};

// 显示添加实验室弹窗
const showAddLabModal = () => {
    showModal.value = true;
    isEditing.value = false;
    currentLab.value = {
        id: '',
        name: '',
        location: '',
        equipment: '',
        image: ''
    };
};

// 显示编辑实验室弹窗
const showEditLabModal = (e: any) => {
    const id = e.currentTarget.dataset.id;
    const lab = labs.value.find((item) => String(item.id) === String(id));
    if (lab) {
        showModal.value = true;
        isEditing.value = true;
        currentLab.value = {
            ...lab
        };
    }

    // 阻止事件冒泡
    e.stopPropagation();
};

// 隐藏弹窗
const hideModal = () => {
    showModal.value = false;
};

// 显示实验室详情
const showLabDetail = (e: any) => {
    const id = e.currentTarget.dataset.id;
    const lab = labs.value.find((item) => String(item.id) === String(id));
    if (lab) {
        showDetailModal.value = true;
        detailLab.value = {
            ...lab
        };
    }
};

// 隐藏详情弹窗
const hideDetailModal = () => {
    showDetailModal.value = false;
};

// 表单输入处理
const onLabNameInput = (e: any) => {
    currentLab.value.name = e.detail.value;
};

const onLabLocationInput = (e: any) => {
    currentLab.value.location = e.detail.value;
};

const onLabEquipmentInput = (e: any) => {
    currentLab.value.equipment = e.detail.value;
};

// 选择图片
const chooseImage = () => {
    uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
            // 获取图片临时路径
            const tempFilePath = res.tempFilePaths[0];
            currentLab.value.image = tempFilePath;
        }
    });
};

// 保存实验室信息（新增走 createLab，编辑走 updateLab）
const saveLabInfo = async () => {
    const currentLabVal = currentLab.value;
    const isEditingVal = isEditing.value;

    // 表单验证
    if (!currentLabVal.name.trim()) {
        uni.showToast({
            title: '请输入实验室名称',
            icon: 'none'
        });
        return;
    }
    if (!currentLabVal.location.trim()) {
        uni.showToast({
            title: '请输入实验室位置',
            icon: 'none'
        });
        return;
    }

    // 组装提交字段：name, location, equipment, image, capacity
    const payload: any = {
        name: currentLabVal.name,
        location: currentLabVal.location,
        equipment: currentLabVal.equipment,
        image: currentLabVal.image,
        capacity: currentLabVal.capacity
    };

    try {
        if (isEditingVal) {
            await updateLab(currentLabVal.id, payload);
        } else {
            await createLab(payload);
        }
        showModal.value = false;
        // 重新从后端拉取列表
        await loadLabs();
        // 显示成功提示
        uni.showToast({
            title: isEditingVal ? '编辑成功' : '添加成功',
            icon: 'success'
        });
    } catch (err: any) {
        uni.showToast({
            title: err?.data?.message || (isEditingVal ? '编辑失败' : '添加失败'),
            icon: 'none'
        });
    }
};

// 显示删除确认
const showDeleteConfirm = (e: any) => {
    const id = e.currentTarget.dataset.id;
    const name = e.currentTarget.dataset.name;
    uni.showModal({
        title: '确认删除',
        content: `删除对象：「${name}」| 确定要删除这个实验室吗？`,
        confirmColor: '#3a7bd5',
        success: (res) => {
            if (res.confirm) {
                // 显示密码验证弹窗
                showDeletePasswordModalFun(id);
            }
        }
    });

    // 阻止事件冒泡
    e.stopPropagation();
};

// 显示密码验证弹窗
const showDeletePasswordModalFun = (id: string) => {
    showDeletePasswordModal.value = true;
    adminPassword.value = '';
    labIdToDelete.value = id;
};

// 隐藏密码验证弹窗
const hideDeletePasswordModal = () => {
    showDeletePasswordModal.value = false;
    adminPassword.value = '';
};

// 管理员密码输入
const onAdminPasswordInput = (e: any) => {
    adminPassword.value = e.detail.value;
};

// 确认密码并删除
const confirmDeleteWithPassword = () => {
    const adminPasswordVal = adminPassword.value;
    const labIdToDeleteVal = labIdToDelete.value;

    // 验证密码不能为空（密码弹窗仅作交互确认，真正的鉴权由后端登录态完成）
    if (!adminPasswordVal.trim()) {
        uni.showToast({
            title: '请输入管理员密码',
            icon: 'none'
        });
        return;
    }

    // 执行删除操作（后端按登录态鉴权）
    deleteLab(labIdToDeleteVal);
};

// 删除实验室（调后端 deleteLab，后端按登录态鉴权）
const deleteLab = async (id: string) => {
    try {
        await deleteLabApi(id);
        showDeletePasswordModal.value = false;
        adminPassword.value = '';
        // 重新从后端拉取列表
        await loadLabs();
        // 显示成功提示
        uni.showToast({
            title: '删除成功',
            icon: 'success'
        });
    } catch (err: any) {
        uni.showToast({
            title: err?.data?.message || '删除失败',
            icon: 'none'
        });
    }
};
</script>
<style lang="less">
@import './admin-lab-management.less';
</style>
