package com.companyleveltraining.backend.notification.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueuePublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationQueuePublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitNotificationProperties properties;

    public NotificationQueuePublisher(RabbitTemplate rabbitTemplate, RabbitNotificationProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public boolean publish(NotificationMessage message) {
        if (!properties.enabled()) {
            return false;
        }
        try {
            rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), message);
            return true;
        } catch (AmqpException ex) {
            log.warn("Publish notification message failed, fallback to database insert: {}", ex.getMessage());
            return false;
        }
    }
}
