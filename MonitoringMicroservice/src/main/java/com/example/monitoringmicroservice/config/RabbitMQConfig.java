package com.example.monitoringmicroservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "device.measurements";
    public static final String DEVICE_EVENTS_EXCHANGE = "device.events.exchange";
    public static final String MONITORING_DEVICE_EVENTS_QUEUE = "monitoring.device.events.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    @Bean
    public Queue deviceMeasurementsQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public FanoutExchange deviceEventsExchange() {
        return new FanoutExchange(DEVICE_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue monitoringDeviceEventsQueue() {
        return new Queue(MONITORING_DEVICE_EVENTS_QUEUE, true);
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public Binding monitoringDeviceBinding(Queue monitoringDeviceEventsQueue, FanoutExchange deviceEventsExchange) {
        return BindingBuilder.bind(monitoringDeviceEventsQueue).to(deviceEventsExchange);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
