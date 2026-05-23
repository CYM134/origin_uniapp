<template>
    <view class="weui-navigation-bar" :class="{ android: !ios }">
        <view
            v-if="show"
            :class="['weui-navigation-bar__inner', extClass]"
            :style="
                displayStyle +
                ';' +
                safeAreaTop +
                ';' +
                innerPaddingRight +
                ';background:' +
                background +
                ';color:' +
                color
            "
        >
            <view class="weui-navigation-bar__left" :style="leftWidth">
                <view
                    v-if="back"
                    class="weui-navigation-bar__btn_goback_wrapper"
                    hover-class="weui-active"
                    hover-stop-propagation
                    @tap="backFun"
                >
                    <view class="weui-navigation-bar__btn_goback"></view>
                </view>
                <view
                    v-if="homeButton"
                    class="weui-navigation-bar__btn_home_wrapper"
                    hover-class="weui-active"
                    hover-stop-propagation
                    @tap="home"
                >
                    <view class="weui-navigation-bar__btn_home"></view>
                </view>
            </view>
            <view class="weui-navigation-bar__center">
                <view v-if="loading" class="weui-navigation-bar__loading">
                    <view class="weui-loading"></view>
                </view>
                <text>{{ title }}</text>
            </view>
            <view class="weui-navigation-bar__right"></view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';

const props = defineProps({
    extClass: { type: String, default: '' },
    title: { type: String, default: '' },
    background: { type: String, default: '' },
    color: { type: String, default: '' },
    back: { type: Boolean, default: true },
    loading: { type: Boolean, default: false },
    homeButton: { type: Boolean, default: false },
    animated: { type: Boolean, default: true },
    show: { type: Boolean, default: true },
    delta: { type: Number, default: 1 }
});

const emit = defineEmits(['back']);

const displayStyle = ref('');
const ios = ref<boolean>(false);
const innerPaddingRight = ref('');
const leftWidth = ref('');
const safeAreaTop = ref('');

const initAttached = () => {
    let rect: UniApp.GetMenuButtonBoundingClientRectRes | undefined;
    // #ifndef H5
    try {
        rect = uni.getMenuButtonBoundingClientRect();
    } catch (e) {
        // 部分平台不支持，忽略
    }
    // #endif
    uni.getSystemInfo({
        success: (res) => {
            const isAndroid = res.platform === 'android';
            const isDevtools = res.platform === 'devtools';
            ios.value = !isAndroid;
            if (rect) {
                innerPaddingRight.value = 'padding-right: ' + (res.windowWidth - rect.left) + 'px';
                leftWidth.value = 'width: ' + (res.windowWidth - rect.left) + 'px';
            }
            safeAreaTop.value =
                isDevtools || (isAndroid && res.safeArea)
                    ? 'height: calc(var(--height) + ' +
                      res.safeArea!.top +
                      'px); padding-top: ' +
                      res.safeArea!.top +
                      'px'
                    : '';
        }
    });
};

const showChangeFun = (visible: boolean) => {
    displayStyle.value = props.animated
        ? 'opacity: ' + (visible ? 1 : 0) + ';transition:opacity 0.5s;'
        : 'display: ' + (visible ? '' : 'none');
};

const backFun = () => {
    if (props.delta) {
        uni.navigateBack({ delta: props.delta });
    }
    emit('back', { detail: { delta: props.delta } });
};

const home = () => {
    console.log('home clicked');
};

watch(
    () => props.show,
    (newVal) => {
        showChangeFun(newVal);
    },
    { immediate: true }
);

onMounted(() => {
    initAttached();
});
</script>

<style lang="less">
@import './navigation-bar.less';
</style>
