import { request } from '@/api/request';

export function logout() {
    return request({ url: '/api/auth/logout', method: 'POST' });
}

// ============ 仪表盘 ============

export function getDashboardSummary() {
    return request({ url: '/api/admin/dashboard/summary', method: 'GET' });
}

// ============ 实验室管理 ============

export function getLabs(keyword) {
    const q = keyword ? `?keyword=${encodeURIComponent(keyword)}` : '';
    return request({ url: `/api/admin/laboratories${q}`, method: 'GET' });
}

export function getLabDetail(id) {
    return request({ url: `/api/admin/laboratories/${id}`, method: 'GET' });
}

export function createLab(payload) {
    return request({ url: '/api/admin/laboratories', method: 'POST', data: payload });
}

export function updateLab(id, payload) {
    return request({ url: `/api/admin/laboratories/${id}`, method: 'PUT', data: payload });
}

export function deleteLab(id) {
    return request({ url: `/api/admin/laboratories/${id}`, method: 'DELETE' });
}

// ============ 课表管理（学期 / 导入 / 导出） ============

export function getSemesters() {
    return request({ url: '/api/academic-semesters', method: 'GET' });
}

export function createImportBatch(payload) {
    return request({ url: '/api/admin/schedule/import-batches', method: 'POST', data: payload });
}

export function getImportBatch(id) {
    return request({ url: `/api/admin/schedule/import-batches/${id}`, method: 'GET' });
}

export function listImportBatches() {
    return request({ url: '/api/admin/schedule/import-batches', method: 'GET' });
}

export function createExportTask(payload) {
    return request({ url: '/api/admin/schedule/export-tasks', method: 'POST', data: payload });
}

export function getExportTask(id) {
    return request({ url: `/api/admin/schedule/export-tasks/${id}`, method: 'GET' });
}

// ============ 预约审批 ============

export function getReservations() {
    return request({ url: '/api/admin/reservations', method: 'GET' });
}

export function getReservationDetail(id) {
    return request({ url: `/api/admin/reservations/${id}`, method: 'GET' });
}

export function approveReservation(id, comment) {
    return request({ url: `/api/admin/reservations/${id}/approve`, method: 'POST', data: { comment } });
}

export function rejectReservation(id, reason) {
    return request({ url: `/api/admin/reservations/${id}/reject`, method: 'POST', data: { reason } });
}

export function getApprovalRecords() {
    return request({ url: '/api/admin/approval-records', method: 'GET' });
}

export function getApprovalStats() {
    return request({ url: '/api/admin/approval-records/stats', method: 'GET' });
}

// ============ 教师注册审核 ============

export function getTeacherRegistrations(status) {
    const q = status ? `?status=${status}` : '';
    return request({ url: `/api/admin/teacher-registrations${q}`, method: 'GET' });
}

export function getTeacherRegistrationDetail(id) {
    return request({ url: `/api/admin/teacher-registrations/${id}`, method: 'GET' });
}

export function approveTeacherRegistration(id) {
    return request({ url: `/api/admin/teacher-registrations/${id}/approve`, method: 'POST' });
}

export function rejectTeacherRegistration(id, reason) {
    return request({ url: `/api/admin/teacher-registrations/${id}/reject`, method: 'POST', data: { reason } });
}

// ============ 系统设置 ============

export function getSystemSettings() {
    return request({ url: '/api/admin/system-settings', method: 'GET' });
}

export function updateSystemSettings(payload) {
    return request({ url: '/api/admin/system-settings', method: 'PUT', data: payload });
}

// ============ 数据备份 ============

export function getBackups() {
    return request({ url: '/api/admin/backups', method: 'GET' });
}

export function createBackup(type) {
    return request({ url: '/api/admin/backups', method: 'POST', data: { type } });
}

export function getBackup(id) {
    return request({ url: `/api/admin/backups/${id}`, method: 'GET' });
}

export function restoreBackup(id) {
    return request({ url: `/api/admin/backups/${id}/restore`, method: 'POST' });
}

export function deleteBackup(id) {
    return request({ url: `/api/admin/backups/${id}`, method: 'DELETE' });
}

// ============ 用户管理 ============

export function getUsers(keyword, role) {
    const params = [];
    if (keyword) params.push(`keyword=${encodeURIComponent(keyword)}`);
    if (role) params.push(`role=${role}`);
    const q = params.length ? `?${params.join('&')}` : '';
    return request({ url: `/api/admin/users${q}`, method: 'GET' });
}

export function createUser(payload) {
    return request({ url: '/api/admin/users', method: 'POST', data: payload });
}

export function updateUser(id, payload) {
    return request({ url: `/api/admin/users/${id}`, method: 'PUT', data: payload });
}

export function toggleUserStatus(id, status) {
    return request({ url: `/api/admin/users/${id}/status`, method: 'PATCH', data: { status } });
}

export function resetUserPassword(id) {
    return request({ url: `/api/admin/users/${id}/reset-password`, method: 'POST' });
}

export function deleteUser(id) {
    return request({ url: `/api/admin/users/${id}`, method: 'DELETE' });
}

// ============ 操作日志 ============

export function getAuditLogs(module) {
    const q = module ? `?module=${module}` : '';
    return request({ url: `/api/admin/audit-logs${q}`, method: 'GET' });
}
