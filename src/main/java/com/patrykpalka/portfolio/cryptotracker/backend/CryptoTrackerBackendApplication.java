package com.patrykpalka.portfolio.cryptotracker.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CryptoTrackerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoTrackerBackendApplication.class, args);
    }
}
