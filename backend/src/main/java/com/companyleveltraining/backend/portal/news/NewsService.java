package com.companyleveltraining.backend.portal.news;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.portal.news.dto.NewsCategoryResponse;
import com.companyleveltraining.backend.portal.news.dto.NewsResponse;
import com.companyleveltraining.backend.portal.news.dto.NewsSaveRequest;
import com.companyleveltraining.backend.security.SecurityUser;

/** 校园资讯业务：门户查看，管理员发布/编辑/上下线/删除。 */
@Service
public class NewsService {

    private final NewsRepository repository;
    private final BizNoGenerator bizNoGenerator;
    private final AuditLogService auditLogService;

    public NewsService(NewsRepository repository, BizNoGenerator bizNoGenerator,
                       AuditLogService auditLogService) {
        this.repository = repository;
        this.bizNoGenerator = bizNoGenerator;
        this.auditLogService = auditLogService;
    }

    public List<NewsCategoryResponse> categories() {
        return repository.categories();
    }

    public List<NewsResponse> portalList(Long categoryId, String keyword) {
        return repository.findPublished(categoryId, keyword);
    }

    public List<NewsResponse> latest(int limit) {
        return repository.findLatest(limit);
    }

    @Transactional
    public NewsResponse portalDetail(Long id) {
        NewsResponse news = repository.findPublishedById(id)
            .orElseThrow(() -> BusinessException.notFound("资讯不存在或未发布"));
        repository.incrementView(id);
        return news;
    }

    // ---- 管理端 ----

    public List<NewsResponse> adminList(String status, Long categoryId, String keyword) {
        return repository.findAllForAdmin(status, categoryId, keyword);
    }

    public NewsResponse adminGet(Long id) {
        return repository.findByIdForAdmin(id).orElseThrow(() -> BusinessException.notFound("资讯不存在"));
    }

    @Transactional
    public NewsResponse create(NewsSaveRequest req, SecurityUser admin) {
        String status = req.status() == null ? "draft" : req.status();
        boolean publishNow = "published".equals(status);
        String categoryCode = repository.categoryCodeOf(req.categoryId());
        Long id = repository.insert(bizNoGenerator.generate("NW"), req.title(), req.summary(),
            req.content(), req.categoryId(), categoryCode, req.coverImage(), req.author(),
            Boolean.TRUE.equals(req.top()), status, publishNow, admin.id());
        auditLogService.record(admin.id(), "admin", "news", "create", "news", id, null);
        return adminGet(id);
    }

    @Transactional
    public NewsResponse update(Long id, NewsSaveRequest req, SecurityUser admin) {
        adminGet(id);
        String categoryCode = repository.categoryCodeOf(req.categoryId());
        repository.update(id, req.title(), req.summary(), req.content(), req.categoryId(),
            categoryCode, req.coverImage(), req.author(), Boolean.TRUE.equals(req.top()));
        if (req.status() != null) {
            repository.updateStatus(id, req.status());
        }
        auditLogService.record(admin.id(), "admin", "news", "update", "news", id, null);
        return adminGet(id);
    }

    @Transactional
    public NewsResponse setStatus(Long id, String status, SecurityUser admin) {
        adminGet(id);
        if (status == null || !List.of("draft", "published", "offline").contains(status)) {
            throw BusinessException.badRequest("状态值非法");
        }
        repository.updateStatus(id, status);
        auditLogService.record(admin.id(), "admin", "news", status, "news", id, null);
        return adminGet(id);
    }

    @Transactional
    public void delete(Long id, SecurityUser admin) {
        adminGet(id);
        repository.softDelete(id);
        auditLogService.record(admin.id(), "admin", "news", "delete", "news", id, null);
    }
}
