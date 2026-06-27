package com.companyleveltraining.backend.portal.calendar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.portal.calendar.dto.CalendarEventResponse;
import com.companyleveltraining.backend.portal.calendar.dto.CalendarEventSaveRequest;

/** 日历日程业务：合并个人事件与预约派生事件。 */
@Service
public class CalendarService {

    private final CalendarRepository repository;

    public CalendarService(CalendarRepository repository) {
        this.repository = repository;
    }

    /** 指定日期范围内的全部日程（个人事件 + 预约派生事件），按开始时间升序。 */
    public List<CalendarEventResponse> events(Long userId, String startDate, String endDate) {
        List<CalendarEventResponse> merged = new ArrayList<>();
        merged.addAll(repository.findStoredInRange(userId, startDate, endDate));
        merged.addAll(repository.findReservationEventsInRange(userId, startDate, endDate));
        merged.sort(Comparator.comparing(CalendarEventResponse::startTime,
            Comparator.nullsLast(Comparator.naturalOrder())));
        return merged;
    }

    /** 近期日程：取若干条即将开始的预约派生事件，供门户首页"我的日程"展示。 */
    public List<CalendarEventResponse> upcoming(Long userId, int limit) {
        return repository.findUpcomingReservations(userId, limit);
    }

    public byte[] exportExcel(Long userId, String startDate, String endDate) {
        List<CalendarEventResponse> list = events(userId, startDate, endDate);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("我的日历");
            String[] headers = {"日期", "开始时间", "结束时间", "类型", "标题", "地点", "备注"};
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < list.size(); i++) {
                CalendarEventResponse ev = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(datePart(ev.startTime()));
                row.createCell(1).setCellValue(timePart(ev.startTime()));
                row.createCell(2).setCellValue(timePart(ev.endTime()));
                row.createCell(3).setCellValue(typeText(ev.eventType()));
                row.createCell(4).setCellValue(nvl(ev.title()));
                row.createCell(5).setCellValue(nvl(ev.location()));
                row.createCell(6).setCellValue(nvl(ev.remark()));
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(Math.max(sheet.getColumnWidth(i), 14 * 256), 36 * 256));
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw BusinessException.badRequest("导出日历失败");
        }
    }

    public Long create(Long userId, CalendarEventSaveRequest req) {
        return repository.insert(userId, req);
    }

    public void update(Long id, Long userId, CalendarEventSaveRequest req) {
        if (repository.findOwnedById(id, userId).isEmpty()) {
            throw BusinessException.notFound("日程不存在或无权操作");
        }
        repository.update(id, userId, req);
    }

    public void delete(Long id, Long userId) {
        if (repository.delete(id, userId) == 0) {
            throw BusinessException.notFound("日程不存在或无权操作");
        }
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

    private String datePart(String value) {
        return value == null || value.length() < 10 ? "" : value.substring(0, 10);
    }

    private String timePart(String value) {
        return value == null || value.length() < 16 ? "" : value.substring(11, 16);
    }

    private String typeText(String value) {
        return switch (value == null ? "" : value) {
            case "reservation" -> "预约";
            case "approval" -> "审批";
            case "custom" -> "个人";
            default -> "其他";
        };
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
