package com.donations.donations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class DonationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DonationsApplication.class, args);
    }
}
