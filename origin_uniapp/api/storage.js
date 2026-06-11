const ACCESS_TOKEN_KEY = 'accessToken';
const CURRENT_USER_KEY = 'currentUser';
const CURRENT_ROLE_KEY = 'currentRole';

function buildLegacyUser(user) {
    if (!user || !user.role) {
        return null;
    }

    if (user.role === 'student') {
        return {
            studentId: user.accountNo,
            name: user.realName,
            gender: user.gender || '',
            phone: user.phone || '',
            email: user.email || '',
            college: user.college || '',
            major: user.major || '',
            registerTime: user.registerTime || '',
            role: user.role,
            status: user.status
        };
    }

    if (user.role === 'teacher') {
        return {
            teacherId: user.accountNo,
            name: user.realName,
            gender: user.gender || '',
            phone: user.phone || '',
            email: user.email || '',
            college: user.college || '',
            department: user.department || '',
            positionTitle: user.positionTitle || '',
            registerTime: user.registerTime || '',
            role: user.role,
            status: user.status
        };
    }

    return {
        accountNo: user.accountNo,
        username: user.realName,
        realName: user.realName,
        gender: user.gender || '',
        phone: user.phone || '',
        email: user.email || '',
        college: user.college || '',
        major: user.major || '',
        department: user.department || '',
        positionTitle: user.positionTitle || '',
        role: user.role,
        status: user.status
    };
}

export function getAccessToken() {
    return uni.getStorageSync(ACCESS_TOKEN_KEY) || '';
}

export function getCurrentUser() {
    return uni.getStorageSync(CURRENT_USER_KEY) || null;
}

export function getCurrentRole() {
    return uni.getStorageSync(CURRENT_ROLE_KEY) || '';
}

export function getStoredUser() {
    return getCurrentUser();
}

export function getStoredRole() {
    return getCurrentRole();
}

export function hasCompleteUserProfile(user) {
    if (!user || !user.role) {
        return false;
    }
    if (user.role === 'student') {
        return Boolean(user.accountNo && user.realName && user.college && user.major);
    }
    if (user.role === 'teacher') {
        return Boolean(user.accountNo && user.realName && user.college && user.department);
    }
    return Boolean(user.accountNo && user.realName);
}

export function syncLegacyUserStorage(user) {
    const legacyUser = buildLegacyUser(user);
    if (!legacyUser) {
        return;
    }

    if (user.role === 'student') {
        uni.setStorageSync('studentInfo', legacyUser);
        uni.setStorageSync('isStudentLoggedIn', true);
    } else if (user.role === 'teacher') {
        uni.setStorageSync('teacherInfo', legacyUser);
        uni.setStorageSync('isTeacherLoggedIn', true);
    } else if (user.role === 'admin') {
        uni.setStorageSync('adminInfo', legacyUser);
        uni.setStorageSync('adminUsername', user.accountNo);
        uni.setStorageSync('isAdminLoggedIn', true);
    }
}

export function saveAuthSession(accessToken, user) {
    uni.setStorageSync(ACCESS_TOKEN_KEY, accessToken);
    uni.setStorageSync(CURRENT_USER_KEY, user);
    uni.setStorageSync(CURRENT_ROLE_KEY, user.role);
    syncLegacyUserStorage(user);
}

export function clearAuthStorage() {
    uni.removeStorageSync(ACCESS_TOKEN_KEY);
    uni.removeStorageSync(CURRENT_USER_KEY);
    uni.removeStorageSync(CURRENT_ROLE_KEY);
    uni.removeStorageSync('studentInfo');
    uni.removeStorageSync('teacherInfo');
    uni.removeStorageSync('adminInfo');
    uni.removeStorageSync('isStudentLoggedIn');
    uni.removeStorageSync('isTeacherLoggedIn');
    uni.removeStorageSync('isAdminLoggedIn');
}
