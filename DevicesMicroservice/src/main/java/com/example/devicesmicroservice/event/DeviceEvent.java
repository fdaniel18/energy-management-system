package com.example.devicesmicroservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEvent implements Serializable {
    private String eventType; // CREATE, DELETE
    private String deviceId;
    private Double maximumConsumptionValue;
    private Long userId;
}
