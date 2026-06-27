import { request, API_BASE_URL } from '@/api/request';
import { getAccessToken } from '@/api/storage';

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

export function importScheduleExcel({ semesterId, filePath, fileName }) {
    return new Promise((resolve, reject) => {
        const token = getAccessToken();
        uni.uploadFile({
            url: `${API_BASE_URL}/api/admin/schedule/import-excel`,
            filePath,
            name: 'file',
            fileName,
            formData: {
                semesterId,
                fileName: fileName || ''
            },
            header: token ? { Authorization: `Bearer ${token}` } : {},
            success: (response) => {
                const statusCode = Number(response.statusCode) || 0;
                const data = parseMaybeJson(response.data);
                if (statusCode >= 200 && statusCode < 300) {
                    resolve(data);
                    return;
                }
                reject({
                    statusCode,
                    data: data || { message: '导入失败' }
                });
            },
            fail: (error) => {
                reject({
                    statusCode: 0,
                    data: { message: '服务器连接失败' },
                    error
                });
            }
        });
    });
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

export function downloadScheduleTemplate() {
    return downloadWithAuth('/api/admin/schedule/template', '课表导入模板.xlsx');
}

export function downloadScheduleDemoExcel() {
    return downloadWithAuth('/api/admin/schedule/demo-excel', '课表示例数据.xlsx');
}

export function downloadScheduleExcel(payload) {
    const params = buildQuery({
        semesterId: payload.semesterId,
        includeRooms: payload.includeRooms,
        includeTeachers: payload.includeTeachers,
        includeStudents: payload.includeStudents
    });
    return downloadWithAuth(`/api/admin/schedule/export-excel?${params}`, payload.fileName || '课表.xlsx');
}

function buildQuery(params) {
    return Object.keys(params)
        .filter((key) => params[key] !== undefined && params[key] !== null && params[key] !== '')
        .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&');
}

function parseMaybeJson(data) {
    if (!data) return null;
    if (typeof data === 'object') return data;
    try {
        return JSON.parse(data);
    } catch (e) {
        return { message: String(data) };
    }
}

function downloadWithAuth(url, fileName) {
    const token = getAccessToken();
    const fullUrl = `${API_BASE_URL}${url}`;
    const headers = token ? { Authorization: `Bearer ${token}` } : {};

    // #ifdef H5
    return fetch(fullUrl, { headers })
        .then(async (response) => {
            if (!response.ok) {
                const text = await response.text();
                throw {
                    statusCode: response.status,
                    data: parseMaybeJson(text) || { message: '下载失败' }
                };
            }
            const blob = await response.blob();
            const objectUrl = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = objectUrl;
            link.download = fileName;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            setTimeout(() => window.URL.revokeObjectURL(objectUrl), 1000);
            return true;
        });
    // #endif

    // #ifndef H5
    return new Promise((resolve, reject) => {
        uni.downloadFile({
            url: fullUrl,
            header: headers,
            success: (response) => {
                const statusCode = Number(response.statusCode) || 0;
                if (statusCode < 200 || statusCode >= 300) {
                    reject({ statusCode, data: { message: '下载失败' } });
                    return;
                }
                uni.openDocument({
                    filePath: response.tempFilePath,
                    fileType: 'xlsx',
                    showMenu: true,
                    success: () => resolve(true),
                    fail: (error) => reject({ statusCode: 0, data: { message: '文件打开失败' }, error })
                });
            },
            fail: (error) => reject({ statusCode: 0, data: { message: '服务器连接失败' }, error })
        });
    });
    // #endif
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
