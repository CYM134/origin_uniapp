<template>
    <view class="page-wrapper">
        <!-- logs.wxml -->
        <!-- #ifdef MP-WEIXIN -->
        <mp-weixin-page-header title="查看启动日志" color="#111111" background="#FFFFFF" />
        <!-- #endif -->
        <!-- #ifndef MP-WEIXIN -->
        <app-navigation-bar title="查看启动日志" :back="true" color="black" background="#FFF"></app-navigation-bar>
        <!-- #endif -->
        <scroll-view class="scrollarea" scroll-y type="list">
            <block v-for="(log, index) in logs" :key="index">
                <view class="log-item">{{ index + 1 }}. {{ log.date }}</view>
            </block>
        </scroll-view>
    </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { onMounted } from 'vue';
import appNavigationBar from '@/components/app-navigation-bar/app-navigation-bar.vue';
// logs.ts
// const util = require('../../utils/util.js')
import { formatTime } from '../../utils/util';
import { getAuditLogs } from '@/api/admin';

const logs = ref<any[]>([]);
const log = ref<any>({ date: '' });

onMounted(async () => {
    // 处理小程序 attached 生命周期：拉取后端审计日志列表
    try {
        const list = (await getAuditLogs()) || [];
        logs.value = list.map((item: any) => {
            return {
                id: item.id,
                module: item.module,
                action: item.action,
                role: item.role,
                userName: item.userName,
                detail: item.detail,
                createTime: item.createTime,
                // 模板按 {{ log.date }} 渲染，映射 createTime 为 date
                date: item.createTime
            };
        });
    } catch (err: any) {
        uni.showToast({ title: err?.data?.message || '加载失败', icon: 'none' });
        logs.value = [];
    }
});
</script>
<style lang="less">
@import './logs.less';
</style>
