package com.example.monitoringmicroservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeterReading {

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("measurement_value")
    private double measurementValue;

    public MeterReading() {
    }

    public MeterReading(long timestamp, String deviceId, double measurementValue) {
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.measurementValue = measurementValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getMeasurementValue() {
        return measurementValue;
    }

    public void setMeasurementValue(double measurementValue) {
        this.measurementValue = measurementValue;
    }

    @Override
    public String toString() {
        return "MeterReading{" +
                "timestamp=" + timestamp +
                ", deviceId='" + deviceId + '\'' +
                ", measurementValue=" + measurementValue +
                '}';
    }
}
