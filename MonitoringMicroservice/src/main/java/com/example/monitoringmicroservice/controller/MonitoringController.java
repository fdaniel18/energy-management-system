package com.example.monitoringmicroservice.controller;

import com.example.monitoringmicroservice.dto.HourlyConsumptionDto;
import com.example.monitoringmicroservice.model.HourlyEnergyConsumption;
import com.example.monitoringmicroservice.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/hourly-consumption")
    public ResponseEntity<List<HourlyEnergyConsumption>> getAllHourlyConsumption() {
        List<HourlyEnergyConsumption> consumptions = monitoringService.getAllConsumption();
        return ResponseEntity.ok(consumptions);
    }

    @GetMapping("/hourly-consumption/device/{deviceId}")
    public ResponseEntity<List<HourlyEnergyConsumption>> getDeviceConsumption(@PathVariable String deviceId) {
        List<HourlyEnergyConsumption> consumptions = monitoringService.getDeviceConsumption(deviceId);
        return ResponseEntity.ok(consumptions);
    }

    @GetMapping("/daily-consumption/device/{deviceId}")
    public ResponseEntity<List<HourlyConsumptionDto>> getDailyConsumption(
            @PathVariable String deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<HourlyConsumptionDto> consumptions = monitoringService.getDailyConsumption(deviceId, date);
        return ResponseEntity.ok(consumptions);
    }
}
