import { clearAuthStorage, getAccessToken } from '@/api/storage';

export const API_BASE_URL = uni.getStorageSync('apiBaseUrl') || 'http://localhost:8080';

function redirectToLogin() {
    setTimeout(() => {
        uni.showToast({
            title: '登录已失效，请重新登录',
            icon: 'none'
        });
        uni.reLaunch({
            url: '/pages/login-select/login-select'
        });
    }, 100);
}

export function request(options) {
    const {
        url,
        method = 'GET',
        data,
        header = {},
        auth = true,
        skipAuthRedirect = false
    } = options;

    return new Promise((resolve, reject) => {
        const requestHeader = {
            ...header
        };

        if (method !== 'GET' && !requestHeader['Content-Type']) {
            requestHeader['Content-Type'] = 'application/json';
        }

        if (auth) {
            const token = getAccessToken();
            if (token) {
                requestHeader.Authorization = `Bearer ${token}`;
            }
        }

        uni.request({
            url: `${API_BASE_URL}${url}`,
            method,
            data,
            header: requestHeader,
            success: (response) => {
                const { statusCode, data: responseData } = response;

                if (statusCode >= 200 && statusCode < 300) {
                    resolve(responseData);
                    return;
                }

                if (statusCode === 401 && !skipAuthRedirect) {
                    clearAuthStorage();
                    redirectToLogin();
                }

                reject({
                    statusCode,
                    data: responseData
                });
            },
            fail: (error) => {
                reject({
                    statusCode: 0,
                    data: {
                        message: '服务器连接失败'
                    },
                    error
                });
            }
        });
    });
}
