package com.example.datasimulator.client;

import com.example.datasimulator.model.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DeviceClient {

    private final WebClient webClient;

    public DeviceClient(@Value("${devices.service.url}") String devicesServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(devicesServiceUrl)
                .build();
    }

    public List<Device> getAllDevices() {
        try {
            Device[] devices = webClient.get()
                    .uri("/devices/internal/all")
                    .retrieve()
                    .bodyToMono(Device[].class)
                    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                            .doBeforeRetry(retrySignal ->
                                    log.warn("Retrying to connect to DevicesMicroservice... attempt {}", retrySignal.totalRetries() + 1)))
                    .block(Duration.ofSeconds(15));

            if (devices != null && devices.length > 0) {
                log.info("Fetched {} devices from DevicesMicroservice", devices.length);
                return Arrays.asList(devices);
            }
            log.warn("No devices returned from DevicesMicroservice");
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching devices from DevicesMicroservice: {}", e.getMessage());
            return List.of();
        }
    }
}
