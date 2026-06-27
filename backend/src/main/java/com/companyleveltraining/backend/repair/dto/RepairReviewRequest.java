package com.companyleveltraining.backend.repair.dto;

/** 管理员审核报修申请。 */
public record RepairReviewRequest(
    String comment,
    String reason
) {
}
