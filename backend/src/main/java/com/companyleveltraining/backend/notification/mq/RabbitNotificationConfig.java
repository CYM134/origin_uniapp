package com.companyleveltraining.backend.notification.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitNotificationConfig {

    @Bean
    @ConditionalOnProperty(prefix = "messaging.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DirectExchange notificationExchange(RabbitNotificationProperties properties) {
        return new DirectExchange(properties.exchange(), true, false);
    }

    @Bean
    @ConditionalOnProperty(prefix = "messaging.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Queue notificationQueue(RabbitNotificationProperties properties) {
        return new Queue(properties.queue(), true);
    }

    @Bean
    @ConditionalOnProperty(prefix = "messaging.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Binding notificationBinding(DirectExchange notificationExchange, Queue notificationQueue,
                                       RabbitNotificationProperties properties) {
        return BindingBuilder.bind(notificationQueue)
            .to(notificationExchange)
            .with(properties.routingKey());
    }

    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(rabbitMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory,
        MessageConverter rabbitMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(rabbitMessageConverter);
        return factory;
    }
}
