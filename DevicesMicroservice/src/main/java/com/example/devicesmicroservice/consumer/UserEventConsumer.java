package com.example.devicesmicroservice.consumer;

import com.example.devicesmicroservice.event.UserEvent;
import com.example.devicesmicroservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final DeviceService deviceService;

    @RabbitListener(queues = "device.user.events.queue")
    public void handleUserEvent(UserEvent event) {
        log.info("DeviceService received user event: {}", event);

        if ("DELETE".equals(event.getEventType())) {
            handleUserDeleted(event);
        }
    }

    private void handleUserDeleted(UserEvent event) {
        try {
            deviceService.unassignUserFromDevices(event.getUserId());
            log.info("Unassigned userId {} from all devices", event.getUserId());
        } catch (Exception e) {
            log.error("Error unassigning userId {} from devices", event.getUserId(), e);
        }
    }
}
