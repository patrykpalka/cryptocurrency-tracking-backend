package com.patrykpalka.portfolio.cryptotracker.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final CryptoApiProperties properties;

    public WebClientConfig(CryptoApiProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WebClient cryptoApiClient(WebClient.Builder builder) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(properties.getHeaderName(), properties.getApiKey())
                .build();
    }
}
