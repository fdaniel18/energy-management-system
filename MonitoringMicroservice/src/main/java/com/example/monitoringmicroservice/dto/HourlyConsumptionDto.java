package com.example.monitoringmicroservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyConsumptionDto {
    private int hour; // 0-23
    private double consumptionValue; // in kWh
}

