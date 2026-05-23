const fs = require('fs');
let c = fs.readFileSync('pages/admin-login/admin-login.vue', 'utf8');
const newScript = \<script setup lang="ts">
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import navigationBar from '@/components/navigation-bar/navigation-bar.vue';
const username = ref<string>('');
const password = ref<string>('');
const rememberPassword = ref<boolean>(false);
const showPassword = ref<boolean>(false);
onLoad(() => {
    const savedUsername = uni.getStorageSync('adminUsername');
    const savedPassword = uni.getStorageSync('adminPassword');
    const savedRememberPassword = uni.getStorageSync('adminRememberPassword');
    if (savedRememberPassword && savedUsername && savedPassword) {
        username.value = savedUsername;
        password.value = savedPassword;
        rememberPassword.value = true;
    }
});
const onUsernameInput = (e: any) => { username.value = e.detail.value; };
const onPasswordInput = (e: any) => { password.value = e.detail.value; };
const toggleRememberPassword = () => { rememberPassword.value = !rememberPassword.value; };
const togglePasswordVisibility = () => { showPassword.value = !showPassword.value; };
const login = () => {
    if (!username.value.trim()) { uni.showToast({ title: '请输入账号', icon: 'none' }); return; }
    if (!password.value.trim()) { uni.showToast({ title: '请输入密码', icon: 'none' }); return; }
    uni.showLoading({ title: '登录中...', mask: true });
    setTimeout(() => {
        uni.hideLoading();
        if (rememberPassword.value) {
            uni.setStorageSync('adminUsername', username.value);
            uni.setStorageSync('adminPassword', password.value);
            uni.setStorageSync('adminRememberPassword', true);
        } else {
            uni.removeStorageSync('adminUsername');
            uni.removeStorageSync('adminPassword');
            uni.removeStorageSync('adminRememberPassword');
        }
        uni.navigateTo({
            url: '../admin-work/admin-work',
            success: () => { uni.showToast({ title: '登录成功', icon: 'success' });}
        });
    }, 1500);
};
</script>\;
const scriptIndex = c.indexOf('<script lang="ts">');
const styleIndex = c.lastIndexOf('<style lang="less">');
c = c.substring(0, scriptIndex) + newScript + "\n" + c.substring(styleIndex);
fs.writeFileSync('pages/admin-login/admin-login.vue', c, 'utf8');
