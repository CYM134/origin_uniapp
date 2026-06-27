<template>
    <view class="page">
        <view class="hd">
            <view class="hd-back" @tap="goBack">
                <text class="hd-back-ico">‹</text>
                <text class="hd-back-txt">返回</text>
            </view>
            <text class="hd-title">AI 校园助手</text>
            <text class="hd-sub">智能问答 · 服务导航</text>
        </view>

        <view class="body">
            <!-- 聊天区 -->
            <scroll-view
                class="chat"
                scroll-y
                :scroll-top="scrollTop"
                :scroll-with-animation="true"
            >
                <view class="chat-inner">
                    <view
                        class="row"
                        v-for="(msg, idx) in messages"
                        :key="idx"
                        :class="'row--' + msg.role"
                    >
                        <view :class="'bubble bubble--' + msg.role">
                            <text class="bubble-text">{{ msg.content }}</text>
                        </view>
                        <text v-if="msg.source === 'fallback'" class="bubble-tag">内置助手</text>
                    </view>

                    <view v-if="sending" class="row row--assistant">
                        <view class="bubble bubble--assistant">
                            <text class="bubble-text">正在思考…</text>
                        </view>
                    </view>

                    <view class="anchor" :style="anchorStyle"></view>
                </view>
            </scroll-view>

            <!-- 快捷问题 -->
            <scroll-view class="quick" scroll-x>
                <view class="quick-inner">
                    <view
                        class="chip"
                        v-for="(q, i) in quickQuestions"
                        :key="i"
                        @tap="onQuick(q)"
                    >
                        <text class="chip-text">{{ q }}</text>
                    </view>
                </view>
            </scroll-view>

            <!-- 输入区 -->
            <view class="composer">
                <input
                    class="composer-input"
                    v-model="input"
                    type="text"
                    placeholder="请输入您的问题…"
                    placeholder-class="composer-ph"
                    confirm-type="send"
                    :disabled="sending"
                    @confirm="onSend"
                />
                <view
                    :class="'send-btn ' + ((sending || !input.trim()) ? 'send-btn--off' : '')"
                    @tap="onSend"
                >
                    <text class="send-text">发送</text>
                </view>
            </view>
        </view>
    </view>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { aiChat } from '@/api/portal';
import { getStoredRole } from '@/api/storage';

const goBack = () => uni.navigateBack({ delta: 1 });

interface ChatMsg {
    role: 'user' | 'assistant';
    content: string;
    source?: string;
}

const role = ref<string>('student');
const messages = ref<ChatMsg[]>([]);
const conversationId = ref<number | null>(null);
const input = ref('');
const sending = ref(false);
const scrollTop = ref(0);

const quickQuestions = ref<string[]>([
    '查询我的待办业务',
    '查询课表',
    '查询实验室空余时间段'
]);

// 通过不断变化的高度配合 anchor 触发滚动到底部
const tick = ref(0);
const anchorStyle = computed(() => 'height:' + (1 + (tick.value % 2)) + 'rpx;');

const scrollToBottom = () => {
    nextTick(() => {
        tick.value += 1;
        scrollTop.value = 100000 + tick.value;
    });
};

const pushWelcome = () => {
    messages.value.push({
        role: 'assistant',
        content: '有什么我可以帮助你的？'
    });
    scrollToBottom();
};

const sendText = async (text: string) => {
    const content = (text || '').trim();
    if (!content || sending.value) {
        return;
    }
    messages.value.push({ role: 'user', content });
    input.value = '';
    sending.value = true;
    scrollToBottom();

    try {
        const res = (await aiChat(content, conversationId.value)) || {};
        if (res?.conversationId != null) {
            conversationId.value = res.conversationId;
        }
        messages.value.push({
            role: 'assistant',
            content: res?.reply || '抱歉，我暂时没有理解您的问题，请换种说法试试。',
            source: res?.source
        });
    } catch (e: any) {
        messages.value.push({
            role: 'assistant',
            content: '抱歉，服务暂时不可用，请稍后再试。'
        });
        uni.showToast({ title: e?.data?.message || '加载失败', icon: 'none' });
    } finally {
        sending.value = false;
        scrollToBottom();
    }
};

const onSend = () => {
    sendText(input.value);
};

const onQuick = (q: string) => {
    sendText(q);
};

onLoad(() => {
    try {
        role.value = getStoredRole() || 'student';
    } catch (e) {
        role.value = 'student';
    }
    pushWelcome();
});
</script>

<style lang="less">@import './ai-assistant.less';</style>
