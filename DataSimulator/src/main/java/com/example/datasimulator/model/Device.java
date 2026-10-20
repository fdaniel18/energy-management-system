package com.example.datasimulator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    private Long id;
    private String name;
    private Double maximumConsumptionValue;
    private Long userId;
}
