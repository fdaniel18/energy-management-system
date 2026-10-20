package com.example.monitoringmicroservice.consumer;

import com.example.monitoringmicroservice.config.RabbitMQConfig;
import com.example.monitoringmicroservice.model.MeterReading;
import com.example.monitoringmicroservice.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeterReadingConsumer {

    private final MonitoringService monitoringService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(MeterReading reading) {
        try {
            log.info("Received meter reading: {}", reading);
            monitoringService.processMeterReading(reading);
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
        }
    }
}
