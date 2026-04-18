package com.edurag.backend.config;

import com.edurag.backend.mq.DocumentMessageProducer;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * Spring Boot 启动时自动创建交换机、队列和绑定关系
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange documentProcessExchange() {
        return new DirectExchange(
                DocumentMessageProducer.DOCUMENT_PROCESS_EXCHANGE, true, false);
    }

    @Bean
    public Queue documentProcessQueue() {
        return QueueBuilder
                .durable(DocumentMessageProducer.DOCUMENT_PROCESS_QUEUE)
                .build();
    }

    @Bean
    public Binding documentProcessBinding() {
        return BindingBuilder
                .bind(documentProcessQueue())
                .to(documentProcessExchange())
                .with(DocumentMessageProducer.DOCUMENT_PROCESS_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}