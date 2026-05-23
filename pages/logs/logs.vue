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

<script lang="ts">
import zpMixins from '@/uni_modules/zp-mixins/index';
import navigationBar from '@/components/navigation-bar/navigation-bar';
// logs.ts
// const util = require('../../utils/util.js')
import { formatTime } from '../../utils/util';
export default zpMixins.extend({
    components: {
        navigationBar
    },
    data() {
        return {
            logs: [],

            log: {
                date: ''
            }
        };
    },
    mounted() {
        // 处理小程序 attached 生命周期
        this.attached();
    },
    methods: {
        attached() {
            this.setData({
                logs: (uni.getStorageSync('logs') || []).map((log: string) => {
                    return {
                        date: formatTime(new Date(log)),
                        timeStamp: log
                    };
                })
            });
        }
    },
    created: function () {}
});
</script>
<style lang="less">
@import './logs.less';
</style>
