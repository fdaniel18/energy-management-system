package com.example.monitoringmicroservice.consumer;

import com.example.monitoringmicroservice.event.DeviceEvent;
import com.example.monitoringmicroservice.service.MonitoringService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceEventConsumer {

    private final MonitoringService monitoringService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "monitoring.device.events.queue")
    public void handleDeviceEvent(Map<String, Object> messageMap) {
        try {
            // Convert Map to DeviceEvent
            DeviceEvent event = objectMapper.convertValue(messageMap, DeviceEvent.class);
            log.info("MonitoringService received device event: {}", event);

            switch (event.getEventType()) {
                case "CREATE":
                    handleDeviceCreated(event);
                    break;
                case "DELETE":
                    handleDeviceDeleted(event);
                    break;
                default:
                    log.warn("Unknown device event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing device event message: {}", e.getMessage(), e);
        }
    }

    private void handleDeviceCreated(DeviceEvent event) {
        try {
            monitoringService.initializeDeviceMonitoring(event.getDeviceId(), event.getMaximumConsumptionValue(), event.getUserId());
            log.info("Initialized monitoring for device: {}", event.getDeviceId());
        } catch (Exception e) {
            log.error("Error initializing monitoring for device: {}", event.getDeviceId(), e);
        }
    }

    private void handleDeviceDeleted(DeviceEvent event) {
        try {
            monitoringService.deleteDeviceMonitoring(event.getDeviceId());
            log.info("Deleted monitoring data for device: {}", event.getDeviceId());
        } catch (Exception e) {
            log.error("Error deleting monitoring data for device: {}", event.getDeviceId(), e);
        }
    }
}
