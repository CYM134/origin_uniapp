package com.companyleveltraining.backend.portal.home;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.companyleveltraining.backend.notification.NotificationService;
import com.companyleveltraining.backend.portal.app.PortalAppService;
import com.companyleveltraining.backend.portal.app.dto.AppResponse;
import com.companyleveltraining.backend.portal.calendar.CalendarService;
import com.companyleveltraining.backend.portal.news.NewsService;
import com.companyleveltraining.backend.portal.notice.NoticeService;
import com.companyleveltraining.backend.portal.task.PortalTaskService;
import com.companyleveltraining.backend.reservation.ReservationService;
import com.companyleveltraining.backend.security.SecurityUser;

/** 师生门户首页聚合：按当前用户角色一次性返回应用、通知、资讯、消息、日程、预约统计等。 */
@Service
public class PortalHomeService {

    private final PortalAppService portalAppService;
    private final NoticeService noticeService;
    private final NewsService newsService;
    private final NotificationService notificationService;
    private final ReservationService reservationService;
    private final CalendarService calendarService;
    private final PortalTaskService portalTaskService;

    public PortalHomeService(PortalAppService portalAppService, NoticeService noticeService,
                             NewsService newsService, NotificationService notificationService,
                             ReservationService reservationService, CalendarService calendarService,
                             PortalTaskService portalTaskService) {
        this.portalAppService = portalAppService;
        this.noticeService = noticeService;
        this.newsService = newsService;
        this.notificationService = notificationService;
        this.reservationService = reservationService;
        this.calendarService = calendarService;
        this.portalTaskService = portalTaskService;
    }

    public Map<String, Object> home(SecurityUser user) {
        List<AppResponse> visible = portalAppService.visibleApps(user.role(), user.id(), null, null);
        List<AppResponse> hot = visible.stream().filter(AppResponse::hot).limit(8).toList();
        List<AppResponse> latest = visible.stream().filter(AppResponse::latest).limit(8).toList();

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.id());
        profile.put("name", user.realName());
        profile.put("accountNo", user.accountNo());
        profile.put("role", user.role());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user", profile);
        result.put("myApps", portalAppService.favorites(user.role(), user.id()));
        result.put("hotApps", hot);
        result.put("latestApps", latest);
        result.put("allApps", visible);
        result.put("notices", noticeService.latest(user.role(), user.id(), 5));
        result.put("news", newsService.latest(5));
        result.put("unreadMessages", notificationService.unreadCount(user.id()));
        result.put("reservationStats", reservationService.getMyStats(user.id()));
        result.put("upcomingEvents", calendarService.upcoming(user.id(), 5));
        boolean isReviewer = "teacher".equals(user.role()) || "admin".equals(user.role());
        result.put("pendingTaskCount", isReviewer ? portalTaskService.todo(user).size() : 0);
        return result;
    }
}
