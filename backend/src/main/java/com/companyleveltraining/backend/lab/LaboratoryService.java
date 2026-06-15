package com.companyleveltraining.backend.lab;

import java.util.List;

import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.lab.dto.LabResponse;
import com.companyleveltraining.backend.lab.dto.LabSaveRequest;

@Service
public class LaboratoryService {

    private final LaboratoryRepository repository;
    private final BizNoGenerator bizNoGenerator;
    private final AuditLogService auditLogService;

    public LaboratoryService(LaboratoryRepository repository, BizNoGenerator bizNoGenerator,
                             AuditLogService auditLogService) {
        this.repository = repository;
        this.bizNoGenerator = bizNoGenerator;
        this.auditLogService = auditLogService;
    }

    public List<LabResponse> list(String keyword, boolean onlyActive) {
        return repository.findAll(keyword, onlyActive);
    }

    public LabResponse get(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> BusinessException.notFound("实验室不存在"));
    }

    public LabResponse create(LabSaveRequest req, Long operatorId) {
        String labCode = bizNoGenerator.generate("LAB");
        Long id = repository.insert(req, labCode, operatorId);
        auditLogService.record(operatorId, "admin", "laboratory", "create",
            "laboratories", id, "{\"name\":\"" + safe(req.name()) + "\"}");
        return repository.findById(id).orElseThrow(() -> BusinessException.notFound("实验室不存在"));
    }

    public LabResponse update(Long id, LabSaveRequest req, Long operatorId) {
        get(id);
        repository.update(id, req, operatorId);
        auditLogService.record(operatorId, "admin", "laboratory", "update",
            "laboratories", id, "{\"name\":\"" + safe(req.name()) + "\"}");
        return repository.findById(id).orElseThrow(() -> BusinessException.notFound("实验室不存在"));
    }

    public void delete(Long id, Long operatorId) {
        get(id);
        if (repository.existsActiveReservation(id)) {
            throw BusinessException.conflict("该实验室存在未完成的预约，无法删除");
        }
        repository.softDelete(id, operatorId);
        auditLogService.record(operatorId, "admin", "laboratory", "delete",
            "laboratories", id, null);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }
}
