package com.example.monitoringmicroservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "monitored_devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitoredDevice {
    @Id
    private String deviceId;
    private Double maximumConsumptionValue;
    private Long userId;
}

