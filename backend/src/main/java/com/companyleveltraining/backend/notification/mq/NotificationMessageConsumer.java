package com.companyleveltraining.backend.notification.mq;

import com.companyleveltraining.backend.notification.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "messaging.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationMessageConsumer {

    private final NotificationService notificationService;

    public NotificationMessageConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${messaging.rabbitmq.notification-queue:campus.notification.queue}")
    public void consume(NotificationMessage message) {
        notificationService.save(message);
    }
}
