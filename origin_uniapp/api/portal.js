import { request } from '@/api/request';

// =====================================================================
// 校园综合服务平台 - 门户 / 应用中心 / 通知公告 / 校园资讯 / 任务 / 消息 /
// 日历 / AI 助手，以及管理员侧的应用、通知、资讯管理与工作台。
// 全部沿用现有 request 封装（自动携带 JWT、401 跳登录）。
// =====================================================================

function buildQuery(params) {
    if (!params) return '';
    const parts = [];
    Object.keys(params).forEach((k) => {
        const v = params[k];
        if (v !== undefined && v !== null && v !== '') {
            parts.push(`${encodeURIComponent(k)}=${encodeURIComponent(v)}`);
        }
    });
    return parts.length ? `?${parts.join('&')}` : '';
}

// ============ 菜单 / 权限 ============

export function getMenus() {
    return request({ url: '/api/auth/menus', method: 'GET' });
}

export function getPermissions() {
    return request({ url: '/api/auth/permissions', method: 'GET' });
}

// ============ 门户首页 ============

export function getPortalHome() {
    return request({ url: '/api/portal/home', method: 'GET' });
}

// ============ 应用中心 ============

export function getAppCategories() {
    return request({ url: '/api/portal/app-categories', method: 'GET' });
}

export function getApps(params) {
    return request({ url: `/api/portal/apps${buildQuery(params)}`, method: 'GET' });
}

export function getFavoriteApps() {
    return request({ url: '/api/portal/apps/favorites', method: 'GET' });
}

export function favoriteApp(id) {
    return request({ url: `/api/portal/apps/${id}/favorite`, method: 'POST' });
}

export function unfavoriteApp(id) {
    return request({ url: `/api/portal/apps/${id}/favorite`, method: 'DELETE' });
}

export function visitApp(id, clientType) {
    return request({ url: `/api/portal/apps/${id}/visit`, method: 'POST', data: { clientType: clientType || 'h5' } });
}

// ============ 通知公告（门户） ============

export function getNotices(keyword) {
    return request({ url: `/api/portal/notices${buildQuery({ keyword })}`, method: 'GET' });
}

export function getNoticeDetail(id) {
    return request({ url: `/api/portal/notices/${id}`, method: 'GET' });
}

export function readNotice(id) {
    return request({ url: `/api/portal/notices/${id}/read`, method: 'POST' });
}

// ============ 校园资讯（门户） ============

export function getNewsCategories() {
    return request({ url: '/api/portal/news/categories', method: 'GET' });
}

export function getNewsList(params) {
    return request({ url: `/api/portal/news${buildQuery(params)}`, method: 'GET' });
}

export function getNewsDetail(id) {
    return request({ url: `/api/portal/news/${id}`, method: 'GET' });
}

// ============ 任务中心 ============

export function getTasksTodo() {
    return request({ url: '/api/portal/tasks/todo', method: 'GET' });
}

export function getTasksDone() {
    return request({ url: '/api/portal/tasks/done', method: 'GET' });
}

export function getTasksMine() {
    return request({ url: '/api/portal/tasks/mine', method: 'GET' });
}

export function getTaskStats() {
    return request({ url: '/api/portal/tasks/stats', method: 'GET' });
}

export function getTaskDetail(id) {
    return request({ url: `/api/portal/tasks/${id}`, method: 'GET' });
}

// 审批动作（复用现有教师/管理员审批接口）
export function teacherApproveTask(id, comment) {
    return request({ url: `/api/teacher/reservations/${id}/approve`, method: 'POST', data: { comment } });
}

export function teacherRejectTask(id, comment) {
    return request({ url: `/api/teacher/reservations/${id}/reject`, method: 'POST', data: { comment } });
}

export function adminApproveTask(id, comment) {
    return request({ url: `/api/admin/reservations/${id}/approve`, method: 'POST', data: { comment } });
}

export function adminRejectTask(id, comment) {
    return request({ url: `/api/admin/reservations/${id}/reject`, method: 'POST', data: { comment } });
}

// ============ 消息中心（复用 notifications） ============

export function getMessages() {
    return request({ url: '/api/notifications', method: 'GET' });
}

export function getMessageUnreadCount() {
    return request({ url: '/api/notifications/unread-count', method: 'GET' });
}

export function markMessageRead(id) {
    return request({ url: `/api/notifications/${id}/read`, method: 'POST' });
}

export function markAllMessagesRead() {
    return request({ url: '/api/notifications/read-all', method: 'POST' });
}

// ============ 我的日历 ============

export function getCalendarEvents(startDate, endDate) {
    return request({ url: `/api/portal/calendar/events${buildQuery({ startDate, endDate })}`, method: 'GET' });
}

export function getUpcomingEvents(limit) {
    return request({ url: `/api/portal/calendar/upcoming${buildQuery({ limit })}`, method: 'GET' });
}

export function createCalendarEvent(payload) {
    return request({ url: '/api/portal/calendar/events', method: 'POST', data: payload });
}

export function updateCalendarEvent(id, payload) {
    return request({ url: `/api/portal/calendar/events/${id}`, method: 'PUT', data: payload });
}

export function deleteCalendarEvent(id) {
    return request({ url: `/api/portal/calendar/events/${id}`, method: 'DELETE' });
}

// ============ AI 校园助手 ============

export function aiChat(message, conversationId) {
    return request({ url: '/api/ai/chat', method: 'POST', data: { message, conversationId } });
}

export function getAiConversations() {
    return request({ url: '/api/ai/conversations', method: 'GET' });
}

export function getAiConversation(id) {
    return request({ url: `/api/ai/conversations/${id}`, method: 'GET' });
}

// ============ 实验室可预约时间 ============

export function getAvailableTimes(labId, date) {
    return request({ url: `/api/laboratories/${labId}/available-times${buildQuery({ date })}`, method: 'GET' });
}

// =====================================================================
// 管理员侧：应用管理 / 通知管理 / 资讯管理 / 综合工作台
// =====================================================================

// ---- 工作台 ----
export function getAdminPortalDashboard() {
    return request({ url: '/api/admin/portal/dashboard', method: 'GET' });
}

// ---- 应用管理 ----
export function getAdminApps(params) {
    return request({ url: `/api/admin/portal/apps${buildQuery(params)}`, method: 'GET' });
}

export function getAdminApp(id) {
    return request({ url: `/api/admin/portal/apps/${id}`, method: 'GET' });
}

export function createApp(payload) {
    return request({ url: '/api/admin/portal/apps', method: 'POST', data: payload });
}

export function updateApp(id, payload) {
    return request({ url: `/api/admin/portal/apps/${id}`, method: 'PUT', data: payload });
}

export function setAppStatus(id, status) {
    return request({ url: `/api/admin/portal/apps/${id}/status`, method: 'PUT', data: { status } });
}

export function sortApps(items) {
    return request({ url: '/api/admin/portal/apps/sort', method: 'PUT', data: items });
}

export function deleteApp(id) {
    return request({ url: `/api/admin/portal/apps/${id}`, method: 'DELETE' });
}

// ---- 通知公告管理 ----
export function getAdminNotices(params) {
    return request({ url: `/api/admin/notices${buildQuery(params)}`, method: 'GET' });
}

export function getAdminNotice(id) {
    return request({ url: `/api/admin/notices/${id}`, method: 'GET' });
}

export function createNotice(payload) {
    return request({ url: '/api/admin/notices', method: 'POST', data: payload });
}

export function updateNotice(id, payload) {
    return request({ url: `/api/admin/notices/${id}`, method: 'PUT', data: payload });
}

export function setNoticeStatus(id, status) {
    return request({ url: `/api/admin/notices/${id}/status`, method: 'PUT', data: { status } });
}

export function deleteNotice(id) {
    return request({ url: `/api/admin/notices/${id}`, method: 'DELETE' });
}

// ---- 校园资讯管理 ----
export function getAdminNews(params) {
    return request({ url: `/api/admin/news${buildQuery(params)}`, method: 'GET' });
}

export function getAdminNewsDetail(id) {
    return request({ url: `/api/admin/news/${id}`, method: 'GET' });
}

export function createNews(payload) {
    return request({ url: '/api/admin/news', method: 'POST', data: payload });
}

export function updateNews(id, payload) {
    return request({ url: `/api/admin/news/${id}`, method: 'PUT', data: payload });
}

export function setNewsStatus(id, status) {
    return request({ url: `/api/admin/news/${id}/status`, method: 'PUT', data: { status } });
}

export function deleteNews(id) {
    return request({ url: `/api/admin/news/${id}`, method: 'DELETE' });
}
