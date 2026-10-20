package com.example.devicesmicroservice.mapper;

import com.example.devicesmicroservice.dto.DeviceDto;
import com.example.devicesmicroservice.model.Device;

public class DeviceMapper {

    public static DeviceDto toDto(Device device) {
        if (device == null) {
            return null;
        }
        return new DeviceDto(
                device.getId(),
                device.getName(),
                device.getMaximumConsumptionValue(),
                device.getUserId()
        );
    }

    public static Device toEntity(DeviceDto deviceDto) {
        if (deviceDto == null) {
            return null;
        }
        Device device = new Device();
        device.setId(deviceDto.getId());
        device.setName(deviceDto.getName());
        device.setMaximumConsumptionValue(deviceDto.getMaximumConsumptionValue());
        device.setUserId(deviceDto.getUserId());
        return device;
    }
}
