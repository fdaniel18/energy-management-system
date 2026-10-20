package com.example.monitoringmicroservice.repository;

import com.example.monitoringmicroservice.model.HourlyEnergyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyEnergyRepository extends JpaRepository<HourlyEnergyConsumption, Long> {

    Optional<HourlyEnergyConsumption> findByDeviceIdAndTimestamp(String deviceId, LocalDateTime timestamp);

    List<HourlyEnergyConsumption> findByDeviceIdOrderByTimestampDesc(String deviceId);

    @Query("SELECT h FROM HourlyEnergyConsumption h WHERE h.deviceId = :deviceId " +
           "AND h.timestamp >= :startDate AND h.timestamp < :endDate " +
           "ORDER BY h.timestamp ASC")
    List<HourlyEnergyConsumption> findByDeviceIdAndDateRange(
            @Param("deviceId") String deviceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
