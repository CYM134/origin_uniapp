package com.companyleveltraining.backend.reservation.dto;

/**
 * 审核请求体（教师审核/管理员终审/驳回）。approve 时 comment 为审核意见；reject 时 comment 为驳回原因。
 * 同时兼容前端可能传 reason 字段。
 */
public record ReviewRequest(
    String comment,
    String reason
) {
    public String text() {
        if (comment != null && !comment.isBlank()) {
            return comment;
        }
        return reason;
    }
}
