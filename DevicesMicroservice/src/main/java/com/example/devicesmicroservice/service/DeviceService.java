package com.example.devicesmicroservice.service;

import com.example.devicesmicroservice.model.Device;
import com.example.devicesmicroservice.repository.DeviceRepository;
import com.example.devicesmicroservice.publisher.DeviceEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceEventPublisher deviceEventPublisher;

    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
    }

    public Device create(Device device) {
        device.setId(null); // ensure id is null for creation
        Device savedDevice = deviceRepository.save(device);

        // Publish device created event
        deviceEventPublisher.publishDeviceCreated(savedDevice);

        return savedDevice;
    }

    public Device update(Long id, Device device) {
        return deviceRepository.findById(id)
                .map(existing -> {
                    existing.setName(device.getName());
                    existing.setMaximumConsumptionValue(device.getMaximumConsumptionValue());
                    existing.setUserId(device.getUserId());
                    return deviceRepository.save(existing);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    }

    public void delete(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        // Publish device deleted event before deleting
        String deviceId = String.format("%d", device.getId());
        deviceEventPublisher.publishDeviceDeleted(deviceId);

        deviceRepository.deleteById(id);
    }

    public List<Device> findByUserId(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

    @Transactional
    public void unassignUserFromDevices(Long userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        devices.forEach(device -> {
            device.setUserId(null);
            deviceRepository.save(device);
        });
    }
}
