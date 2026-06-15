package com.companyleveltraining.backend.notification;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(String notificationNo, Long recipientUserId, String recipientRole,
                       String recipientAccountNo, String title, String content, String type,
                       Long relatedApplicationId) {
        jdbcTemplate.update("""
            INSERT INTO notifications
              (notification_no, recipient_user_id, recipient_role, recipient_account_no,
               title, content, type, related_application_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, notificationNo, recipientUserId, recipientRole, recipientAccountNo,
            title, content, type, relatedApplicationId);
    }

    public List<Map<String, Object>> findByUser(Long userId) {
        return jdbcTemplate.queryForList("""
            SELECT id, notification_no AS notificationNo, title, content, type,
                   related_application_id AS relatedApplicationId,
                   is_read AS `read`, read_at AS readAt, created_at AS createTime
            FROM notifications
            WHERE recipient_user_id = ?
            ORDER BY created_at DESC
            """, userId);
    }

    public int unreadCount(Long userId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE recipient_user_id = ? AND is_read = 0",
            Integer.class, userId);
        return count == null ? 0 : count;
    }

    public int markRead(Long id, Long userId) {
        return jdbcTemplate.update("""
            UPDATE notifications SET is_read = 1, read_at = CURRENT_TIMESTAMP(3)
            WHERE id = ? AND recipient_user_id = ? AND is_read = 0
            """, id, userId);
    }

    public int markAllRead(Long userId) {
        return jdbcTemplate.update("""
            UPDATE notifications SET is_read = 1, read_at = CURRENT_TIMESTAMP(3)
            WHERE recipient_user_id = ? AND is_read = 0
            """, userId);
    }
}
