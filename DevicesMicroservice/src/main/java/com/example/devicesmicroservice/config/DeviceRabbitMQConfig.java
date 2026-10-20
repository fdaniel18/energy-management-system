package com.example.devicesmicroservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeviceRabbitMQConfig {

    public static final String DEVICE_EVENTS_EXCHANGE = "device.events.exchange";

    @Bean
    public FanoutExchange deviceEventsExchange() {
        return new FanoutExchange(DEVICE_EVENTS_EXCHANGE);
    }

    @Bean
    public ObjectMapper deviceObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }

    @Bean
    public MessageConverter deviceMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
