package com.example.datasimulator.service;

import com.example.datasimulator.client.DeviceClient;
import com.example.datasimulator.config.RabbitMQConfig;
import com.example.datasimulator.model.Device;
import com.example.datasimulator.model.MeterReading;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class DataSimulatorService {

    private final RabbitTemplate rabbitTemplate;
    private final DeviceClient deviceClient;
    private final Random random = new Random();

    private List<Device> cachedDevices = List.of();
    private long lastDeviceFetch = 0;
    private static final long DEVICE_CACHE_TTL = 60000; // 1 minute

    public DataSimulatorService(RabbitTemplate rabbitTemplate, DeviceClient deviceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.deviceClient = deviceClient;
    }

    @PostConstruct
    public void init() {
        // Load devices immediately on startup
        System.out.println("DataSimulator starting - loading devices...");
        refreshDeviceCache();
    }

    private void refreshDeviceCache() {
        cachedDevices = deviceClient.getAllDevices();
        lastDeviceFetch = System.currentTimeMillis();
        System.out.println("Refreshed device cache, found " + cachedDevices.size() + " devices");
    }

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void generateAndSendMeasurement() {
        // Refresh device list if cache is stale
        long now = System.currentTimeMillis();
        if (now - lastDeviceFetch > DEVICE_CACHE_TTL) {
            refreshDeviceCache();
        }

        if (cachedDevices.isEmpty()) {
            System.out.println("No devices available to generate measurements");
            return;
        }

        // For each available device, generate and send a measurement
        for (Device device : cachedDevices) {
            String deviceId = String.valueOf(device.getId());

            // Generate random energy consumption value (between 0.5 and 10.0 kWh)
            double measurementValue = 0.5 + (10.0 - 0.5) * random.nextDouble();
            measurementValue = Math.round(measurementValue * 100.0) / 100.0; // Round to 2 decimals

            long timestamp = System.currentTimeMillis();

            MeterReading reading = new MeterReading(timestamp, deviceId, measurementValue);
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, reading);

            System.out.println("Sent measurement for device " + deviceId + " (" + device.getName() + "): " + measurementValue + " kWh");
        }
    }
}
