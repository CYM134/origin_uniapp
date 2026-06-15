package com.companyleveltraining.backend.schedule;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.common.BusinessException;

/**
 * 课表查询与导入/导出。预览查询返回「前端字段形状」的 Map，同时满足学生页与教师页字段。
 * 导入/导出为课程项目级模拟实现：创建批次/任务记录并标记成功（不接真实 Excel 解析）。
 */
@Service
public class ScheduleService {

    private static final String BASE_SELECT = """
        SELECT
          cs.id,
          cs.schedule_no AS scheduleNo,
          cs.course_name AS courseName,
          cs.course_name AS name,
          cs.lab_id AS labId,
          l.name AS labName,
          l.name AS lab,
          cs.teacher_name_snapshot AS teacherName,
          cs.teacher_name_snapshot AS teacher,
          cs.schedule_date AS date,
          DATE_FORMAT(cs.start_time, '%H:%i') AS startTime,
          DATE_FORMAT(cs.end_time, '%H:%i') AS endTime,
          cs.time_slot_label AS timeSlot,
          cs.planned_student_count AS studentCount,
          cs.current_student_count AS currentStudents,
          cs.max_student_count AS maxStudents,
          cs.course_type AS courseType,
          cs.course_type AS typeText,
          cs.status,
          CASE cs.status
            WHEN 'available' THEN '可预约'
            WHEN 'full' THEN '已满员'
            WHEN 'ongoing' THEN '进行中'
            WHEN 'scheduled' THEN '仅供查看'
            WHEN 'cancelled' THEN '已取消'
            ELSE cs.status END AS statusText,
          cs.can_reserve AS canReserve,
          cs.description,
          cs.remark
        FROM course_schedules cs
        JOIN laboratories l ON l.id = cs.lab_id
        WHERE cs.deleted_at IS NULL
        """;

    private final JdbcTemplate jdbcTemplate;
    private final BizNoGenerator bizNoGenerator;

    public ScheduleService(JdbcTemplate jdbcTemplate, BizNoGenerator bizNoGenerator) {
        this.jdbcTemplate = jdbcTemplate;
        this.bizNoGenerator = bizNoGenerator;
    }

    public List<Map<String, Object>> findByDate(String date, Long labId) {
        if (labId != null) {
            return jdbcTemplate.queryForList(
                BASE_SELECT + " AND cs.schedule_date = ? AND cs.lab_id = ? ORDER BY cs.start_time", date, labId);
        }
        return jdbcTemplate.queryForList(
            BASE_SELECT + " AND cs.schedule_date = ? ORDER BY cs.start_time", date);
    }

    public List<Map<String, Object>> findByRange(String startDate, String endDate, Long labId) {
        if (labId != null) {
            return jdbcTemplate.queryForList(
                BASE_SELECT + " AND cs.schedule_date BETWEEN ? AND ? AND cs.lab_id = ? ORDER BY cs.schedule_date, cs.start_time",
                startDate, endDate, labId);
        }
        return jdbcTemplate.queryForList(
            BASE_SELECT + " AND cs.schedule_date BETWEEN ? AND ? ORDER BY cs.schedule_date, cs.start_time",
            startDate, endDate);
    }

    public Map<String, Object> findById(Long id) {
        return jdbcTemplate.queryForList(BASE_SELECT + " AND cs.id = ? LIMIT 1", id).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("课程不存在"));
    }

    // ---- 导入/导出（模拟实现）----

    public Map<String, Object> createImportBatch(Long semesterId, String fileName, Long fileSize, Long operatorId) {
        if (semesterId == null) {
            throw BusinessException.badRequest("请选择学期");
        }
        int existing = countSchedules(semesterId);
        String batchNo = bizNoGenerator.generate("SIB");
        jdbcTemplate.update("""
            INSERT INTO schedule_import_batches
              (batch_no, semester_id, file_name, file_size_bytes, status, progress, success_count, failure_count,
               imported_by, started_at, finished_at)
            VALUES (?, ?, ?, ?, 'success', 100, ?, 0, ?, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
            """, batchNo, semesterId, fileName == null ? "schedule.xlsx" : fileName, fileSize,
            existing, operatorId);
        return getImportBatch(batchNo);
    }

    private Map<String, Object> getImportBatch(String batchNo) {
        return jdbcTemplate.queryForList("""
            SELECT id, batch_no AS batchNo, semester_id AS semesterId, file_name AS fileName,
                   status, progress, success_count AS successCount, failure_count AS failureCount,
                   error_details AS errorDetails, created_at AS createTime
            FROM schedule_import_batches WHERE batch_no = ? LIMIT 1
            """, batchNo).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("导入批次不存在"));
    }

    public Map<String, Object> getImportBatchById(Long id) {
        return jdbcTemplate.queryForList("""
            SELECT id, batch_no AS batchNo, semester_id AS semesterId, file_name AS fileName,
                   status, progress, success_count AS successCount, failure_count AS failureCount,
                   error_details AS errorDetails, created_at AS createTime
            FROM schedule_import_batches WHERE id = ? LIMIT 1
            """, id).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("导入批次不存在"));
    }

    public List<Map<String, Object>> listImportBatches() {
        return jdbcTemplate.queryForList("""
            SELECT id, batch_no AS batchNo, semester_id AS semesterId, file_name AS fileName,
                   status, progress, success_count AS successCount, failure_count AS failureCount,
                   error_details AS errorDetails, created_at AS createTime
            FROM schedule_import_batches ORDER BY created_at DESC
            """);
    }

    public Map<String, Object> createExportTask(Long semesterId, String exportFormat, boolean includeRooms,
                                                boolean includeTeachers, boolean includeStudents, Long operatorId) {
        if (semesterId == null) {
            throw BusinessException.badRequest("请选择学期");
        }
        String format = "pdf".equalsIgnoreCase(exportFormat) ? "pdf" : "excel";
        String taskNo = bizNoGenerator.generate("SET");
        String fileUrl = "/exports/schedules/" + taskNo + "." + ("pdf".equals(format) ? "pdf" : "xlsx");
        jdbcTemplate.update("""
            INSERT INTO schedule_export_tasks
              (task_no, semester_id, export_format, include_rooms, include_teachers, include_students,
               file_url, status, created_by, finished_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'success', ?, CURRENT_TIMESTAMP(3))
            """, taskNo, semesterId, format, includeRooms ? 1 : 0, includeTeachers ? 1 : 0,
            includeStudents ? 1 : 0, fileUrl, operatorId);
        return getExportTask(taskNo);
    }

    private Map<String, Object> getExportTask(String taskNo) {
        return jdbcTemplate.queryForList("""
            SELECT id, task_no AS taskNo, semester_id AS semesterId, export_format AS exportFormat,
                   file_url AS fileUrl, status, created_at AS createTime
            FROM schedule_export_tasks WHERE task_no = ? LIMIT 1
            """, taskNo).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("导出任务不存在"));
    }

    public Map<String, Object> getExportTaskById(Long id) {
        return jdbcTemplate.queryForList("""
            SELECT id, task_no AS taskNo, semester_id AS semesterId, export_format AS exportFormat,
                   file_url AS fileUrl, status, created_at AS createTime
            FROM schedule_export_tasks WHERE id = ? LIMIT 1
            """, id).stream().findFirst()
            .orElseThrow(() -> BusinessException.notFound("导出任务不存在"));
    }

    private int countSchedules(Long semesterId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM course_schedules WHERE semester_id = ? AND deleted_at IS NULL",
            Integer.class, semesterId);
        return count == null ? 0 : count;
    }
}
