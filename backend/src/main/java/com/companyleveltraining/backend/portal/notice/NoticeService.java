package com.companyleveltraining.backend.portal.notice;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.portal.notice.dto.NoticeResponse;
import com.companyleveltraining.backend.portal.notice.dto.NoticeSaveRequest;
import com.companyleveltraining.backend.security.SecurityUser;

/** 通知公告业务：门户查看 / 已读，以及管理员发布、编辑、置顶、上下线、删除。 */
@Service
public class NoticeService {

    private final NoticeRepository repository;
    private final BizNoGenerator bizNoGenerator;
    private final AuditLogService auditLogService;

    public NoticeService(NoticeRepository repository, BizNoGenerator bizNoGenerator,
                         AuditLogService auditLogService) {
        this.repository = repository;
        this.bizNoGenerator = bizNoGenerator;
        this.auditLogService = auditLogService;
    }

    // ---- 门户 ----

    public List<NoticeResponse> portalList(String role, Long userId, String keyword) {
        return repository.findPublishedForRole(role, userId, keyword);
    }

    public List<NoticeResponse> latest(String role, Long userId, int limit) {
        return repository.findLatestForRole(role, userId, limit);
    }

    @Transactional
    public NoticeResponse portalDetail(String role, Long userId, Long id) {
        NoticeResponse notice = repository.findPublishedById(role, userId, id)
            .orElseThrow(() -> BusinessException.notFound("公告不存在或未发布"));
        repository.markRead(id, userId);
        return notice;
    }

    public void markRead(String role, Long userId, Long id) {
        if (repository.findPublishedById(role, userId, id).isEmpty()) {
            throw BusinessException.notFound("公告不存在或未发布");
        }
        repository.markRead(id, userId);
    }

    // ---- 管理端 ----

    public List<NoticeResponse> adminList(String status, String keyword) {
        return repository.findAllForAdmin(status, keyword);
    }

    public NoticeResponse adminGet(Long id) {
        return repository.findByIdForAdmin(id).orElseThrow(() -> BusinessException.notFound("公告不存在"));
    }

    @Transactional
    public NoticeResponse create(NoticeSaveRequest req, SecurityUser admin) {
        String status = req.status() == null ? "draft" : req.status();
        boolean publishNow = "published".equals(status);
        String targetRoles = joinRoles(req);
        Long id = repository.insert(bizNoGenerator.generate("NT"), req.title(), req.content(),
            req.noticeType(), req.publishScope(), targetRoles, Boolean.TRUE.equals(req.top()),
            status, publishNow, admin.id(), admin.realName());
        auditLogService.record(admin.id(), "admin", "notice", "create", "notice", id, null);
        return adminGet(id);
    }

    @Transactional
    public NoticeResponse update(Long id, NoticeSaveRequest req, SecurityUser admin) {
        adminGet(id);
        repository.update(id, req.title(), req.content(), req.noticeType(),
            req.publishScope(), joinRoles(req), Boolean.TRUE.equals(req.top()));
        if (req.status() != null) {
            repository.updateStatus(id, req.status());
        }
        auditLogService.record(admin.id(), "admin", "notice", "update", "notice", id, null);
        return adminGet(id);
    }

    @Transactional
    public NoticeResponse setStatus(Long id, String status, SecurityUser admin) {
        adminGet(id);
        if (status == null || !List.of("draft", "published", "offline").contains(status)) {
            throw BusinessException.badRequest("状态值非法");
        }
        repository.updateStatus(id, status);
        auditLogService.record(admin.id(), "admin", "notice", status, "notice", id, null);
        return adminGet(id);
    }

    @Transactional
    public void delete(Long id, SecurityUser admin) {
        adminGet(id);
        repository.softDelete(id);
        auditLogService.record(admin.id(), "admin", "notice", "delete", "notice", id, null);
    }

    private String joinRoles(NoticeSaveRequest req) {
        if ("role".equals(req.publishScope()) && req.targetRoles() != null && !req.targetRoles().isEmpty()) {
            return String.join(",", req.targetRoles());
        }
        return null;
    }
}
