package com.companyleveltraining.backend.schedule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.companyleveltraining.backend.common.BizNoGenerator;
import com.companyleveltraining.backend.common.BusinessException;

/**
 * 课表查询与导入/导出。预览查询返回「前端字段形状」的 Map，同时满足学生页与教师页字段。
 */
@Service
public class ScheduleService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] IMPORT_HEADERS = {
        "课程名称", "实验室ID", "实验室名称", "教师名称", "日期(yyyy-MM-dd)", "开始时间(HH:mm)",
        "结束时间(HH:mm)", "时间段", "计划人数", "最大人数", "课程类型", "状态", "是否可预约",
        "课程说明", "备注"
    };

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

    public Map<String, Object> createImportBatch(Long semesterId, String fileName, Long fileSize, Long operatorId) {
        if (semesterId == null) {
            throw BusinessException.badRequest("请选择学期");
        }
        ensureSemesterExists(semesterId);
        int existing = countSchedules(semesterId);
        String batchNo = bizNoGenerator.generate("SIB");
        jdbcTemplate.update("""
            INSERT INTO schedule_import_batches
              (batch_no, semester_id, file_name, file_size_bytes, status, progress, success_count, failure_count,
               imported_by, started_at, finished_at)
            VALUES (?, ?, ?, ?, 'success', 100, ?, 0, ?, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
            """, batchNo, semesterId, defaultFileName(fileName), fileSize, existing, operatorId);
        Map<String, Object> batch = new LinkedHashMap<>(getImportBatch(batchNo));
        batch.put("message", "导入批次已创建");
        return batch;
    }

    @Transactional
    public Map<String, Object> importExcel(Long semesterId, MultipartFile file, String uploadFileName, Long operatorId) {
        if (semesterId == null) {
            throw BusinessException.badRequest("请选择学期");
        }
        ensureSemesterExists(semesterId);
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("请选择要导入的 Excel 文件");
        }

        List<String> errors = new ArrayList<>();
        List<ParsedSchedule> parsedRows;
        try (InputStream input = file.getInputStream()) {
            parsedRows = parseImportWorkbook(input, errors);
        } catch (IOException ex) {
            throw BusinessException.badRequest("Excel 文件读取失败");
        }

        String batchNo = bizNoGenerator.generate("SIB");
        int successCount = parsedRows.size();
        int failureCount = errors.size();
        String status = successCount > 0 ? "success" : "failed";
        String errorDetails = String.join("；", errors);
        String originalFileName = blankToNull(uploadFileName) == null ? file.getOriginalFilename() : uploadFileName;

        jdbcTemplate.update("""
            INSERT INTO schedule_import_batches
              (batch_no, semester_id, file_name, file_size_bytes, status, progress, success_count, failure_count,
               error_details, imported_by, started_at, finished_at)
            VALUES (?, ?, ?, ?, ?, 100, ?, ?, ?, ?, CURRENT_TIMESTAMP(3), CURRENT_TIMESTAMP(3))
            """, batchNo, semesterId, defaultFileName(originalFileName), file.getSize(), status, successCount,
            failureCount, errorDetails.isBlank() ? null : errorDetails, operatorId);

        Long batchId = jdbcTemplate.queryForObject(
            "SELECT id FROM schedule_import_batches WHERE batch_no = ? LIMIT 1", Long.class, batchNo);

        if (successCount > 0) {
            jdbcTemplate.update("""
                UPDATE course_schedules
                SET deleted_at = CURRENT_TIMESTAMP(3)
                WHERE semester_id = ? AND deleted_at IS NULL
                """, semesterId);
            for (ParsedSchedule row : parsedRows) {
                Long teacherUserId = findTeacherUserId(row.teacherName());
                jdbcTemplate.update("""
                    INSERT INTO course_schedules
                      (schedule_no, semester_id, lab_id, teacher_user_id, teacher_name_snapshot, course_name,
                       course_type, schedule_date, weekday, start_time, end_time, time_slot_label,
                       planned_student_count, current_student_count, max_student_count, status, can_reserve,
                       description, remark, source_batch_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?)
                    """,
                    bizNoGenerator.generate("CS"), semesterId, row.labId(), teacherUserId, blankToNull(row.teacherName()),
                    row.courseName(), blankToNull(row.courseType()), row.date(), row.date().getDayOfWeek().getValue(),
                    Time.valueOf(row.startTime()), Time.valueOf(row.endTime()), row.timeSlot(), row.plannedStudentCount(),
                    row.maxStudentCount(), row.status(), row.canReserve() ? 1 : 0, blankToNull(row.description()),
                    blankToNull(row.remark()), batchId);
            }
        }

        Map<String, Object> batch = new LinkedHashMap<>(getImportBatch(batchNo));
        if (successCount > 0 && failureCount > 0) {
            batch.put("message", "部分导入成功：成功 " + successCount + " 行，失败 " + failureCount + " 行");
        } else if (successCount > 0) {
            batch.put("message", "课表导入成功，共导入 " + successCount + " 行");
        } else {
            batch.put("message", "课表导入失败，请检查模板格式和必填字段");
        }
        return batch;
    }

    public byte[] buildImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("课表导入模板");
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row header = sheet.createRow(0);
            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(IMPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            Row sample = sheet.createRow(1);
            String[] values = {
                "数据库系统实验", "5", "402实验室", "张教授", "2026-09-08", "08:00", "09:50",
                "08:00-09:50", "30", "40", "实验课", "可预约", "是", "请提前安装 MySQL", "样例行，可删除"
            };
            for (int i = 0; i < values.length; i++) {
                sample.createCell(i).setCellValue(values[i]);
            }
            autosizeColumns(sheet, IMPORT_HEADERS.length);
            return writeWorkbook(workbook);
        } catch (IOException ex) {
            throw BusinessException.badRequest("生成课表模板失败");
        }
    }

    public byte[] buildDemoScheduleExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("课表示例");
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row header = sheet.createRow(0);
            for (int i = 0; i < IMPORT_HEADERS.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(IMPORT_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            String[][] rows = {
                {"数据结构与算法", "3", "互联网+新商科实验室", "张教授", "2026-09-08", "08:00", "09:50", "08:00-09:50", "30", "40", "理论课", "可预约", "是", "链表、树与图的算法实验", "示例课表"},
                {"计算机网络实验", "1", "国际课程实验室", "李老师", "2026-09-08", "10:00", "11:50", "10:00-11:50", "28", "35", "实验课", "仅供查看", "否", "网络协议抓包与分析", "示例课表"},
                {"数据库系统实验", "5", "402实验室", "张教授", "2026-09-09", "14:00", "15:50", "14:00-15:50", "32", "40", "实验课", "可预约", "是", "SQL 建模与查询优化", "示例课表"},
                {"软件工程实践", "2", "IBC实验中心", "陈老师", "2026-09-10", "16:00", "17:50", "16:00-17:50", "24", "30", "实践课", "可预约", "是", "项目迭代与协作演练", "示例课表"}
            };
            for (int i = 0; i < rows.length; i++) {
                writeStringRow(sheet.createRow(i + 1), rows[i]);
            }
            autosizeColumns(sheet, IMPORT_HEADERS.length);
            return writeWorkbook(workbook);
        } catch (IOException ex) {
            throw BusinessException.badRequest("生成课表示例失败");
        }
    }

    public byte[] exportExcel(Long semesterId, boolean includeRooms, boolean includeTeachers, boolean includeStudents) {
        if (semesterId == null) {
            throw BusinessException.badRequest("请选择学期");
        }
        ensureSemesterExists(semesterId);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            SELECT
              cs.id,
              cs.course_name AS courseName,
              cs.lab_id AS labId,
              l.name AS labName,
              l.location AS labLocation,
              cs.teacher_name_snapshot AS teacherName,
              DATE_FORMAT(cs.schedule_date, '%Y-%m-%d') AS scheduleDate,
              cs.weekday,
              DATE_FORMAT(cs.start_time, '%H:%i') AS startTime,
              DATE_FORMAT(cs.end_time, '%H:%i') AS endTime,
              cs.time_slot_label AS timeSlot,
              cs.planned_student_count AS plannedStudentCount,
              cs.current_student_count AS currentStudentCount,
              cs.max_student_count AS maxStudentCount,
              cs.course_type AS courseType,
              cs.status,
              cs.can_reserve AS canReserve,
              cs.description,
              cs.remark
            FROM course_schedules cs
            JOIN laboratories l ON l.id = cs.lab_id
            WHERE cs.semester_id = ? AND cs.deleted_at IS NULL
            ORDER BY cs.schedule_date, cs.start_time, cs.id
            """, semesterId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("课表");
            CellStyle headerStyle = createHeaderStyle(workbook);
            List<String> headers = buildExportHeaders(includeRooms, includeTeachers, includeStudents);

            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < rows.size(); i++) {
                Row excelRow = sheet.createRow(i + 1);
                writeExportRow(excelRow, i + 1, rows.get(i), includeRooms, includeTeachers, includeStudents);
            }
            autosizeColumns(sheet, headers.size());
            return writeWorkbook(workbook);
        } catch (IOException ex) {
            throw BusinessException.badRequest("导出课表失败");
        }
    }

    public String buildExportFileName(Long semesterId) {
        ensureSemesterExists(semesterId);
        String semesterName = jdbcTemplate.queryForObject(
            "SELECT semester_name FROM academic_semesters WHERE id = ? LIMIT 1", String.class, semesterId);
        String safeName = semesterName == null ? "课表" : semesterName.replaceAll("[\\\\/:*?\"<>|]", "_");
        return safeName + "课表.xlsx";
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
        ensureSemesterExists(semesterId);
        String format = "pdf".equalsIgnoreCase(exportFormat) ? "pdf" : "excel";
        String taskNo = bizNoGenerator.generate("SET");
        String fileUrl = "/api/admin/schedule/export-excel?semesterId=" + semesterId;
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

    private List<ParsedSchedule> parseImportWorkbook(InputStream input, List<String> errors) {
        try (Workbook workbook = WorkbookFactory.create(input)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw BusinessException.badRequest("Excel 文件中没有工作表");
            }
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.CHINA);
            List<ParsedSchedule> rows = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isBlankRow(row, formatter)) {
                    continue;
                }
                try {
                    rows.add(parseScheduleRow(row, formatter));
                } catch (IllegalArgumentException ex) {
                    errors.add("第" + (rowIndex + 1) + "行：" + ex.getMessage());
                }
            }
            if (rows.isEmpty() && errors.isEmpty()) {
                errors.add("没有可导入的数据行");
            }
            return rows;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.badRequest("Excel 文件格式不正确，请使用模板重新导入");
        }
    }

    private ParsedSchedule parseScheduleRow(Row row, DataFormatter formatter) {
        String courseName = requiredString(row, formatter, 0, "课程名称");
        Long labId = parseLong(cellString(row, formatter, 1));
        String labName = cellString(row, formatter, 2);
        LabInfo lab = resolveLab(labId, labName);
        String teacherName = cellString(row, formatter, 3);
        LocalDate date = parseDate(row.getCell(4), formatter, "日期");
        LocalTime startTime = parseTime(row.getCell(5), formatter, "开始时间");
        LocalTime endTime = parseTime(row.getCell(6), formatter, "结束时间");
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }

        String timeSlot = cellString(row, formatter, 7);
        if (timeSlot.isBlank()) {
            timeSlot = TIME_FORMATTER.format(startTime) + "-" + TIME_FORMATTER.format(endTime);
        }

        int plannedCount = parseInteger(cellString(row, formatter, 8), 0, "计划人数");
        int maxCount = parseInteger(cellString(row, formatter, 9), 0, "最大人数");
        if (maxCount <= 0) {
            maxCount = Math.max(plannedCount, lab.capacity());
        }
        if (plannedCount > maxCount) {
            maxCount = plannedCount;
        }

        String courseType = cellString(row, formatter, 10);
        boolean canReserve = parseBoolean(cellString(row, formatter, 12), false);
        String status = normalizeStatus(cellString(row, formatter, 11), canReserve);
        if ("full".equals(status)) {
            canReserve = false;
        }
        if ("cancelled".equals(status)) {
            canReserve = false;
        }

        return new ParsedSchedule(
            lab.id(), courseName, teacherName, date, startTime.withSecond(0).withNano(0),
            endTime.withSecond(0).withNano(0), timeSlot, plannedCount, maxCount, courseType, status,
            canReserve, cellString(row, formatter, 13), cellString(row, formatter, 14)
        );
    }

    private LabInfo resolveLab(Long labId, String labName) {
        if (labId != null) {
            return jdbcTemplate.queryForList("""
                SELECT id, name, capacity
                FROM laboratories
                WHERE id = ? AND deleted_at IS NULL AND status <> 'deleted'
                LIMIT 1
                """, labId).stream().findFirst()
                .map(row -> new LabInfo(toLong(row.get("id")), String.valueOf(row.get("name")),
                    toInt(row.get("capacity"))))
                .orElseThrow(() -> new IllegalArgumentException("实验室ID不存在：" + labId));
        }
        if (labName == null || labName.isBlank()) {
            throw new IllegalArgumentException("实验室ID或实验室名称必填");
        }
        return jdbcTemplate.queryForList("""
            SELECT id, name, capacity
            FROM laboratories
            WHERE (name = ? OR lab_code = ?) AND deleted_at IS NULL AND status <> 'deleted'
            ORDER BY id
            LIMIT 1
            """, labName, labName).stream().findFirst()
            .map(row -> new LabInfo(toLong(row.get("id")), String.valueOf(row.get("name")),
                toInt(row.get("capacity"))))
            .orElseThrow(() -> new IllegalArgumentException("实验室不存在：" + labName));
    }

    private Long findTeacherUserId(String teacherName) {
        if (teacherName == null || teacherName.isBlank()) {
            return null;
        }
        try {
            return jdbcTemplate.queryForObject("""
                SELECT id
                FROM sys_users
                WHERE role = 'teacher' AND real_name = ? AND deleted_at IS NULL
                ORDER BY id
                LIMIT 1
                """, Long.class, teacherName);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private List<String> buildExportHeaders(boolean includeRooms, boolean includeTeachers, boolean includeStudents) {
        List<String> headers = new ArrayList<>();
        headers.add("序号");
        headers.add("课程名称");
        if (includeRooms) {
            headers.add("实验室ID");
            headers.add("实验室名称");
            headers.add("实验室地点");
        }
        if (includeTeachers) {
            headers.add("教师名称");
        }
        headers.add("日期");
        headers.add("星期");
        headers.add("开始时间");
        headers.add("结束时间");
        headers.add("时间段");
        headers.add("课程类型");
        if (includeStudents) {
            headers.add("计划人数");
            headers.add("当前人数");
            headers.add("最大人数");
        }
        headers.add("状态");
        headers.add("是否可预约");
        headers.add("课程说明");
        headers.add("备注");
        return headers;
    }

    private void writeExportRow(Row excelRow, int index, Map<String, Object> row, boolean includeRooms,
                                boolean includeTeachers, boolean includeStudents) {
        int col = 0;
        excelRow.createCell(col++).setCellValue(index);
        excelRow.createCell(col++).setCellValue(stringValue(row.get("courseName")));
        if (includeRooms) {
            excelRow.createCell(col++).setCellValue(stringValue(row.get("labId")));
            excelRow.createCell(col++).setCellValue(stringValue(row.get("labName")));
            excelRow.createCell(col++).setCellValue(stringValue(row.get("labLocation")));
        }
        if (includeTeachers) {
            excelRow.createCell(col++).setCellValue(stringValue(row.get("teacherName")));
        }
        excelRow.createCell(col++).setCellValue(stringValue(row.get("scheduleDate")));
        excelRow.createCell(col++).setCellValue(weekdayText(row.get("weekday")));
        excelRow.createCell(col++).setCellValue(stringValue(row.get("startTime")));
        excelRow.createCell(col++).setCellValue(stringValue(row.get("endTime")));
        excelRow.createCell(col++).setCellValue(stringValue(row.get("timeSlot")));
        excelRow.createCell(col++).setCellValue(stringValue(row.get("courseType")));
        if (includeStudents) {
            excelRow.createCell(col++).setCellValue(toInt(row.get("plannedStudentCount")));
            excelRow.createCell(col++).setCellValue(toInt(row.get("currentStudentCount")));
            excelRow.createCell(col++).setCellValue(toInt(row.get("maxStudentCount")));
        }
        excelRow.createCell(col++).setCellValue(statusText(stringValue(row.get("status"))));
        excelRow.createCell(col++).setCellValue(toBool(row.get("canReserve")) ? "是" : "否");
        excelRow.createCell(col++).setCellValue(stringValue(row.get("description")));
        excelRow.createCell(col).setCellValue(stringValue(row.get("remark")));
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private byte[] writeWorkbook(Workbook workbook) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        workbook.write(output);
        return output.toByteArray();
    }

    private void autosizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            int width = Math.min(Math.max(sheet.getColumnWidth(i), 14 * 256), 32 * 256);
            sheet.setColumnWidth(i, width);
        }
    }

    private void writeStringRow(Row row, String[] values) {
        for (int i = 0; i < values.length; i++) {
            row.createCell(i).setCellValue(values[i]);
        }
    }

    private LocalDate parseDate(Cell cell, DataFormatter formatter, String fieldName) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return DateUtil.getLocalDateTime(cell.getNumericCellValue()).toLocalDate();
        }
        String value = cellString(cell, formatter);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "必填");
        }
        String normalized = value.replace("年", "-").replace("月", "-").replace("日", "")
            .replace("/", "-").replace(".", "-").trim();
        for (DateTimeFormatter parser : List.of(
            DateTimeFormatter.ofPattern("uuuu-M-d"),
            DateTimeFormatter.ISO_LOCAL_DATE
        )) {
            try {
                return LocalDate.parse(normalized, parser);
            } catch (DateTimeParseException ignored) {
                // 尝试下一个格式
            }
        }
        throw new IllegalArgumentException(fieldName + "格式应为 yyyy-MM-dd");
    }

    private LocalTime parseTime(Cell cell, DataFormatter formatter, String fieldName) {
        if (cell != null && cell.getCellType() == CellType.NUMERIC) {
            return DateUtil.getLocalDateTime(cell.getNumericCellValue()).toLocalTime().withSecond(0).withNano(0);
        }
        String value = cellString(cell, formatter).replace("：", ":").trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "必填");
        }
        for (DateTimeFormatter parser : List.of(
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm:ss")
        )) {
            try {
                return LocalTime.parse(value, parser).withSecond(0).withNano(0);
            } catch (DateTimeParseException ignored) {
                // 尝试下一个格式
            }
        }
        throw new IllegalArgumentException(fieldName + "格式应为 HH:mm");
    }

    private String requiredString(Row row, DataFormatter formatter, int index, String fieldName) {
        String value = cellString(row, formatter, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "必填");
        }
        return value;
    }

    private String cellString(Row row, DataFormatter formatter, int index) {
        return row == null ? "" : cellString(row.getCell(index), formatter);
    }

    private String cellString(Cell cell, DataFormatter formatter) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < IMPORT_HEADERS.length; i++) {
            if (!cellString(row, formatter, i).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private int parseInteger(String value, int defaultValue, String fieldName) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            int number = (int) Math.round(Double.parseDouble(value.replace(",", "")));
            if (number < 0) {
                throw new NumberFormatException("negative");
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + "必须是非负数字");
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Math.round(Double.parseDouble(value.replace(",", "")));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return List.of("1", "true", "yes", "y", "是", "可", "可预约").contains(normalized);
    }

    private String normalizeStatus(String value, boolean canReserve) {
        if (value == null || value.isBlank()) {
            return canReserve ? "available" : "scheduled";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (List.of("available", "可预约", "可用").contains(normalized)) {
            return "available";
        }
        if (List.of("full", "已满", "已满员", "满员").contains(normalized)) {
            return "full";
        }
        if (List.of("ongoing", "进行中").contains(normalized)) {
            return "ongoing";
        }
        if (List.of("scheduled", "已排课", "仅供查看", "排课").contains(normalized)) {
            return "scheduled";
        }
        if (List.of("cancelled", "canceled", "取消", "已取消").contains(normalized)) {
            return "cancelled";
        }
        throw new IllegalArgumentException("状态只能填写可预约、已满员、进行中、仅供查看或已取消");
    }

    private String statusText(String status) {
        return switch (status) {
            case "available" -> "可预约";
            case "full" -> "已满员";
            case "ongoing" -> "进行中";
            case "cancelled" -> "已取消";
            case "scheduled" -> "仅供查看";
            default -> status;
        };
    }

    private String weekdayText(Object value) {
        int weekday = toInt(value);
        return switch (weekday) {
            case 1 -> "周一";
            case 2 -> "周二";
            case 3 -> "周三";
            case 4 -> "周四";
            case 5 -> "周五";
            case 6 -> "周六";
            case 7 -> "周日";
            default -> "";
        };
    }

    private void ensureSemesterExists(Long semesterId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM academic_semesters WHERE id = ?", Integer.class, semesterId);
        if (count == null || count == 0) {
            throw BusinessException.badRequest("学期不存在");
        }
    }

    private int countSchedules(Long semesterId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM course_schedules WHERE semester_id = ? AND deleted_at IS NULL",
            Integer.class, semesterId);
        return count == null ? 0 : count;
    }

    private String defaultFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "schedule.xlsx";
        }
        return fileName;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private long toLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private int toInt(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value == null || String.valueOf(value).isBlank()) {
            return 0;
        }
        return (int) Math.round(Double.parseDouble(String.valueOf(value)));
    }

    private boolean toBool(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private record ParsedSchedule(
        Long labId,
        String courseName,
        String teacherName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        String timeSlot,
        int plannedStudentCount,
        int maxStudentCount,
        String courseType,
        String status,
        boolean canReserve,
        String description,
        String remark
    ) {
    }

    private record LabInfo(Long id, String name, int capacity) {
    }
}
