package com.companyleveltraining.backend.schedule;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.companyleveltraining.backend.audit.AuditLogService;
import com.companyleveltraining.backend.common.SecurityUtils;

/**
 * 管理员课表管理接口，对应 admin-schedule-management 的导入批次与导出任务。
 */
@RestController
@RequestMapping("/api/admin/schedule")
public class AdminScheduleController {

    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ScheduleService service;
    private final AuditLogService auditLogService;

    public AdminScheduleController(ScheduleService service, AuditLogService auditLogService) {
        this.service = service;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/import-batches")
    public List<Map<String, Object>> listImports() {
        SecurityUtils.requireRole("admin");
        return service.listImportBatches();
    }

    @PostMapping("/import-batches")
    public Map<String, Object> createImport(@RequestBody Map<String, Object> body) {
        SecurityUtils.requireRole("admin");
        Long semesterId = toLong(body.get("semesterId"));
        String fileName = body.get("fileName") == null ? null : String.valueOf(body.get("fileName"));
        Long fileSize = toLong(body.get("fileSize"));
        Map<String, Object> batch = service.createImportBatch(semesterId, fileName, fileSize, SecurityUtils.currentUserId());
        auditLogService.record(SecurityUtils.currentUserId(), "admin", "schedule", "import",
            "schedule_import_batches", toLong(batch.get("id")), null);
        return batch;
    }

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> importExcel(@RequestParam Long semesterId,
                                           @RequestParam(required = false) String fileName,
                                           @RequestParam MultipartFile file) {
        SecurityUtils.requireRole("admin");
        Map<String, Object> batch = service.importExcel(semesterId, file, fileName, SecurityUtils.currentUserId());
        auditLogService.record(SecurityUtils.currentUserId(), "admin", "schedule", "import",
            "schedule_import_batches", toLong(batch.get("id")), null);
        return batch;
    }

    @GetMapping("/import-batches/{id}")
    public Map<String, Object> getImport(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.getImportBatchById(id);
    }

    @PostMapping("/export-tasks")
    public Map<String, Object> createExport(@RequestBody Map<String, Object> body) {
        SecurityUtils.requireRole("admin");
        Long semesterId = toLong(body.get("semesterId"));
        String format = body.get("exportFormat") == null ? "excel" : String.valueOf(body.get("exportFormat"));
        boolean rooms = toBool(body.get("includeRooms"), true);
        boolean teachers = toBool(body.get("includeTeachers"), true);
        boolean students = toBool(body.get("includeStudents"), false);
        Map<String, Object> task = service.createExportTask(semesterId, format, rooms, teachers, students,
            SecurityUtils.currentUserId());
        auditLogService.record(SecurityUtils.currentUserId(), "admin", "schedule", "export",
            "schedule_export_tasks", toLong(task.get("id")), null);
        return task;
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        SecurityUtils.requireRole("admin");
        return excelResponse("课表导入模板.xlsx", service.buildImportTemplate());
    }

    @GetMapping("/demo-excel")
    public ResponseEntity<byte[]> downloadDemoExcel() {
        SecurityUtils.requireRole("admin");
        return excelResponse("课表示例数据.xlsx", service.buildDemoScheduleExcel());
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam Long semesterId,
                                              @RequestParam(defaultValue = "true") boolean includeRooms,
                                              @RequestParam(defaultValue = "true") boolean includeTeachers,
                                              @RequestParam(defaultValue = "false") boolean includeStudents) {
        SecurityUtils.requireRole("admin");
        Map<String, Object> task = service.createExportTask(semesterId, "excel", includeRooms, includeTeachers,
            includeStudents, SecurityUtils.currentUserId());
        auditLogService.record(SecurityUtils.currentUserId(), "admin", "schedule", "export",
            "schedule_export_tasks", toLong(task.get("id")), null);
        return excelResponse(service.buildExportFileName(semesterId), service.exportExcel(semesterId, includeRooms,
            includeTeachers, includeStudents));
    }

    @GetMapping("/export-tasks/{id}")
    public Map<String, Object> getExport(@PathVariable Long id) {
        SecurityUtils.requireRole("admin");
        return service.getExportTaskById(id);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean toBool(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private ResponseEntity<byte[]> excelResponse(String fileName, byte[] content) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
            .contentType(EXCEL_MEDIA_TYPE)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
            .body(content);
    }
}
