<template>
    <view>
        <!-- admin-lab-management.wxml -->
        <navigation-bar title="实验室信息管理" :back="true" color="white" background="#3a7bd5"></navigation-bar>
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

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
// admin-lab-management.ts
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            // 实验室列表
            labs: [
                // 示例数据
                {
                    id: '1',
                    name: '国际课程实验室',
                    location: '综合楼-东A301',
                    equipment: '可移动组合桌椅60套，纳米投影书写墙，话筒2支，服务器2台',
                    image: '/static/images/东A301.png'
                },
                {
                    id: '2',
                    name: 'IBC实验中心',
                    location: '综合楼-西A302',
                    equipment: '开放公共自习室，可容纳百人，打印机投影仪各类设施齐全',
                    image: '/static/images/西A302.jpg'
                },
                {
                    id: '3',
                    name: '互联网+新商科实验室',
                    location: '综合楼-西A303',
                    equipment: '80个智能工位，配备翻盖式电脑，seewo 2台',
                    image: '/static/images/西A303.png'
                }
            ],
            filteredLabs: [] as any[],
            searchKeyword: '',
            // 弹窗控制
            showModal: false,
            isEditing: false,
            currentLab: {
                id: '',
                name: '',
                location: '',
                equipment: '',
                image: ''
            },
            // 详情弹窗
            showDetailModal: false,
            detailLab: {
                id: '',
                name: '',
                location: '',
                equipment: '',
                image: ''
            },
            // 删除密码弹窗
            showDeletePasswordModal: false,
            adminPassword: '',
            labIdToDelete: ''
        };
    },
    mounted() {
        // 处理小程序 attached 生命周期
        this.attached();
    },
    methods: {
        attached() {
            // 初始化过滤后的实验室列表
            this.setData({
                filteredLabs: this.labs
            });
        },

        // 搜索实验室
        onSearchInput(e: any) {
            const keyword = e.detail.value.trim();
            this.setData({
                searchKeyword: keyword
            });
            this.filterLabs();
        },

        // 根据关键词过滤实验室
        filterLabs() {
            const { labs, searchKeyword } = this;
            if (!searchKeyword) {
                this.setData({
                    filteredLabs: labs
                });
                return;
            }
            const filtered = labs.filter((lab) => {
                return lab.name.includes(searchKeyword) || lab.location.includes(searchKeyword) || lab.equipment.includes(searchKeyword);
            });
            this.setData({
                filteredLabs: filtered
            });
        },

        // 显示添加实验室弹窗
        showAddLabModal() {
            this.setData({
                showModal: true,
                isEditing: false,
                currentLab: {
                    id: '',
                    name: '',
                    location: '',
                    equipment: '',
                    image: ''
                }
            });
        },

        // 显示编辑实验室弹窗
        showEditLabModal(e: any) {
            const id = e.currentTarget.dataset.id;
            const lab = this.labs.find((item) => item.id === id);
            if (lab) {
                this.setData({
                    showModal: true,
                    isEditing: true,
                    currentLab: {
                        ...lab
                    }
                });
            }

            // 阻止事件冒泡
            e.stopPropagation();
        },

        // 隐藏弹窗
        hideModal() {
            this.setData({
                showModal: false
            });
        },

        // 显示实验室详情
        showLabDetail(e: any) {
            const id = e.currentTarget.dataset.id;
            const lab = this.labs.find((item) => item.id === id);
            if (lab) {
                this.setData({
                    showDetailModal: true,
                    detailLab: {
                        ...lab
                    }
                });
            }
        },

        // 隐藏详情弹窗
        hideDetailModal() {
            this.setData({
                showDetailModal: false
            });
        },

        // 表单输入处理
        onLabNameInput(e: any) {
            this.setData({
                'currentLab.name': e.detail.value
            });
        },

        onLabLocationInput(e: any) {
            this.setData({
                'currentLab.location': e.detail.value
            });
        },

        onLabEquipmentInput(e: any) {
            this.setData({
                'currentLab.equipment': e.detail.value
            });
        },

        // 选择图片
        chooseImage() {
            uni.chooseImage({
                count: 1,
                sizeType: ['compressed'],
                sourceType: ['album', 'camera'],
                success: (res) => {
                    // 获取图片临时路径
                    const tempFilePath = res.tempFilePaths[0];
                    this.setData({
                        'currentLab.image': tempFilePath
                    });
                }
            });
        },

        // 保存实验室信息
        saveLabInfo() {
            const { currentLab, isEditing, labs } = this;

            // 表单验证
            if (!currentLab.name.trim()) {
                uni.showToast({
                    title: '请输入实验室名称',
                    icon: 'none'
                });
                return;
            }
            if (!currentLab.location.trim()) {
                uni.showToast({
                    title: '请输入实验室位置',
                    icon: 'none'
                });
                return;
            }

            // 克隆当前实验室列表
            const newLabs = [...labs];
            if (isEditing) {
                // 编辑现有实验室
                const index = newLabs.findIndex((item) => item.id === currentLab.id);
                if (index !== -1) {
                    newLabs[index] = {
                        ...currentLab
                    };
                }
            } else {
                // 添加新实验室
                const newId = String(Date.now());
                newLabs.push({
                    ...currentLab,
                    id: newId
                });
            }

            // 更新数据
            this.setData(
                {
                    labs: newLabs,
                    filteredLabs: newLabs,
                    showModal: false
                },
                () => {
                    // 如果有搜索关键词，重新过滤
                    if (this.searchKeyword) {
                        this.filterLabs();
                    }

                    // 显示成功提示
                    uni.showToast({
                        title: isEditing ? '编辑成功' : '添加成功',
                        icon: 'success'
                    });
                }
            );
        },

        // 显示删除确认
        showDeleteConfirm(e: any) {
            const id = e.currentTarget.dataset.id;
            const name = e.currentTarget.dataset.name;
            uni.showModal({
                title: '确认删除',
                content: `删除对象：「${name}」| 确定要删除这个实验室吗？`,
                confirmColor: '#3a7bd5',
                success: (res) => {
                    if (res.confirm) {
                        // 显示密码验证弹窗
                        this.showDeletePasswordModalFun(id);
                    }
                }
            });

            // 阻止事件冒泡
            e.stopPropagation();
        },

        // 显示密码验证弹窗
        showDeletePasswordModalFun(id: string) {
            this.setData({
                showDeletePasswordModal: true,
                adminPassword: '',
                labIdToDelete: id
            });
        },

        // 隐藏密码验证弹窗
        hideDeletePasswordModal() {
            this.setData({
                showDeletePasswordModal: false,
                adminPassword: ''
            });
        },

        // 管理员密码输入
        onAdminPasswordInput(e: any) {
            this.setData({
                adminPassword: e.detail.value
            });
        },

        // 确认密码并删除
        confirmDeleteWithPassword() {
            const { adminPassword, labIdToDelete } = this;

            // 验证密码不能为空
            if (!adminPassword.trim()) {
                uni.showToast({
                    title: '请输入管理员密码',
                    icon: 'none'
                });
                return;
            }

            // 验证管理员密码是否正确
            // 这里应该与存储的管理员密码进行比对，这里使用模拟数据
            // 实际应用中应该从服务器验证或从本地存储获取加密后的密码进行比对
            const storedPassword = uni.getStorageSync('adminPassword') || 'admin123';
            if (adminPassword !== storedPassword) {
                uni.showToast({
                    title: '密码错误',
                    icon: 'none'
                });
                return;
            }

            // 密码正确，执行删除操作
            this.deleteLab(labIdToDelete);
        },

        // 删除实验室
        deleteLab(id: string) {
            const { labs } = this;
            const newLabs = labs.filter((item) => item.id !== id);
            this.setData(
                {
                    labs: newLabs,
                    filteredLabs: newLabs,
                    showDeletePasswordModal: false,
                    adminPassword: ''
                },
                () => {
                    // 如果有搜索关键词，重新过滤
                    if (this.searchKeyword) {
                        this.filterLabs();
                    }

                    // 显示成功提示
                    uni.showToast({
                        title: '删除成功',
                        icon: 'success'
                    });
                }
            );
        }
    },
    created: function () {}
});
</script>
<style lang="less">
@import './admin-lab-management.less';
</style>
