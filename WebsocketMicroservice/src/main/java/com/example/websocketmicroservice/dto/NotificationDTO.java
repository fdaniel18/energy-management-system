package com.example.websocketmicroservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO implements Serializable {
    private String message;
    private String deviceId;
    private Long userId;
    private Double measurementValue;
    private Double maximumConsumption;
}

