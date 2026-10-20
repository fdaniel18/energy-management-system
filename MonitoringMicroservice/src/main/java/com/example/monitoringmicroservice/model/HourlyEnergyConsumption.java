package com.example.monitoringmicroservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hourly_energy_consumption")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyEnergyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "consumption_value", nullable = false)
    private Double consumptionValue;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    public HourlyEnergyConsumption(String deviceId, LocalDateTime timestamp, Double consumptionValue, LocalDateTime updatedAt) {
        this.deviceId = deviceId;
        this.timestamp = timestamp;
        this.consumptionValue = consumptionValue;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
