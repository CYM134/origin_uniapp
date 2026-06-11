import { request } from '@/api/request';
import { clearAuthStorage, getCurrentRole, getCurrentUser, saveAuthSession } from '@/api/storage';

export function loginByPassword(payload) {
    return request({
        url: '/api/auth/login',
        method: 'POST',
        data: payload,
        auth: false,
        skipAuthRedirect: true
    });
}

export async function fetchCurrentUser() {
    const user = await request({
        url: '/api/auth/me',
        method: 'GET'
    });

    const accessToken = uni.getStorageSync('accessToken') || '';
    if (accessToken) {
        saveAuthSession(accessToken, user);
    }

    return user;
}

export function logout() {
    clearAuthStorage();
}

export function getStoredUser() {
    return getCurrentUser();
}

export function getStoredRole() {
    return getCurrentRole();
}
