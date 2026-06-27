package com.companyleveltraining.backend.notification.mq;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitNotificationProperties {

    private final boolean enabled;
    private final String exchange;
    private final String queue;
    private final String routingKey;

    public RabbitNotificationProperties(
        @Value("${messaging.rabbitmq.enabled:true}") boolean enabled,
        @Value("${messaging.rabbitmq.notification-exchange:campus.notification.exchange}") String exchange,
        @Value("${messaging.rabbitmq.notification-queue:campus.notification.queue}") String queue,
        @Value("${messaging.rabbitmq.notification-routing-key:campus.notification.created}") String routingKey
    ) {
        this.enabled = enabled;
        this.exchange = exchange;
        this.queue = queue;
        this.routingKey = routingKey;
    }

    public boolean enabled() {
        return enabled;
    }

    public String exchange() {
        return exchange;
    }

    public String queue() {
        return queue;
    }

    public String routingKey() {
        return routingKey;
    }
}
