package com.blps.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlpsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlpsBackendApplication.class, args);
    }
}
