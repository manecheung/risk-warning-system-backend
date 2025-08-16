package org.example.riskwarningsystembackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RiskWarningSystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskWarningSystemBackendApplication.class, args);
    }

    }

