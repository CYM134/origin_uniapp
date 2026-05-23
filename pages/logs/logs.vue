<template>
    <view>
        <!-- logs.wxml -->
        <navigation-bar title="查看启动日志" :back="true" color="black" background="#FFF"></navigation-bar>
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
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
// logs.ts
// const util = require('../../utils/util.js')
import { formatTime } from '../../utils/util';

const logs = ref<any[]>([]);
const log = ref<any>({ date: '' });

onMounted(() => {
    // 处理小程序 attached 生命周期
    logs.value = (uni.getStorageSync('logs') || []).map((logItem: string) => {
        return {
            date: formatTime(new Date(logItem)),
            timeStamp: logItem
        };
    });
});
</script>
<style lang="less">
@import './logs.less';
</style>
