import { request } from '@/api/request';

function buildQuery(params) {
    const parts = [];
    Object.keys(params || {}).forEach((key) => {
        const value = params[key];
        if (value !== undefined && value !== null && value !== '') {
            parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`);
        }
    });
    return parts.length ? `?${parts.join('&')}` : '';
}

export function getLabs() {
    return request({ url: '/api/laboratories', method: 'GET' });
}

export function createRepair(payload) {
    return request({ url: '/api/repairs', method: 'POST', data: payload });
}

export function getMyRepairs() {
    return request({ url: '/api/repairs/mine', method: 'GET' });
}

export function getMyRepairStats() {
    return request({ url: '/api/repairs/mine/stats', method: 'GET' });
}

export function getRepairDetail(id) {
    return request({ url: `/api/repairs/${id}`, method: 'GET' });
}

export function cancelRepair(id) {
    return request({ url: `/api/repairs/${id}/cancel`, method: 'POST' });
}

export function getAdminRepairs(status) {
    return request({ url: `/api/admin/repairs${buildQuery({ status })}`, method: 'GET' });
}

export function getAdminRepairDetail(id) {
    return request({ url: `/api/admin/repairs/${id}`, method: 'GET' });
}

export function approveRepair(id, comment) {
    return request({ url: `/api/admin/repairs/${id}/approve`, method: 'POST', data: { comment } });
}

export function rejectRepair(id, reason) {
    return request({ url: `/api/admin/repairs/${id}/reject`, method: 'POST', data: { reason } });
}
