<template>
    <view class="mp-weixin-page-header" :style="wrapperStyle">
        <view class="mp-weixin-page-header__inner" :style="innerStyle">
            <view class="mp-weixin-page-header__center">
                <text class="mp-weixin-page-header__title">{{ title }}</text>
            </view>
            <view class="mp-weixin-page-header__actions" :style="sideStyle">
                <view
                    v-if="back"
                    class="mp-weixin-page-header__back"
                    hover-class="mp-weixin-page-header__back--active"
                    hover-stop-propagation
                    @tap="goBack"
                >
                    <view class="mp-weixin-page-header__back-icon"></view>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

const props = defineProps({
    title: { type: String, default: '' },
    background: { type: String, default: '#ffffff' },
    color: { type: String, default: '#111827' },
    back: { type: Boolean, default: true }
});

const statusBarHeight = ref(20);
const navHeight = ref(44);
const sideWidth = ref(88);

const initLayout = () => {
    const systemInfo = uni.getSystemInfoSync();
    statusBarHeight.value = systemInfo.statusBarHeight || systemInfo.safeArea?.top || 20;

    try {
        const rect = uni.getMenuButtonBoundingClientRect();
        if (rect && rect.width) {
            const verticalPadding = rect.top - statusBarHeight.value;
            navHeight.value = rect.height + verticalPadding * 2;
            sideWidth.value = systemInfo.windowWidth - rect.left;
            return;
        }
    } catch (error) {
        console.warn('menu button rect unavailable', error);
    }

    navHeight.value = systemInfo.platform === 'android' ? 48 : 44;
    sideWidth.value = 96;
};

const wrapperStyle = computed(
    () => `height:${statusBarHeight.value + navHeight.value}px;`
);

const innerStyle = computed(
    () =>
        `height:${statusBarHeight.value + navHeight.value}px;padding-top:${statusBarHeight.value}px;background:${props.background};color:${props.color};`
);

const sideStyle = computed(() => `width:${sideWidth.value}px;`);

const goBack = () => {
    uni.navigateBack({
        fail: () => {
            uni.reLaunch({ url: '/pages/login-select/login-select' });
        }
    });
};

onMounted(() => {
    initLayout();
});
</script>

<style lang="less">
.mp-weixin-page-header {
    position: relative;
    width: 100%;
    flex-shrink: 0;
}

.mp-weixin-page-header__inner {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    display: flex;
    align-items: center;
    box-sizing: border-box;
    z-index: 1000;
    box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.08);
}

.mp-weixin-page-header__center {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    padding-top: inherit;
    pointer-events: none;
}

.mp-weixin-page-header__title {
    max-width: calc(100% - 240rpx);
    font-size: 32rpx;
    font-weight: 600;
    line-height: 1;
    letter-spacing: 1rpx;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.mp-weixin-page-header__actions {
    position: relative;
    z-index: 1;
    height: 100%;
    display: flex;
    align-items: center;
    flex-shrink: 0;
    padding-left: 24rpx;
    box-sizing: border-box;
}

.mp-weixin-page-header__back {
    width: 64rpx;
    height: 64rpx;
    border-radius: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.14);
}

.mp-weixin-page-header__back--active {
    opacity: 0.72;
}

.mp-weixin-page-header__back-icon {
    width: 20rpx;
    height: 20rpx;
    border-left: 4rpx solid currentColor;
    border-bottom: 4rpx solid currentColor;
    transform: translateX(4rpx) rotate(45deg);
}
</style>
