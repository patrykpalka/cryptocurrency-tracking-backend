package com.patrykpalka.portfolio.cryptotracker.backend.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "api.crypto")
@Validated
public class CryptoApiProperties {

    @NotBlank(message = "Base URL cannot be empty")
    private String baseUrl;

    @NotBlank(message = "API header name cannot be empty")
    private String headerName;

    @NotBlank(message = "API key cannot be empty")
    private String apiKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
