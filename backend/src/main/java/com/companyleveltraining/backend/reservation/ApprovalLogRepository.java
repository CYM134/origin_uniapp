package com.companyleveltraining.backend.reservation;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 预约审批流水仓储，写入 reservation_approval_logs，记录每一次提交/审核/取消/完成/重审。
 */
@Repository
public class ApprovalLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public ApprovalLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Long applicationId, String stage, String action, String fromStatus,
                       String toStatus, Long reviewerId, String reviewerName, String comment) {
        jdbcTemplate.update("""
            INSERT INTO reservation_approval_logs
              (application_id, stage, action, from_status, to_status, reviewer_user_id, reviewer_name_snapshot, comment)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, applicationId, stage, action, fromStatus, toStatus, reviewerId, reviewerName, comment);
    }

    public List<Map<String, Object>> findByApplication(Long applicationId) {
        return jdbcTemplate.queryForList("""
            SELECT id, stage, action, from_status AS fromStatus, to_status AS toStatus,
                   reviewer_user_id AS reviewerId, reviewer_name_snapshot AS reviewerName,
                   comment, created_at AS createTime
            FROM reservation_approval_logs
            WHERE application_id = ?
            ORDER BY created_at ASC
            """, applicationId);
    }
}
