<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app';

onLaunch(() => {
    // 展示本地存储能力
    const logs = (uni.getStorageSync('logs') as number[]) || [];
    logs.unshift(Date.now());
    uni.setStorageSync('logs', logs);

    // 登录
    uni.login({
        success: (res) => {
            console.log(res.code);
            // 发送 res.code 到后台换取 openId, sessionKey, unionId
        }
    });
});

onShow(() => {});

onHide(() => {});
</script>

<style lang="less">
@import './app.less';
</style>
