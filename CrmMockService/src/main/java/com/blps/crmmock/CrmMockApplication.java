package com.blps.crmmock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrmMockApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrmMockApplication.class, args);
    }
}
