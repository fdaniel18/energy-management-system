package com.example.devicesmicroservice.publisher;

import com.example.devicesmicroservice.config.DeviceRabbitMQConfig;
import com.example.devicesmicroservice.event.DeviceEvent;
import com.example.devicesmicroservice.model.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishDeviceCreated(Device device) {
        String deviceId = String.format("%d", device.getId());
        DeviceEvent event = new DeviceEvent("CREATE", deviceId, device.getMaximumConsumptionValue(), device.getUserId());
        rabbitTemplate.convertAndSend(DeviceRabbitMQConfig.DEVICE_EVENTS_EXCHANGE, "", event);
        log.info("Published device created event: deviceId={}", deviceId);
    }

    public void publishDeviceDeleted(String deviceId) {
        DeviceEvent event = new DeviceEvent("DELETE", deviceId, null, null);
        rabbitTemplate.convertAndSend(DeviceRabbitMQConfig.DEVICE_EVENTS_EXCHANGE, "", event);
        log.info("Published device deleted event: deviceId={}", deviceId);
    }
}
