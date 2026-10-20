package com.example.monitoringmicroservice.repository;

import com.example.monitoringmicroservice.model.MonitoredDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonitoredDeviceRepository extends JpaRepository<MonitoredDevice, String> {
}

