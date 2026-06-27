package com.companyleveltraining.backend.notification.mq;

public record NotificationMessage(
    Long recipientUserId,
    String recipientRole,
    String recipientAccountNo,
    String title,
    String content,
    String type,
    Long relatedApplicationId
) {
}
