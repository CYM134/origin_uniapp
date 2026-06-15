import { request } from '@/api/request';

// ============ 注册 / 账号 ============

export function registerStudent(payload) {
    return request({ url: '/api/auth/student/register', method: 'POST', data: payload, auth: false, skipAuthRedirect: true });
}

export function checkStudentExists(studentNo) {
    return request({ url: `/api/auth/student/exists?studentNo=${encodeURIComponent(studentNo)}`, method: 'GET', auth: false, skipAuthRedirect: true });
}

export function getColleges() {
    return request({ url: '/api/colleges', method: 'GET', auth: false, skipAuthRedirect: true });
}

export function changePassword(payload) {
    return request({ url: '/api/auth/change-password', method: 'POST', data: payload });
}

export function logout() {
    return request({ url: '/api/auth/logout', method: 'POST' });
}

// ============ 个人资料 ============

export function getStudentProfile() {
    return request({ url: '/api/student/profile', method: 'GET' });
}

export function updateStudentProfile(payload) {
    return request({ url: '/api/student/profile', method: 'PUT', data: payload });
}

export function updateStudentAvatar(avatar) {
    return request({ url: '/api/student/avatar', method: 'POST', data: { avatar } });
}

// ============ 实验室 / 字典 / 学期 ============

export function getLabs() {
    return request({ url: '/api/laboratories', method: 'GET' });
}

export function getApplicationTypes() {
    return request({ url: '/api/dict/application-types', method: 'GET' });
}

export function getCurrentSemester() {
    return request({ url: '/api/academic-semesters/current', method: 'GET' });
}

// ============ 课表预览 ============

export function getCourseSchedulesByDate(date, labId) {
    const lab = labId ? `&labId=${labId}` : '';
    return request({ url: `/api/course-schedules?date=${date}${lab}`, method: 'GET' });
}

export function getCourseSchedulesByWeek(startDate, endDate, labId) {
    const lab = labId ? `&labId=${labId}` : '';
    return request({ url: `/api/course-schedules/week?startDate=${startDate}&endDate=${endDate}${lab}`, method: 'GET' });
}

export function getCourseScheduleDetail(id) {
    return request({ url: `/api/course-schedules/${id}`, method: 'GET' });
}

// ============ 预约申请（学生） ============

export function createStudentReservation(payload) {
    return request({ url: '/api/student/reservations', method: 'POST', data: payload });
}

export function getMyReservations() {
    return request({ url: '/api/reservations/mine', method: 'GET' });
}

export function getMyReservationStats() {
    return request({ url: '/api/reservations/mine/stats', method: 'GET' });
}

export function getReservationDetail(id) {
    return request({ url: `/api/reservations/${id}`, method: 'GET' });
}

export function cancelReservation(id) {
    return request({ url: `/api/reservations/${id}/cancel`, method: 'POST' });
}

export function completeReservation(id) {
    return request({ url: `/api/reservations/${id}/complete`, method: 'POST' });
}

// ============ 通知 ============

export function getNotifications() {
    return request({ url: '/api/notifications', method: 'GET' });
}

export function getUnreadCount() {
    return request({ url: '/api/notifications/unread-count', method: 'GET' });
}

export function markNotificationRead(id) {
    return request({ url: `/api/notifications/${id}/read`, method: 'POST' });
}

export function markAllNotificationsRead() {
    return request({ url: '/api/notifications/read-all', method: 'POST' });
}
