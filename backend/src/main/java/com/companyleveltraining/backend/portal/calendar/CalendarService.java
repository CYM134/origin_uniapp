package com.companyleveltraining.backend.portal.calendar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
}
