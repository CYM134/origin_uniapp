package com.companyleveltraining.backend.portal.app;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.portal.app.dto.AppCategoryResponse;
import com.companyleveltraining.backend.portal.app.dto.AppResponse;
import com.companyleveltraining.backend.portal.app.dto.AppSaveRequest;
import com.companyleveltraining.backend.portal.app.dto.AppSortItem;

/** 应用中心业务：可见应用、分类、收藏、访问统计，以及管理员应用维护。 */
@Service
public class PortalAppService {

    private final PortalAppRepository repository;
    private final AuditLogService auditLogService;

    public PortalAppService(PortalAppRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    public List<AppCategoryResponse> categories() {
        return repository.findCategories();
    }

    public List<AppResponse> visibleApps(String role, Long userId, Long categoryId, String keyword) {
        return repository.findVisibleApps(role, userId, categoryId, keyword);
    }

    public List<AppResponse> favorites(String role, Long userId) {
        return repository.findFavorites(role, userId);
    }

    public boolean addFavorite(String role, Long userId, Long appId) {
        if (repository.findVisibleById(role, userId, appId).isEmpty()) {
            throw BusinessException.notFound("应用不存在或不可见");
        }
        repository.addFavorite(userId, appId);
        return true;
    }

    public boolean removeFavorite(Long userId, Long appId) {
        repository.removeFavorite(userId, appId);
        return false;
    }

    public Long recordVisit(String role, Long userId, Long appId, String clientType, String ip) {
        if (repository.findVisibleById(role, userId, appId).isEmpty()) {
            throw BusinessException.notFound("应用不存在或不可见");
        }
        repository.recordVisit(userId, appId, clientType, ip);
        return repository.currentVisitCount(appId);
    }

    // ---- 管理端 ----

    public List<AppResponse> adminList(Long categoryId, String keyword) {
        return repository.findAllForAdmin(categoryId, keyword);
    }

    public AppResponse adminGet(Long id) {
        return repository.findByIdForAdmin(id).orElseThrow(() -> BusinessException.notFound("应用不存在"));
    }

    @Transactional
    public AppResponse create(AppSaveRequest req, Long operatorId) {
        if (repository.existsAppCode(req.appCode(), null)) {
            throw BusinessException.conflict("应用编码已存在");
        }
        Long id = repository.insert(req);
        auditLogService.record(operatorId, "admin", "portal_app", "create", "portal_app", id,
            "{\"appName\":\"" + safe(req.appName()) + "\"}");
        return adminGet(id);
    }

    @Transactional
    public AppResponse update(Long id, AppSaveRequest req, Long operatorId) {
        adminGet(id);
        if (repository.existsAppCode(req.appCode(), id)) {
            throw BusinessException.conflict("应用编码已存在");
        }
        repository.update(id, req);
        auditLogService.record(operatorId, "admin", "portal_app", "update", "portal_app", id,
            "{\"appName\":\"" + safe(req.appName()) + "\"}");
        return adminGet(id);
    }

    @Transactional
    public AppResponse setStatus(Long id, Integer status, Long operatorId) {
        adminGet(id);
        if (status == null || (status != 0 && status != 1)) {
            throw BusinessException.badRequest("状态值非法");
        }
        repository.updateStatus(id, status);
        auditLogService.record(operatorId, "admin", "portal_app", "status", "portal_app", id,
            "{\"status\":" + status + "}");
        return adminGet(id);
    }

    @Transactional
    public void updateSort(List<AppSortItem> items, Long operatorId) {
        if (items == null) {
            return;
        }
        for (AppSortItem item : items) {
            if (item.id() != null && item.sortOrder() != null) {
                repository.updateSort(item.id(), item.sortOrder());
            }
        }
        auditLogService.record(operatorId, "admin", "portal_app", "sort", "portal_app", null, null);
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        adminGet(id);
        repository.delete(id);
        auditLogService.record(operatorId, "admin", "portal_app", "delete", "portal_app", id, null);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }
}
