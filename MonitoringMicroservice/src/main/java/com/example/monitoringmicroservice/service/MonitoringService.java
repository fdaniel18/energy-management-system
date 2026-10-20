package com.example.monitoringmicroservice.service;

import com.example.monitoringmicroservice.config.RabbitMQConfig;
import com.example.monitoringmicroservice.dto.HourlyConsumptionDto;
import com.example.monitoringmicroservice.dto.NotificationDTO;
import com.example.monitoringmicroservice.model.HourlyEnergyConsumption;
import com.example.monitoringmicroservice.model.MeterReading;
import com.example.monitoringmicroservice.model.MonitoredDevice;
import com.example.monitoringmicroservice.repository.HourlyEnergyRepository;
import com.example.monitoringmicroservice.repository.MonitoredDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final HourlyEnergyRepository hourlyEnergyRepository;
    private final MonitoredDeviceRepository monitoredDeviceRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void processMeterReading(MeterReading reading) {
        // Convert timestamp to LocalDateTime and truncate to hour (00:00, 01:00, etc.)
        LocalDateTime timestamp = Instant.ofEpochMilli(reading.getTimestamp())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        LocalDateTime hourTimestamp = timestamp.withMinute(0).withSecond(0).withNano(0);

        // Find existing record for this device and hour, or create new one
        HourlyEnergyConsumption consumption = hourlyEnergyRepository
                .findByDeviceIdAndTimestamp(reading.getDeviceId(), hourTimestamp)
                .orElse(new HourlyEnergyConsumption(
                        reading.getDeviceId(),
                        hourTimestamp,
                        0.0,
                        LocalDateTime.now()
                ));

        // Add new measurement value to existing consumption
        consumption.setConsumptionValue(consumption.getConsumptionValue() + reading.getMeasurementValue());
        consumption.setUpdatedAt(LocalDateTime.now());

        // Save to database
        hourlyEnergyRepository.save(consumption);

        // Check for overconsumption
        checkOverconsumption(consumption);

        log.info("Processed reading for device {} at hour {} - Total consumption: {} kWh, Updated at: {}",
                reading.getDeviceId(),
                hourTimestamp,
                String.format("%.2f", consumption.getConsumptionValue()),
                consumption.getUpdatedAt());
    }

    private void checkOverconsumption(HourlyEnergyConsumption consumption) {
        if (Boolean.TRUE.equals(consumption.getNotificationSent())) {
            return;
        }
        String deviceId = consumption.getDeviceId();
        Double currentConsumption = consumption.getConsumptionValue();

        monitoredDeviceRepository.findById(deviceId).ifPresent(device -> {
            if (device.getMaximumConsumptionValue() != null && currentConsumption > device.getMaximumConsumptionValue()) {
                NotificationDTO notification = new NotificationDTO(
                        "Device " + deviceId + " exceeded maximum consumption limit!",
                        deviceId,
                        device.getUserId(),
                        currentConsumption,
                        device.getMaximumConsumptionValue()
                );
                rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, notification);
                log.warn("Overconsumption detected for device {}: {} > {}", deviceId, currentConsumption, device.getMaximumConsumptionValue());

                consumption.setNotificationSent(true);
                hourlyEnergyRepository.save(consumption);
            }
        });
    }

    public List<HourlyEnergyConsumption> getDeviceConsumption(String deviceId) {
        return hourlyEnergyRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    public List<HourlyEnergyConsumption> getAllConsumption() {
        return hourlyEnergyRepository.findAll();
    }

    public List<HourlyConsumptionDto> getDailyConsumption(String deviceId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<HourlyEnergyConsumption> consumptions = hourlyEnergyRepository
                .findByDeviceIdAndDateRange(deviceId, startOfDay, endOfDay);

        // Create a map of hour -> consumption value
        Map<Integer, Double> consumptionMap = consumptions.stream()
                .collect(Collectors.toMap(
                        c -> c.getTimestamp().getHour(),
                        HourlyEnergyConsumption::getConsumptionValue,
                        (existing, replacement) -> existing // Keep first value if duplicate
                ));

        // Create list with all 24 hours, filling missing hours with 0.0
        List<HourlyConsumptionDto> result = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            double value = consumptionMap.getOrDefault(hour, 0.0);
            result.add(new HourlyConsumptionDto(hour, value));
        }

        log.info("Retrieved daily consumption for device {} on date {} - {} data points",
                deviceId, date, consumptions.size());

        return result;
    }

    @Transactional
    public void initializeDeviceMonitoring(String deviceId, Double maximumConsumptionValue, Long userId) {
        // Save monitored device metadata
        MonitoredDevice monitoredDevice = new MonitoredDevice(deviceId, maximumConsumptionValue, userId);
        monitoredDeviceRepository.save(monitoredDevice);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentHour = now.withMinute(0).withSecond(0).withNano(0);

        // Create initial entry with 0 consumption for the current hour
        HourlyEnergyConsumption initialEntry = new HourlyEnergyConsumption(
                deviceId,
                currentHour,
                0.0,
                now
        );

        hourlyEnergyRepository.save(initialEntry);
        log.info("Initialized monitoring for device {} with timestamp {} and consumption 0.0",
                deviceId, currentHour);
    }

    @Transactional
    public void deleteDeviceMonitoring(String deviceId) {
        monitoredDeviceRepository.deleteById(deviceId);
        List<HourlyEnergyConsumption> entries = hourlyEnergyRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
        hourlyEnergyRepository.deleteAll(entries);
        log.info("Deleted {} monitoring entries for device {}", entries.size(), deviceId);
    }
}
