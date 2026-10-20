package com.example.devicesmicroservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String USER_EVENTS_EXCHANGE = "user.events.exchange";
    public static final String DEVICE_USER_EVENTS_QUEUE = "device.user.events.queue";

    @Bean
    public FanoutExchange userEventsExchange() {
        return new FanoutExchange(USER_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue deviceUserEventsQueue() {
        return new Queue(DEVICE_USER_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding deviceBinding(Queue deviceUserEventsQueue, FanoutExchange userEventsExchange) {
        return BindingBuilder.bind(deviceUserEventsQueue).to(userEventsExchange);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
