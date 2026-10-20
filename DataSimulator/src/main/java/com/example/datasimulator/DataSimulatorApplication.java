package com.example.datasimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSimulatorApplication.class, args);
        System.out.println("Device Data Simulator started. Generating measurements every 10 seconds...");
    }

}
