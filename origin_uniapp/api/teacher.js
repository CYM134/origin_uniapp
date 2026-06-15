import { request } from '@/api/request';

// ============ 注册 / 账号 ============

export function registerTeacher(payload) {
    return request({ url: '/api/auth/teacher/register', method: 'POST', data: payload, auth: false, skipAuthRedirect: true });
}

export function checkTeacherExists(teacherNo) {
    return request({ url: `/api/auth/teacher/exists?teacherNo=${encodeURIComponent(teacherNo)}`, method: 'GET', auth: false, skipAuthRedirect: true });
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

export function getTeacherProfile() {
    return request({ url: '/api/teacher/profile', method: 'GET' });
}

export function updateTeacherProfile(payload) {
    return request({ url: '/api/teacher/profile', method: 'PUT', data: payload });
}

export function updateTeacherAvatar(avatar) {
    return request({ url: '/api/teacher/avatar', method: 'POST', data: { avatar } });
}

// ============ 实验室 / 学期 / 课表 ============

export function getLabs() {
    return request({ url: '/api/laboratories', method: 'GET' });
}

export function getCurrentSemester() {
    return request({ url: '/api/academic-semesters/current', method: 'GET' });
}

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

// ============ 预约申请（教师本人发起） ============

export function createTeacherReservation(payload) {
    return request({ url: '/api/teacher/reservations', method: 'POST', data: payload });
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

// ============ 教师审核学生申请 ============

export function getPendingReviews() {
    return request({ url: '/api/teacher/reservations/pending', method: 'GET' });
}

export function getPendingReviewCount() {
    return request({ url: '/api/teacher/reservations/pending-count', method: 'GET' });
}

export function getReviewedApplications() {
    return request({ url: '/api/teacher/reservations/reviewed', method: 'GET' });
}

export function approveReservation(id, comment) {
    return request({ url: `/api/teacher/reservations/${id}/approve`, method: 'POST', data: { comment } });
}

export function rejectReservation(id, reason) {
    return request({ url: `/api/teacher/reservations/${id}/reject`, method: 'POST', data: { reason } });
}

export function reapproveReservation(id, comment) {
    return request({ url: `/api/teacher/reservations/${id}/reapprove`, method: 'POST', data: { comment } });
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
